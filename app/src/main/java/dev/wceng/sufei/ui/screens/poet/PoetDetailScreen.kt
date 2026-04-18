package dev.wceng.sufei.ui.screens.poet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.PoetDescription
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.screens.explore.PoemPreviewCard
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun PoetDetailScreen(
    onBack: () -> Unit,
    onPoemClick: (String) -> Unit,
    viewModel: PoetDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PoetDetailContent(
        uiState = uiState,
        onBack = onBack,
        onPoemClick = onPoemClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoetDetailContent(
    uiState: PoetDetailUiState,
    onBack: () -> Unit,
    onPoemClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (uiState) {
                is PoetDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PoetDetailUiState.Success -> {
                    PoetDetailScrollableContent(
                        poet = uiState.poet,
                        poems = uiState.poems,
                        onPoemClick = onPoemClick
                    )
                }
                is PoetDetailUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun PoetDetailScrollableContent(
    poet: Poet,
    poems: List<UserPoem>,
    onPoemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头部：姓名与朝代
        Text(
            text = poet.name,
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = poet.dynasty,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        // 一句话生平
        if (!poet.lifetime.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = poet.lifetime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 详细描述区 (生平、成就等)
        poet.descriptions.forEach { desc ->
            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider(modifier = Modifier.width(40.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "· ${desc.type} ·",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = desc.content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 28.sp
                )
            )
        }

        // 作品区
        if (poems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "作品集",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            poems.forEach { userPoem ->
                PoemPreviewCard(
                    userPoem = userPoem,
                    onClick = { onPoemClick(userPoem.poem.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PoetDetailPreview() {
    SuFeiTheme {
        PoetDetailContent(
            uiState = PoetDetailUiState.Success(
                poet = Poet(
                    id = "1",
                    name = "李白",
                    dynasty = "唐代",
                    lifetime = "李白（701年－762年），字太白，号青莲居士...",
                    descriptions = listOf(
                        PoetDescription("生平", "这是李白的生平描述..."),
                        PoetDescription("轶事典故", "李白搁笔的故事...")
                    )
                ),
                poems = listOf() // 预览暂不加载作品
            ),
            onBack = {},
            onPoemClick = {}
        )
    }
}
