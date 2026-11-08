package com.example.crosswordgame.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * DragDropContainer: Orchestrates the global drag and drop experience with enhanced visibility.
 * Implements "Lift & Follow" interaction to prevent finger from blocking the view.
 */
@Composable
fun DragDropContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragDropState() }
    val density = LocalDensity.current
    
    // 60dp offset to lift the item above the finger for better visibility
    val verticalOffsetPx = with(density) { 60.dp.toPx() }

    CompositionLocalProvider(LocalDragDropState provides state) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
            
            if (state.isDragging) {
                var targetSize by remember { mutableStateOf(IntSize.Zero) }
                
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = state.dragPosition + state.dragOffset
                            
                            // Lift & Follow: Position the item exactly above the touch point
                            translationX = offset.x.minus(targetSize.width / 2)
                            translationY = offset.y.minus(targetSize.height / 2).minus(verticalOffsetPx)
                            
                            // Visual Feedback: Scale up and slight transparency
                            scaleX = 1.2f
                            scaleY = 1.2f
                            alpha = 0.95f
                        }
                        .shadow(elevation = 8.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .onGloballyPositioned { targetSize = it.size }
                ) {
                    state.draggableContent?.invoke()
                }
            }
        }
    }
}
