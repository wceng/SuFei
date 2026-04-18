package dev.wceng.sufei.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.model.UserPreferences
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    DetailContent(
        uiState = uiState,
        onBack = onBack,
        onFavoriteToggle = { isFavorite ->
            viewModel.toggleFavorite(isFavorite)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    uiState: DetailUiState,
    onBack: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit
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
                actions = {
                    if (uiState is DetailUiState.Success) {
                        val userPoem = uiState.userPoem
                        IconButton(onClick = { onFavoriteToggle(!userPoem.isFavorite) }) {
                            Icon(
                                imageVector = if (userPoem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (userPoem.isFavorite) Color(0xFFE09E87) else LocalContentColor.current
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Success -> {
                    PoemReader(
                        userPoem = uiState.userPoem,
                        userPreferences = uiState.userPreferences,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
                is DetailUiState.Error -> {
                    Text(
                        text = uiState.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoemReader(
    userPoem: UserPoem,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val poem = userPoem.poem
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题动态字号：20sp
        Text(
            text = poem.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (20 * userPreferences.fontSizeMultiplier).sp,
                letterSpacing = 2.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 朝代作者：14sp
        Text(
            text = "${poem.dynasty} · ${poem.author}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = (14 * userPreferences.fontSizeMultiplier).sp
            )
        )

        // 标签展示 (TagCloud)
        if (poem.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                poem.tags.forEach { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = (11 * userPreferences.fontSizeMultiplier).sp
                                )
                            )
                        },
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        ),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- 核心改动：仅诗词正文内容可选 ---
        val paragraphs = remember(poem.content) { poem.content.split("\n") }

        SelectionContainer {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                paragraphs.forEach { paragraph ->
                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val verses = paragraph.split(Regex("(?<=[，。！？；])")).filter { it.isNotBlank() }
                        verses.forEach { verse ->
                            Text(
                                text = verse.trim(),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Light,
                                    fontSize = (18 * userPreferences.fontSizeMultiplier).sp,
                                    lineHeight = (32 * userPreferences.lineHeightMultiplier).sp,
                                    letterSpacing = 2.sp
                                ),
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        if (!poem.notes.isNullOrBlank() || !poem.translation.isNullOrBlank() || !poem.intro.isNullOrBlank() || !poem.background.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(64.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(32.dp))
            
            InterpretationSection(title = "注释", content = poem.notes, multiplier = userPreferences.fontSizeMultiplier)
            InterpretationSection(title = "译文", content = poem.translation, multiplier = userPreferences.fontSizeMultiplier)
            InterpretationSection(title = "赏析", content = poem.intro, multiplier = userPreferences.fontSizeMultiplier)
            InterpretationSection(title = "背景", content = poem.background, multiplier = userPreferences.fontSizeMultiplier)
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun InterpretationSection(title: String, content: String?, multiplier: Float) {
    if (content.isNullOrBlank()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = (16 * multiplier).sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                lineHeight = (24 * multiplier).sp,
                fontSize = (14 * multiplier).sp
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailContentPreview() {
    SuFeiTheme {
        DetailContent(
            uiState = DetailUiState.Success(
                userPoem = UserPoem(
                    poem = Poem(
                        id = "uuid",
                        sourceUrl = "",
                        title = "望月怀远",
                        author = "张九龄",
                        dynasty = "唐代",
                        content = "海上生明月，天涯共此时。\n\n情人怨遥夜，竟夕起相思。",
                        notes = "怀远：怀念远方的亲人。",
                        translation = "辽阔无边的大海上升起一轮明月...",
                        background = "这是创作背景...",
                        tags = listOf("唐诗三百首", "五言绝句", "月亮"),
                    ),
                    isFavorite = false
                ),
                userPreferences = UserPreferences(fontSizeMultiplier = 1.0f, lineHeightMultiplier = 1.0f)
            ),
            onBack = {},
            onFavoriteToggle = {}
        )
    }
}
