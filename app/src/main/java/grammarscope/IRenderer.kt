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
     * @param padWidth      annotation pad width
     * @param padTopOffset  annotation pad top offset
     * @param padHeight     annotation pad height
     * @param lineHeight    text line height
     * @return used pad height
     */
    fun layout(
        document: Document,
        textComponent: TextView,
        padWidth: Int,
        padTopOffset: Int,
        padHeight: Int,
        lineHeight: Int
    ): Int

    /**
     * Paint
     *
     * @param canvas graphics context
     */
    fun paint(canvas: Canvas)

    /**
     * Get top offset
     *
     * @return top offset
     */
    val topOffset: Int

    /**
     * Get height
     *
     * @return height
     */
    val height: Int

    /**
     * Get back color
     *
     * @return back color
     */
    val backColor: Int?
}