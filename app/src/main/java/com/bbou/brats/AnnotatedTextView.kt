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

class AnnotatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
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

    private val arrowPath = Path()
    private val annotations = mutableListOf<Annotation>()

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

        // Draw all annotations
        for (annotation in annotations) {
            when (annotation) {
                is Annotation.Arrow -> drawBetweenLinesArrow(canvas, annotation)
                is Annotation.Icon -> drawBetweenLinesIcon(canvas, annotation)
            }
        }
    }

    private fun drawBetweenLinesArrow(canvas: Canvas, arrow: Annotation.Arrow) {
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
                val fromLineBottom = layout.getLineBottom(fromLine).toFloat() + paddingTop
                val toLineTop = layout.getLineTop(toLine).toFloat() + paddingTop

                // Calculate positions in the space between lines
                val spaceBetweenLines = toLineTop - fromLineBottom

                // Draw arrow in the space between lines
                if (fromLine < toLine) {
                    // Downward arrow
                    canvas.drawLine(fromWordX, fromLineBottom, toWordX, toLineTop, paint)

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
                    canvas.drawLine(fromWordX, fromLineBottom, toWordX, toLineTop, paint)

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

                // Get bottom of current line and top of next line
                val lineBottom = layout.getLineBottom(line).toFloat() + paddingTop
                val nextLineTop = layout.getLineTop(line + 1).toFloat() + paddingTop

                // Draw in the middle of the space between lines
                val iconY = (lineBottom + nextLineTop) / 2f

                when (icon.type) {
                    IconType.STAR -> {
                        val radius = (nextLineTop - lineBottom) / 3f
                        for (i in 0 until 5) {
                            val angle = Math.PI / 2 + i * 2 * Math.PI / 5
                            val nextAngle = angle + Math.PI / 5

                            val outerX1 = wordCenterX + radius * Math.cos(angle).toFloat()
                            val outerY1 = iconY - radius * Math.sin(angle).toFloat()

                            val innerX = wordCenterX + radius * 0.4f * Math.cos(angle + Math.PI / 5).toFloat()
                            val innerY = iconY - radius * 0.4f * Math.sin(angle + Math.PI / 5).toFloat()

                            val outerX2 = wordCenterX + radius * Math.cos(nextAngle).toFloat()
                            val outerY2 = iconY - radius * Math.sin(nextAngle).toFloat()

                            canvas.drawLine(outerX1, outerY1, innerX, innerY, paint)
                            canvas.drawLine(innerX, innerY, outerX2, outerY2, paint)
                        }
                    }

                    IconType.CIRCLE -> {
                        val radius = (nextLineTop - lineBottom) / 3f
                        canvas.drawCircle(wordCenterX, iconY, radius, paint)
                    }

                    IconType.ARROW_DOWN -> {
                        val arrowSize = (nextLineTop - lineBottom) / 3f
                        canvas.drawLine(wordCenterX, iconY - arrowSize, wordCenterX, iconY + arrowSize, paint)
                        canvas.drawLine(wordCenterX - arrowSize, iconY, wordCenterX, iconY + arrowSize, paint)
                        canvas.drawLine(wordCenterX + arrowSize, iconY, wordCenterX, iconY + arrowSize, paint)
                    }
                }
            }
        }
    }

    /**
     * Get the screen position of a word
     */
    private fun getWordPosition(wordStart: Int, wordEnd: Int): Rect? {
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
        paint.color = color
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
    STAR, CIRCLE, ARROW_DOWN
}
