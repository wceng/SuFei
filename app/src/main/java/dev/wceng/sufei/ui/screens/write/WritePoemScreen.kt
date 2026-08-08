package dev.wceng.sufei.ui.screens.write

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import dev.wceng.sufei.R
import dev.wceng.sufei.ui.theme.NotoSerifSC
import dev.wceng.sufei.ui.theme.SuFeiTheme
import kotlinx.coroutines.launch

/** 妃红色印章色 —— 与首页诗人印章一致 */
private val SealRed = Color(0xFFE09E87)

// --- 数据模型 ---

data class TunePattern(
    val name: String,
    val pattern: String,
    val description: String? = null
)

val samplePatterns = listOf(
    TunePattern("五言绝句", "⊙○⊙●●，⊙●●○○。\n⊙●⊙○●，⊙○●●○。"),
    TunePattern("七言绝句", "⊙●○○⊙●●，⊙○●●●○○。\n⊙○●●⊙○●，⊙●○○●●○。"),
    TunePattern("五言律诗", "⊙●○○●，平○●●○。\n⊙○平●●，⊙●●○○。\n⊙●○○●，平○●●○。\n⊙○平●●，⊙●●○○。"),
    TunePattern("七言律诗", "⊙●○○⊙●●，⊙○●●●○○。\n⊙○●●⊙○●，⊙●○○●●○。\n⊙●○○⊙●●，⊙○●●●○○。\n⊙○●●⊙○●，⊙●○○●●○。"),
    TunePattern("相见欢", "⊙○⊙●○○（韵）\n●○○（韵）\n⊙●⊙○⊙●●○○（韵）\n\n⊙○⊙●○○（韵）\n●○○（韵）\n⊙●⊙○⊙●●○○（韵）"),
    TunePattern("浣溪沙", "⊙●○○⊙●○（韵）\n⊙○⊙●●○○（韵）\n⊙○⊙●●○○（韵）\n\n⊙●⊙○○●●\n⊙○⊙●●○○（韵）\n⊙○⊙●●○○（韵）"),
    TunePattern("卜算子", "⊙●●○○\n⊙●○○●（韵）\n⊙●○○●●○\n⊙●○○●（韵）\n\n⊙●●○○\n⊙●○○●（韵）\n⊙●○○●●○\n⊙●○○●（韵）"),
    TunePattern("鹧鸪天", "⊙●○○●●○（韵）\n⊙○⊙●●○○（韵）\n⊙○⊙●○○●\n⊙●○○●●○（韵）\n\n⊙○●\n●○○（韵）\n⊙○⊙●●○○（韵）\n⊙○⊙●○○●\n⊙●○○●●○（韵）"),
    TunePattern("水调歌头", "⊙●●○●\n⊙●●○○（韵）\n⊙○⊙●⊙●\n⊙●●○○（韵）\n⊙●⊙○⊙●\n⊙●⊙○⊙●\n⊙●●○○（韵）\n⊙●⊙○●\n⊙●●○○（韵）\n\n⊙○●\n⊙●●\n●○○（韵）\n⊙○⊙●\n⊙●⊙●●○○（韵）\n⊙●⊙○⊙●\n⊙●⊙○⊙●\n⊙●●○○（韵）\n⊙●⊙○●\n⊙●●○○（韵）")
)

sealed class GridCell {
    data class InputSlot(val pattern: Char, val index: Int) : GridCell()
}

/** 句末标点候选（循环切换顺序） */
private val punctuationOptions = listOf("，", "。", "？", "！", "；", "、")

/**
 * 解析结果：rows 每行仅含输入格；defaultPuncts 与 rows 平行，记录每行的句末标点（无则 ""）。
 */
private data class ParsedGrid(
    val rows: List<List<GridCell>>,
    val defaultPuncts: List<String>
)

// --- 逻辑处理 ---

/**
 * 将格律字符串解析为行列表，每行对应一句诗（以句末标点切分）。
 * 格律串中的空白行会保留为空行，由编辑器渲染为“联/阙”之间的间距。
 */
