package grammarscope

import android.graphics.Canvas
import android.widget.TextView
import grammarscope.document.Document

/**
 * Renderer interface
 *
 * @author Bernard Bou
 */
interface IRenderer {
    /**
     * Layout
     *
     * @param document      document
     * @param textComponent text component
     * @return used pad height
     */
    fun annotate(
        document: Document,
        textComponent: TextView,
    ): Int

    /**
     * Paint
     *
     * @param canvas graphics context
     */
    fun paint(canvas: Canvas)
}