package com.example.crosswordgame.utils

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.crosswordgame.utils.AppLogger

internal val LocalDragDropState = compositionLocalOf { DragDropState() }

/**
 * DragDropState: Manages the state and hit-testing logic for drag and drop interactions.
 * Built with defensive checks to prevent app crashes during rapid dragging.
 */
class DragDropState {
    var isDragging: Boolean by mutableStateOf(false)
    var dragOffset: Offset by mutableStateOf(Offset.Zero)
    var dragPosition: Offset by mutableStateOf(Offset.Zero)
    var draggableContent by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
    
    private val dropTargets = mutableStateMapOf<String, Rect>()
    var hoveredTargetId: String? by mutableStateOf(null)

    fun onDragStart(data: Any, position: Offset, content: @Composable () -> Unit) {
        try {
            dataToDrop = data
            dragPosition = position
            draggableContent = content
            isDragging = true
            dragOffset = Offset.Zero
            AppLogger.d("DragDrop", "Drag started for: $data")
        } catch (e: Exception) {
            AppLogger.e("DragDrop", "Error in onDragStart", e)
            resetState()
        }
    }

    fun onDrag(offset: Offset) {
        if (!isDragging) return
        try {
            dragOffset += offset
            updateHoveredTarget()
        } catch (e: Exception) {
            AppLogger.e("DragDrop", "Error during drag", e)
            resetState()
        }
    }

    fun onDragEnd(onDrop: (String?, Any?) -> Unit) {
        if (!isDragging) return
        try {
            updateHoveredTarget() 
            val targetId = hoveredTargetId
            val data = dataToDrop
            
            AppLogger.d("DragDrop", "Drag ended. Target: $targetId")
            onDrop(targetId, data)
        } catch (e: Exception) {
            AppLogger.e("DragDrop", "Error in onDragEnd", e)
        } finally {
            resetState()
        }
    }

    fun resetState() {
        isDragging = false
        dragOffset = Offset.Zero
        dragPosition = Offset.Zero
        hoveredTargetId = null
        dataToDrop = null
        draggableContent = null
    }

    fun addDropTarget(id: String, rect: Rect) {
        dropTargets[id] = rect
    }

    fun removeDropTarget(id: String) {
        dropTargets.remove(id)
    }

    private fun updateHoveredTarget() {
        if (!isDragging) return
        val currentGlobalPosition = dragPosition + dragOffset
        hoveredTargetId = CollisionDetector.findTargetId(currentGlobalPosition, dropTargets)
    }
}
