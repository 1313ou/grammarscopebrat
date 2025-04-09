package grammarscope.document

import grammarscope.Segment

class GNode(
    override val index: Int,
    val text: String,
    override val segment: Segment,
) : GraphNode

class GEdge(
    override val source: GraphNode,
    override val target: GraphNode,
    override val label: String,
    override val index: Int,
) : GraphEdge {
    override val lowIndex: Int = minOf(source.index, target.index)
    override val highIndex: Int= maxOf(source.index, target.index)
}

class GGraph(
    override val nodes: Collection<GraphNode>,
    override val edges: Collection<GraphEdge>
) : Graph<GraphNode, GraphEdge>

class SampleDocument : Document {

    override fun getGraph(sentenceIdx: Int): Graph<GraphNode, GraphEdge> {
        val root = GNode(0, "ROOT", Segment(0, 0))
        val bob = GNode(1, "Bob", Segment(0, 3))
        val gave = GNode(2, "gave", Segment(4, 8))
        val alice = GNode(3, "Alice", Segment(9, 14))
        val the = GNode(4, "the", Segment(15, 18))
        val car = GNode(5, "car", Segment(19, 22))
        val that = GNode(6, "that", Segment(23, 27))
        val she = GNode(7, "she", Segment(28, 31))
        val drives = GNode(8, "drives", Segment(32, 37))
        val stop = GNode(9, ".", Segment(39, 39))
        val nodes = listOf(root, bob, gave, alice, the, car, that, she, drives, stop)

        val rootEdge = GEdge(gave, root, "root", 0)
        val nsubjEdge = GEdge(bob, gave, "nsubj", 1)
        val iobjEdge = GEdge(alice, gave, "iobj", 2)
        val dobjEdge = GEdge(car, gave, "dobj", 3)
        val detEdge = GEdge(the, car, "det", 4)
        val ccompEdge = GEdge(drives, gave, "ccomp", 5)
        val nsubj2Edg = GEdge(she, drives, "nsubj2", 7)
        val markEdge = GEdge(that, drives, "mark", 8)
        val puncEdge = GEdge(stop, gave, "punc", 9)
        val edges = listOf(rootEdge,nsubjEdge, iobjEdge, dobjEdge, detEdge, ccompEdge, nsubj2Edg, markEdge, puncEdge)

        return GGraph(nodes,edges)
    }

    override fun split(segment1: Segment, segment2: Segment): MutableList<Segment> {
        TODO("Not yet implemented")
    }

    override val sentenceCount: Int
        get() = 1
}