private fun parsePattern(pattern: String): ParsedGrid {
    var inputIndex = 0
    val rows = mutableListOf<List<GridCell>>()
    val defaultPuncts = mutableListOf<String>()
    pattern.split('\n').forEach { line ->
        var producedRow = false
        val row = mutableListOf<GridCell>()
        var trailingPunct = ""
        for (char in line) {
            when (char) {
                '○', '●', '⊙' -> row.add(GridCell.InputSlot(char, inputIndex++))
                '，', '。', '？', '！', '；', '、' -> {
                    // 句末标点即本句终点，另起一行
                    if (row.isNotEmpty()) {
                        rows.add(row.toList())
                        defaultPuncts.add(char.toString())
                        row.clear()
                        producedRow = true
                    }
                }
                else -> { /* 忽略“韵”等标记 */ }
            }
        }
        if (row.isNotEmpty()) {
            rows.add(row.toList())
            defaultPuncts.add(trailingPunct)
            producedRow = true
        }
        // 空白行：保留为空行，作为“联/阙”间距
        if (!producedRow) {
            rows.add(emptyList())
            defaultPuncts.add("")
        }
    }
    return ParsedGrid(rows, defaultPuncts)
}

// --- UI 组件 ---

@Composable
fun PoemCell(
    text: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    pattern: Char?,
    modifier: Modifier = Modifier,
    cellSize: Dp = 44.dp
) {
    // 选中态轻量动效：轻微放大 + 颜色平滑过渡
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1f,
        label = "cellScale"
    )
    // 填字后淡化平仄提示，避免与汉字争夺视线（保留微弱的格律参考）
    val patternAlpha by animateFloatAsState(
        targetValue = if (text.isNotEmpty()) 0.12f else 0.45f,
        label = "patternAlpha"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        label = "cellBorderColor"
    )
    val borderWidth = if (isFocused) 1.5.dp else 1.dp
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
            text.isNotEmpty() -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> Color.Transparent
        },
        label = "cellBackgroundColor"
    )

    Box(
        modifier = modifier
            .size(cellSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (isFocused) 2.dp else 0.dp, MaterialTheme.shapes.small)
            .background(backgroundColor, MaterialTheme.shapes.small)
            .border(borderWidth, borderColor, MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 底部的平仄提示（填字后淡化）
        pattern?.let {
            Text(
                text = it.toString(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp),
                style = TextStyle(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = patternAlpha)
                )
            )
        }

        // 单字展示（隐藏输入框负责实际输入）
        Text(
            text = text,
            style = TextStyle(
                fontFamily = NotoSerifSC,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun PunctuationSlot(text: String, onClick: () -> Unit, size: Dp = 36.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.ifEmpty { "·" },
            style = TextStyle(
                fontFamily = NotoSerifSC,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (text.isEmpty())
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/** 悬浮菜单里的单个标点选项（横排小方盒） */
@Composable
private fun PunctMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else
                    Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = NotoSerifSC,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * 句末标点悬浮菜单：锚定在标点槽**上方**，右缘与槽右缘对齐，横排显示所有符号。
 * 使用自定义 PopupPositionProvider 精确定位（DropdownMenu 默认锚在下方且内容尺寸会被 intrinsic 约束塌掉）。
 */
@Composable
private fun PunctMenuPopup(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val gapPx = with(density) { 8.dp.roundToPx() }
                return IntOffset(
                    // 右缘对齐标点槽右缘，长句行尾也不会溢出屏幕
                    x = anchorBounds.right - popupContentSize.width,
                    // 位于标点槽上方，紧贴其上
                    y = (anchorBounds.top - popupContentSize.height - gapPx).coerceAtLeast(0)
                )
            }
        }
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PunctMenuItem(
                    text = stringResource(R.string.write_poem_no_punct),
                    isSelected = current.isEmpty(),
                    onClick = { onSelect("") }
                )
                punctuationOptions.forEach { option ->
                    PunctMenuItem(
                        text = option,
                        isSelected = current == option,
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
fun PoemGridEditor(
    cellsByLine: List<List<GridCell>>,
    charList: List<String>,
    focusedIndex: Int,
    onCellClick: (Int) -> Unit,
    puncts: List<String>,
    onPunctSelect: (Int, String) -> Unit
) {
    // 当前打开标点菜单的行（null = 无）
    var menuRow by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // 按最宽的“句”自适应格子尺寸，保证每句独占一行且不折行；每句末尾恒有一个标点槽
        val gap = 4.dp
        val punctSize = 36.dp // 标点槽点击区域（比格子略小，但远大于文字本身）
        val cellSize = cellsByLine
            .filter { it.isNotEmpty() }
            .map { row ->
                val inputs = row.count { it is GridCell.InputSlot }
                if (inputs > 0) {
                    (maxWidth - gap * (inputs - 1) - (punctSize + gap)) / inputs
                } else {
                    Dp.Infinity
                }
            }
            .minOrNull()
            ?.coerceIn(34.dp, 44.dp)
            ?: 44.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            cellsByLine.forEachIndexed { rowIndex, lineCells ->
                if (lineCells.isEmpty()) {
                    // 格律串中的空行代表“联/阙”的分隔，给出更大间距
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                        // 标点槽与格子底部对齐（标点应落在句子的下缘而非中间）
                        verticalAlignment = Alignment.Bottom
                    ) {
                        lineCells.forEach { cell ->
                            val slot = cell as GridCell.InputSlot
                            PoemCell(
                                text = charList.getOrElse(slot.index) { "" },
                                isFocused = focusedIndex == slot.index,
                                onClick = { onCellClick(slot.index) },
                                pattern = slot.pattern,
                                cellSize = cellSize
                            )
                        }
                        // 句末标点槽：点击在槽位上方弹出横排符号菜单
                        Box {
                            PunctuationSlot(
                                text = puncts.getOrElse(rowIndex) { "" },
                                onClick = { menuRow = rowIndex },
                                size = punctSize
                            )
                            if (menuRow == rowIndex) {
                                PunctMenuPopup(
                                    current = puncts.getOrElse(rowIndex) { "" },
                                    onSelect = { value ->
                                        onPunctSelect(rowIndex, value)
                                        menuRow = null
                                    },
                                    onDismiss = { menuRow = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 题材（词牌）选择抽屉 ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TunePickerSheet(
    tunes: List<TunePattern>,
    selectedTune: TunePattern,
    onTuneSelected: (TunePattern) -> Unit,
    onDismiss: () -> Unit
) {
    // 默认展开为全屏：跳过半屏状态，内容铺满可用高度
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val selectAndClose: (TunePattern) -> Unit = { tune ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onTuneSelected(tune)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.write_poem_select_tune),
                fontFamily = NotoSerifSC,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(tunes) { tune ->
                    val isSelected = tune == selectedTune
                    ListItem(
                        modifier = Modifier.clickable { selectAndClose(tune) },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                Color.Transparent
                        ),
                        leadingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SealRed
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = tune.name,
                                fontFamily = NotoSerifSC,
                                fontSize = 18.sp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            // 题材下方的平仄标记（首行）
                            tune.pattern.split('\n').firstOrNull()?.let { firstLine ->
                                Text(
                                    text = firstLine,
                                    fontFamily = NotoSerifSC,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- 主页面 ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePoemScreen(
    onBack: () -> Unit,
    viewModel: WritePoemViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var selectedTune by remember { mutableStateOf(samplePatterns[0]) }
    var showTunePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveSuccessMsg = stringResource(R.string.write_poem_save_success)

    // 解析后的格子结构
    val parsed = remember(selectedTune) { parsePattern(selectedTune.pattern) }
    val cellsByLine = parsed.rows
    val puncts = remember(selectedTune) { parsed.defaultPuncts.toMutableStateList() }
    val totalInputSlots = remember(cellsByLine) {
        cellsByLine.flatten().filterIsInstance<GridCell.InputSlot>().size
    }

    // 输入状态管理：每格一字符（"" = 未填），光标独立于文本长度，
    // 因此可以点击任意空位，“输入”是替换当前格而不是在中间插入。
    // 关键：IME 光标永远位于“当前格之后”的间隙（selection = cursorIndex + 1），
    // 这样退格总是能删除当前格——包括格 0（若光标在最前间隙，软键盘退格无字符可删，不触发事件）
    val chars = remember(selectedTune) { MutableList(totalInputSlots) { "" }.toMutableStateList() }
    var cursorIndex by remember(selectedTune) { mutableIntStateOf(0) }
    val buildFullText: () -> String = { chars.joinToString("") { it.ifEmpty { "　" } } }
    var poemValue by remember(selectedTune) {
        mutableStateOf(TextFieldValue(buildFullText(), TextRange(1)))
    }
    val hiddenFocusRequester = remember { FocusRequester() }
    val focusedIndex = cursorIndex.coerceIn(0, (totalInputSlots - 1).coerceAtLeast(0))

    // 显式唤起输入法：焦点已持有时 requestFocus 无效，需显式 show()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showKeyboardTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(showKeyboardTick) {
        if (showKeyboardTick > 0) keyboardController?.show()
    }

    // 点按格子：光标定位到该格（空位亦可），并唤起输入法
    val handleCellClick: (Int) -> Unit = { index ->
        cursorIndex = index.coerceIn(0, (totalInputSlots - 1).coerceAtLeast(0))
        poemValue = TextFieldValue(buildFullText(), TextRange(cursorIndex + 1))
        hiddenFocusRequester.requestFocus()
        showKeyboardTick++
    }

    // 提交输入：取“当前格后间隙处新增的字符”写入对应格（替换），随后光标推进
    val commitInput: (TextFieldValue) -> Unit = { newValue ->
        val oldText = buildFullText()
        val delta = newValue.text.length - oldText.length
        if (delta > 0) {
            // 新增（含 IME 候选一次提交多字）：插入点在当前格之后的间隙
            val start = (cursorIndex + 1).coerceAtMost(newValue.text.length)
            val added = newValue.text.substring(start, (start + delta).coerceAtMost(newValue.text.length))
            var written = 0
            while (written < added.length && cursorIndex + written < totalInputSlots) {
                chars[cursorIndex + written] = added[written].toString()
                written++
            }
            cursorIndex = (cursorIndex + written).coerceAtMost(totalInputSlots - 1)
        } else if (delta < 0) {
            // 退格：IME 删的是当前格字符（光标在其后间隙），删除当前格并回退
            var c = cursorIndex
            val steps = (-delta).coerceAtMost(c + 1)
            repeat(steps) {
                if (c in 0 until totalInputSlots) chars[c] = ""
                c = (c - 1).coerceAtLeast(0)
            }
            cursorIndex = c
        } else {
            // 纯光标移动（方向键等）：selection 是间隙位置，高亮格 = selection - 1
            cursorIndex = (newValue.selection.end - 1).coerceIn(0, totalInputSlots - 1)
        }
        poemValue = TextFieldValue(buildFullText(), TextRange(cursorIndex + 1))
    }

    // 从悬浮菜单选择句末标点（"" 表示无标点）
    val handlePunctSelect: (Int, String) -> Unit = { rowIndex, value ->
        puncts[rowIndex] = value
    }

    val saveContent: () -> Unit = {
        val fullContent = buildString {
            cellsByLine.forEachIndexed { rowIndex, line ->
                line.forEach { cell ->
                    when (cell) {
                        is GridCell.InputSlot -> append(chars.getOrElse(cell.index) { "" }.ifEmpty { " " })
                    }
                }
                puncts.getOrElse(rowIndex) { "" }.takeIf { it.isNotEmpty() }?.let { append(it) }
                append("\n")
            }
        }.trim()
        viewModel.savePoem(title, fullContent, selectedTune.name)
        scope.launch {
            snackbarHostState.showSnackbar(saveSuccessMsg)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        // 布局区压缩到键盘上方：bottomBar（存/保存至枕边）悬浮在键盘之上，固定不动
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.write_poem_title),
                        fontFamily = NotoSerifSC,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = saveContent) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = stringResource(R.string.write_poem_save),
                            tint = SealRed
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题输入（纸上题名：居中衬线 + 极淡下划线）
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = NotoSerifSC,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Column(
                            modifier = Modifier.widthIn(max = 320.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (title.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.write_poem_title_placeholder),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = NotoSerifSC,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            )
                        }
                    }
                )

                // 词牌选择（行内轻量选择，去掉表单感；作为写作格式选择器贴近网格）
                Row(
                    modifier = Modifier
                        .clickable { showTunePicker = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.write_poem_tune_label),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = NotoSerifSC),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = selectedTune.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = NotoSerifSC),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // 格子铺编辑器
                PoemGridEditor(
                    cellsByLine = cellsByLine,
                    charList = chars,
                    focusedIndex = focusedIndex,
                    onCellClick = handleCellClick,
                    puncts = puncts,
                    onPunctSelect = handlePunctSelect
                )

                // 隐藏输入框：持有全部按键与 IME 连接，格子仅作展示
                BasicTextField(
                    value = poemValue,
                    onValueChange = { newValue ->
                        // IME 组合中（拼音候选等）由输入框自行管理，组合结束再统一提交
                        if (newValue.composition == null) {
                            commitInput(newValue)
                        } else {
                            poemValue = newValue
                        }
                    },
                    modifier = Modifier
                        .size(1.dp)
                        .focusRequester(hiddenFocusRequester),
                    textStyle = TextStyle(fontSize = 20.sp, color = Color.Transparent),
                    cursorBrush = SolidColor(Color.Transparent),
                    singleLine = true
                )

                // 平仄图例
                Text(
                    text = stringResource(R.string.write_poem_metrical_legend),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerifSC),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                // 标点操作提示
                Text(
                    text = stringResource(R.string.write_poem_punct_hint),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = NotoSerifSC),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showTunePicker) {
        TunePickerSheet(
            tunes = samplePatterns,
            selectedTune = selectedTune,
            onTuneSelected = { tune ->
                selectedTune = tune
                showTunePicker = false
            },
            onDismiss = { showTunePicker = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WritePoemScreenPreview() {
    SuFeiTheme {
        WritePoemScreen(onBack = {})
    }
}
