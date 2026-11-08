package com.example.crosswordgame.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * CollisionDetector: Provides accurate hit-testing for drag and drop interactions.
 */
object CollisionDetector {
    
    /**
     * Identifies the drop target under the given point with optional tolerance.
     */
    fun findTargetId(
        dragPoint: Offset, 
        targets: Map<String, Rect>,
        tolerance: Float = 0.4f // Optimized tolerance for mobile touch
    ): String? {
        return targets.entries.find { entry ->
            val rect = entry.value
            // Expand the detection area to make snapping feel "smarter"
            val expandedRect = Rect(
                left = rect.left - rect.width * tolerance,
                top = rect.top - rect.height * tolerance,
                right = rect.right + rect.width * tolerance,
                bottom = rect.bottom + rect.height * tolerance
            )
            expandedRect.contains(dragPoint)
        }?.key
    }
}
