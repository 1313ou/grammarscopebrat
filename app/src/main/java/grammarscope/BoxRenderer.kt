package grammarscope

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.TextView
import com.bbou.brats.modelToViewF
import grammarscope.Utils.height
import grammarscope.document.Document
import java.util.TreeMap

interface Drawable {
    fun draw(canvas: Canvas, paint: Paint)
}

interface ISource

/**
 * Box renderer
 *
 * @author Bernard Bou
 */
abstract class BoxRenderer {

    /**
     * Boxes (indexed by line)
     */
    protected val drawables: MutableMap<Int, MutableList<Drawable>> = TreeMap<Int, MutableList<Drawable>>()

    // A N N O T A T E

    abstract fun annotate(
        document: Document,
        textComponent: TextView,
        padWidth: Float,
        padTopOffset: Float,
        padHeight: Float,
        lineHeight: Float
    ): Int

    /**
     * Make drawable
     *
     * @param segments      segments for box to span
     * @param headSegment   head segment
     * @param tag           box tag
     * @param color         box color
     * @param slot          box slot
     * @param padTopOffset  top
     * @param lineHeight    text line height
     * @param source        source object
     * @param textComponent text component to provide segment locations
     */
    protected fun makeDrawable(
        segments: MutableList<Segment>,
        headSegment: Segment,
        tag: String,
        color: Paint,
        slot: Int,
        padTopOffset: Int,
        lineHeight: Int,
        source: ISource,
        textComponent: TextView
    ) {
        // get segments
        if (segments.isEmpty()) return

        // location
        val fromRectangle: RectF = textComponent.modelToViewF(segments[0].from)
        val toRectangle: RectF = textComponent.modelToViewF(segments[segments.size - 1].to)

        // head
        val headRectangle: RectF = textComponent.modelToViewF(headSegment)

        // test whether box fits on line
        if (fromRectangle.top == toRectangle.top) {
            // box fits on line
            val y: Float = fromRectangle.top
            val headOffset: Float = if (false) -1F else headRectangle.left + headRectangle.width() / 2F - fromRectangle.left
            val boxRectangle = RectF()
            boxRectangle.left = fromRectangle.left
            boxRectangle.top = y + lineHeight + padTopOffset + TOP_INSET
            boxRectangle.right = toRectangle.right - fromRectangle.left
            boxRectangle.bottom = boxRectangle.top - 1 // invalid
            val drawable: Drawable = makeDrawable(boxRectangle, color, tag, slot, false, false, headOffset, source)
            addDrawable(drawable, fromRectangle.top.toInt())
        } else {
            // box does not fit on line : make one box per line
            val lastSegmentIndex = segments.size // last

            // cursor
            var xRight = fromRectangle.left + fromRectangle.width()
            var xLeft = fromRectangle.left
            var y = fromRectangle.top

            // head state
            var wasHead = false

            // first segment in box
            var isFirst = true

            // iterate over word segments
            var currentSegmentIndex = 0
            for (segment in segments) {
                currentSegmentIndex++

                // last character in this segment
                val lastCharRectangle: RectF = textComponent.modelToViewF(segment.to - 1)

                // if line skipped (this segment is not on the current line, the previous one is)
                if (lastCharRectangle.top != y) {
                    // line break before this segment
                    val headOffset = if (wasHead) -1F else headRectangle.left + headRectangle.width() / 2F - xLeft
                    val boxRectangle = RectF()
                    boxRectangle.left = xLeft - (if (isFirst) 0 else X_MARGIN)
                    boxRectangle.top = y + lineHeight + padTopOffset + TOP_INSET
                    boxRectangle.right = boxRectangle.left + xRight - xLeft + X_MARGIN
                    boxRectangle.bottom = boxRectangle.top - 1
                    val drawable: Drawable = makeDrawable(boxRectangle, color, tag, slot, !isFirst, true, headOffset, source)
                    addDrawable(drawable, y.toInt())

                    // move left cursor ahead to first character of this segment
                    val rectangle2: RectF = textComponent.modelToViewF(segment.from)
                    xLeft = rectangle2.left
                    y = rectangle2.top
                    isFirst = false
                }

                // move right cursor ahead to end of next space
                val nextCharRectangle: RectF = textComponent.modelToViewF(segment.to)
                xRight = nextCharRectangle.left + nextCharRectangle.width()

                // mark head if this was head segment
                wasHead = segment == headSegment

                // finish it off if this is the last segment
                if (currentSegmentIndex == lastSegmentIndex) {
                    // last segment
                    val headOffset = if (!wasHead) -1F else headRectangle.left + headRectangle.width() / 2F - xLeft
                    val boxRectangle = RectF()
                    boxRectangle.left = xLeft - (if (isFirst) 0 else X_MARGIN)
                    boxRectangle.top = y + lineHeight + padTopOffset + TOP_INSET
                    boxRectangle.right = boxRectangle.left + xRight - xLeft
                    boxRectangle.bottom =boxRectangle.top -1
                    val drawable = makeDrawable(boxRectangle, color, tag, slot, true, false, headOffset, source)
                    addDrawable(drawable, y.toInt())
                }
            }
        }
    }

