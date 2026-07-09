package dev.wceng.sufei.ui.screens.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.R
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.domain.model.GroupedUserPoem
import dev.wceng.sufei.ui.components.SuFeiSearchField
import dev.wceng.sufei.ui.theme.SuFeiTheme
import kotlinx.coroutines.launch

@Composable
fun CollectionScreen(
    onPoemClick: (String) -> Unit,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val favoritePoems by viewModel.favoritePoems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val removedFavoriteMsg = stringResource(R.string.snackbar_removed_favorite)
    val undoLabel = stringResource(R.string.snackbar_undo)

    CollectionContent(
        favoritePoems = favoritePoems,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        snackbarHostState = snackbarHostState,
        onPoemClick = onPoemClick,
        onToggleFavorite = { id, isFav ->
            viewModel.toggleFavorite(id, isFav)
            if (!isFav) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = removedFavoriteMsg,
                        actionLabel = undoLabel,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionContent(
    favoritePoems: List<GroupedUserPoem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onPoemClick: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(stringResource(R.string.tab_collection), fontWeight = FontWeight.Bold) 
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchVisible = !isSearchVisible
                        if (!isSearchVisible) {
                            onSearchQueryChange("")
                            focusManager.clearFocus()
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = stringResource(if (isSearchVisible) R.string.action_close else R.string.action_search)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SuFeiSearchField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                    placeholder = stringResource(R.string.explore_main_search_placeholder)
                )
                
                LaunchedEffect(isSearchVisible) {
                    if (isSearchVisible) {
                        focusRequester.requestFocus()
                    }
                }
            }

            if (favoritePoems.isEmpty()) {
                val isEmptyCollection = searchQuery.isEmpty()
                EmptyCollectionState(
                    title = if (isEmptyCollection) 
                        stringResource(R.string.empty_collection_title) 
                    else 
                        stringResource(R.string.collection_search_no_results),
                    subtitle = if (isEmptyCollection) 
                        stringResource(R.string.empty_collection_subtitle) 
                    else 
                        "",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    favoritePoems.forEach { group ->
                        stickyHeader(key = group.dateLabel) {
                            DateHeader(
                                dateLabel = group.dateLabel,
                                modifier = Modifier.animateItem()
                            )
                        }
                        items(group.poems, key = { it.poem.id }) { userPoem ->
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
    }
}

@Composable
fun DateHeader(
    dateLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 8.dp)
        )
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
                    contentDescription = stringResource(R.string.action_remove_favorite),
                    tint = Color(0xFFE09E87) // 妃红色
                )
            }
        }
    }
}

@Composable
fun EmptyCollectionState(
    title: String = stringResource(R.string.empty_collection_title),
    subtitle: String = stringResource(R.string.empty_collection_subtitle),
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionContentPreview() {
    SuFeiTheme {
        CollectionContent(
            favoritePoems = listOf(
                GroupedUserPoem(
                    dateLabel = "7月8日",
                    poems = listOf(
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
                            isFavorite = true,
                            favoritedTimestamp = System.currentTimeMillis()
                        )
                    )
                )
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            snackbarHostState = remember { SnackbarHostState() },
            onPoemClick = {},
            onToggleFavorite = { _, _ -> }
        )
    }
}
