package com.bbou.brats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import com.bbou.brats.Annotation.Icon.IconType

class AnnotatedTextViewTest @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

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
        drawAnnotationSpace(canvas)

        // Draw text
        super.onDraw(canvas)

        drawLineSpace(canvas)

        // Draw all annotations
        for (annotation in annotations) {
            annotation.draw(canvas, this)
        }
    }

    private fun drawAnnotationSpace(canvas: Canvas) {
        val paintRect = Paint().apply {
            color = "#FFFFC0".toColorInt()
            strokeWidth = 2f
            style = Paint.Style.FILL
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
            val y1 = base + descent
            val y2 = y1 + lineSpacingExtra //bottom

            // Print the positions.
            println("Line $line: Top = $top, Bottom = $bottom, Base = $base, Height = $height, Ascent= $ascent/$lineAscent, Descent = $descent/$lineDescent, Leading = $leading")

            // Paint rect
            val left: Float = layout.getLineLeft(line) + paddingLeft
            val right: Float = layout.getLineRight(line) + paddingLeft
            val rect = RectF(left, y1, right, y2)
            canvas.drawRect(rect, paintRect)
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

            // Paint lines.
            val x1 = 0f
            val x2 = width.toFloat() / 2f
            val x3 = width.toFloat()

            canvas.drawLine(x1, base, x2, base, paintBase)
            canvas.drawLine(x1, base + ascent, x2, base + ascent, paintAsDesCent)
            canvas.drawLine(x1, base + descent, x2, base + descent, paintAsDesCent)

            canvas.drawLine(x2, top, x3, top, paintTop)
            canvas.drawLine(x2, bottom, x3, bottom, paintBottom)
        }
    }
}

/**
 * Get the screen position of a word
 */
fun TextView.getWordPosition(wordStart: Int, wordEnd: Int): Rect? {
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
