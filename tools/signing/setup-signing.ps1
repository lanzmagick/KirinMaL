#Requires -Version 5.1
<#
KirinMaL signing setup for Lanz Magick.
Uses the four-secret workflow also used by KirinDL, with KirinMaL's own key.
SPDX-License-Identifier: GPL-3.0-or-later
#>
[CmdletBinding()]
param(
    [string] $BundlePath,
    [string] $LauncherDirectory,
    [switch] $CheckOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$Repository = 'lanzmagick/KirinMaL'
$ExpectedKeystoreSha256 = 'f53daa2f2aea5abf95f3d4689feeae3a0dd2e0a65de760cc42a660d9d6f19c3a'
$SecretNames = @('KEYSTORE_FILE', 'KEYSTORE_PASSWORD', 'KEY_ALIAS', 'KEY_PASSWORD')

function Read-SigningBundle {
    param([string] $Directory)

    $secretsPath = Join-Path $Directory 'GitHub-Secrets-4-Kunci.txt'
    $keystorePath = Join-Path $Directory 'KirinMaL-release.jks'
    if (-not (Test-Path -LiteralPath $secretsPath -PathType Leaf) -or
        -not (Test-Path -LiteralPath $keystorePath -PathType Leaf)) {
        throw 'Extract the private KirinMaL-4-Kunci-Release.zip first. Run this BAT inside that folder, or pass -BundlePath "C:\path\to\folder".'
    }

    $values = @{}
    foreach ($line in [IO.File]::ReadAllLines($secretsPath, [Text.Encoding]::UTF8)) {
        if ($line -match '^\s*(KEYSTORE_FILE|KEYSTORE_PASSWORD|KEY_ALIAS|KEY_PASSWORD)\s*=(.*)$') {
            $name = $Matches[1]
            if ($values.ContainsKey($name)) {
                throw "Duplicate entry for $name. Restore the original secrets file."
            }
            $values[$name] = $Matches[2]
        }
    }
    foreach ($name in $SecretNames) {
        if (-not $values.ContainsKey($name) -or [string]::IsNullOrWhiteSpace($values[$name])) {
            throw "Missing $name in the private bundle. Nothing was uploaded."
        }
    }

    $fileHash = (Get-FileHash -LiteralPath $keystorePath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($fileHash -ne $ExpectedKeystoreSha256) {
        throw 'This is not the original KirinMaL release keystore. Nothing was uploaded.'
    }
    try {
        $decoded = [Convert]::FromBase64String($values['KEYSTORE_FILE'])
    } catch {
        throw 'KEYSTORE_FILE is not valid base64. Nothing was uploaded.'
    }
    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        $encodedHash = [BitConverter]::ToString($sha256.ComputeHash($decoded)).Replace('-', '').ToLowerInvariant()
    } finally {
        $sha256.Dispose()
        [Array]::Clear($decoded, 0, $decoded.Length)
    }
    if ($encodedHash -ne $fileHash) {
        throw 'KEYSTORE_FILE does not match KirinMaL-release.jks. Nothing was uploaded.'
    }
    return $values
}

function Find-GitHubCli {
    $command = Get-Command gh -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -ne $command) { return $command.Source }

    # Also find an existing installation when this terminal has an old PATH.
    foreach ($base in @($env:ProgramFiles, $env:ProgramW6432)) {
        if (-not [string]::IsNullOrWhiteSpace($base)) {
            $candidate = Join-Path $base 'GitHub CLI\gh.exe'
            if (Test-Path -LiteralPath $candidate -PathType Leaf) { return $candidate }
        }
    }
    return $null
}

function Get-GitHubCli {
    $cli = Find-GitHubCli
    if ($null -ne $cli) { return $cli }
    $winget = Get-Command winget.exe -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $winget) {
        throw 'GitHub CLI is needed. Install it from https://cli.github.com, then run the same BAT again.'
    }
    Write-Host 'Installing GitHub CLI with WinGet...'
    & $winget.Source install --id GitHub.cli --exact --source winget --accept-source-agreements --accept-package-agreements --disable-interactivity | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw 'GitHub CLI installation failed. Install it from https://cli.github.com, then run the BAT again.'
    }
    $cli = Find-GitHubCli
    if ($null -eq $cli) {
        throw 'GitHub CLI was installed. Reopen Windows Terminal and run the same BAT again.'
    }
    return $cli
}

