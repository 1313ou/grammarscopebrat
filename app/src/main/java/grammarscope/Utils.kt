package grammarscope

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Path

object Utils {

    /**
     * Draw triangle
     *
     * @param color   color
     * @param x0      x coordinate
     * @param y0      y coordinate
     * @param w       arrow width
     * @param h2      arrow half height
     * @param reverse reverse
     * @param rotation rotation
     * @param paint   paint
     */
    @JvmStatic
    fun Canvas.drawTriangle(x0: Float, y0: Float, w: Float, h2: Float, reverse: Boolean, rotation: Float, paint: Paint) {
        val trianglePath = triangle(x0, y0, w, h2, reverse)
        save()
        try {
            translate(x0, y0)
            rotate(rotation)
            translate(-x0, -y0)
            drawPath(trianglePath, paint)
        } finally {
            restore()
        }
    }

    /**
     * Draw triangle
     *
     * @param color   color
     * @param x0      x coordinate
     * @param y0      y coordinate
     * @param w       arrow width
     * @param h2      arrow half height
     * @param reverse reverse
     * @param paint   paint
     */
    @JvmStatic
    fun Canvas.drawTriangle(x0: Float, y0: Float, w: Float, h2: Float, reverse: Boolean, paint: Paint) {
        val trianglePath = triangle(x0, y0, w, h2, reverse)
        drawPath(trianglePath, paint)
    }

    private fun triangle(x0: Float, y0: Float, w: Float, h2: Float, reverse: Boolean): Path {
        val x1 = x0 + (if (reverse) w else -w)
        return Path().apply {
            moveTo(x0, y0)
            lineTo(x1, y0 - h2)
            lineTo(x1, y0 + h2)
            close()
        }
    }

    @JvmStatic
    fun Canvas.drawDot(x0: Float, y0: Float, r: Float, paint: Paint) {
        if (r == 0.5F) {
            drawLine(x0, y0, x0, y0, paint)
        }
        val x1 = x0 - r
        val y1 = y0 - r
        val x2 = x0 + r
        val y2 = y0 + r
        drawOval(x1, y1, x2, y2, paint)
    }

    fun Canvas.drawEllipse(x: Float, y: Float) {}

    fun Canvas.drawUpwardTriangle(x: Float, y: Float, dx: Float, dy: Float, color: Int ) {}


    @JvmStatic
    fun FontMetrics.height(): Float {
        return descent - ascent + leading // precise line height information, and be sure you're including the inter-line spacing
        // return fontSpacing // a quick approximation of the line height.
    }


}