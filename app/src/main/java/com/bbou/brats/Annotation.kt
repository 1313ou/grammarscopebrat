package com.bbou.brats

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.widget.TextView
import kotlin.math.cos
import kotlin.math.sin

// Sealed class for different types of between-lines annotations
sealed class Annotation {

    abstract fun draw(canvas: Canvas, textView: TextView)

    data class Arrow(
        val fromWordStart: Int, val fromWordEnd: Int,
        val toWordStart: Int, val toWordEnd: Int
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

        override fun draw(canvas: Canvas, textView: TextView) {

            val arrowPath = Path()

            val paint: Paint = textView.paint
            val fontMetrics = paint.fontMetrics
            val ascent = fontMetrics.ascent
            val descent = fontMetrics.descent
            val leading = fontMetrics.leading
            val height = -ascent + descent + leading

            val fromWordPos = textView.getWordPosition(fromWordStart, fromWordEnd)
            val toWordPos = textView.getWordPosition(toWordStart, toWordEnd)

            if (fromWordPos != null && toWordPos != null) {
                // Get lines for each word
                val fromLine = textView.layout.getLineForOffset(fromWordStart)
                val toLine = textView.layout.getLineForOffset(toWordStart)

                // Only draw if words are on different lines
                if (fromLine != toLine) {
                    // Get middle of the word
                    val fromWordX = (fromWordPos.left + fromWordPos.right) / 2f
                    val toWordX = (toWordPos.left + toWordPos.right) / 2f

                    // Get bottom of first line and top of second line
                    val fromLineBottom = textView.layout.getLineTop(fromLine).toFloat() + textView.paddingTop + height
                    val toLineTop = textView.layout.getLineTop(toLine).toFloat() + textView.paddingTop

                    // Draw arrow in the space between lines
                    if (fromLine < toLine) {
                        // Downward arrow
                        canvas.drawLine(fromWordX, fromLineBottom, toWordX, toLineTop, drawPaint)

                        // Draw arrowhead
                        val arrowSize = 10f
                        arrowPath.reset()
                        arrowPath.moveTo(toWordX, toLineTop)
                        arrowPath.lineTo(toWordX - arrowSize, toLineTop - arrowSize)
                        arrowPath.lineTo(toWordX + arrowSize, toLineTop - arrowSize)
                        arrowPath.close()

                        canvas.drawPath(arrowPath, fillPaint)
                    } else {
                        // Upward arrow
                        canvas.drawLine(fromWordX, fromLineBottom, toWordX, toLineTop, drawPaint)

                        // Draw arrowhead
                        val arrowSize = 10f
                        arrowPath.reset()
                        arrowPath.moveTo(toWordX, toLineTop)
                        arrowPath.lineTo(toWordX - arrowSize, toLineTop + arrowSize)
                        arrowPath.lineTo(toWordX + arrowSize, toLineTop + arrowSize)
                        arrowPath.close()

                        canvas.drawPath(arrowPath, fillPaint)
                    }
                }
            }
        }
    }

    data class Icon(

        val wordStart: Int,
        val wordEnd: Int,
        val type: IconType
    ) : Annotation() {

        enum class IconType {
            STAR, CIRCLE, DIAMOND, ARROW_DOWN;
        }

        private val drawPaint = Paint().apply {
            color = Color.RED
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        override fun draw(canvas: Canvas, textView: TextView) {
            val wordPos = textView.getWordPosition(wordStart, wordEnd)

            if (wordPos != null) {
                // Get the line for the word
                val line = textView.layout.getLineForOffset(wordStart)

                // Only draw if there's another line below
                if (line < textView.layout.lineCount - 1) {
                    // Calculate center of the word
                    val wordCenterX = (wordPos.left + wordPos.right) / 2f

                    val paint: Paint = textView.paint
                    val fontMetrics = paint.fontMetrics
                    val ascent = fontMetrics.ascent
                    val descent = fontMetrics.descent
                    val leading = fontMetrics.leading
                    val height = -ascent + descent + leading

                    // Get bottom of current line and top of next line
                    val lineTop = textView.layout.getLineTop(line).toFloat() + textView.paddingTop
                    val lineBottom = lineTop + height
                    val nextLineTop = textView.layout.getLineTop(line + 1).toFloat() + textView.paddingTop

                    // Draw in the middle of the space between lines
                    val iconY = (lineBottom + nextLineTop) / 2f

                    // Calculate the available space for the icon
                    val availableSpace = (nextLineTop - lineBottom) / 2f
                    val iconSize = availableSpace.coerceAtMost(20f) // Cap the icon size

                    when (type) {
                        IconType.STAR -> {
                            val radius = iconSize
                            for (i in 0 until 5) {
                                val angle = Math.PI / 2 + i * 2 * Math.PI / 5
                                val nextAngle = angle + Math.PI / 5

                                val outerX1 = wordCenterX + radius * cos(angle).toFloat()
                                val outerY1 = iconY - radius * sin(angle).toFloat()

                                val innerX = wordCenterX + radius * 0.4f * cos(angle + Math.PI / 5).toFloat()
                                val innerY = iconY - radius * 0.4f * sin(angle + Math.PI / 5).toFloat()

                                val outerX2 = wordCenterX + radius * cos(nextAngle).toFloat()
                                val outerY2 = iconY - radius * sin(nextAngle).toFloat()

                                canvas.drawLine(outerX1, outerY1, innerX, innerY, drawPaint)
                                canvas.drawLine(innerX, innerY, outerX2, outerY2, drawPaint)
                            }
                        }

                        IconType.CIRCLE -> {
                            canvas.drawCircle(wordCenterX, iconY, iconSize, drawPaint)
                        }

                        IconType.ARROW_DOWN -> {
                            // Draw vertical line
                            canvas.drawLine(wordCenterX, iconY - iconSize, wordCenterX, iconY + iconSize, drawPaint)
                            // Draw arrow head
                            canvas.drawLine(wordCenterX - iconSize / 2, iconY + iconSize / 2, wordCenterX, iconY + iconSize, drawPaint)
                            canvas.drawLine(wordCenterX + iconSize / 2, iconY + iconSize / 2, wordCenterX, iconY + iconSize, drawPaint)
                        }

                        IconType.DIAMOND -> {
                            val path = Path()
                            path.moveTo(wordCenterX, iconY - iconSize)  // Top
                            path.lineTo(wordCenterX + iconSize, iconY)  // Right
                            path.lineTo(wordCenterX, iconY + iconSize)  // Bottom
                            path.lineTo(wordCenterX - iconSize, iconY)  // Left
                            path.close()
                            canvas.drawPath(path, drawPaint)
                        }
                    }
                }
            }
        }
    }
}