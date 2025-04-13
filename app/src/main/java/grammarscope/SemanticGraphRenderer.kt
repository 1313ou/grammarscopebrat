package grammarscope

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.bbou.brats.Annotation.BoxAnnotation
import com.bbou.brats.Annotation.EdgeAnnotation
import grammarscope.Edge.Companion.makeEdge
import grammarscope.allocator.Allocator
import grammarscope.document.Document
import grammarscope.document.Graph
import grammarscope.document.GraphEdge
import grammarscope.document.GraphNode
import kotlin.math.max

/**
 * Semantic graph renderer
 *
 * @param textView textView
 * @param renderAsCurves  whether to render edges as curves
 *
 * @author Bernard Bou
 */
class SemanticGraphRenderer(
    val textView: TextView,
    val renderAsCurves: Boolean
) : IRenderer {

    /**
     * Height
     */
    var height: Int = textView.height - textView.paddingTop - textView.paddingBottom

    /**
     * Width
     */
    private var width = textView.width - textView.paddingRight - textView.paddingLeft

    /**
     * Top
     */
    private val padTopOffset: Int = 5

    /**
     * Annotation pad width
     */
    private var padWidth = width

    /**
     * Annotation pad width
     */
    private var padHeight = textView.lineSpacingExtra

    /**
     * Paint for tag
     */
    private val tagPaint: Paint = Paint().apply { textSize = 30F }

    /**
     * Metrics for tag
     */
    private val lineHeight: Float = textView.paint.fontMetrics.height()

    /**
     * Relation palette
     */
    private lateinit var relationPalette: (GraphEdge) -> Int

    /**
     * Back color
     */
    val backColor: Int = DEFAULT_BACKCOLOR

    // DATA

    /**
     * Edges to draw
     */
    internal val edges: MutableCollection<EdgeAnnotation> = ArrayList()

    /**
     * Boxes to draw
     */
    internal val boxes: MutableCollection<BoxAnnotation> = ArrayList()

    // L A Y O U T

    private fun dumpLines() {
        for (line in 0..textView.layout.lineCount) {
            var r = Rect()
            textView.layout.getLineBounds(line, r)
            println("$line $r")
        }
    }

    /*
    topOffset
    label top inset
    label top inflate
    tag height
    label bottom inflate
    label bottom

    pad top = text bottom
    pad top inset
    edge tag
    first edge base
    ...
    last edge base
    pad bottom inset
    pad bottom = pad height
    */

    override fun layout(
        document: Document,
        textComponent: TextView,
    ): Int {
        if (document.sentenceCount == 0) return 0

        // dumpLines()

        //
        relationPalette = { e -> "#FF0000".toColorInt() }

        // space height
        val tagFontHeight: Float = tagPaint.fontMetrics.height()
        val tagHeight: Float = tagFontHeight + 2 * LABEL_INFLATE
        val tagSpace: Float = tagHeight + LABEL_TOP_INSET + LABEL_BOTTOM_INSET

        // where edges start (slot 0)
        val firstEdgeBase: Float = PAD_TOP_INSET + EDGES_INSET_TOP + tagSpace // relative to pad topOffset
        if (firstEdgeBase > padHeight) return 0
        val lastEdgeBase: Float = padHeight - PAD_BOTTOM_INSET - EDGES_INSET_BOTTOM // relative to pad

        // topOffset

        // iterate over sentences
        val n: Int = document.sentenceCount
        for (sentenceIdx in 0..<n) {
            val graph: Graph<GraphNode, GraphEdge> = document.getGraph(sentenceIdx)

            // NODES
            for (node in graph.nodes) {

                // location
                val segment = node.segment
                val rectangle: Rect = textComponent.modelToView(segment)

                // val boxL = rectangle.left
                // val boxT: Float = rectangle.top + lineHeight + padTopOffset + PAD_TOP_INSET
                // val boxH: Float = padHeight - PAD_TOP_INSET
                // val boxW = rectangle.width()
                // val box = RectF(boxL.toFloat(), boxT, boxW.toFloat(), boxH)
                val box = RectF(rectangle)
                this.boxes.add(BoxAnnotation(box))
            }

            // EDGES
            // edge list
            val gEdges: Collection<GraphEdge> = graph.edges

            // height and anchor allocator
            val allocator = Allocator(graph.nodes, graph.edges)

            // println(allocator)

            // build edges
            for (gEdge in gEdges) {
                // segment
                val fromWord = gEdge.source.segment
                val toWord = gEdge.target.segment

                val isBackwards = fromWord.from > toWord.from
                val leftSegment = if (isBackwards) toWord else fromWord
                val rightSegment = if (isBackwards) fromWord else toWord

                // location
                val leftRectangle: Rect = textComponent.modelToView(leftSegment)
                val rightRectangle: Rect = textComponent.modelToView(rightSegment)

                // data
                val label: String? = gEdge.label
                val color: Int = relationPalette.invoke(gEdge)
                val slot = allocator.getSlot(gEdge)
                val edgeYOffset = slot * tagSpace // relative to first slot
                val isVisible = firstEdgeBase + edgeYOffset < lastEdgeBase

                // if it fits on one line
                if (leftRectangle.top == rightRectangle.top) {
                    // compute edge
                    val xEdge1 = leftRectangle.left + leftRectangle.width() / 2F
                    val xEdge2 = rightRectangle.left + rightRectangle.width() / 2F
                    val yEdge = leftRectangle.top + lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                    val xAnchor1 = (allocator.getLeftAnchor(gEdge) * X_SHIFT)
                    val xAnchor2 = (allocator.getRightAnchor(gEdge) * X_SHIFT)
                    val yAnchor: Float = leftRectangle.top + lineHeight + padTopOffset + PAD_TOP_INSET
                    val bottom = leftRectangle.top + lineHeight + padTopOffset + padHeight

                    val edge: Edge = makeEdge(xEdge1, xEdge2, yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, bottom, label, isBackwards, isLeftTerminal = true, isRightTerminal = true, isVisible, color, tagPaint = tagPaint)
                    this.edges.add(EdgeAnnotation(edge))

                } else {
                    // edge does not fit on line : make one edge per line

                    // get segments in span

                    val segments: MutableList<Segment> = document.split(leftSegment, rightSegment)
                    if (segments.isEmpty()) {
                        continue
                    }

                    // for(Segment segment :segments) System.out.println(" segment=" + document.getString(segment))
                    val lastSegmentIndex = segments.size // last

                    // cursor
                    var xRight = leftRectangle.left + leftRectangle.width()
                    //int xRightOfs = leftRectangle.width / 2
                    var xLeft = leftRectangle.left
                    var xLeftOfs = leftRectangle.width() / 2
                    var y = leftRectangle.top

                    // first segment in line
                    var isFirst = true

                    // iterate over word segments
                    var currentSegmentIndex = 0
                    for (segment in segments) {
                        currentSegmentIndex++

                        // if is not visible
                        val rectangle: Rect? = textComponent.modelToView(segment)
                        if (rectangle == null) {
                            continue
                        }

                        // if line skipped (the current segment is on the current line)
                        if (rectangle.top != y) {
                            // line break before this segment
                            val xEdge1 = xLeft + xLeftOfs - (if (isFirst) 0 else X_MARGIN)
                            val xEdge2 = xRight + X_MARGIN
                            val yEdge = y + lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                            val xAnchor1 = (allocator.getLeftAnchor(gEdge) * X_SHIFT)
                            val xAnchor2 = (allocator.getRightAnchor(gEdge) * X_SHIFT)
                            val yAnchor: Float = y + lineHeight + padTopOffset + PAD_TOP_INSET
                            val bottom = y + lineHeight + padTopOffset + padHeight

                            val edge: Edge = makeEdge(xEdge1.toFloat(), xEdge2.toFloat(), yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, bottom, label, isBackwards, isLeftTerminal = isFirst, isRightTerminal = false, isVisible, color, tagPaint = tagPaint)
                            this.edges.add(EdgeAnnotation(edge))

                            // move ahead cursor1
                            val rectangle2: Rect = textComponent.modelToView(segment.from)
                            xLeft = rectangle2.left
                            xLeftOfs = rectangle2.width() / 2
                            y = rectangle2.top
                            isFirst = false
                        }

                        // move ahead cursor2
                        xRight = rectangle.left + rectangle.width()
                        val xRightOfs = rectangle.width() / 2

                        // finish it off if this is the last segment
                        if (currentSegmentIndex == lastSegmentIndex) {
                            // last segment
                            val xEdge1 = xLeft + xLeftOfs - (if (isFirst) 0 else X_MARGIN)
                            val xEdge2 = xRight - xRightOfs
                            val yEdge = y + lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                            val xAnchor1 = (allocator.getLeftAnchor(gEdge) * X_SHIFT)
                            val xAnchor2 = (allocator.getRightAnchor(gEdge) * X_SHIFT)
                            val yAnchor: Float = y + lineHeight + padTopOffset + PAD_TOP_INSET
                            val bottom = y + lineHeight + padTopOffset + padHeight

                            val edge: Edge = makeEdge(xEdge1.toFloat(), xEdge2.toFloat(), yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, bottom, label, isBackwards, isLeftTerminal = isFirst, isRightTerminal = true, isVisible, color, tagPaint = tagPaint)
                            this.edges.add(EdgeAnnotation(edge))
                        }
                    }
                }
            }
            break
        }
        return this.height
    }

    override fun paint(canvas: Canvas) {
        SemanticGraphPainter.paintBoxes(canvas, this.boxes)
        SemanticGraphPainter.paintEdges(canvas, this.edges, this.padWidth, this.renderAsCurves)
    }

    /**
     * Get rectangle for segment in text
     *
     * @param segment       target segment
     * @return rectangle
     */
    private fun TextView.modelToView(segment: Segment): Rect {

        val fromRectangle: Rect = modelToView(segment.from)
        val toRectangle: Rect = modelToView(segment.to)
        val left = fromRectangle.left.toInt()
        val top = fromRectangle.top.toInt()
        val right = toRectangle.right.toInt()
        val bottom = max(fromRectangle.bottom, toRectangle.bottom)
        return Rect(left, top, right, bottom)
    }

    private fun TextView.modelToView(pos: Int): Rect {
        if (pos < 0 || pos > text.length) {
            return throw IllegalArgumentException("Invalid position: $pos")
        }
        val metrics: FontMetrics = paint.fontMetrics
        val layout: Layout = layout
        val line: Int = layout.getLineForOffset(pos)
        val baseline: Int = layout.getLineBaseline(line)
        val top = baseline + metrics.ascent + paddingTop //layout.getLineTop(line)
        val bottom = baseline + metrics.descent + paddingTop // layout.getLineBottom(line)
        val x: Float = layout.getPrimaryHorizontal(pos)
        val width: Float = if (pos < text.length) {
            layout.getPrimaryHorizontal(pos + 1) - x
        } else {
            // Handle the end of the text.
            if (text.isNotEmpty()) {
                // Get the previous character position.
                val previousPos = pos - 1
                val previousX = layout.getPrimaryHorizontal(previousPos)
                x - previousX
            } else {
                // Handle empty text.
                0F
            }
        }
        val left = x.toInt() + paddingLeft
        val right = (left + width).toInt()
        return Rect(left, top.toInt(), right, bottom.toInt())
    }

    companion object {

        @JvmStatic
        fun FontMetrics.height(): Float {
            return descent - ascent + leading // precise line height information, and be sure you're including the inter-line spacing
            // return fontSpacing // a quick approximation of the line height.
        }

        // COLORS

        private const val DEFAULT_BACKCOLOR: Int = 0x80C6DBEE.toInt()

        // INSETS / MARGINS / OFFSETS

        private const val PAD_TOP_INSET = 4

        private const val PAD_BOTTOM_INSET = 2

        private const val EDGES_INSET_TOP = 10

        private const val EDGES_INSET_BOTTOM = 10

        private const val LABEL_TOP_INSET = 15

        private const val LABEL_BOTTOM_INSET = 15

        private const val LABEL_INFLATE = 1

        private const val X_MARGIN = 40

        private const val X_SHIFT = 3
    }
}