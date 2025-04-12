package grammarscope

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.RectF
import com.bbou.brats.Annotation.BoxAnnotation
import com.bbou.brats.Annotation.EdgeAnnotation

object SemanticGraphPainter {

    // P A I N T

    fun paint(g: Canvas, edgeAnnotations: Collection<EdgeAnnotation>, boxAnnotations: Collection<BoxAnnotation>, padWidth: Int, renderAsCurves: Boolean) {

        // draw boxes
        for (boxAnnotation in boxAnnotations) {
            drawBox(g, boxAnnotation.box)
        }

        // draw edges
        val overflowPaint = Paint().apply {
            color = DEFAULT_OVERFLOW_COLOR
            style = Paint.Style.STROKE
            strokeWidth = OVERFLOW_STROKE_WIDTH
            pathEffect = OVERFLOW_STROKE
        }

        val handlePaint = Paint().apply {
            color = DEFAULT_OVERFLOW_COLOR
            style = Paint.Style.STROKE
            strokeWidth = OVERFLOW_STROKE_WIDTH
            pathEffect = HANDLE_STROKE
        }

        for (edgeAnnotation in edgeAnnotations) {
            val edge = edgeAnnotation.edge
            if (edge.isVisible) {
                edge.draw(g, renderAsCurves)
            } else {

                // can't fit in
                var y: Float = edge.bottom - 1F

                // line
                g.drawLine(0f, y.toFloat(), padWidth.toFloat(), y.toFloat(), overflowPaint)

                // handle
                y -= OVERFLOW_Y_OFFSET
                val x = padWidth - OVERFLOW_X_OFFSET - OVERFLOW_WIDTH
                val path = Path().apply {
                    moveTo(x - OVERFLOW_WIDTH, y - OVERFLOW_HEIGHT)
                    lineTo(x, y)
                    lineTo(x + OVERFLOW_WIDTH, y - OVERFLOW_HEIGHT)
                    close()
                }
                g.drawPath(path, handlePaint)
            }
        }

        // draw labels
        for (edgeAnnotation in edgeAnnotations) {
            if (edgeAnnotation.edge.isVisible) {
                // TODO edgeAnnotation.edge.drawLabel(g)
            }
        }
    }

    /**
     * Draw box
     *
     * @param g   graphics context
     * @param box box for typed dependency node
     */
    private fun drawBox(g: Canvas, box: RectF) {
        val boxL = box.left.toInt()
        val boxT = box.top.toInt()
        val boxH = box.height().toInt()
        val boxW = box.width().toInt()
        val boxR = boxL + boxW
        val boxB = boxT + boxH
        val boxM = boxL + boxW / 2

        // draw axis
        val axisPaint = Paint().apply {
            color = axisColor
            pathEffect = AXIS_STROKE
        }
        g.drawLine(boxM.toFloat(), boxT.toFloat(), boxM.toFloat(), boxB.toFloat(), axisPaint)

        // draw span lines
        val spanPaint = Paint().apply {
            color = spanColor
            pathEffect = SPAN_STROKE
        }
        val shape: Path = CurlyBracePath(boxL, boxR, boxM, boxT, false)
        g.drawPath(shape, spanPaint)

        // g2.drawLine(boxL, boxT, boxR, boxT); // pad topOffset
        // g2.drawLine(boxL, boxB, boxR, boxB); // pad bottom
    }

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

    val DEFAULT_SPAN_COLOR: Int = Color.GRAY
    val DEFAULT_AXIS_COLOR: Int = Color.WHITE
    val DEFAULT_OVERFLOW_COLOR: Int = Color.GRAY

    val OVERFLOW_X_OFFSET = 0F
    val OVERFLOW_Y_OFFSET = 0F
    val OVERFLOW_WIDTH = 0F
    val OVERFLOW_HEIGHT = 0F

    // STROKES
    val SOLID: PathEffect? = null
    val DOTTED: PathEffect = DashPathEffect(floatArrayOf(1.0f, 1.0f), 0f)
    val DASHED: PathEffect = DashPathEffect(floatArrayOf(5.0f, 5.0f), 0f)

    val EDGE_STROKE: PathEffect? = SOLID
    val OVERFLOW_STROKE_WIDTH = 1F
    val OVERFLOW_STROKE: PathEffect = DashPathEffect(floatArrayOf(20f, 10f, 5f, 10f), 0f)
    val HANDLE_STROKE: PathEffect? = SOLID
    val AXIS_STROKE: PathEffect? = SOLID
    val SPAN_STROKE: PathEffect? = SOLID
}