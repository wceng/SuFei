package dev.wceng.sufei.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.components.PoemPreviewCard
import dev.wceng.sufei.ui.components.PoetPreviewCard
import dev.wceng.sufei.ui.theme.SuFeiTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DrawerType {
    TAG, TUNE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onPoemClick: (String) -> Unit,
    onPoetClick: (String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchResults.collectAsState()
    val selectedDynasty by viewModel.selectedDynasty.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedTune by viewModel.selectedTune.collectAsState()
    val recommendedTags by viewModel.recommendedTags.collectAsState()
    val recommendedTunes by viewModel.recommendedTunes.collectAsState()

    val allTags by viewModel.allTags.collectAsState()
    val allTunes by viewModel.allTunes.collectAsState()
    val drawerSearchQuery by viewModel.drawerSearchQuery.collectAsState()

    var showDrawer by remember { mutableStateOf(false) }
    var drawerType by remember { mutableStateOf(DrawerType.TAG) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        ExploreContent(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            selectedDynasty = selectedDynasty,
            onDynastySelect = viewModel::onDynastySelect,
            selectedTag = selectedTag,
            onTagSelect = viewModel::onTagSelect,
            selectedTune = selectedTune,
            onTuneSelect = viewModel::onTuneSelect,
            recommendedTags = recommendedTags,
            recommendedTunes = recommendedTunes,
            searchResult = searchResult,
            onPoemClick = onPoemClick,
            onPoetClick = onPoetClick,
            onAllTagsClick = {
                drawerType = DrawerType.TAG
                showDrawer = true
            },
            onAllTunesClick = {
                drawerType = DrawerType.TUNE
                showDrawer = true
            },
        )

        if (showDrawer) {
            ModalBottomSheet(
                onDismissRequest = {
                    showDrawer = false
                    viewModel.onDrawerSearchQueryChange("")
                },
                sheetState = sheetState,
            ) {
                ExploreDrawerContent(
                    title = if (drawerType == DrawerType.TAG) "标签广场" else "词牌广场",
                    query = drawerSearchQuery,
                    onQueryChange = viewModel::onDrawerSearchQueryChange,
                    items = if (drawerType == DrawerType.TAG) allTags else allTunes,
                    selectedItem = if (drawerType == DrawerType.TAG) selectedTag else selectedTune,
                    onItemClick = { item ->
                        if (drawerType == DrawerType.TAG) {
                            viewModel.onTagSelect(if (selectedTag == item) null else item)
                        } else {
                            viewModel.onTuneSelect(if (selectedTune == item) null else item)
                        }
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showDrawer = false
                            viewModel.onDrawerSearchQueryChange("")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreDrawerContent(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    items: List<String>,
    selectedItem: String?,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("搜索内容") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items.forEach { item ->
                        FilterChip(
                            selected = selectedItem == item,
                            onClick = { onItemClick(item) },
                            label = { Text(text = item) }
                        )
                    }
                }
                // 底部留白，避免被导航栏遮挡
                Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDynasty: String?,
    onDynastySelect: (String?) -> Unit,
    selectedTag: String?,
    onTagSelect: (String?) -> Unit,
    selectedTune: String?,
    onTuneSelect: (String?) -> Unit,
    recommendedTags: List<Tag>,
    recommendedTunes: List<Tune>,
    searchResult: SearchResult,
    onPoemClick: (String) -> Unit,
    onPoetClick: (String) -> Unit,
    onAllTagsClick: () -> Unit,
    onAllTunesClick: () -> Unit,
) {
    val dynasties = listOf(
        "唐代", "宋代", "元代", "明代", "清代",
        "魏晋", "南北朝", "两汉", "先秦", "隋代",
        "五代", "金朝", "近代", "现代", "当代"
    )

    val dynastyListState = rememberLazyListState()
    val tuneListState = rememberLazyListState()
    val tagListState = rememberLazyListState()

    // 定义滚动行为
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(selectedDynasty) {
        selectedDynasty?.let { name ->
            val index = dynasties.indexOf(name)
            if (index >= 0) dynastyListState.animateScrollToItem(index)
        }
    }
    LaunchedEffect(selectedTune, recommendedTunes) {
        selectedTune?.let { name ->
            val index = recommendedTunes.indexOfFirst { it.name == name }
            if (index >= 0) tuneListState.animateScrollToItem(index)
        }
    }
    LaunchedEffect(selectedTag, recommendedTags) {
        selectedTag?.let { name ->
            val index = recommendedTags.indexOfFirst { it.name == name }
            if (index >= 0) tagListState.animateScrollToItem(index)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        // 固定搜索框
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = { },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = { Text("搜索诗人、标题、内容") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                )
            },
            expanded = false,
            onExpandedChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = { }
        )

        // 标签栏容器：位移 + 渐变消失
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val height = placeable.height
                    if (scrollBehavior.state.heightOffsetLimit != -height.toFloat()) {
                        scrollBehavior.state.heightOffsetLimit = -height.toFloat()
                    }
                    
                    val currentHeight = (height + scrollBehavior.state.heightOffset).roundToInt()
                    layout(placeable.width, currentHeight) {
                        placeable.placeRelative(0, scrollBehavior.state.heightOffset.roundToInt())
                    }
                }
                .graphicsLayer {
                    // 计算透明度：1.0 -> 0.0
                    val limit = scrollBehavior.state.heightOffsetLimit
                    alpha = if (limit != 0f) {
                        (1f - (scrollBehavior.state.heightOffset / limit)).coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                }
                .clipToBounds()
        ) {
            Column {
                FilterRow(
                    items = dynasties,
                    selected = selectedDynasty,
                    onSelect = onDynastySelect,
                    state = dynastyListState
                )
                FilterRowWithMore(
                    items = recommendedTunes.map { it.name },
                    selected = selectedTune,
                    onSelect = onTuneSelect,
                    onMore = onAllTunesClick,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    state = tuneListState
                )
                FilterRowWithMore(
                    items = recommendedTags.map { it.name },
                    selected = selectedTag,
                    onSelect = onTagSelect,
                    onMore = onAllTagsClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    state = tagListState
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            if (searchResult.poets.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SectionTitle("诗人")
                }
                items(searchResult.poets, key = { "poet_${it.id}" }) { poet ->
                    PoetPreviewCard(poet = poet, onClick = { onPoetClick(poet.id) })
                }
            }

            if (searchResult.poems.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SectionTitle("诗词")
                }
                items(searchResult.poems, key = { "poem_${it.poem.id}" }) { userPoem ->
                    PoemPreviewCard(userPoem = userPoem, onClick = { onPoemClick(userPoem.poem.id) })
                }
            }
        }
    }
}

@Composable
fun FilterRow(
    items: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    state: LazyListState
) {
    LazyRow(
        state = state,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { _, item ->
            FilterChip(
                selected = selected == item,
                onClick = { onSelect(if (selected == item) null else item) },
                label = { Text(text = item) }
            )
        }
    }
}

@Composable
fun FilterRowWithMore(
    items: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    onMore: () -> Unit,
    containerColor: Color,
    state: LazyListState
) {
    LazyRow(
        state = state,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(items) { _, item ->
            FilterChip(
                selected = selected == item,
                onClick = { onSelect(if (selected == item) null else item) },
                label = { Text(text = item) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = containerColor
                )
            )
        }
        item {
            IconButton(onClick = onMore, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun ExploreContentPreview() {
    SuFeiTheme {
        ExploreContent(
            searchQuery = "李白",
            onSearchQueryChange = {},
            selectedDynasty = "唐代",
            onDynastySelect = {},
            selectedTag = "送别",
            onTagSelect = {},
            selectedTune = "浣溪沙",
            onTuneSelect = {},
            recommendedTags = listOf(Tag("送别")),
            recommendedTunes = listOf(Tune("浣溪沙")),
            searchResult = SearchResult(
                poets = listOf(Poet(id = "1", name = "李白", dynasty = "唐代", lifetime = "诗仙李白...")),
                poems = listOf(
                    UserPoem(
                        poem = Poem(id = "1", sourceUrl = "", title = "静夜思", author = "李白", dynasty = "唐代", content = "床前明月光...", tags = listOf()),
                        isFavorite = false
                    )
                )
            ),
            onPoemClick = {},
            onPoetClick = {},
            onAllTagsClick = {},
            onAllTunesClick = {},
        )
    }
}
