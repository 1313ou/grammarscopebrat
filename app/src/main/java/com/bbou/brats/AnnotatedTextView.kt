package com.bbou.brats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.cos
import kotlin.math.sin

class AnnotatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val annotations = mutableListOf<Annotation>()

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

    /**
     * Add an arrow between lines connecting two words
     */
    fun addArrow(
        fromWordStart: Int, fromWordEnd: Int,
        toWordStart: Int, toWordEnd: Int
    ) {
        annotations.add(
            Annotation.Arrow(
                fromWordStart, fromWordEnd,
                toWordStart, toWordEnd
            )
        )
        invalidate()
    }

    /**
     * Add an icon below a specific word (in the space between lines)
     */
    fun addIcon(wordStart: Int, wordEnd: Int, type: IconType) {
        annotations.add(
            Annotation.Icon(wordStart, wordEnd, type)
        )
        invalidate()
    }

    /**
     * Highlight a specific word
     */
    fun highlightWord(wordStart: Int, wordEnd: Int, color: Int = Color.YELLOW) {
        val spannable = if (text is SpannableString) {
            text as SpannableString
        } else {
            SpannableString(text)
        }
        spannable.setSpan(
            BackgroundColorSpan(color),
            wordStart,
            wordEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = spannable
    }

    /**
     * Clear all annotations
     */
    fun clearAnnotations() {
        annotations.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw text
        super.onDraw(canvas)

        drawLineSpace(canvas)

        // Draw all annotations
        for (annotation in annotations) {
            when (annotation) {
                is Annotation.Arrow -> drawBetweenLinesArrow(canvas, annotation)
                is Annotation.Icon -> drawBetweenLinesIcon(canvas, annotation)
            }
        }
    }

    private fun drawLineSpace(canvas: Canvas) {
        val paintTop = Paint().apply {
            color = Color.MAGENTA
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paintBottom = Paint().apply {
            color = Color.CYAN
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paintBase = Paint().apply {
            color = Color.BLUE
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paintAsDesCent = Paint().apply {
            color = Color.GREEN
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paint: Paint = this.paint
        val fontMetrics = paint.fontMetrics
        val ascent = fontMetrics.ascent
        val descent = fontMetrics.descent
        val leading = fontMetrics.leading
        val height = -ascent + descent + leading

        val lineCount = layout.lineCount
        for (line in 0 until lineCount) {
            // Get the top and the bottom of the line.
            val top = layout.getLineTop(line).toFloat() + paddingTop
            val bottom = layout.getLineBottom(line).toFloat() + paddingTop
            val base = layout.getLineBaseline(line).toFloat() + paddingTop
            val lineAscent = layout.getLineAscent(line).toFloat()
            val lineDescent = layout.getLineDescent(line).toFloat()

            // Print the positions.
            println("Line $line: Top = $top, Bottom = $bottom, Base = $base, Height = $height, Ascent= $ascent, Descent = $descent, Leading = $leading")

            // Add a line at the top and bottom.
            val x1 = 0f
            val x2 = 500f
            val x3 = 1000f
            canvas.drawLine(x1, base, x2, base, paintBase)
            canvas.drawLine(x1, base + ascent, x2, base + ascent, paintAsDesCent)
            canvas.drawLine(x1, base + descent, x2, base + descent, paintAsDesCent)

            canvas.drawLine(x2, top, x3, top, paintTop)
            canvas.drawLine(x2, bottom, x3, bottom, paintBottom)
        }
    }

    private fun drawBetweenLinesArrow(canvas: Canvas, arrow: Annotation.Arrow) {
        val arrowPath = Path()

        val paint: Paint = this.paint
        val fontMetrics = paint.fontMetrics
        val ascent = fontMetrics.ascent
        val descent = fontMetrics.descent
        val leading = fontMetrics.leading
        val height = -ascent + descent + leading

        val fromWordPos = getWordPosition(arrow.fromWordStart, arrow.fromWordEnd)
        val toWordPos = getWordPosition(arrow.toWordStart, arrow.toWordEnd)

        if (fromWordPos != null && toWordPos != null) {
            // Get lines for each word
            val fromLine = layout.getLineForOffset(arrow.fromWordStart)
            val toLine = layout.getLineForOffset(arrow.toWordStart)

            // Only draw if words are on different lines
            if (fromLine != toLine) {
                // Get middle of the word
                val fromWordX = (fromWordPos.left + fromWordPos.right) / 2f
                val toWordX = (toWordPos.left + toWordPos.right) / 2f

                // Get bottom of first line and top of second line
                val fromLineBottom = layout.getLineTop(fromLine).toFloat() + paddingTop + height
                val toLineTop = layout.getLineTop(toLine).toFloat() + paddingTop

                // Calculate positions in the space between lines
                val spaceBetweenLines = toLineTop - fromLineBottom

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

    private fun drawBetweenLinesIcon(canvas: Canvas, icon: Annotation.Icon) {
        val wordPos = getWordPosition(icon.wordStart, icon.wordEnd)

        if (wordPos != null) {
            // Get the line for the word
            val line = layout.getLineForOffset(icon.wordStart)

            // Only draw if there's another line below
            if (line < layout.lineCount - 1) {
                // Calculate center of the word
                val wordCenterX = (wordPos.left + wordPos.right) / 2f

                val paint: Paint = this.paint
                val fontMetrics = paint.fontMetrics
                val ascent = fontMetrics.ascent
                val descent = fontMetrics.descent
                val leading = fontMetrics.leading
                val height = -ascent + descent + leading

                // Get bottom of current line and top of next line
                val lineTop = layout.getLineTop(line).toFloat() + paddingTop
                val lineBottom = lineTop + height
                //val lineBottom = layout.getLineBottom(line).toFloat() + paddingTop
                //val lineHeight = lineBottom - lineTop
                val nextLineTop = layout.getLineTop(line + 1).toFloat() + paddingTop

                // Draw in the middle of the space between lines
                val iconY = (lineBottom + nextLineTop) / 2f

                // Calculate the available space for the icon
                val availableSpace = (nextLineTop - lineBottom) / 2f
                val iconSize = availableSpace.coerceAtMost(20f) // Cap the icon size

                when (icon.type) {
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

    /**
     * Get the screen position of a word
     */
    fun getWordPosition(wordStart: Int, wordEnd: Int): Rect? {
        if (wordStart < 0 || wordEnd > text.length || layout == null) {
            return null
        }

        val bounds = Rect()

        try {
            // Find the line that contains the word
            val line = layout.getLineForOffset(wordStart)

            // Get the bounds of the line
            layout.getLineBounds(line, bounds)

            // Get horizontal bounds
            val startX = layout.getPrimaryHorizontal(wordStart)
            val endX = layout.getPrimaryHorizontal(wordEnd)

            // Create the word bounds
            bounds.left = startX.toInt() + paddingLeft
            bounds.right = endX.toInt() + paddingLeft

            return bounds
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Set the paint color for annotations
     */
    fun setAnnotationColor(color: Int) {
        drawPaint.color = color
        fillPaint.color = color
        invalidate()
    }
}

// Sealed class for different types of between-lines annotations
sealed class Annotation {
    data class Arrow(
        val fromWordStart: Int, val fromWordEnd: Int,
        val toWordStart: Int, val toWordEnd: Int
    ) : Annotation()

    data class Icon(
        val wordStart: Int, val wordEnd: Int,
        val type: IconType
    ) : Annotation()
}

enum class IconType {
    STAR, CIRCLE, DIAMOND, ARROW_DOWN;
}
