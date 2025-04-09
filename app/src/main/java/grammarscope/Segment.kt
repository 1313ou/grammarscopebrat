package grammarscope

import java.io.Serializable

/**
 * Segment (pair of from- and to-indices). Indices refer to nth character in text. To-index is not included in segment.
 *
 * @param from from index
 * @param to   to index
 *
 * @author Bernard Bou
 */
data class Segment(
    /**
     * From index
     */
    val from: Int,
    /**
     * To index (excluded)
     */
    val to: Int

) : Serializable {

    val isVisible: Boolean
        /**
         * Whether segment is visible
         *
         * @return Whether segment is visible
         */
        get() = this.from >= 0 && this.to > 0 && this.from < this.to

    /**
     * Extract text
     *
     * @param text text
     * @return segment
     */
    fun getText(text: String): String {
        return getText(text, 0)
    }

    /**
     * Extract text
     *
     * @param text   text
     * @param offset offset
     * @return segment
     */
    fun getText(text: String, offset: Int): String {
        return text.substring(this.from - offset, this.to - offset)
    }

    /*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return "[$from-$to]"
    }

    companion object {

        /**
         * Merge segments into one
         *
         * @param segments ordered segments
         * @return segment
         */
        fun merge(segments: MutableList<out Segment?>?): Segment? {
            if (segments == null || segments.isEmpty()) return null
            return Segment(segments.get(0)!!.from, segments.get(segments.size - 1)!!.to)
        }

        /**
         * Merge segments to segment
         *
         * @param segments ordered segments
         * @return segment
         */
        fun merge(vararg segments: Segment?): Segment? {
            if (segments.isEmpty()) return null
            return Segment(segments[0]!!.from, segments[segments.size - 1]!!.to)
        }
    }
}