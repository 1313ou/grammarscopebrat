package com.bbou.brats

import android.graphics.Path
import android.graphics.PointF

/**
 * Curve path
 *
 * @author Bernard Bou
 */
class CurvePath(
    xArcFrom: Float,
    xArcTo: Float,
    xTextFrom: Float,
    xTextTo: Float,
    yArcFrom: Float,
    yArcTo: Float,
    yText: Float,
    flat1: Boolean,
    flat2: Boolean
) : Path() {
    /**
     * Left corner
     */
    var xCornerLeft: Float
        private set

    /**
     * Right corner
     */
    var xCornerRight: Float
        private set

    /**
     * Left control point
     */
    var xControlLeft: Float
        private set

    /**
     * Right control point
     */
    var xControlRight: Float = 0f
        private set

    /**
     * Curve y base
     */
    val yBase: Float = yText.toFloat()

    /**
     * Constructor
     *
     * @param xArcFrom  arc x from
     * @param xArcTo    arc x to
     * @param xTextFrom label start
     * @param xTextTo   label end
     * @param yArcFrom  left origin anchor y
     * @param yArcTo    right destination anchor y
     * @param yText     arc base
     * @param flat1     true if rowIndex == leftRow
     * @param flat2     true if rowIndex == rightRow
     */
    init {

        // E N D S

        var xTextStart = xTextFrom
        var xTextEnd = xTextTo

        // A D J U S T

        // adjust ends by margin for arc drawing
        xTextStart -= ARC_TEXT_MARGIN
        xTextEnd += ARC_TEXT_MARGIN

        // ensure left to right direction
        if (xArcFrom > xArcTo) {
            // swap
            val tmp = xTextStart
            xTextStart = xTextEnd
            xTextEnd = tmp
        }

        val isUfoCatcher1 = xTextFrom < xArcFrom
        val isUfoCatcher2 = xTextTo > xArcTo

        val ufoCatcherMod1 = if (isUfoCatcher1) if (CHUNK_REVERSE_1) -0.5f else 0.5f else 1f
        val ufoCatcherMod2 = if (isUfoCatcher2) if (CHUNK_REVERSE_2) -0.5f else 0.5f else 1f

        // P R E

        // pre-label arc segment
        val p1 = PointF(xTextStart.toFloat(), this.yBase)
        moveTo(p1.x, p1.y)

        this.xCornerLeft = -1f
        this.xControlLeft = -1f
        if (!flat1) {
            // corner x
            this.xCornerLeft = xArcFrom + ufoCatcherMod1 * ARC_SLANT_SPAN
            if ( /* !isUfoCatcher1 && */this.xCornerLeft > xTextStart)  // for normal cases, should not be past textStart even if narrow
            {
                this.xCornerLeft = xTextStart.toFloat()
            }

            // arc origin
            val p2 = PointF(xArcFrom.toFloat(), yArcFrom.toFloat())
            if (SMOOTH_ARC_CURVES) {
                // plateau
                lineTo(this.xCornerLeft, this.yBase)

                // curve
                if (isUfoCatcher1) {
                    this.xControlLeft = this.xCornerLeft + 2 * ufoCatcherMod1 * REVERSE_ARC_CONTROL_X
                } else {
                    this.xControlLeft = if (SMOOTH_ARC_CURVES_USE_STEEPNESS) SMOOTH_ARC_STEEPNESS * xArcFrom + (1 - SMOOTH_ARC_STEEPNESS) * this.xCornerLeft else xArcFrom.toFloat()
                }
                quadTo(this.xControlLeft, this.yBase, p2.x, p2.y) // dive
            } else {
                lineTo(this.xCornerLeft, this.yBase) // plateau
                lineTo(p2.x, p2.y) // dive
            }
        } else  // flat
        {
            val p2 = PointF(xArcFrom.toFloat(), this.yBase)
            lineTo(p2.x, p2.y)
        }

        // P O S T

        // post-label arc segment
        val p3 = PointF(xTextEnd.toFloat(), this.yBase)
        moveTo(p3.x, p3.y)

        this.xCornerRight = -1f
        if (!flat2) {
            // corner x
            this.xCornerRight = xArcTo - ufoCatcherMod2 * ARC_SLANT_SPAN
            if ( /* !isUfoCatcher2 && */this.xCornerRight < xTextEnd)  // for normal cases, should not be past textEnd even if narrow
            {
                this.xCornerRight = xTextEnd.toFloat()
            }

            // arc destination
            val p4 = PointF(xArcTo.toFloat(), yArcTo.toFloat())
            if (SMOOTH_ARC_CURVES) {
                // plateau
                lineTo(this.xCornerRight, this.yBase)

                // curve
                if (isUfoCatcher2) {
                    this.xControlRight = this.xCornerRight - 2 * ufoCatcherMod2 * REVERSE_ARC_CONTROL_X
                } else {
                    this.xControlRight = if (SMOOTH_ARC_CURVES_USE_STEEPNESS) SMOOTH_ARC_STEEPNESS * xArcTo + (1 - SMOOTH_ARC_STEEPNESS) * this.xCornerRight else xArcTo.toFloat()
                }
                quadTo(this.xControlRight, this.yBase, p4.x, p4.y)
            } else {
                lineTo(this.xCornerRight, this.yBase)
                lineTo(p4.x, p4.y)
            }
        } else { // flat
            val p2 = PointF(xArcTo.toFloat(), this.yBase)
            lineTo(p2.x, p2.y)
        }
    }

    companion object {

        /**
         * Steepness of smooth curves (control point)
         */
        const val SMOOTH_ARC_STEEPNESS: Float = .6f

        /**
         * Control point distance for "UFO catchers"
         */
        const val REVERSE_ARC_CONTROL_X: Float = 10f

        /**
         * Slant span
         */
        const val ARC_SLANT_SPAN: Float = 15f

        /**
         * Text margin
         */
        private const val ARC_TEXT_MARGIN = 1

        /**
         * Whether to smooth arc curves
         */
        const val SMOOTH_ARC_CURVES: Boolean = true

        /**
         * Whether to use steepness in smooth arc curves
         */
        const val SMOOTH_ARC_CURVES_USE_STEEPNESS: Boolean = false

        private const val CHUNK_REVERSE_1 = true

        private const val CHUNK_REVERSE_2 = true
    }
}
