package grammarscope

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import grammarscope.Edge.Companion.truncate
import grammarscope.Utils.drawEllipse
import grammarscope.Utils.drawTriangle
import grammarscope.Utils.drawUpwardTriangle
import kotlin.math.min
import grammarscope.Utils.height

/**
 * Bar between lines
 */
internal class BoxedLabel(
    rectangle: RectF,
    /**
     * Bar color
     */
    val backgroundColor: Paint?,
    /**
     * Bar tag (relation)
     */
    val tag: String,
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
    val left = rectangle.left

    /**
     * Bar top
     */
    val top = rectangle.top

    /**
     * Bar width
     */
    val width = rectangle.width()

    /**
     * Bar height (if -1, take the common bar height)
     */
    val height= rectangle.height()

    /**
     * Get bar rectangle
     *
     * @return bar rectangle
     */
    fun getRectangle(barHeight: Float): RectF {
        val height = if (this.height != -1F) this.height else barHeight
        val top = this.top + this.slot * height
        return RectF(this.left, top, this.width, (if (this.height != -1F) this.height else barHeight))
    }

    /**
     * Draw bar
     *
     * @param g graphics context
     */
    override fun draw(g: Canvas, p: Paint) {

        val tagFlagArg: Boolean = true
        val tagFitsInFlagArg: Boolean = true
        val heightArg: Float = 1F
        val tagYOffsetArg: Float = 1F
        val foreColorArg: Int = 0

        // bar rectangle
        val barRectangle: RectF = getRectangle(heightArg)

        // head
        if (this.head >= 0) {
            val x1 = this.left + this.head
            g.drawUpwardTriangle(x1, barRectangle.top, HEAD_DX, HEAD_DY, BoxRenderer.HEAD_COLOR)
        }

        // previous
        if (this.hasPreviousBar || this.hasNextBar) {
            val yArrowTip: Float = barRectangle.top + barRectangle.height() / 2F
            val hArrowTip = min(barRectangle.height() / 2F, ARROW_DY)
            val arrowPaint = Paint().apply { color = BoxRenderer.ARROW_COLOR }
            if (this.hasPreviousBar) {
                g.drawTriangle(left, yArrowTip, ARROW_DX, hArrowTip, reverse=true,  arrowPaint)
            }

            // next
            if (this.hasNextBar) {
                val xArrowTip = this.left + this.width
                g.drawTriangle(xArrowTip.toFloat(), yArrowTip, ARROW_DX, hArrowTip, reverse = false, arrowPaint)
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
                g.drawEllipse(barRectangle.centerX(), barRectangle.centerY())
                return
            }

            // offsets
            val tagMetrics = p.fontMetrics
            val tagDescent= tagMetrics.descent
            val tagHeight = tagMetrics.height()
            val tagWidth= p.measureText(truncatedTag)

            val tagXOffset = (this.width - tagWidth) / 2F
            var tagYOffset: Float = tagYOffsetArg
            if (tagYOffset == -1F) {
                tagYOffset = (barRectangle.height() - tagHeight) / 2F
                if (tagYOffset < 0) return
                tagYOffset += tagHeight
                tagYOffset -= tagDescent
            }

            // position
            val xTag = this.left + tagXOffset
            val yTag = barRectangle.top + tagYOffset
            g.drawText(truncatedTag, xTag, yTag, Paint().apply { foreColorArg} )

            // box it
            val a = tagMetrics.ascent
            val h = tagMetrics.height()
            val w = p.measureText(truncatedTag)
            g.drawRoundRect(xTag - 2, yTag - a - 2, w + 3, h + 3, 4F, 4F, p)
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
