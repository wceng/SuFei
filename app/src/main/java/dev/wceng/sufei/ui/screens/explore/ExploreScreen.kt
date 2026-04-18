package dev.wceng.sufei.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.data.model.Poem
import dev.wceng.sufei.data.model.Poet
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.ui.theme.SuFeiTheme

@Composable
fun ExploreScreen(
    onPoemClick: (String) -> Unit,
    onPoetClick: (String) -> Unit,
    onAllTagsClick: () -> Unit,
    onAllTunesClick: () -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchResults.collectAsState()
    val selectedDynasty by viewModel.selectedDynasty.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val selectedTune by viewModel.selectedTune.collectAsState()
    val recommendedTags by viewModel.recommendedTags.collectAsState()
    val recommendedTunes by viewModel.recommendedTunes.collectAsState()

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
        onAllTagsClick = onAllTagsClick,
        onAllTunesClick = onAllTunesClick,
    )
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

    // 为每一行过滤列表维护滚动状态
    val dynastyListState = rememberLazyListState()
    val tuneListState = rememberLazyListState()
    val tagListState = rememberLazyListState()

    // 监听选中项的变化，自动滚动到可见区域
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
    ) {
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
                )
            },
            expanded = false,
            onExpandedChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = { }
        )

        // 1. 朝代过滤
        FilterRow(
            items = dynasties,
            selected = selectedDynasty,
            onSelect = onDynastySelect,
            state = dynastyListState
        )

        // 2. 词牌过滤
        FilterRowWithMore(
            items = recommendedTunes.map { it.name },
            selected = selectedTune,
            onSelect = onTuneSelect,
            onMore = onAllTunesClick,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            state = tuneListState
        )

        // 3. 标签过滤
        FilterRowWithMore(
            items = recommendedTags.map { it.name },
            selected = selectedTag,
            onSelect = onTagSelect,
            onMore = onAllTagsClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            state = tagListState
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                label = { Text(text = item, fontFamily = FontFamily.Serif, fontSize = 13.sp) }
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
                label = { Text(text = item, fontFamily = FontFamily.Serif, fontSize = 13.sp) },
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
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    )
}

@Composable
fun PoetPreviewCard(
    poet: Poet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "诗人",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = poet.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = poet.dynasty,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            if (!poet.lifetime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = poet.lifetime,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 18.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PoemPreviewCard(
    userPoem: UserPoem,
    onClick: () -> Unit
) {
    val poem = userPoem.poem
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = poem.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = poem.author,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = poem.content.replace("\n", " "),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreContentPreview() {
    SuFeiTheme {
        ExploreContent(
            searchQuery = "",
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
