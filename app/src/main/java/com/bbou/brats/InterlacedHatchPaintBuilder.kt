package com.bbou.brats

import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import kotlin.math.sqrt

/**
 * A Paint factory for creating seamless interlaced hatch patterns.
 */
class InterlacedHatchPaintBuilder {

    /**
     * Creates a Paint with an interlaced hatching pattern with no gaps.
     *
     * @param color Color of the first hatch pattern
     * @param angle Angle of the first hatch pattern in degrees
     * @param spacing Spacing between lines (default: 10f)
     * @param strokeWidth Width of the hatch lines (default: 1f)
     * @return A Paint object ready to use with Canvas
     */
    fun build(
        angle: Float,
        spacing: Float = 20f,
        strokeWidth: Float = 10f,
        color: Int,
    ): Paint {

        // Calculate a size that ensures pattern repeats correctly
        val diagonalSize = (spacing * 10).toInt().coerceAtLeast(100)

        // Create bitmap large enough to avoid gaps
        val bitmap = createBitmap(diagonalSize, diagonalSize)
        val canvas = Canvas(bitmap)

        // Fill with transparent background
        canvas.drawColor(Color.TRANSPARENT)

        // Calculate how many lines we need to cover the diagonal
        val diagonalLength = sqrt((diagonalSize * diagonalSize * 2).toDouble()).toFloat()
        val linesNeeded = (diagonalLength / spacing).toInt() + 4 // Add extra lines for safety

        // Start position before canvas edges to ensure complete coverage
        val startPos = -diagonalSize / 2f
        val endPos = diagonalSize * 1.5f

        // Paint for pattern lines
        val linePaint = Paint().apply {
            this.isAntiAlias = true
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            this.color = color
        }
        // Draw set of lines
         drawLinesAtAngle(canvas, angle, spacing, startPos, endPos, linesNeeded, diagonalSize, linePaint)

        // Create a shader from our pattern
        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

        // Return a paint with the shader
        return Paint().apply {
            isAntiAlias = true
            this.shader = shader
        }
    }

    /**
     * Helper method to draw many lines at the specified angle
     */
    private fun drawLinesAtAngle(
        canvas: Canvas,
        angleDegrees: Float,
        spacing: Float,
        startPos: Float,
        endPos: Float,
        lineCount: Int,
        size: Int,
        paint: Paint
    ) {
        canvas.withSave {
            // Center and rotate the canvas
            val centerPoint = size / 2f
            rotate(angleDegrees, centerPoint, centerPoint)

            // Draw lines across canvas
            for (i in 0 until lineCount) {
                val y = startPos + (i * spacing)
                drawLine(startPos, y, endPos, y, paint)
            }
        }
    }
}
