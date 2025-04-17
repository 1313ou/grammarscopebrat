package com.bbou.brats

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.toColorInt
import grammarscope.DependencyPainter.drawEdge
import grammarscope.Edge

// Sealed class for different types of between-lines annotations

enum class AnnotationType {
    EDGE,
    BOX,
    TOKENBOX,
}

sealed class Annotation {

    data class BoxAnnotation(
        val box: RectF,
        val color: Int,
        val isToken: Boolean = false,
    ) : Annotation() {

        fun draw(canvas: Canvas) {
            val boxColor = color
            canvas.drawRect(box, if (isToken) boxTokenPaint else boxPaint.apply { this.color = boxColor })
        }

        companion object {
            var boxPaint = Paint().apply {
                style = Paint.Style.FILL
                //color = "#40ff0000".toColorInt()
            }
            var boxTokenPaint = Paint().apply {
                style = Paint.Style.FILL
                color = "#40ffff00".toColorInt()
            }
        }
    }

    data class EdgeAnnotation(
        val edge: Edge
    ) : Annotation() {

        fun draw(canvas: Canvas, padWidth: Float) {
            drawEdge(canvas, this, padWidth)
        }

        override fun toString(): String {
            return "EdgeAnnotation ${edge.tag} y=${edge.yBase} h=${edge.height} x1=${edge.x1} x2=${edge.x2}"
        }
    }
}