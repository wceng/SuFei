package dev.wceng.sufei.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.repository.ImportState
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun SplashScreen(
    onInitComplete: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val importState by viewModel.importState.collectAsState()

    // 监听导入状态，成功后跳转
    LaunchedEffect(importState) {
        if (importState is ImportState.Success) {
            onInitComplete()
        }
    }

    SplashContent(importState = importState)
}

@Composable
fun SplashContent(importState: ImportState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App 名称 (Logo)
            Text(
                text = "素扉",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 8.sp
                )
            )
            
            Spacer(modifier = Modifier.height(64.dp))

            // 导入状态展示
            when (val state = importState) {
                is ImportState.Importing -> {
                    // 根据设计规范，在 UI 层定义文学化提示语
                    val message = when {
                        state.progress < 0.33f -> "正在为您裁切宣纸..."
                        state.progress < 0.66f -> "正为您整理万卷书..."
                        else -> "墨香已至，即将开启..."
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(200.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
                is ImportState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> {
                    // Idle 或 Success 状态不展示内容
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    SuFeiTheme {
        SplashContent(
            importState = ImportState.Importing(0.45f)
        )
    }
}