    /**
     * Make drawable
     *
     * @param boxRectangle    drawable height
     * @param backgroundColor drawable background color
     * @param tag             drawable tag
     * @param slot            drawable's allocated slot
     * @param hasPreviousFlag whether this drawable continues previous line
     * @param hasNextFlag     whether this drawable is continued on next line
     * @param headOffset      head x offset (from left) if any
     * @param source          source object
     * @return drawable
     */
    protected abstract fun makeDrawable(
        boxRectangle: RectF,
        backgroundColor: Paint?,
        tag: String?,
        slot: Int,
        hasPreviousFlag: Boolean,
        hasNextFlag: Boolean,
        headOffset: Float,
        source: ISource?
    ): Drawable

    /**
     * Add drawable to drawables
     *
     * @param drawable drawable
     * @param lineId   line id
     */
    private fun addDrawable(drawable: Drawable, lineId: Int) {
        val drawables: MutableList<Drawable> = this.drawables.computeIfAbsent(lineId) { k: Int? -> ArrayList<Drawable>() }
        drawables.add(drawable)
    }

    fun paint(canvas: Canvas) {
        val paint = Paint().apply { typeface = tagFont }
        for (entry in this.drawables.entries) {
            val drawables: MutableList<Drawable> = entry.value
            for (drawable in drawables) {
                drawable.draw(canvas, paint)
            }
        }
    }

    /**
     * Compute box height giving each box an equal share of the pad height
     *
     * @param slotCount number of slots
     * @param padHeight pad height
     * @return box height
     */
    protected fun computeBoxHeight(slotCount: Int, padHeight: Float): Float {
        var height = 0F
        if (slotCount > 0) {
            val allocatableHeight = padHeight - TOP_INSET - BOTTOM_INSET
            height = allocatableHeight / slotCount

            val maxHeight = this.defaultHeight
            if (height > maxHeight) {
                height = maxHeight
            }
        }
        return height
    }

    /**
     * Minimum box height to accept label
     */
    protected val defaultHeight: Float
         get() {
            val tagHeight: Float = Paint().fontMetrics.height()
            return tagHeight + LABEL_TOP_INSET + LABEL_BOTTOM_INSET + 2
        }

    companion object {

        /**
         * Head marker color
         */
        const val HEAD_COLOR: Int = Color.GRAY

        /**
         * Arrow color
         */
        const val ARROW_COLOR: Int = Color.LTGRAY

        /**
         * Top inset
         */
        private const val TOP_INSET = 5

        /**
         * Bottom inset
         */
        private const val BOTTOM_INSET = 5

        /**
         * Label top inset
         */
        private const val LABEL_TOP_INSET = 1

        /**
         * Label bottom inset
         */
        private const val LABEL_BOTTOM_INSET = 1

        /**
         * X margin
         */
        private const val X_MARGIN = 40

        /**
         * Tag font
         */
        protected val tagFont: Typeface = Typeface.SANS_SERIF

        /**
         * Compute used height
         *
         * @param slotCount slot count
         * @param boxHeight box height
         * @return used height
         */
        @JvmStatic
        protected fun computeUsed(slotCount: Int, boxHeight: Float): Float {
            return TOP_INSET + BOTTOM_INSET + slotCount * boxHeight
        }
    }
}
