package grammarscope

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.RectF
import androidx.core.graphics.toColorInt
import com.bbou.brats.Annotation.BoxAnnotation
import com.bbou.brats.Annotation.EdgeAnnotation

object DependencyPainter {

    fun paintBoxes(canvas: Canvas, boxAnnotations: Collection<BoxAnnotation>) {
        for (boxAnnotation in boxAnnotations) {
            boxAnnotation.draw(canvas)
        }
    }

    fun paintEdges(canvas: Canvas, edgeAnnotations: Collection<EdgeAnnotation>, padWidth: Float) {

        // draw labels
        for (edgeAnnotation in edgeAnnotations) {
            drawEdge(canvas, edgeAnnotation, padWidth)
        }

        // draw labels
        //for (edgeAnnotation in edgeAnnotations) {
        //    if (edgeAnnotation.edge.isVisible) {
        //        edgeAnnotation.edge.drawTag(canvas)
        //    }
        //}
    }

    fun drawEdge(canvas: Canvas, edgeAnnotation: EdgeAnnotation, padWidth: Float) {
        val edge = edgeAnnotation.edge
        println(edge)
        if (edge.isVisible) {
            edge.draw(canvas, renderAsCurves)
            edge.drawTag(canvas)
        } else {

            // can't fit in / overflow
            var y: Float = edge.bottom - 1F

            // line
            canvas.drawLine(0f, y, padWidth, y, overflowPaint)

            // handle
            y -= OVERFLOW_Y_OFFSET
            val x = padWidth - OVERFLOW_X_OFFSET - OVERFLOW_WIDTH
            val path = Path().apply {
                moveTo(x - OVERFLOW_WIDTH, y - OVERFLOW_HEIGHT)
                lineTo(x, y)
                lineTo(x + OVERFLOW_WIDTH, y - OVERFLOW_HEIGHT)
                close()
            }
            canvas.drawPath(path, overflowHandlePaint)
        }
    }

    /**
     * Draw box
     *
     * @param g   graphics context
     * @param box box for typed dependency node
     */
    private fun drawBox(g: Canvas, box: RectF) {
        val boxL = box.left
        val boxT = box.top
        val boxH = box.height()
        val boxW = box.width()
        val boxR = boxL + boxW
        val boxB = boxT + boxH
        val boxM = boxL + boxW / 2F

        // draw axis
        val axisPaint = Paint().apply {
            color = axisColor
            pathEffect = AXIS_STROKE
        }
        g.drawLine(boxM, boxT, boxM, boxB, axisPaint)

        // draw span lines
        val spanPaint = Paint().apply {
            color = spanColor
            pathEffect = SPAN_STROKE
        }
        val shape: Path = CurlyBracePath(boxL, boxR, boxM, boxT, false)
        g.drawPath(shape, spanPaint)

        // g.drawLine(boxL, boxT, boxR, boxT) // pad topOffset
        // g.drawLine(boxL, boxB, boxR, boxB) // pad bottom
    }

    var renderAsCurves = true

    /**
     * Back color
     */
    var backColor: Int = "#80C6DBEE".toColorInt()

    var spanColor: Int = 0
    var axisColor: Int = 0

    /**
     * Set span color
     *
     * @param color color
     */
    fun setSpanColor(color: Int?) {
        spanColor = color ?: DEFAULT_SPAN_COLOR
    }

    /**
     * Set axis color
     *
     * @param color color
     */
    fun setAxisColor(color: Int?) {
        axisColor = color ?: DEFAULT_AXIS_COLOR
    }

    const val DEFAULT_SPAN_COLOR: Int = Color.GRAY
    const val DEFAULT_AXIS_COLOR: Int = Color.WHITE
    const val DEFAULT_OVERFLOW_COLOR: Int = Color.GRAY

    const val OVERFLOW_X_OFFSET = 50F
    const val OVERFLOW_Y_OFFSET = 10F
    const val OVERFLOW_WIDTH = 30F
    const val OVERFLOW_HEIGHT = 30F

    // STROKES
    val SOLID: PathEffect? = null
    val DOTTED: PathEffect = DashPathEffect(floatArrayOf(1.0f, 1.0f), 0f)
    val DASHED: PathEffect = DashPathEffect(floatArrayOf(5.0f, 5.0f), 0f)

    val EDGE_STROKE: PathEffect? = SOLID

    const val OVERFLOW_STROKE_WIDTH = 8F
    val OVERFLOW_STROKE: PathEffect = DashPathEffect(floatArrayOf(20f, 5f, 5f, 5f), 0f)
    val OVERFLOW_HANDLE_STROKE: PathEffect? = SOLID

    val AXIS_STROKE: PathEffect? = SOLID
    val SPAN_STROKE: PathEffect? = SOLID

    val overflowPaint = Paint().apply {
        color = DEFAULT_OVERFLOW_COLOR
        style = Paint.Style.STROKE
        strokeWidth = OVERFLOW_STROKE_WIDTH
        pathEffect = OVERFLOW_STROKE
    }

    val overflowHandlePaint = Paint().apply {
        color = DEFAULT_OVERFLOW_COLOR
        style = Paint.Style.STROKE
        strokeWidth = OVERFLOW_STROKE_WIDTH
        pathEffect = OVERFLOW_HANDLE_STROKE
    }
}