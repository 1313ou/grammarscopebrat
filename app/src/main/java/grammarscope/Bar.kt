package grammarscope

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import grammarscope.Edge.Companion.truncate
import grammarscope.Utils.drawEllipse
import grammarscope.Utils.drawTriangle
import grammarscope.Utils.drawUpwardTriangle
import grammarscope.Utils.height
import kotlin.math.min

/**
 * Bar between lines
 */
internal class Bar(
    rectangle: RectF,
    /**
     * Bar color
     */
    val backgroundColor: Paint?,
    /**
     * Bar tag (relation)
     */
    val tag: String?,
    /**
     * Allocated slot
     */
    val slot: Int,
    /**
     * Whether this bar continues on previous line
     */
    val hasPreviousBar: Boolean,
    /**
     * Whether this bar is continued on next line
     */
    val hasNextBar: Boolean,
    /**
     * Head x1 offset (from x1) if any (negative if no head is contained)
     */
    val head: Float,
    /**
     * Source object
     */
    val source: ISource?

) : Drawable {

    /**
     * Bar left
     */
    val left: Float = rectangle.left

    /**
     * Bar top
     */
    val top: Float = rectangle.top

    /**
     * Bar width
     */
    val width: Float = rectangle.width()

    /**
     * Bar height (if -1, take the common bar height)
     */
    val height: Float = rectangle.height()

    /**
     * Get bar rectangle
     *
     * @return bar rectangle
     */
    fun getRectangle(barHeight: Float): RectF {
        val height = if (this.height != -1F) this.height else barHeight
        val top = this.top + this.slot * height
        return RectF(this.left.toFloat(), top.toFloat(), this.width.toFloat(), (if (this.height != -1F) this.height else barHeight).toFloat())
    }

    /**
     * Draw bar
     *
     * @param canvas graphics context
     */
    override fun draw(canvas: Canvas, p: Paint) {

        val tagFlagArg: Boolean = true
        val tagFitsInFlagArg: Boolean = true
        val heightArg: Float = 1F
        val tagYOffsetArg: Float = 1F
        val foreColorArg: Int = 0
        val borderColorArg: Int = 0
        val backgroundColorArg: Int = 0

        // bar rectangle
        val barRectangle = getRectangle(heightArg)

        // box
        p.apply {
            color = backgroundColorArg
            style = Paint.Style.FILL
        }
        canvas.drawRect(barRectangle, p)
        p.apply {
            color = borderColorArg
            style = Paint.Style.STROKE
        }
        canvas.drawRect(barRectangle, p)

        // head
        if (this.head >= 0) {
            val x1 = this.left + this.head
            canvas.drawUpwardTriangle(x1, barRectangle.top, HEAD_DX, HEAD_DY, BoxRenderer.HEAD_COLOR)
        }

        // previous
        if (this.hasPreviousBar || this.hasNextBar) {
            val yArrowTip = barRectangle.top + barRectangle.height() / 2f
            val hArrowTip = min(barRectangle.height() / 2f, ARROW_DY)
            val arrowPaint = Paint().apply { color = BoxRenderer.ARROW_COLOR }

            if (this.hasPreviousBar) {
                canvas.drawTriangle(this.left, yArrowTip, ARROW_DX, hArrowTip, reverse = true, arrowPaint)
            }

            // next
            if (this.hasNextBar) {
                val xArrowTip = this.left + this.width
                canvas.drawTriangle(xArrowTip, yArrowTip, ARROW_DX.toFloat(), hArrowTip, reverse = false, arrowPaint)
            }
        }

        // tag
        val tagFlag: Boolean = tagFlagArg
        val tagFitsInFlag: Boolean? = tagFitsInFlagArg
        if (tagFlag && (tagFitsInFlag == null || tagFitsInFlag) && this.tag != null) {

            // truncate if needed
            val truncatedTag: String? = truncate(this.tag, this.width, p)
            if (truncatedTag == null) {
                // doesn't fit
                // g2.drawLine(barRectangle.x, barRectangle.y, barRectangle.x + barRectangle.width, barRectangle.y +
                // barRectangle.height);
                // g2.drawLine(barRectangle.x, barRectangle.y + barRectangle.height, barRectangle.x + barRectangle.width,
                // barRectangle.y);
                canvas.drawEllipse(barRectangle.centerX(), barRectangle.centerY())
                return
            }

            // offsets
            // tag metrics
            val tagMetrics = p.fontMetrics
            val tagDescent = tagMetrics.descent
            val tagHeight = tagMetrics.height()
            val tagWidth = p.measureText(truncatedTag)

            val tagXOffset = (this.width - tagWidth) / 2
            var tagYOffset = tagYOffsetArg
            if (tagYOffset == -1F) {
                tagYOffset = (barRectangle.height() - tagHeight) / 2F
                if (tagYOffset < 0F) return
                tagYOffset += tagHeight
                tagYOffset -= tagDescent
            }

            // position
            val xTag = this.left + tagXOffset
            val yTag = barRectangle.top + tagYOffset
            val paint = Paint().apply { color = foreColorArg }
            canvas.drawText(truncatedTag, xTag, yTag, paint)
        }
    }

    override fun toString(): String {
        return "$tag x=$left y=$top w=$width h=$height head=$head ${if (this.hasPreviousBar) "--" else "|-"} ${if (this.hasNextBar) "--" else "-|"}"
    }

    companion object {
        /**
         * Arrow width
         */
        const val ARROW_DX = 5F

        /**
         * Arrow height
         */
        const val ARROW_DY = 5F

        /**
         * Head width
         */
        const val HEAD_DX = 2F

        /**
         * Head height
         */
        const val HEAD_DY = 3F
    }
}
