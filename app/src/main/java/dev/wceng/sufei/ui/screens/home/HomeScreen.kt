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

@Composable
private fun HomeContent(
    userPoem: UserPoem,
    onPoemClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit
) {
    val poem = userPoem.poem
    val displayLines = poem.content.split("，", "。", "\n")
        .filter { it.isNotBlank() }
        .take(2)
        .map { if (poem.content.contains(it + "，")) "$it，" else if (poem.content.contains(it + "。")) "$it。" else it }

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

                // 作者印章：通过 IntrinsicSize 确保外框紧致包裹
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

        // 底部操作栏保持不变...
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
                        title = "水调歌头·题萧独清山水胜",
                        author = "沈澄",
                        dynasty = "元",
                        content = "山水据全胜，消得独清人。",
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
