package dev.wceng.sufei.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.theme.NotoSerifSC
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun HomeScreen(
    onPoemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is HomeUiState.Success -> {
                HomeContent(
                    userPoem = state.userPoem,
                    onPoemClick = { onPoemClick(state.userPoem.poem.id) },
                    onFavoriteToggle = { viewModel.toggleFavorite(state.userPoem.poem.id, it) }
                )
            }
            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 竖排文本组件，支持标点符号特殊处理
 */
@Composable
private fun VerticalText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    spacing: androidx.compose.ui.unit.Dp = 4.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        text.forEach { char ->
            val isPunctuation = char == '，' || char == '。' || char == '；' || char == '！' || char == '？'
            Text(
                text = char.toString(),
                style = if (isPunctuation) style.copy(fontSize = style.fontSize * 0.9f) else style,
                modifier = if (isPunctuation) {
                    Modifier.offset(x = 3.dp, y = (-3).dp)
                } else {
                    Modifier
                }
            )
        }
    }
}

/**
 * 多列竖排文本，用于处理过长的标题
 */
@Composable
private fun MultiColumnVerticalText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    spacing: androidx.compose.ui.unit.Dp = 4.dp,
    columnSpacing: androidx.compose.ui.unit.Dp = 12.dp,
    maxCharsPerColumn: Int = 8
) {
    val columns = text.chunked(maxCharsPerColumn)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
        verticalAlignment = Alignment.Top
    ) {
        columns.asReversed().forEach { columnText ->
            VerticalText(
                text = columnText,
                style = style,
                spacing = spacing
            )
        }
    }
}

/**
 * 判断是否为词/曲
 */
private fun isCi(poem: Poem): Boolean {
    if (poem.tags.any { it.contains("词") || it.contains("曲") || it.contains("诗余") }) return true
    if (poem.title.contains("·") || poem.title.contains("・")) return true
    
    val lines = poem.content.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return false
    val lengths = lines.map { it.filter { char -> char.isLetterOrDigit() }.length }
    val isRegularPoem = lengths.all { it == 5 || it == 7 } && lengths.distinct().size == 1
    
    return !isRegularPoem
}

/**
 * 提取精彩片段，确保是完整句子
 */
private fun extractHighlight(poem: Poem): List<String> {
    val content = poem.content
    val isCiPoem = isCi(poem)

    val fullSentences = content
        .split(Regex("(?<=[。！？])"))
        .map { it.trim() }
        .filter { it.isNotBlank() && it.length > 2 }

    if (fullSentences.isEmpty()) return listOf(content.take(12))

    val targetFullSentence = if (isCiPoem) {
        fullSentences.last()
    } else {
        val allPhrases = content.split(Regex("(?<=[，。！？])")).map { it.trim() }.filter { it.isNotEmpty() }
        when {
            allPhrases.size >= 8 && fullSentences.size >= 2 -> fullSentences[1]
            else -> fullSentences.first()
        }
    }

    return targetFullSentence
        .split(Regex("(?<=[，；。！？])"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

@Composable
private fun HomeContent(
    userPoem: UserPoem,
    onPoemClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit
) {
    val poem = userPoem.poem
    val displayLines = remember(poem.content) { extractHighlight(poem) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .clickable(onClick = onPoemClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：标题与诗人
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.wrapContentWidth()
            ) {
                MultiColumnVerticalText(
                    text = poem.title,
                    spacing = 3.dp,
                    columnSpacing = 12.dp,
                    maxCharsPerColumn = 8,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = NotoSerifSC,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .border(0.8.dp, Color(0xFFE09E87))
                        .padding(horizontal = 3.dp, vertical = 5.dp)
                ) {
                    VerticalText(
                        text = poem.author,
                        spacing = 2.dp,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = NotoSerifSC,
                            color = Color(0xFFE09E87),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // 右侧：诗词正文
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayLines.asReversed().forEachIndexed { index, line ->
                    VerticalText(
                        text = line,
                        spacing = 6.dp,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NotoSerifSC,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 36.sp,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontSize = 22.sp
                        )
                    )
                    if (index < displayLines.size - 1) {
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分享实现
            IconButton(onClick = {
                val shareText = "《${poem.title}》· ${poem.author} [${poem.dynasty}]\n\n" +
                        "${displayLines.joinToString("\n")}\n\n" +
                        "—— 来自「素扉」数字诗集"
                
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "分享诗词")
                context.startActivity(shareIntent)
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            IconButton(onClick = { onFavoriteToggle(!userPoem.isFavorite) }) {
                Icon(
                    imageVector = if (userPoem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (userPoem.isFavorite) Color(0xFFE09E87) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SuFeiTheme {
        Box(modifier = Modifier.background(Color(0xFFF8F3E9))) {
            HomeContent(
                userPoem = UserPoem(
                    poem = Poem(
                        id = "1",
                        sourceUrl = "",
                        title = "望岳",
                        author = "杜甫",
                        dynasty = "唐",
                        content = "岱宗夫如何？齐鲁青未了。\n造化钟神秀，阴阳割昏晓。\n荡胸生曾云，决眦入归鸟。\n会当凌绝顶，一览众山小。",
                        tags = listOf()
                    ),
                    isFavorite = false
                ),
                onPoemClick = {},
                onFavoriteToggle = {}
            )
        }
    }
}
