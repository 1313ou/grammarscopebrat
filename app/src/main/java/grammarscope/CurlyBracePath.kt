package grammarscope

import android.graphics.Path
import android.graphics.PointF

/**
 * Curly brace
 *
 * @param xFrom endpoint1 x
 * @param xTo   endpoint2 x
 * @param xMid  mid point x
 * @param y     y
 * @param down  base-tip orientation
 * cubicTo(x1, y1, x2, y2, x3, y3) : Bezier curve to (x3,y3), using the control points (x1,y1) and (x2,y2)
 * @author Bernard Bou
 */
class CurlyBracePath(xFrom: Int, xTo: Int, xMid: Int, y: Int, down: Boolean) : Path() {

    init {
        val yTop: Int = y - CURLY_HEIGHT
        val yBase = if (down) yTop else y
        val yTip = if (down) y else yTop

        // end points for first curve
        val p1 = PointF(xFrom.toFloat(), yBase.toFloat())
        val pm = PointF(xMid.toFloat(), yTip.toFloat())

        // end points for second curve
        val p2 = PointF(xTo.toFloat(), yBase.toFloat())

        // control points for first curve
        val c1 = PointF(xFrom.toFloat(), yTip.toFloat())
        val cm = PointF(xMid.toFloat(), yBase.toFloat())

        // control points for second curve
        val c2 = PointF(xTo.toFloat(), yTip.toFloat())

        // define path
        moveTo(p1.x, p1.y)
        // p1 to pm
        cubicTo(c1.x, c1.y, cm.x, cm.y, pm.x, pm.y)
        // pm to p2
        cubicTo(cm.x, cm.y, c2.x, c2.y, p2.x, p2.y)
    }

    companion object {

        private const val CURLY_HEIGHT = 4
    }
}