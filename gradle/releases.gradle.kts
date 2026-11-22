tasks.register("prepareRelease") {
    group = "releases"
    description = "Bumps the app version and creates the next release-notes draft"

    dependsOn("updateVersion")
    notCompatibleWithConfigurationCache("for release metadata generation")
}

tasks.register("updateVersion") {
    group = "releases"
    description = "Increments AppVersion and creates a release-notes draft"

    doLast {
        val versionFile = file("buildSrc/src/main/kotlin/Version.kt")
        if (!versionFile.isFile) {
            throw GradleException("Missing canonical version file: ${versionFile.path}")
        }

        var source = versionFile.readText()

        fun intValue(name: String): Int {
            val match = Regex("""const val ${name} = (\d+)""").find(source)
                ?: throw GradleException("Missing AppVersion.${name}")
            return match.groupValues[1].toInt()
        }

        val major = intValue("MAJOR")
        val minor = intValue("MINOR")
        val patch = intValue("PATCH") + 1
        val code = intValue("CODE") + 1
        val suffix = Regex("""const val SUFFIX = "([^"]*)"""" ).find(source)
            ?.groupValues?.get(1)
            ?: throw GradleException("Missing AppVersion.SUFFIX")
        val name = "${major}.${minor}.${patch}${suffix}"

        fun replaceExactly(pattern: Regex, replacement: String, label: String) {
            if (!pattern.containsMatchIn(source)) {
                throw GradleException("Cannot update AppVersion.${label}")
            }
            source = pattern.replace(source, replacement)
        }

        replaceExactly(Regex("""const val PATCH = \d+"""), "const val PATCH = ${patch}", "PATCH")
        replaceExactly(Regex("""const val NAME = "[^"]*""""), """const val NAME = "${name}"""", "NAME")
        replaceExactly(Regex("""const val CODE = \d+"""), "const val CODE = ${code}", "CODE")

        versionFile.writeText(source)

        val notesFile = file("release-notes/${name}.md")
        if (!notesFile.exists()) {
            val templateFile = file("release-notes/_TEMPLATE.md")
            if (!templateFile.isFile) {
                throw GradleException("Missing release-notes/_TEMPLATE.md")
            }
            val templateBody = templateFile.readText()
                .substringAfter("\n\n", "")
                .replace("x.y.z", name)
                .replace("<AppVersion.CODE>", code.toString())
            notesFile.parentFile.mkdirs()
            notesFile.writeText("# KirinMaL v${name}\n\n${templateBody}")
        }

        println("Prepared KirinMaL v${name} (version code ${code}).")
        println("Fill in ${notesFile.relativeTo(projectDir)} before publishing.")
    }
}
