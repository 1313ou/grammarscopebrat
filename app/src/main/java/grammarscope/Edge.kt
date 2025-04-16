package grammarscope

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import grammarscope.DependencyAnnotator.Companion.height
import grammarscope.Utils.drawDot
import grammarscope.Utils.drawTriangle
import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.min
import androidx.core.graphics.withSave

/**
 * Edge as used by renderer
 *
 * @property x1              x1
 * @property x2              x2
 * @property yBase           y
 * @property x1Anchor        anchor x1
 * @property x2Anchor        anchor x2
 * @property yAnchor         anchor y
 * @property height          height
 * @property tag             edge label/tag
 * @property isVertical      label/tag is vertical
 * @property color           color
 * @property isBackwards     direction
 * @property isLeftTerminal  is left terminal
 * @property isRightTerminal is right terminal
 * @property isVisible       is visible
 * @property bottom          pad bottom
 */
data class Edge
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
     * Bottom past which this edge is not visible
     */
    val bottom: Float,
    /**
     * Edge tag (relation)
     */
    val tag: String,
    /**
     * Tag start position
     */
    val tagPosition: PointF,
    /**
     * Tag width
     */
    val tagWidth: Float,
    /**
     * Tag space
     */
    val tagRectangle: RectF,
    /**
     * Whether this label is vertical
     */
    val isVertical: Boolean,
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
     * Whether this edge is visible
     */
    val isVisible: Boolean,
    /**
     * Edge color
     */
    val edgeColor: Int?,
) {
    // P O S I T I O N   A N D   L A Y O U T

    /**
     * Rectangle for edge space including tag
     */
    val rectangle: RectF
        get() = RectF(x1, yBase - height, x2 - x1, height)

    // D R A W

    /**
     * Draw edge
     *
     * @param canvas      graphics context
     * @param asCurve whether to draw edge as curve (or straight arrow)
     */
    fun draw(canvas: Canvas, asCurve: Boolean) {
        if (asCurve) {
            drawCurvePath(canvas)
        } else {
            drawStraightArrow(canvas)
        }
    }

    /**
     * Draw edge as curve
     *
     * @param canvas canvas
     */
    private fun drawCurvePath(canvas: Canvas) {
        // edge
        val paint = Paint().apply {
            color = edgeColor ?: unspecifiedEdgeColor
            style = Paint.Style.STROKE
            strokeWidth = EDGE_STROKE
            isAntiAlias = true
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
        canvas.drawPath(shape, paint)

        // control
        // val cox1 = shape.xCornerRight
        // val cox2 = shape.xCornerLeft
        val ctx1 = shape.xControlRight
        val ctx2 = shape.xControlLeft
        val cty = shape.yBase

        // arrow tip at corner
        if (isRightTerminal && !isBackwards) {
            // val xc = (int) shape.getXCornerRight()
            // val yc = yText
            // g.drawTriangle(ARROW_COLOR, xc, yc, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, false)
            // double theta = toDegrees(atan2((double)y - yc, (double)x - xc)) // corner
            val theta = toDegrees(atan2(yTo.toDouble() - cty, xTo.toDouble() - ctx1))
            val paint = Paint().apply {
                color = arrowTipColor
            }
            canvas.drawTriangle(xTo, yTo, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, reverse = false, rotation = theta.toFloat(), paint)

        } else if (isLeftTerminal && isBackwards) {
            // val xc = shape.xCornerLeft
            // val yc = yText
            // canvas.drawTriangle(ARROW_COLOR, xc, yc, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, true)
            // val theta = toDegrees(atan2((double)yc - y, (double)xc - x)) // corner
            val theta = toDegrees(atan2(cty.toDouble() - yFrom, ctx2.toDouble() - xFrom))
            val paint = Paint().apply {
                color = arrowTipColor
            }
            canvas.drawTriangle(xFrom, yFrom, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, reverse = true, rotation = theta.toFloat(), paint)
        }

        // g.drawDot(Color.RED, cox1, cty, 1)
        // g.drawDot(Color.RED, cox2, cty, 1)
        // g.drawDiamond(Color.BLUE, ctx1, cty, 1)
        // g.drawDiamond(Color.BLUE, ctx2, cty, 1)
    }

    /**
     * Draw edge as straight arrow
     *
     * @param canvas canvas
     */
    private fun drawStraightArrow(canvas: Canvas) {
        // edge
        val paint = Paint().apply {
            color = edgeColor ?: unspecifiedEdgeColor
            style = Paint.Style.STROKE
            strokeWidth = 2F
        }
        canvas.drawLine(x1, yBase, x2 - 1F, yBase, paint)

        // arrow tip
        val drawArrowEnd = if (isBackwards) isLeftTerminal else isRightTerminal
        if (drawArrowEnd) {
            val xTip = if (isBackwards) x1 else x2
            val paint = Paint().apply {
                color = arrowTipColor
            }
            canvas.drawTriangle(xTip, yBase, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, isBackwards, paint)
        }

        // arrow start
        val drawArrowStart = if (isBackwards) isRightTerminal else isLeftTerminal
        if (drawArrowStart) {
            val xTip = if (isBackwards) x2 - ARROW_START_DIAMETER else x1 + ARROW_START_DIAMETER
            val paint = Paint().apply {
                color = arrowStartColor
            }
            canvas.drawDot(xTip, yBase, ARROW_START_DIAMETER, paint)
        }
    }

    /**
     * Draw tag
     *
     * @param canvas canvas
     */
    fun drawTag(canvas: Canvas) {
        // tag background
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = tagBackColor
        }
        canvas.drawRect(tagRectangle.left, tagRectangle.top, tagRectangle.right, tagRectangle.bottom, paint)

        // tag text
        val textPaint = Paint().apply {
            typeface = tagTypeFace
            color = tagColor
            textSize = tagTextSize
            isAntiAlias = true
        }
        if (isVertical) {
            canvas.withSave {
                rotate(180f, tagPosition.x.toFloat(), tagPosition.y.toFloat())
                drawText(tag, tagPosition.x.toFloat(), tagPosition.y.toFloat(), textPaint)
            }
        } else canvas.drawText(tag, tagPosition.x.toFloat(), tagPosition.y.toFloat(), textPaint)
    }

    override fun toString(): String {
        return "'$tag' _${yBase.toInt()} ↕${height.toInt()} ${x1.toInt()}${if (isLeftTerminal) "|" else ""}${if (isBackwards) "🡄" else "🡆"}${if (isRightTerminal) "|" else ""}${x2.toInt()}" //← → ⇽ ⇾ ⏐ ▴ ▾ ▶ ◀ ➔ ➽ ⟵ ⟶ ⥼ ⥽ ⬅ ⮕ 🡄🡅 🡆🡇🠈🠊🠉🠋 🢀🢂🡪🡲🡺🢀↕
    }

    companion object {

        const val ARROW_TIP_WIDTH = 15F
        const val ARROW_TIP_HEIGHT = 15F
        const val ARROW_START_DIAMETER = 10F

        const val EDGE_STROKE = 3F

        val DEFAULT_LABEL_TYPEFACE: Typeface = Typeface.SANS_SERIF
        const val DEFAULT_TAG_TEXT_SIZE = 40F
        const val LABEL_BOTTOM_INSET = 10F
        const val LABEL_INFLATE = 1F

        const val DEFAULT_EDGE_COLOR: Int = Color.DKGRAY
        const val DEFAULT_ARROW_TIP_COLOR: Int = Color.DKGRAY
        const val DEFAULT_ARROW_START_COLOR: Int = Color.GRAY
        const val DEFAULT_TAG_COLOR: Int = Color.DKGRAY
        const val DEFAULT_TAG_BACKCOLOR: Int = Color.WHITE

        var unspecifiedEdgeColor = DEFAULT_EDGE_COLOR
        var arrowTipColor = DEFAULT_ARROW_TIP_COLOR
        var arrowStartColor = DEFAULT_ARROW_START_COLOR
        var tagTypeFace = DEFAULT_LABEL_TYPEFACE
        var tagTextSize = DEFAULT_TAG_TEXT_SIZE
        var tagColor = DEFAULT_TAG_COLOR
        var tagBackColor = DEFAULT_TAG_BACKCOLOR

        /**
         * Set edge color
         *
         * @param color color
         */
        fun setEdgeColor(color: Int?) {
            unspecifiedEdgeColor = color ?: DEFAULT_EDGE_COLOR
            arrowTipColor = color ?: DEFAULT_ARROW_TIP_COLOR
            arrowStartColor = color ?: DEFAULT_ARROW_START_COLOR
        }

        /**
         * Set label color
         *
         * @param color color
         */
        fun setLabelColor(color: Int?) {
            tagColor = color ?: DEFAULT_TAG_COLOR
        }

        /**
         * Compute tag
         *
         * @param tag edge tag
         * @param isVertical
         * @param x1 from
         * @param x1Anchor from anchor
         * @param x2 to
         * @param x2Anchor to anchor
         * @param yBase edge base
         * @param tagPaint paint
         * @return tag position, width, rectangle
         */
        fun computeTag(tag: String, isVertical: Boolean, x1: Float, x1Anchor: Float, x2: Float, x2Anchor: Float, yBase: Float, tagPaint: Paint): Triple<PointF, Float, RectF> {
            if (isVertical) {

                // tag width
                val tagFontHeight = tagPaint.fontMetrics.height()
                val tagFontDescent = tagPaint.fontMetrics.descent
                val tagWidth = tagFontHeight

                // tag position
                val xa1 = x1 + x1Anchor
                val xa2 = x2 + x2Anchor
                val labelLeft = xa1 + (xa2 - xa1 - tagFontHeight) / 2 + tagFontDescent
                val labelBase = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE
                val tagPosition = PointF(labelLeft, labelBase)

                // tag rectangle
                val x = tagPosition.x - tagFontDescent - LABEL_INFLATE
                val y = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - LABEL_INFLATE
                val h = tagPaint.measureText(tag) + 2 * LABEL_INFLATE
                val w = tagFontHeight + 2 * LABEL_INFLATE
                val tagRectangle = RectF(x, y, x + w, y + h)

                return Triple(tagPosition, tagWidth, tagRectangle)
            } else {

                // tag width
                val tagWidth = tagPaint.measureText(tag)

                // tag position
                val xa1 = x1 + x1Anchor
                val xa2 = x2 + x2Anchor
                val labelLeft = xa1 + (xa2 - xa1 - tagWidth) / 2
                val fontDescent: Float = tagPaint.fontMetrics.descent
                val labelBase: Float = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - fontDescent
                val tagPosition = PointF(labelLeft, labelBase)

                // tag rectangle
                val tagFontHeight: Float = tagPaint.fontMetrics.height()
                val x = tagPosition.x - LABEL_INFLATE
                val y = (yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - tagFontHeight - LABEL_INFLATE)
                val w = tagWidth + 2 * LABEL_INFLATE
                val h = tagFontHeight + 2 * LABEL_INFLATE
                val tagRectangle = RectF(x, y, x + w, y + h)

                return Triple(tagPosition, tagWidth, tagRectangle)
            }
        }

        /**
         * Make edge
         *
         * @param fromX           x from
         * @param toX             x to
         * @param baseY           base y
         * @param fromAnchorX     x anchor from
         * @param toAnchorX       x anchor to
         * @param yAnchor         y anchor
         * @param height          height
         * @param bottom          pad bottom
         * @param label           label
         * @param isBackwards     whether edge is backwards
         * @param isLeftTerminal  whether this edge left-terminates
         * @param isRightTerminal whether this edge right-terminates
         * @param isVisible       whether this edge is visible
         * @param color           color
         */
        fun makeEdge(
            fromX: Float,
            toX: Float,
            baseY: Float,
            fromAnchorX: Float,
            toAnchorX: Float,
            yAnchor: Float,
            height: Float,
            bottom: Float,
            label: String?,
            isBackwards: Boolean,
            isLeftTerminal: Boolean,
            isRightTerminal: Boolean,
            isVisible: Boolean,
            color: Int,
            tagPaint: Paint,

            ): Edge {

            // tag
            val maxWidth = toX + toAnchorX - (fromX + fromAnchorX)
            val (tag, isVertical) = processTag(label, maxWidth, tagPaint)
            val (tagPosition, tagWidth, tagRectangle) = computeTag(tag, isVertical, fromX, fromAnchorX, toX, toAnchorX, baseY, tagPaint)

            // edge
            val edge = Edge(
                fromX,
                toX,
                baseY,
                fromAnchorX,
                toAnchorX,
                yAnchor,
                height,
                bottom,
                tag,
                tagPosition,
                tagWidth,
                tagRectangle,
                isVertical = isVertical,
                isBackwards = isBackwards,
                isLeftTerminal = isLeftTerminal,
                isRightTerminal = isRightTerminal,
                isVisible = isVisible,
                color,
            )
            return edge
        }

        // T R U N C A T E   T A G

        /**
         * Truncate margin
         */
        const val TRUNCATE_MARGIN = 30

        /**
         * Character used when there is not enough space. If null, label will be truncated, and rotated 90°
         */
        val LABEL_STAND_IN_CHAR: String? = null // "▾"; // "↓▾▿☟"

        /**
         * Truncate length when label is vertical
         */
        const val LABEL_VERTICAL_TRUNCATE: Int = 4

        fun processTag(label: String?, maxWidth: Float, paint: Paint): Pair<String, Boolean> {
            if (label == null) {
                return "" to false
            }
            // truncate if needed to fit in
            var isVertical = false
            var truncatedLabel: String? = truncate(label, maxWidth - TRUNCATE_MARGIN, paint)
            if (truncatedLabel == null) {
                // has failed
                if (LABEL_STAND_IN_CHAR != null) truncatedLabel = LABEL_STAND_IN_CHAR
                else {
                    truncatedLabel = label
                    if (truncatedLabel.length > LABEL_VERTICAL_TRUNCATE) truncatedLabel = label.substring(0, min(LABEL_VERTICAL_TRUNCATE.toDouble(), label.length.toDouble()).toInt()) + '⋯'
                    isVertical = true
                }
            }
            return truncatedLabel to isVertical
        }

        /**
         * Truncate label to width
         *
         * @param label   label
         * @param width   width
         * @param paint   paint for text measurement
         * @return truncate label, null if can't
         */
        fun truncate(label: String, width: Float, paint: Paint): String? {
            val tagCharWidth: Float = paint.measureText("M")
            val n = (width / tagCharWidth).toInt()

            // if available space width less than one character, return null
            if (n < 1) return null // can't

            // do not truncate if all fits in
            val xOffset = (width - paint.measureText(label)) / 2
            if (xOffset > 0) return label

            // truncate
            return if (n >= 3) {
                label.substring(0, min(n - 2, label.length).toInt()) + '⋯'
            } else {
                label.substring(0, min(n, label.length))
            }
        }
    }
}