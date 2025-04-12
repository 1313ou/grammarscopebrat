package com.bbou.brats

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.widget.TextView
import grammarscope.Edge
import grammarscope.IRenderer

// Sealed class for different types of between-lines annotations
sealed class Annotation {

    abstract fun draw(canvas: Canvas, textView: AnnotatedTextView)

    data class BoxAnnotation(
        val box: RectF
    ) : Annotation() {

        private val drawPaint = Paint().apply {
            color = Color.RED
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        private val fillPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        override fun draw(canvas: Canvas, textView: AnnotatedTextView) {

        }
    }

    data class EdgeAnnotation(
        val edge: Edge
    ) : Annotation() {

        private val drawPaint = Paint().apply {
            color = Color.RED
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        private val fillPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        override fun draw(canvas: Canvas, textView: AnnotatedTextView) {
        }

        override fun toString(): String {
            return "EdgeAnnotation ${edge.tag} y=${edge.yBase} h=${edge.height} x1=${edge.x1} x2=${edge.x2}"
        }
    }
}