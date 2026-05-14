package dev.wceng.sufei.ui.screens.poet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.PoetDescription
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.components.PoemPreviewCard
import dev.wceng.sufei.ui.theme.SuFeiTheme

enum class PoetDetailTab(val title: String) {
    INTRODUCTION("介绍"),
    WORKS("作品")
}

@Composable
fun PoetDetailScreen(
    onBack: () -> Unit,
    onPoemClick: (String) -> Unit,
    onAllWorksClick: (String) -> Unit,
    viewModel: PoetDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PoetDetailContent(
        uiState = uiState,
        onBack = onBack,
        onPoemClick = onPoemClick,
        onAllWorksClick = onAllWorksClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoetDetailContent(
    uiState: PoetDetailUiState,
    onBack: () -> Unit,
    onPoemClick: (String) -> Unit,
    onAllWorksClick: (String) -> Unit
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
                        onPoemClick = onPoemClick,
                        onAllWorksClick = onAllWorksClick
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
    onPoemClick: (String) -> Unit,
    onAllWorksClick: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(PoetDetailTab.INTRODUCTION) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 公共头部
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = poet.name,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = poet.dynasty,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        if (!poet.lifetime.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = poet.lifetime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. 标签切换行 (使用 FilterChip 更加轻量)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            PoetDetailTab.entries.forEach { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    label = { Text(tab.title) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 根据状态渲染内容
        when (selectedTab) {
            PoetDetailTab.INTRODUCTION -> {
                if (poet.descriptions.isNotEmpty()) {
                    poet.descriptions.forEach { desc ->
                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider(
                            modifier = Modifier.width(40.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "· ${desc.type} ·",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = desc.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无介绍数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            PoetDetailTab.WORKS -> {
                if (poems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 全部作品入口
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAllWorksClick(poet.name) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "作品集",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "查看全部",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "全部作品",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 展示作品
                    poems.forEach { userPoem ->
                        PoemPreviewCard(
                            userPoem = userPoem,
                            onClick = { onPoemClick(userPoem.poem.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无作品数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
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
                poems = listOf()
            ),
            onBack = {},
            onPoemClick = {},
            onAllWorksClick = {}
        )
    }
}
