package com.example.crosswordgame.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * DropTarget: A composable that registers its bounds for hit-testing during drag interactions.
 */
@Composable
fun DropTarget(
    id: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(isHovered: Boolean) -> Unit
) {
    val state = LocalDragDropState.current
    val isHovered = state.hoveredTargetId == id

    Box(
        modifier = modifier.onGloballyPositioned {
            state.addDropTarget(id, it.boundsInWindow())
        }
    ) {
        content(isHovered)
    }

    DisposableEffect(id) {
        onDispose { state.removeDropTarget(id) }
    }
}
