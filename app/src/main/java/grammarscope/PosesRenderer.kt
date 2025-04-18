package grammarscope

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.TextView
import grammarscope.document.Document

object Source : (String) -> String, ISource {
    override fun invoke(pos: String): String {
        return "POS"
    }
}

/**
 * Parts-of-speech renderer
 *
 * @author Bernard Bou
 */
class PosesRenderer : BoxRenderer() {

    override fun annotate(
        document: Document,
        textComponent: TextView,
        padWidth: Float,
        padTopOffset: Float,
        padHeight: Float,
        lineHeight: Float
    ): Int {
        var used = false

        // bar height
        val barHeight = defaultHeight
        if (barHeight < padHeight) {
            // iterate over sentences
            val n = document.sentenceCount
            for (sentenceIdx in 0..<n) {
                val segment = document.getSentenceSegment(sentenceIdx)
                val words = document.split(segment)
                for (word in words) {
                    // pos tag
                    val pos = document.getPos(sentenceIdx, word)
                    if (pos == null) {
                        continue
                    }

                    // get segments
                    val segments: MutableList<Segment> = ArrayList<Segment>()
                    segments.add(word)

                    // data

                    // slot
                    val slot = 0

                    // at least one use
                    used = true

                    // make
                    makeDrawable(segments, segments.get(0), pos, Paint().apply { color = backColor }, slot, padTopOffset.toInt(), lineHeight.toInt(), Source, textComponent)
                }
            }
        }

        if (used) {
            // bar height
            // TODO getContext().setHeight(barHeight)


            // return used space
            val height = computeUsed(1, barHeight)
            // TODO this.topOffset = padTopOffset
            return height.toInt()
        }
        return 0
    }

    // D R A W A B L E F A C T O R Y

    override fun makeDrawable(barRectangle: RectF, backgroundColor: Paint?, tag: String?, slot: Int, hasPreviousFlag: Boolean, hasNextFlag: Boolean, headOffset: Float, source: ISource?): Drawable {
        return BoxedLabel(barRectangle, backgroundColor, tag!!, slot, hasPreviousFlag, hasNextFlag, headOffset, source)
    }

    companion object {
        /**
         * Default back color
         */
        private val DEFAULT_BACKCOLOR = Color.argb(0x40, 0xA0, 0xA4, 0xAD) // 0x9E, 0xEB, 0xFF

        /**
         * Default fore color
         */
        private const val DEFAULT_FORECOLOR = Color.WHITE

        /**
         * Default border color
         */
        private const val DEFAULT_BORDERCOLOR = Color.DKGRAY

        /**
         * Back color
         */
        private var backColor: Int = DEFAULT_BACKCOLOR

        /**
         * Fore color
         */
        private var foreColor: Int = DEFAULT_FORECOLOR

        /**
         * Border color
         */
        private var borderColor: Int = DEFAULT_FORECOLOR

        /**
         * Set back color
         *
         * @param color color
         */
        fun setBackColor(color: Int?) {
            backColor = color ?: DEFAULT_BACKCOLOR
        }

        /**
         * Set fore color
         *
         * @param color color
         */
        fun setForeColor(color: Int?) {
            foreColor = color ?: DEFAULT_FORECOLOR
        }

        /**
         * Set border color
         *
         * @param color color
         */
        fun setBorderColor(color: Int?) {
            borderColor = color ?: DEFAULT_BORDERCOLOR
        }
    }
}
