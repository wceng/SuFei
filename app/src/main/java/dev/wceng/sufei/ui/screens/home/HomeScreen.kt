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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.R
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.components.DraggableCard
import dev.wceng.sufei.ui.theme.NotoSerifSC
import dev.wceng.sufei.util.PoemExtractor

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
                    userPoems = state.userPoems,
                    onPoemClick = { onPoemClick(it) },
                    onFavoriteToggle = { id, isFav -> viewModel.toggleFavorite(id, isFav) },
                    onSwiped = { viewModel.onCardSwiped() }
                )
            }

            is HomeUiState.Error -> {
                Text(
                    text = stringResource(state.messageRes),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    userPoems: List<UserPoem>,
    onPoemClick: (String) -> Unit,
    onFavoriteToggle: (String, Boolean) -> Unit,
    onSwiped: () -> Unit
) {
    val context = LocalContext.current
    // 获取当前顶层诗词用于按钮逻辑
    val currentPoem = userPoems.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        // 卡片堆叠层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp, top = 60.dp), // 留出下方按钮空间
            contentAlignment = Alignment.Center
        ) {
            if (userPoems.isEmpty()) {
                CircularProgressIndicator()
            } else {
                // 反向遍历以确保第一张在最上面
                userPoems.asReversed().forEachIndexed { index, userPoem ->
                    val isTopCard = index == userPoems.size - 1

                    val cardModifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 48.dp)

                    if (isTopCard) {
                        DraggableCard(
                            item = userPoem,
                            onSwiped = { _, _ -> onSwiped() },
                            modifier = cardModifier
                        ) {
                            PoemCardContent(userPoem, onClick = { onPoemClick(userPoem.poem.id) })
                        }
                    } else {
                        // 底层卡片：略微缩小或偏移以增加层次感 (可选)
                        Box(modifier = cardModifier) {
                            PoemCardContent(userPoem, onClick = {})
                        }
                    }
                }
            }
        }

        // 底部操作栏（始终绑定顶层卡片）
        if (currentPoem != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分享
                val displayLines =
                    remember(currentPoem) { PoemExtractor.extractHighlight(currentPoem.poem) }
                val actionShare = stringResource(R.string.action_share)
                val sharePoemTitle = stringResource(R.string.share_poem_title)
                val shareFromApp = stringResource(R.string.share_from_app)
                val shareFormat = stringResource(R.string.share_content_format)

                IconButton(onClick = {
                    val shareText = shareFormat.format(
                        currentPoem.poem.title,
                        currentPoem.poem.author,
                        currentPoem.poem.dynasty,
                        displayLines.joinToString("\n"),
                        shareFromApp
                    )
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, sharePoemTitle))
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = actionShare,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }

                // 收藏
                IconButton(onClick = {
                    onFavoriteToggle(
                        currentPoem.poem.id,
                        !currentPoem.isFavorite
                    )
                }) {
                    Icon(
                        imageVector = if (currentPoem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.action_favorite),
                        tint = if (currentPoem.isFavorite) Color(0xFFE09E87) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.4f
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PoemCardContent(
    userPoem: UserPoem,
    onClick: () -> Unit
) {
    val poem = userPoem.poem
    val displayLines = remember(poem) { PoemExtractor.extractHighlight(poem) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
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
    }
}

/**
 * 竖排文本组件
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
            val isPunctuation =
                char == '，' || char == '。' || char == '；' || char == '！' || char == '？'
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
 * 多列竖排文本
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
