package dev.wceng.sufei.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun DraggableCard(
    item: Any,
    onSwiped: (SwipeResult, Any) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val swipeXLeft = -(screenWidth.value * 3.2).toFloat()
    val swipeXRight = (screenWidth.value * 3.2).toFloat()
    val swipeYTop = -1000f
    val swipeYBottom = 1000f
    
    val swipeX = remember { Animatable(0f) }
    val swipeY = remember { Animatable(0f) }
    
    swipeX.updateBounds(swipeXLeft, swipeXRight)
    swipeY.updateBounds(swipeYTop, swipeYBottom)

    // 只有在未完全飞出时才渲染卡片
    if (abs(swipeX.value) < swipeXRight - 50f) {
        val rotationFraction = (swipeX.value / 60).coerceIn(-40f, 40f)
        Box(
            modifier = modifier
                .dragContent(
                    swipeX = swipeX,
                    swipeY = swipeY,
                    maxX = swipeXRight
                )
                .graphicsLayer(
                    translationX = swipeX.value,
                    translationY = swipeY.value,
                    rotationZ = rotationFraction,
                )
        ) {
            content()
        }
    } else {
        // 动画到达边界，触发回调
        val swipeResult = if (swipeX.value > 0) SwipeResult.RIGHT else SwipeResult.LEFT
        LaunchedEffect(item) {
            onSwiped(swipeResult, item)
        }
    }
}

fun Modifier.dragContent(
    swipeX: Animatable<Float, AnimationVector1D>,
    swipeY: Animatable<Float, AnimationVector1D>,
    maxX: Float
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    pointerInput(Unit) {
        detectDragGestures(
            onDragCancel = {
                coroutineScope.launch {
                    launch { swipeX.animateTo(0f) }
                    launch { swipeY.animateTo(0f) }
                }
            },
            onDragEnd = {
                coroutineScope.launch {
                    // 如果滑动不到 1/4 则回弹
                    if (abs(swipeX.targetValue) < abs(maxX) / 4) {
                        launch { swipeX.animateTo(0f, tween(400)) }
                        launch { swipeY.animateTo(0f, tween(400)) }
                    } else {
                        // 飞出屏幕
                        launch {
                            if (swipeX.targetValue > 0) {
                                swipeX.animateTo(maxX, tween(400))
                            } else {
                                swipeX.animateTo(-maxX, tween(400))
                            }
                        }
                    }
                }
            }
        ) { change, dragAmount ->
            change.consume()
            coroutineScope.launch {
                // 使用 animateTo 产生平滑跟随效果，与参考代码保持一致
                launch { swipeX.animateTo(swipeX.targetValue + dragAmount.x) }
                launch { swipeY.animateTo(swipeY.targetValue + dragAmount.y) }
            }
        }
    }
}
