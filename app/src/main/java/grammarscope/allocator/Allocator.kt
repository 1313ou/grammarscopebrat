package grammarscope.allocator

import grammarscope.document.GraphEdge
import grammarscope.document.GraphNode
import kotlin.math.abs

/**
 * Slot and anchor allocator
 *
 * @author Bernard Bou
 * @see SlotAllocator
 *
 * @see AnchorAllocator
 */
class Allocator(nodes: Collection<GraphNode>, edges: Collection<GraphEdge>) {
    /**
     * Slot allocator
     */
    private val slotAllocator = SlotAllocator<GraphEdge>()

    /**
     * Anchor allocator
     */
    private val anchorAllocator = AnchorAllocator()

    /**
     * Edge comparator based on edge's low index
     */
    private val leftComparator = Comparator { edge1: GraphEdge, edge2: GraphEdge ->
        val compare = edge1.lowIndex.compareTo(edge2.lowIndex)
        if (compare != 0)
            return@Comparator compare
        -1 * slotAllocator.getSlot(edge1).compareTo(slotAllocator.getSlot(edge2))
    }

    /**
     * Edge comparator based on edge's high index
     */
    private val rightComparator = Comparator { edge1: GraphEdge, edge2: GraphEdge ->
        val compare = -1 * edge1.highIndex.compareTo(edge2.highIndex)
        if (compare != 0) return@Comparator compare
        -1 * this@Allocator.slotAllocator.getSlot(edge1).compareTo(this@Allocator.slotAllocator.getSlot(edge2))
    }

    /**
     * Constructor
     *
     * @param nodes nodes
     * @param edges edges
     */
    init {
        // Edge comparator based on edge's span
        val slotComparator: Comparator<GraphEdge> = object : Comparator<GraphEdge> {
            override fun compare(edge1: GraphEdge, edge2: GraphEdge): Int {
                val span1 = getSpan(edge1)
                val span2 = getSpan(edge2)
                if (span1 > span2) return 1
                else if (span1 < span2) return -1
                return 0
            }

            /**
             * Compute span (as bound by node indices)
             *
             * @return span
             */
            private fun getSpan(edge: GraphEdge): Int {
                return abs((edge.target.index - edge.source.index).toDouble()).toInt()
            }
        }
        // slot allocator
        slotAllocator.allocate(edges, slotComparator)

        // anchor allocator
        anchorAllocator.allocate(nodes, edges, leftComparator, rightComparator)
    }

    /**
     * Get edge's slot
     *
     * @param edge edge
     * @return slot
     */
    fun getSlot(edge: GraphEdge): Int {
        return slotAllocator.getSlot(edge)
    }

    /**
     * Get edge's left anchor
     *
     * @param edge edge
     * @return left anchor
     */
    fun getLeftAnchor(edge: GraphEdge): Float {
        return anchorAllocator.getLeftAnchor(edge)
    }

    /**
     * Get edge's right anchor
     *
     * @param edge edge
     * @return right anchor
     */
    fun getRightAnchor(edge: GraphEdge): Float {
        return anchorAllocator.getRightAnchor(edge)
    }


    override fun toString(): String {
        return "SLOTS\n" + slotAllocator + "ANCHORS\n" + anchorAllocator
    }
}
