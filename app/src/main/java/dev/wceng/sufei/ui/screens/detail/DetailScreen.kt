package dev.wceng.sufei.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val isTtsPlaying by viewModel.isTtsPlaying.collectAsState()
    val currentSentenceIndex by viewModel.currentSentenceIndex.collectAsState()

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.stopTts()
        }
    }

    DetailContent(
        uiState = uiState,
        isTtsPlaying = isTtsPlaying,
        currentSentenceIndex = currentSentenceIndex,
        onBack = onBack,
        onFavoriteToggle = { isFavorite ->
            viewModel.toggleFavorite(isFavorite)
        },
        onTtsToggle = { sentences ->
            viewModel.toggleTts(sentences)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    uiState: DetailUiState,
    isTtsPlaying: Boolean,
    currentSentenceIndex: Int?,
    onBack: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onTtsToggle: (List<String>) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        val poem = userPoem.poem
                        IconButton(onClick = {
                            val paragraphs = poem.content.split("\n")
                            val verses = paragraphs.flatMap { p ->
                                p.split(Regex("(?<=[，。！？；])")).filter { it.isNotBlank() }
                            }
                            val ttsSentences = listOf(poem.title, poem.dynasty, poem.author) + verses
                            onTtsToggle(ttsSentences)
                        }) {
                            Icon(
                                imageVector = if (isTtsPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isTtsPlaying) "停止朗读" else "朗读",
                                tint = if (isTtsPlaying) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { onFavoriteToggle(!userPoem.isFavorite) }) {
                            Icon(
                                imageVector = if (userPoem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (userPoem.isFavorite) Color(0xFFE09E87) else LocalContentColor.current
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
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
                        currentSentenceIndex = currentSentenceIndex
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
    currentSentenceIndex: Int?,
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
        // 标题
        Text(
            text = poem.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (20 * userPreferences.fontSizeMultiplier).sp,
                letterSpacing = 2.sp,
                color = if (currentSentenceIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 朝代作者
        Row {
            Text(
                text = poem.dynasty,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (currentSentenceIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = (14 * userPreferences.fontSizeMultiplier).sp
                )
            )
            Text(
                text = " · ",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = (14 * userPreferences.fontSizeMultiplier).sp
                )
            )
            Text(
                text = poem.author,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (currentSentenceIndex == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = (14 * userPreferences.fontSizeMultiplier).sp
                )
            )
        }

        // 标签展示 (TagCloud)
        if (poem.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                poem.tags.forEach { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
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

        // --- 生命周期感知选择 ---
        val lifecycleOwner = LocalLifecycleOwner.current
        val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
        val isSelectionEnabled = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

        val paragraphs = remember(poem.content) { poem.content.split("\n") }

        if (isSelectionEnabled) {
            SelectionContainer {
                PoemBody(paragraphs, userPreferences, currentSentenceIndex)
            }
        } else {
            PoemBody(paragraphs, userPreferences, currentSentenceIndex)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PoemBody(paragraphs: List<String>, userPreferences: UserPreferences, currentSentenceIndex: Int?) {
    var globalVerseIndex = 3 // 从 3 开始，因为 0:标题, 1:朝代, 2:作者
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        paragraphs.forEach { paragraph ->
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val verses = paragraph.split(Regex("(?<=[，。！？；])")).filter { it.isNotBlank() }
                verses.forEach { verse ->
                    val isHighlight = currentSentenceIndex == globalVerseIndex
                    Text(
                        text = verse.trim(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = if (isHighlight) FontWeight.Normal else FontWeight.Light,
                            fontSize = (18 * userPreferences.fontSizeMultiplier).sp,
                            lineHeight = (32 * userPreferences.lineHeightMultiplier).sp,
                            letterSpacing = 2.sp,
                            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        ),
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                    globalVerseIndex++
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = (16 * multiplier).sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
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
            isTtsPlaying = false,
            currentSentenceIndex = null,
            onBack = {},
            onFavoriteToggle = {},
            onTtsToggle = {}
        )
    }
}
