package com.bbou.brats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import com.bbou.brats.Annotation.BoxAnnotation
import com.bbou.brats.Annotation.EdgeAnnotation
import grammarscope.Segment
import grammarscope.DependencyPainter
import grammarscope.DependencyAnnotator
import grammarscope.document.SampleDocument
import kotlin.math.max

class AnnotatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    val document = SampleDocument()

    internal lateinit var annotations: Map<AnnotationType, Collection<Annotation>>

    fun prepare() {
        val text = document.text
        this.text = text

        // Highlight root
        //val rootPos = findWordPosition(text, "gave")
        //if (rootPos != null) {
        //    textView.highlightWord(rootPos.first, rootPos.second, "#FFEB3B".toColorInt())
        //}
    }

    fun annotate() {
        // renderer
        val (annotations, _) = DependencyAnnotator(this).annotate(document)!!
        this.annotations = annotations
    }

    // override fun requestLayout() {
    //     super.requestLayout()
    // }

    // override fun invalidate() {
    //     super.invalidate()
    // }

    override fun onDraw(canvas: Canvas) {

        // Draw text
        super.onDraw(canvas)

        if (text != "") {
            dumpLineText()
            drawAnnotationSpace(canvas)
            drawLineSpace(canvas)

            annotate()

            // Draw all annotations
            // for (annotation in annotations) {
            //     annotation.draw(canvas, this)
            // }

            val boxAnnotations = annotations[AnnotationType.BOX]!!.map { it as BoxAnnotation }
            DependencyPainter.paintBoxes(canvas, boxAnnotations)

            val edgeAnnotations = annotations[AnnotationType.EDGE]!!.map { it as EdgeAnnotation }
            DependencyPainter.paintEdges(canvas, edgeAnnotations, padWidth = width.toFloat(), renderAsCurves = true)
        }
    }

    private fun dumpLineText() {
        val lineCount = layout.lineCount
        for (line in 0 until lineCount) {
            val lineStart: Int = layout.getLineStart(line)
            val lineEnd: Int = layout.getLineEnd(line)
            val lineText: CharSequence = text.subSequence(lineStart, lineEnd)
            println("[$line]- $lineText")
        }
    }

    private fun drawAnnotationSpace(canvas: Canvas) {
        val paintRect = Paint().apply {
            color = "#FFffffb0".toColorInt()
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
            val top = layout.getLineTop(line) + paddingTop.toFloat()
            val bottom = layout.getLineBottom(line) + paddingTop.toFloat()
            val base = layout.getLineBaseline(line) + paddingTop.toFloat()
            val lineAscent = layout.getLineAscent(line)
            val lineDescent = layout.getLineDescent(line)

            // Print the positions.
            println("Line $line: Top = $top, Bottom = $bottom, Base = $base, Height = $height, Ascent= $ascent/$lineAscent, Descent = $descent/$lineDescent, Leading = $leading")

            // Paint lines.
            val x1 = 0f
            val x2 = width.toFloat() / 2f
            val x3 = width.toFloat()

            //// base
            //canvas.drawLine(x1, base, x2, base, paintBase)
            //// top text
            //canvas.drawLine(x1, base + ascent, x2, base + ascent, paintAsDesCent)
            //// bottom text
            //canvas.drawLine(x1, base + descent, x2, base + descent, paintAsDesCent)

            // top
            canvas.drawLine(x1, top, x2, top, paintTop)
            // bottom
            canvas.drawLine(x2, bottom, x3, bottom, paintBottom)
        }
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
}

/**
 * Helper method to find a word's position in text
 */
private fun findWordPosition(text: String, word: String): Pair<Int, Int>? {
    val index = text.indexOf(word)
    if (index != -1) {
        return Pair(index, index + word.length)
    }
    return null
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

/**
 * Get rectangle for segment in text
 *
 * @param segment       target segment
 * @return rectangle
 */
fun TextView.modelToView(segment: Segment): Rect {

    val fromRectangle = modelToView(segment.from)
    val toRectangle = modelToView(segment.to)
    val left = fromRectangle.left
    val top = fromRectangle.top
    val right = toRectangle.right
    val bottom = max(fromRectangle.bottom, toRectangle.bottom)
    return Rect(left, top, right, bottom)
}

/**
 * Get rectangle for segment in text
 *
 * @param segment       target segment
 * @return rectangle
 */
fun TextView.modelToViewF(segment: Segment): RectF {

    val fromRectangle = modelToViewF(segment.from)
    val toRectangle = modelToViewF(segment.to)
    val left = fromRectangle.left
    val top = fromRectangle.top
    val right = toRectangle.right
    val bottom = max(fromRectangle.bottom, toRectangle.bottom)
    return RectF(left, top, right, bottom)
}

fun TextView.modelToView(pos: Int): Rect {
    if (pos < 0 || pos > text.length) {
        return throw IllegalArgumentException("Invalid position: $pos")
    }
    val metrics: FontMetrics = paint.fontMetrics
    val layout: Layout = layout
    val line = layout.getLineForOffset(pos)
    val baseline = layout.getLineBaseline(line)
    val top = baseline + metrics.ascent + paddingTop //layout.getLineTop(line)
    val bottom = baseline + metrics.descent + paddingTop // layout.getLineBottom(line)
    val x = layout.getPrimaryHorizontal(pos)
    val width = if (pos < text.length) {
        layout.getPrimaryHorizontal(pos + 1) - x
    } else {
        // Handle the end of the text.
        if (text.isNotEmpty()) {
            // Get the previous character position.
            val previousPos = pos - 1
            val previousX = layout.getPrimaryHorizontal(previousPos)
            x - previousX
        } else {
            // Handle empty text.
            0F
        }
    }
    val left = x + paddingLeft
    val right = left + width
    return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}


fun TextView.modelToViewF(pos: Int): RectF {
    if (pos < 0 || pos > text.length) {
        return throw IllegalArgumentException("Invalid position: $pos")
    }
    val metrics: FontMetrics = paint.fontMetrics
    val layout: Layout = layout
    val line = layout.getLineForOffset(pos)
    val baseline = layout.getLineBaseline(line)
    val top = baseline + metrics.ascent + paddingTop //layout.getLineTop(line)
    val bottom = baseline + metrics.descent + paddingTop // layout.getLineBottom(line)
    val x = layout.getPrimaryHorizontal(pos)
    val width = if (pos < text.length) {
        layout.getPrimaryHorizontal(pos + 1) - x
    } else {
        // Handle the end of the text.
        if (text.isNotEmpty()) {
            // Get the previous character position.
            val previousPos = pos - 1
            val previousX = layout.getPrimaryHorizontal(previousPos)
            x - previousX
        } else {
            // Handle empty text.
            0F
        }
    }
    val left = x + paddingLeft
    val right = left + width
    return RectF(left, top, right, bottom)
}
