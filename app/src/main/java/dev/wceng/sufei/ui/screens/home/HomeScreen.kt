package dev.wceng.sufei.ui.screens.home

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
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
                    Modifier.offset(x = 4.dp, y = (-4).dp)
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
    // 按照最大字符数分列
    val columns = text.chunked(maxCharsPerColumn)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
        verticalAlignment = Alignment.Top
    ) {
        // 传统布局：多列从右往左排列
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
    // 1. 显式标签判断
    if (poem.tags.any { it.contains("词") || it.contains("曲") || it.contains("诗余") }) return true
    // 2. 标题特征 (词牌名·题目)
    if (poem.title.contains("·") || poem.title.contains("・")) return true
    // 3. 内容结构 (上下阕)
//    if (poem.content.contains("\n\n")) return true
    
    // 4. 句式结构检测 (律诗/绝句通常每句 5 或 7 字)
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

    // 1. 按“完整语义句”拆分 (以 。！？ 结尾)
    val fullSentences = content
        .split(Regex("(?<=[。！？])"))
        .map { it.trim() }
        .filter { it.isNotBlank() && it.length > 2 }

    if (fullSentences.isEmpty()) return listOf(content.take(12))

    // 2. 选取目标完整句
    val targetFullSentence = if (isCiPoem) {
        // 词：取最后一句完整句 (通常是结拍，情感最深)
        fullSentences.last()
    } else {
        // 诗：按短句统计行数
        val allPhrases = content.split(Regex("(?<=[，。！？])")).map { it.trim() }.filter { it.isNotEmpty() }
        when {
            // 律诗 (8句)：提取颔联 (第二句完整句)
            allPhrases.size >= 8 && fullSentences.size >= 2 -> fullSentences[1]
            // 其他：提取第一句完整句
            else -> fullSentences.first()
        }
    }

    // 3. 将选中的完整句拆分为展示列 (按标点分列)
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
    
    // 智能提取逻辑
    val displayLines = remember(poem.content) { extractHighlight(poem) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // 主内容区：两端分布
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 56.dp)
                .clickable(onClick = onPoemClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：标题与诗人 (支持长标题多列显示)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.wrapContentWidth()
            ) {
                MultiColumnVerticalText(
                    text = poem.title,
                    spacing = 4.dp,
                    columnSpacing = 16.dp,
                    maxCharsPerColumn = 8,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 30.sp
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 作者印章
                Box(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .border(1.dp, Color(0xFFE09E87))
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    VerticalText(
                        text = poem.author,
                        spacing = 2.dp,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFE09E87),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
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
                        spacing = 8.dp,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 48.sp,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            fontSize = 28.sp
                        )
                    )
                    if (index < displayLines.size - 1) {
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                }
            }
        }

        // 底部操作栏
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: 分享 */ }) {
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
