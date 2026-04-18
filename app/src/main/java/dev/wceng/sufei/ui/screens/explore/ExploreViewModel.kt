package dev.wceng.sufei.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.wceng.sufei.data.model.SearchResult
import dev.wceng.sufei.data.model.Tag
import dev.wceng.sufei.data.model.Tune
import dev.wceng.sufei.data.model.UserPoem
import dev.wceng.sufei.data.repository.PoemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val poemRepository: PoemRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedDynasty = MutableStateFlow<String?>(null)
    val selectedDynasty = _selectedDynasty.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    private val _selectedTune = MutableStateFlow<String?>(null)
    val selectedTune = _selectedTune.asStateFlow()

    private val hotTagNames = listOf(
        "唐诗三百首", "宋词三百首", "古诗三百首", "送别", "思乡",
        "山水", "边塞", "咏物", "抒情", "爱情",
        "爱国", "哲理", "闺怨", "豪放", "婉约"
    )

    /**
     * 热门标签：固定显示 15 个热门标签，并确保选中项可见
     */
    val recommendedTags: StateFlow<List<Tag>> = _selectedTag
        .map { selected ->
            val top = hotTagNames.map { Tag(it) }.toMutableList()
            if (selected != null && top.none { it.name == selected }) {
                top.add(0, Tag(selected)) // 如果选中的不在热门列表中，插到最前面
            }
            top
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hotTagNames.map { Tag(it) }
        )

    private val hotTuneNames = listOf(
        "浣溪沙", "水调歌头", "菩萨蛮", "鹧鸪天", "满江红",
        "临江仙", "蝶恋花", "西江月", "念奴娇", "减字木兰花",
        "沁园春", "点绛唇", "贺新郎", "清平乐", "虞美人"
    )

    /**
     * 热门词牌：固定显示 15 个热门词牌，并确保选中项可见
     */
    val recommendedTunes: StateFlow<List<Tune>> = _selectedTune
        .map { selected ->
            val top = hotTuneNames.map { Tune(it) }.toMutableList()
            if (selected != null && top.none { it.name == selected }) {
                top.add(0, Tune(selected))
            }
            top
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hotTuneNames.map { Tune(it) }
        )

    // 混合搜索结果：包含诗人和诗词
    val searchResults: StateFlow<SearchResult> =
        combine(_searchQuery, _selectedDynasty, _selectedTag, _selectedTune) { query, dynasty, tag, tune ->
            DataTuple(query, dynasty, tag, tune)
        }
            .debounce(300)
            .flatMapLatest { tuple ->
                if (tuple.query.isBlank() && tuple.dynasty == null && tuple.tag == null && tuple.tune == null) {
                    // 默认状态显示 50 首诗词
                    poemRepository.getAllUserPoems(limit = 50).map { poems ->
                        SearchResult(poems = poems)
                    }
                } else {
                    // 正式启用词牌过滤
                    poemRepository.searchAll(tuple.query, tuple.dynasty, tuple.tag, tuple.tune, limit = 50)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SearchResult()
            )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onDynastySelect(dynasty: String?) {
        _selectedDynasty.value = dynasty
    }

    fun onTagSelect(tag: String?) {
        _selectedTag.value = tag
    }

    fun onTuneSelect(tune: String?) {
        _selectedTune.value = tune
    }

    private data class DataTuple(val query: String, val dynasty: String?, val tag: String?, val tune: String?)
}
