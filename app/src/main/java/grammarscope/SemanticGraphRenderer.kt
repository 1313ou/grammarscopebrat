package grammarscope

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.widget.TextView
import grammarscope.Utils.height
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
    textView: TextView,
    val renderAsCurves: Boolean
) : ProtoRenderer() {

    /**
     * Pad width
     */
    private var padWidth = 0

    /**
     * Metrics for tag
     */
    private val tagMetrics: Paint.FontMetrics = textView.paint.fontMetrics

    /**
     * Relation palette
     */
    private lateinit var relationPalette: (GraphEdge) -> Int

    /**
     * Back color
     */
    override val backColor: Int = DEFAULT_BACKCOLOR

    // DATA

    /**
     * Edges to draw
     */
    private val edges: MutableCollection<Edge> = ArrayList<Edge>()

    /**
     * Boxes to draw
     */
    private val boxes: MutableCollection<RectF> = ArrayList<RectF>()


    // C O N S T R U C T

    // L A Y O U T

    /* label inset topOffset ^ */
    /* label inflate topOffset ^ ----------- */
    /* font tagFontHeight tagHeight tagSpace | tag | */
    /* label inflate bottom v ----------- */
    /* label inset bottom v */
    /* ---- pad topOffset = text bottom */
    /* pad topOffset inset */
    /* pad topOffset inset 2 */
    /* tag 1 */
    /* ---- first edge base */
    /* ---- last edge base */
    /* pad bottom inset 2 */
    /* ---- pad height = pad bottom */
    override fun layout(
        document: Document,
        textComponent: TextView,
        padWidth: Int,
        padTopOffset: Int,
        padHeight: Int,
        lineHeight: Int
    ): Int {
        if (document.sentenceCount == 0) return 0

        // store pad width
        this.padWidth = padWidth

        // space height
        val tagFontHeight: Float = this.tagMetrics.height()
        val tagHeight: Float = tagFontHeight + 2 * LABEL_INFLATE
        val tagSpace: Float = tagHeight + LABEL_TOP_INSET + LABEL_BOTTOM_INSET

        // where edges start (slot 0)
        val firstEdgeBase: Float = PAD_TOP_INSET + EDGES_INSET_TOP + tagSpace // relative to pad topOffset
        if (firstEdgeBase > padHeight) return 0
        val lastEdgeBase: Int = padHeight - PAD_BOTTOM_INSET - EDGES_INSET_BOTTOM // relative to pad

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

                val boxL = rectangle.left
                val boxT: Int = rectangle.top + lineHeight + padTopOffset + PAD_TOP_INSET
                val boxH: Int = padHeight - PAD_TOP_INSET
                val boxW = rectangle.width()
                val box = RectF(boxL.toFloat(), boxT.toFloat(), boxW.toFloat(), boxH.toFloat())
                this.boxes.add(box)
            }

            // EDGES

            // edge list
            val edges: Collection<GraphEdge> = graph.edges

            // height and anchor allocator
            val allocator = Allocator(graph.nodes, graph.edges)

            // System.out.println(allocator);

            // build edges
            for (edge in edges) {
                // segment
                val fromWord = edge.source.segment
                val toWord = edge.target.segment
                val isBackwards = fromWord.from > toWord.from

                val leftSegment = if (isBackwards) toWord else fromWord
                val rightSegment = if (isBackwards) fromWord else toWord

                // System.out.println("edge " + edge + " @" + leftSegment + "-" + rightSegment);

                // location
                val leftRectangle: Rect = textComponent.modelToView(leftSegment)
                val rightRectangle: Rect = textComponent.modelToView(rightSegment)

                // data
                val label: String? = edge.label
                val color: Int = relationPalette.invoke(edge)
                val slot = allocator.getSlot(edge)
                val edgeYOffset = slot * tagSpace // relative to first slot
                val isVisible = firstEdgeBase + edgeYOffset < lastEdgeBase

                // if it fits on one line
                if (leftRectangle.top == rightRectangle.top) {
                    // compute edge
                    val xEdge1 = leftRectangle.left + leftRectangle.width() / 2
                    val xEdge2 = rightRectangle.left + rightRectangle.width() / 2
                    val yEdge = leftRectangle.top + lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                    val xAnchor1 = (allocator.getLeftAnchor(edge) * X_SHIFT).toInt()
                    val xAnchor2 = (allocator.getRightAnchor(edge) * X_SHIFT).toInt()
                    val yAnchor: Int = leftRectangle.top + lineHeight + padTopOffset + PAD_TOP_INSET
                    val bottom = leftRectangle.top + lineHeight + padTopOffset + padHeight

                    val e: Edge = Edge.Companion.makeEdge(xEdge1, xEdge2, yEdge.toInt(), xAnchor1, xAnchor2, yAnchor, tagHeight.toInt(), label, color, isBackwards, true, true, bottom, isVisible)
                    this.edges.add(e)

                } else {
                    // edge does not fit on line : make one edge per line

                    // get segments in span

                    val segments: MutableList<Segment> = document.split(leftSegment, rightSegment)
                    if (segments.isEmpty()) {
                        continue
                    }

                    // for(Segment segment :segments) System.out.println(" segment=" + document.getString(segment));
                    val lastSegmentIndex = segments.size // last

                    // cursor
                    var xRight = leftRectangle.left + leftRectangle.width()
                    //int xRightOfs = leftRectangle.width / 2;
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
                            val xEdge2: Int = xRight + X_MARGIN
                            val yEdge = y + lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                            val xAnchor1 = (allocator.getLeftAnchor(edge) * X_SHIFT).toInt()
                            val xAnchor2 = (allocator.getRightAnchor(edge) * X_SHIFT).toInt()
                            val yAnchor: Int = y + lineHeight + padTopOffset + PAD_TOP_INSET
                            val bottom = y + lineHeight + padTopOffset + padHeight

                            val e: Edge = Edge.Companion.makeEdge(xEdge1, xEdge2, yEdge.toInt(), xAnchor1, xAnchor2, yAnchor, tagHeight.toInt(), label, color, isBackwards, isFirst, false, bottom, isVisible)
                            this.edges.add(e)

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
                            val xAnchor1 = (allocator.getLeftAnchor(edge) * X_SHIFT).toInt()
                            val xAnchor2 = (allocator.getRightAnchor(edge) * X_SHIFT).toInt()
                            val yAnchor: Int = y + lineHeight + padTopOffset + PAD_TOP_INSET
                            val bottom = y + lineHeight + padTopOffset + padHeight

                            val e: Edge = Edge.Companion.makeEdge(xEdge1, xEdge2, yEdge.toInt(), xAnchor1, xAnchor2, yAnchor, tagHeight.toInt(), label, color, isBackwards, isFirst, true, bottom, isVisible)
                            this.edges.add(e)
                        }
                    }
                }
            }
        }
        this.height = padHeight
        this.topOffset = padTopOffset
        return this.height
    }

    override fun paint(canvas: Canvas) {
        SemanticGraphPainter.paint(canvas, this.edges, this.boxes, this.padWidth, this.renderAsCurves)
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
        val x = fromRectangle.left.toInt()
        val y = fromRectangle.top.toInt()
        val w = toRectangle.left.toInt() - x
        val h = max(fromRectangle.height(), toRectangle.height()).toInt()
        return Rect(x, y, w, h)
    }

    private fun TextView.modelToView(pos: Int): Rect {
        if (pos < 0 || pos > text.length) {
            return throw IllegalArgumentException("Invalid position: $pos")
        }
        val layout: Layout = layout
        val line: Int = layout.getLineForOffset(pos)
        val x: Float = layout.getPrimaryHorizontal(pos)
        val top: Int = layout.getLineTop(line)
        val bottom: Int = layout.getLineBottom(line)
        val width: Float = if (pos < text.length) {
            layout.getPrimaryHorizontal(pos + 1) - x
        } else {
            // handle the end of the text.
            if (pos > 0) layout.getPrimaryHorizontal(pos) - layout.getPrimaryHorizontal(pos - 1) else 0F
        }
        val left = x.toInt()
        val right = (x + width).toInt()
        return Rect(left, top, right, bottom)
    }


    companion object {

        // COLORS

        private const val DEFAULT_BACKCOLOR: Int = 0x80C6DBEE.toInt()

        // INSETS / MARGINS / OFFSETS

        private const val PAD_TOP_INSET = 4

        private const val PAD_BOTTOM_INSET = 2

        private const val EDGES_INSET_TOP = 10

        private const val EDGES_INSET_BOTTOM = 0

        private const val LABEL_TOP_INSET = 1

        private const val LABEL_BOTTOM_INSET = 1

        private const val LABEL_INFLATE = 1

        private const val X_MARGIN = 40

        private const val X_SHIFT = 3
    }
}