package dev.wceng.sufei.ui.screens.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.theme.SuFeiTheme
import kotlinx.coroutines.launch

@Composable
fun CollectionScreen(
    onPoemClick: (String) -> Unit,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val favoritePoems by viewModel.favoritePoems.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectionContent(
        favoritePoems = favoritePoems,
        snackbarHostState = snackbarHostState,
        onPoemClick = onPoemClick,
        onToggleFavorite = { id, isFav ->
            viewModel.toggleFavorite(id, isFav)
            if (!isFav) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "已取消收藏",
                        actionLabel = "撤销",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.toggleFavorite(id, true)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionContent(
    favoritePoems: List<UserPoem>,
    snackbarHostState: SnackbarHostState,
    onPoemClick: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("枕边", fontWeight = FontWeight.Bold) 
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (favoritePoems.isEmpty()) {
            EmptyCollectionState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoritePoems, key = { it.poem.id }) { userPoem ->
                    FavoritePoemItem(
                        modifier = Modifier.animateItem(),
                        userPoem = userPoem,
                        onClick = { onPoemClick(userPoem.poem.id) },
                        onToggleFavorite = { onToggleFavorite(userPoem.poem.id, false) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritePoemItem(
    modifier: Modifier,
    userPoem: UserPoem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val poem = userPoem.poem
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${poem.dynasty} · ${poem.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = poem.content.replace("\n", " "),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "取消收藏",
                    tint = Color(0xFFE09E87) // 妃红色
                )
            }
        }
    }
}

@Composable
fun EmptyCollectionState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "暂无枕边书",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            )
            Text(
                text = "且向万卷求",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionContentPreview() {
    SuFeiTheme {
        CollectionContent(
            favoritePoems = listOf(
                UserPoem(
                    poem = Poem(
                        id = "1",
                        sourceUrl = "",
                        title = "春晓",
                        author = "孟浩然",
                        dynasty = "唐",
                        content = "春眠不觉晓，处处闻啼鸟。\n夜来风雨声，花落知多少。",
                        tags = listOf()
                    ),
                    isFavorite = true
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onPoemClick = {},
            onToggleFavorite = { _, _ -> }
        )
    }
}
