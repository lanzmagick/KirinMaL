package com.axiel7.moelist.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axiel7.moelist.ui.base.BrowserHandler
import com.axiel7.moelist.ui.generated.resources.UiRes
import com.axiel7.moelist.ui.generated.resources.ic_round_arrow_forward_24
import com.axiel7.moelist.ui.generated.resources.ic_round_menu_book_24
import com.axiel7.moelist.ui.generated.resources.login_with_mal
import com.axiel7.moelist.ui.generated.resources.please_login_to_use_this_feature
import com.axiel7.moelist.ui.theme.MoeListTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginView(
    modifier: Modifier = Modifier
) {
    val viewModel: LoginViewModel = koinViewModel()

    LoginContent(
        event = viewModel,
        modifier = modifier,
    )

    DisposableEffect(viewModel) {
        viewModel.continueLogin()
        onDispose {  }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginContent(
    event: LoginEvent? = null,
    modifier: Modifier = Modifier
) {
    val browserHandler = BrowserHandler.LocalBrowserHandler.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Icon(
                        painterResource(UiRes.drawable.ic_round_menu_book_24),
                        contentDescription = null,
                        modifier = Modifier.padding(20.dp).size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text("MyAnimeList", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(UiRes.string.please_login_to_use_this_feature),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { event?.openLogin(browserHandler) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E51A2),
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp)) {
                        Text("MAL", color = Color.White, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(UiRes.string.login_with_mal), modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.width(8.dp))
                    Icon(painterResource(UiRes.drawable.ic_round_arrow_forward_24),
                        contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MoeListTheme {
        LoginContent()
    }
}
