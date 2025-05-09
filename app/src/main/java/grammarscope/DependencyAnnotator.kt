package grammarscope

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.widget.TextView
import com.bbou.brats.Annotation
import com.bbou.brats.Annotation.BoxAnnotation
import com.bbou.brats.Annotation.EdgeAnnotation
import com.bbou.brats.AnnotationType
import com.bbou.brats.modelToViewF
import grammarscope.Edge.Companion.makeEdge
import grammarscope.Utils.height
import grammarscope.allocator.Allocator
import grammarscope.document.Document
import grammarscope.document.Graph
import grammarscope.document.GraphEdge
import grammarscope.document.GraphNode
import kotlin.random.Random

/**
 * Semantic graph renderer
 *
 * @param textView textView
 *
 * @author Bernard Bou
 */
class DependencyAnnotator(
    val textView: TextView,
) {

    /**
     * Height
     */
    private val height: Int = textView.height - textView.paddingTop - textView.paddingBottom

    /**
     * Metrics for tag
     */
    private val lineHeight: Float = textView.paint.fontMetrics.height()

    /**
     * Top
     */
    private val padTopOffset: Int = 35

    /**
     * Annotation pad width
     */
    private val padHeight = textView.lineSpacingExtra

    /**
     * Paint for tag
     */
    private val tagPaint: Paint = Paint().apply { textSize = Edge.tagTextSize }

    /**
     * Relation palette
     */
    private var relationPalette: (label: String?) -> Int = { e -> Color.DKGRAY }
    private val random = Random.Default
    private var relationPalette2: (label: String?) -> Int = {
        Color.argb(0x40, random.nextInt(255), random.nextInt(255), random.nextInt(255))
    }

    fun annotate(
        document: Document,
    ): Pair<Map<AnnotationType, Collection<Annotation>>, Int>? {
        if (document.sentenceCount == 0) return null

        // dumpLines()
        val edges: MutableCollection<EdgeAnnotation> = ArrayList()
        val boxes: MutableCollection<BoxAnnotation> = ArrayList()

        // space height
        val tagFontHeight: Float = tagPaint.fontMetrics.height()
        val tagHeight: Float = tagFontHeight + 2 * LABEL_INFLATE
        val tagSpace: Float = tagHeight + LABEL_TOP_INSET + LABEL_BOTTOM_INSET

        // where edges start (slot 0)
        val firstEdgeBase: Float = PAD_TOP_INSET + EDGES_INSET_TOP + tagSpace // relative to pad topOffset
        if (firstEdgeBase > padHeight) return null
        val lastEdgeBase: Float = padHeight - PAD_BOTTOM_INSET - EDGES_INSET_BOTTOM // relative to pad

        // topOffset

        // iterate over sentences
        val n: Int = document.sentenceCount
        for (sentenceIdx in 0..<n) {
            val graph: Graph<GraphNode, GraphEdge> = document.getGraph(sentenceIdx)

            // NODES
            for (node in graph.nodes) {
                val color: Int = relationPalette2.invoke("")

                // location
                val segment = node.segment
                val wordBox = textView.modelToViewF(segment)
                boxes.add(BoxAnnotation(wordBox, color, true))

                val annotationTop = wordBox.top + lineHeight
                val annotationBottom = annotationTop + padHeight
                val annotationBox = RectF(wordBox.left, annotationTop + padTopOffset + PAD_TOP_INSET, wordBox.right, annotationBottom - PAD_BOTTOM_INSET)
                boxes.add(BoxAnnotation(annotationBox, color))
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
                val leftRectangle = textView.modelToViewF(leftSegment)
                val rightRectangle = textView.modelToViewF(rightSegment)

                // data
                val label: String? = gEdge.label
                val color: Int = relationPalette.invoke(gEdge.label)
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
                    edges.add(EdgeAnnotation(edge))

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
                        val textBox = textView.modelToViewF(segment)

                        // if line skipped (the current segment is on the current line)
                        if (textBox.top != y) {
                            // line break before this segment
                            val xEdge1 = xLeft + xLeftOfs - (if (isFirst) 0 else X_MARGIN)
                            val xEdge2 = xRight + X_MARGIN
                            val yEdge = y + lineHeight + padTopOffset + firstEdgeBase + edgeYOffset
                            val xAnchor1 = (allocator.getLeftAnchor(gEdge) * X_SHIFT)
                            val xAnchor2 = (allocator.getRightAnchor(gEdge) * X_SHIFT)
                            val yAnchor: Float = y + lineHeight + padTopOffset + PAD_TOP_INSET
                            val bottom = y + lineHeight + padHeight

                            val edge: Edge = makeEdge(xEdge1, xEdge2, yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, bottom, label, isBackwards, isLeftTerminal = isFirst, isRightTerminal = false, isVisible, color, tagPaint = tagPaint)
                            edges.add(EdgeAnnotation(edge))

                            // move ahead cursor1
                            val rectangle2 = textView.modelToViewF(segment.from)
                            xLeft = rectangle2.left
                            xLeftOfs = rectangle2.width() / 2
                            y = rectangle2.top
                            isFirst = false
                        }

                        // move ahead cursor2
                        xRight = textBox.left + textBox.width()
                        val xRightOfs = textBox.width() / 2

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

                            val edge: Edge = makeEdge(xEdge1, xEdge2, yEdge, xAnchor1, xAnchor2, yAnchor, tagHeight, bottom, label, isBackwards, isLeftTerminal = isFirst, isRightTerminal = true, isVisible, color, tagPaint = tagPaint)
                            edges.add(EdgeAnnotation(edge))
                        }
                    }
                }
            }
            break
        }
        return mapOf(AnnotationType.EDGE to edges, AnnotationType.BOX to boxes) to this.height
    }

    private fun dumpLines() {
        for (line in 0..textView.layout.lineCount) {
            var r = Rect()
            textView.layout.getLineBounds(line, r)
            println("$line $r")
        }
    }

    companion object {

         // INSETS / MARGINS / OFFSETS

        private const val PAD_TOP_INSET = 4

        private const val PAD_BOTTOM_INSET = 2

        private const val EDGES_INSET_TOP = 10

        private const val EDGES_INSET_BOTTOM = 10

        private const val LABEL_TOP_INSET = 15

        private const val LABEL_BOTTOM_INSET = 15

        private const val LABEL_INFLATE = 1

        private const val X_MARGIN = 40

        private const val X_SHIFT = 20
    }
}