function Invoke-GhPrivate {
    param(
        [string] $Executable,
        [string[]] $Arguments,
        [AllowNull()] [string] $InputText = $null
    )

    # All arguments are fixed names/options. Secret values only enter stdin.
    $info = New-Object Diagnostics.ProcessStartInfo
    $info.FileName = $Executable
    $info.Arguments = $Arguments -join ' '
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardInput = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    $info.EnvironmentVariables['GH_HOST'] = 'github.com'
    [void] $info.EnvironmentVariables.Remove('GH_DEBUG')
    $process = New-Object Diagnostics.Process
    $process.StartInfo = $info
    try {
        [void] $process.Start()
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        if ($null -ne $InputText) { $process.StandardInput.Write($InputText) }
        $process.StandardInput.Close()
        $process.WaitForExit()
        [void] $stderr.GetAwaiter().GetResult()
        return [PSCustomObject] @{
            ExitCode = $process.ExitCode
            Output = $stdout.GetAwaiter().GetResult().Trim()
        }
    } finally {
        $process.Dispose()
    }
}

try {
    Write-Host 'KirinMaL - setup existing Android release key'
    Write-Host "Target: $Repository"
    # Resolve after parameter binding. The BAT supplies its directory explicitly
    # so Windows PowerShell does not need a default evaluated from PSScriptRoot.
    if ([string]::IsNullOrWhiteSpace($BundlePath)) {
        if (-not [string]::IsNullOrWhiteSpace($LauncherDirectory)) {
            $BundlePath = $LauncherDirectory
        } elseif (-not [string]::IsNullOrWhiteSpace($PSScriptRoot)) {
            $BundlePath = $PSScriptRoot
        } elseif (-not [string]::IsNullOrWhiteSpace($MyInvocation.MyCommand.Path)) {
            $BundlePath = Split-Path -Parent $MyInvocation.MyCommand.Path
        } else {
            throw 'Cannot locate the private bundle folder. Run the BAT with -BundlePath "C:\path\to\folder".'
        }
    }
    if (-not (Test-Path -LiteralPath $BundlePath -PathType Container)) {
        throw 'The private bundle folder does not exist. Check the -BundlePath value.'
    }
    # Canonicalize relative and literal paths before using .NET file APIs.
    $BundlePath = (Get-Item -LiteralPath $BundlePath -Force).FullName
    $values = Read-SigningBundle -Directory $BundlePath
    Write-Host 'Original KirinMaL keystore and all four local entries verified.'
    if ($CheckOnly) {
        Write-Host 'CHECK PASSED. No installation, login, upload or build was performed.'
        exit 0
    }

    $gh = Get-GitHubCli
    $identity = Invoke-GhPrivate -Executable $gh -Arguments @('api', 'user', '--hostname', 'github.com', '--jq', '.login')
    if ($identity.ExitCode -ne 0) {
        Write-Host 'Sign in to the GitHub account that manages the KirinMaL repository.'
        $env:GH_HOST = 'github.com'
        Remove-Item Env:GH_DEBUG -ErrorAction SilentlyContinue
        & $gh auth login --hostname github.com --git-protocol https --web
        if ($LASTEXITCODE -ne 0) { throw 'GitHub login did not finish. Run the BAT again after signing in.' }
        $identity = Invoke-GhPrivate -Executable $gh -Arguments @('api', 'user', '--hostname', 'github.com', '--jq', '.login')
    }
    if ($identity.ExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($identity.Output)) {
        throw 'Could not verify the active GitHub account. Nothing was uploaded.'
    }
    Write-Host "GitHub account: $($identity.Output)"
    $access = Invoke-GhPrivate -Executable $gh -Arguments @('secret', 'list', '--repo', $Repository, '--app', 'actions', '--json', 'name')
    if ($access.ExitCode -ne 0) {
        throw "This account cannot access Actions secrets for $Repository. Nothing was uploaded."
    }

    foreach ($name in $SecretNames) {
        $result = Invoke-GhPrivate -Executable $gh -Arguments @('secret', 'set', $name, '--repo', $Repository, '--app', 'actions') -InputText $values[$name]
        if ($result.ExitCode -ne 0) {
            throw "Upload failed at $name. Earlier entries may already be set; run the same BAT again to finish."
        }
        Write-Host "OK: $name"
    }
    Write-Host 'DONE. All four signing secrets were uploaded to lanzmagick/KirinMaL.'
    Write-Host 'The existing release key and MAL_CLIENT_ID are unchanged. No build was started.'
} catch {
    Write-Host ("FAILED: " + $_.Exception.Message) -ForegroundColor Red
    exit 1
} finally {
    if ((Test-Path variable:values) -and $null -ne $values) { $values.Clear() }
}
