package com.example.crosswordgame.utils

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow

/**
 * DraggableItem: A robust wrapper to make any UI element draggable.
 * Consumes touch events fully to prevent app-exit issues during dragging.
 */
@Composable
fun <T> DraggableItem(
    data: T,
    onDrop: (String?, T) -> Unit,
    content: @Composable () -> Unit
) {
    val state = LocalDragDropState.current
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { currentPosition = it.positionInWindow() }
            .pointerInput(data) {
                try {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            state.onDragStart(data as Any, currentPosition + offset, content)
                        },
                        onDrag = { change, dragAmount ->
                            // CRITICAL: Consume the change fully to prevent parent intercepting or system gesture interference
                            if (change.pressed) {
                                change.consume()
                                state.onDrag(dragAmount)
                            }
                        },
                        onDragEnd = {
                            state.onDragEnd { targetId, droppedData ->
                                @Suppress("UNCHECKED_CAST")
                                onDrop(targetId, droppedData as T)
                            }
                        },
                        onDragCancel = {
                            state.onDragEnd { _, _ -> }
                        }
                    )
                } catch (e: Exception) {
                    state.resetState()
                }
            }
    ) {
        content()
    }
}
