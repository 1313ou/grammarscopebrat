package com.bbou.brats

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import com.bbou.brats.Utils.drawDot
import com.bbou.brats.Utils.drawTriangle
import kotlin.math.atan2

/**
 * Edge as used by renderer
 */
internal class Edge
    (
    /**
     * Edge x left coordinate
     */
    val x1: Float,
    /**
     * Edge x right coordinate
     */
    val x2: Float,
    /**
     * Edge vertical base position
     */
    val yBase: Float,
    /**
     * Edge x left anchor
     */
    val x1Anchor: Float,
    /**
     * Edge x right anchor
     */
    val x2Anchor: Float,
    /**
     * Edge vertical base position
     */
    val yAnchor: Float,
    /**
     * Edge height
     */
    val height: Float,
    /**
     * Edge tag (relation)
     */
    val tag: String,
    /**
     * Whether this label is vertical
     */
    val isVertical: Boolean,
    /**
     * Edge color
     */
    val color: Color,
    /**
     * Whether this edge is continued on next line
     */
    val isBackwards: Boolean,
    /**
     * Whether this edge is continued on previous line
     */
    val isLeftTerminal: Boolean,
    /**
     * Whether this edge is continued on next line
     */
    val isRightTerminal: Boolean,
    /**
     * Bottom past which this edge is not visible
     */
    val bottom: Int,
    /**
     * Whether this edge is visible
     */
    val isVisible: Boolean
) {

    /**
     * Tag start position
     */
    private val tagPosition: PointF

    /**
     * Tag space
     */
    val tagRectangle: RectF

    /**
     * Tag width
     */
    val tagWidth: Int

    /**
     * Constructor
     *
     * @param x1              x1
     * @param x2              x2
     * @param yBase           y
     * @param x1Anchor        anchor x1
     * @param x2Anchor        anchor x2
     * @param yAnchor         anchor y
     * @param height          height
     * @param tag             edge label/tag
     * @param isVertical      label/tag is vertical
     * @param color           color
     * @param isBackwards     direction
     * @param isLeftTerminal  is left terminal
     * @param isRightTerminal is right terminal
     * @param bottom          pad bottom
     * @param isVisible       is visible
     */
    init {
        if (isVertical) {

            // tag width
            val tagFontHeight = tagMetrics.descent - tagMetrics.ascent + tagMetrics.leading
            val tagFontDescent = tagMetrics.descent
            tagWidth = tagFontHeight.toInt()
            val paint = Paint()
            val tagWidth: Float = paint.measureText(tag)

            // tag position
            val xa1 = x1 + x1Anchor
            val xa2 = x2 + x2Anchor
            val labelLeft = xa1 + (xa2 - xa1 - tagFontHeight) / 2 + tagFontDescent
            val labelBase = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE
            tagPosition = PointF(labelLeft, labelBase)

            // tag rectangle
            val x = tagPosition.x - tagFontDescent - LABEL_INFLATE
            val y = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - LABEL_INFLATE
            val h = tagWidth + 2 * LABEL_INFLATE
            val w = tagFontHeight + 2 * LABEL_INFLATE
            tagRectangle = RectF(x, y, x + w, y + h)
        } else {

            // tag width
            val paint = Paint()
            tagWidth = paint.measureText(tag).toInt()

            // tag position
            val xa1 = x1 + x1Anchor
            val xa2 = x2 + x2Anchor
            val labelLeft = xa1 + (xa2 - xa1 - tagWidth) / 2
            val fontDescent: Float = tagMetrics.descent
            val labelBase: Float = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - fontDescent
            tagPosition = PointF(labelLeft, labelBase)

            // tag rectangle
            val tagFontHeight: Float = tagMetrics.descent - tagMetrics.ascent + tagMetrics.leading
            val x = tagPosition.x - LABEL_INFLATE
            val y = (yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - tagFontHeight - LABEL_INFLATE)
            val w = tagWidth + 2 * LABEL_INFLATE
            val h = tagFontHeight + 2 * LABEL_INFLATE
            tagRectangle = RectF(x, y, x + w, y + h)
        }
    }

    // P O S I T I O N   A N D   L A Y O U T

    /**
     * Rectangle for edge space including tag
     */
    val rectangle: RectF
        get() = RectF(x1, yBase - height, x2 - x1, height)

    // D R A W

    /**
     * Draw label
     *
     * @param g graphics context
     */
    fun drawLabel(g: Canvas) {
        // tag background
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = color
        }
        g.drawRect(tagRectangle.left, tagRectangle.top, tagRectangle.width(), tagRectangle.height(), paint)

        // tag text
        val textPaint = Paint().apply {
            typeface = tagFont
            color = labelColor
        }
        if (isVertical) {
            val saveCount = g.save()
            try {
                g.rotate(180f, tagPosition.x.toFloat(), tagPosition.y.toFloat())
                g.drawText(tag, tagPosition.x.toFloat(), tagPosition.y.toFloat(), textPaint)
            } finally {
                g.restoreToCount(saveCount)
            }
        } else g.drawText(tag, tagPosition.x.toFloat(), tagPosition.y.toFloat(), textPaint)
    }

    /**
     * Draw edge
     *
     * @param g2      graphics context
     * @param asCurve whether to draw edge as curve (or straight arrow)
     */
    fun draw(g2: Canvas, asCurve: Boolean) {
        if (asCurve) {
            drawCurvePath(g2)
        } else {
            drawStraightArrow(g2)
        }
    }

    /**
     * Draw edge as curve
     *
     * @param g2 graphics context
     */
    private fun drawCurvePath(g2: Canvas) {
        // edge
        val paint = Paint().apply {
            color = edgeColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = EDGE_STROKE
        }
        val xTextFrom = tagRectangle.left
        val xTextTo = tagRectangle.left + tagRectangle.width()
        val yText: Float = if (isVertical) tagPosition.y else tagRectangle.top + tagRectangle.height() / 2
        val xFrom = x1 + x1Anchor
        val xTo = x2 + x2Anchor
        val yFrom = if (isLeftTerminal) yAnchor else yText
        val yTo = if (isRightTerminal) yAnchor else yText
        val flatLeft = !isLeftTerminal
        val flatRight = !isRightTerminal

        // curve
        val shape = CurvePath(xFrom, xTo, xTextFrom, xTextTo, yFrom, yTo, yText, flatLeft, flatRight)
        g2.drawPath(shape, paint)

        // control
        // final int cox1 = (int) shape.getXCornerRight();
        // final int cox2 = (int) shape.getXCornerLeft();
        val ctx1 = shape.xControlRight.toInt()
        val ctx2 = shape.xControlLeft.toInt()
        val cty = shape.yBase.toInt()

        // arrow tip at corner
        if (isRightTerminal && !isBackwards) {
            // int xc = (int) shape.getXCornerRight();
            // int yc = yText;
            // Utils.drawTriangle(g2, ARROW_COLOR, xc, yc, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, false);
            // double theta = Math.atan2((double)y - yc, (double)x - xc); // corner
            val theta = atan2(yTo.toDouble() - cty, xTo.toDouble() - ctx1)
            val paint = Paint().apply {
                color = arrowTipColor
            }
            g2.drawTriangle(xTo, yTo, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, false, theta.toFloat(), paint)

        } else if (isLeftTerminal && isBackwards) {
            // int xc = (int) shape.getXCornerLeft();
            // int yc = yText;
            // Utils.drawTriangle(g2, ARROW_COLOR, xc, yc, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, true);
            // double theta = Math.atan2((double)yc - y, (double)xc - x); // corner
            val theta = atan2(cty.toDouble() - yFrom, ctx2.toDouble() - xFrom)
            val paint = Paint().apply {
                color = arrowTipColor
            }
            g2.drawTriangle(xFrom, yFrom, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, true, theta.toFloat(), paint)
        }

        // Utils.drawDot(g2, Color.RED, cox1, cty, 1);
        // Utils.drawDot(g2, Color.RED, cox2, cty, 1);
        // Utils.drawDiamond(g2, Color.BLUE, ctx1, cty, 1);
        // Utils.drawDiamond(g2, Color.BLUE, ctx2, cty, 1);
    }

    /**
     * Draw edge as straight arrow
     *
     * @param g2 graphics context
     */
    private fun drawStraightArrow(g2: Canvas) {
        // edge
        val paint = Paint().apply {
            color = edgeColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = EDGE_STROKE
        }
        g2.drawLine(x1.toFloat(), yBase.toFloat(), (x2 - 1).toFloat(), yBase.toFloat(), paint)

        // arrow tip
        val drawArrowEnd = if (isBackwards) isLeftTerminal else isRightTerminal
        if (drawArrowEnd) {
            val xTip = if (isBackwards) x1 else x2
            val paint = Paint().apply {
                color = arrowTipColor
            }
            g2.drawTriangle(xTip, yBase, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, isBackwards, paint)
        }

        // arrow start
        val drawArrowStart = if (isBackwards) isRightTerminal else isLeftTerminal
        if (drawArrowStart) {
            val xTip = if (isBackwards) x2 - ARROW_START_DIAMETER else x1 + ARROW_START_DIAMETER
            val paint = Paint().apply {
                color = arrowStartColor
            }
            g2.drawDot(xTip, yBase, ARROW_START_DIAMETER, paint)
        }
    }

    override fun toString(): String {
        return "$tag y=$yBase h=$height x1=$x1 x2=$x2 ${if (isBackwards) "backward" else "forward"} ${if (isLeftTerminal) "|-" else "--"}${if (isRightTerminal) "-|" else "--"}"
    }

    companion object {
        lateinit var tagFont: Typeface
        lateinit var tagMetrics: FontMetrics

        var labelColor = 0x000000
        var edgeColor = 0x000000
        var arrowTipColor = 0x000000
        var arrowStartColor = 0x000000

        val ARROW_TIP_WIDTH = 5F
        val ARROW_TIP_HEIGHT = 5F
        val ARROW_START_DIAMETER = 5F
        val EDGE_STROKE = 5F

        val LABEL_BOTTOM_INSET = 0F
        val LABEL_INFLATE = 0F
    }
}

