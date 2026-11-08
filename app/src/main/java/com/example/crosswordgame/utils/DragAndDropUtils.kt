package com.example.crosswordgame.utils

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

/**
 * Enhanced DraggableScreen: Manages visual feedback and global drag state.
 */
@Composable
fun DraggableScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(
        LocalDragTargetInfo provides state
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
            if (state.isDragging) {
                var targetSize by remember { mutableStateOf(IntSize.Zero) }
                Box(modifier = Modifier
                    .graphicsLayer {
                        val offset = (state.dragPosition + state.dragOffset)
                        // Dynamic scale and shadow-like lifting effect
                        scaleX = 1.2f
                        scaleY = 1.2f
                        alpha = 0.9f
                        translationX = offset.x.minus(targetSize.width / 2)
                        translationY = offset.y.minus(targetSize.height * 2f) 
                    }
                    .onGloballyPositioned {
                        targetSize = it.size
                    }
                ) {
                    state.draggableComposable?.invoke()
                }
            }
        }
    }
}

@Composable
fun <T> DragTarget(
    modifier: Modifier,
    dataToDrop: T,
    content: @Composable (() -> Unit)
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    val currentState = LocalDragTargetInfo.current
    val scope = rememberCoroutineScope()

    Box(modifier = modifier
        .onGloballyPositioned {
            currentPosition = it.positionInWindow()
        }
        .pointerInput(dataToDrop) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    currentState.dataToDrop = dataToDrop
                    currentState.isDragging = true
                    currentState.dragPosition = currentPosition + offset
                    currentState.draggableComposable = content
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                },
                onDragEnd = {
                    currentState.isDragging = false
                    currentState.dragOffset = Offset.Zero
                    currentState.onDrop(scope)
                },
                onDragCancel = {
                    currentState.dragOffset = Offset.Zero
                    currentState.isDragging = false
                }
            )
        }) {
        content()
    }
}

@Composable
fun DropTarget(
    modifier: Modifier,
    onDrop: (data: Any) -> Unit,
    content: @Composable (BoxScope.(isHovered: Boolean) -> Unit)
) {
    val dragInfo = LocalDragTargetInfo.current
    val dragPosition = dragInfo.dragPosition + dragInfo.dragOffset
    var isCurrentDropTarget by remember { mutableStateOf(false) }

    LaunchedEffect(dragInfo.isDropped) {
        if (dragInfo.isDropped && isCurrentDropTarget) {
            dragInfo.dataToDrop?.let { onDrop(it) }
        }
    }

    Box(modifier = modifier.onGloballyPositioned {
        val rect = it.boundsInWindow()
        isCurrentDropTarget = dragInfo.isDragging && rect.contains(dragPosition)
    }) {
        content(isCurrentDropTarget)
    }
}

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var isDropped: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)

    fun onDrop(scope: CoroutineScope) {
        isDropped = true
        scope.launch {
            delay(100) // Brief window for targets to capture the drop event
            isDropped = false
        }
    }
}
