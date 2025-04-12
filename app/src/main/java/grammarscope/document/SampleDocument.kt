package grammarscope.document

import grammarscope.Segment

data class Word(
    val text: String,
    val segment: Segment,
)

data class GNode(
    override val index: Int,
    val word: Word,
) : GraphNode {
    val text: String
        get() = word.text
    override val segment: Segment
        get() = word.segment
}

data class GEdge(
    override val source: GraphNode,
    override val target: GraphNode,
    override val label: String,
    override val index: Int,
) : GraphEdge {
    override val lowIndex: Int = minOf(source.index, target.index)
    override val highIndex: Int = maxOf(source.index, target.index)
}

class GGraph(
    override val nodes: Collection<GraphNode>,
    override val edges: Collection<GraphEdge>
) : Graph<GraphNode, GraphEdge>

class SampleDocument : Document {

    override val text: String = "Bob gave Alice the car that she drives .\n"

    // val rootW = Word("", Segment(0, 0))
    val bobW = Word("Bob", Segment(0, 2))
    val gaveW = Word("gave", Segment(4, 7))
    val aliceW = Word("Alice", Segment(9, 13))
    val theW = Word("the", Segment(15, 17))
    val carW = Word("car", Segment(19, 21))
    val thatW = Word("that", Segment(23, 26))
    val sheW = Word("she", Segment(28, 30))
    val drivesW = Word("drives", Segment(32, 37))
    val stopW = Word(".", Segment(39, 39))
    val words = listOf(/*rootW,*/ bobW, gaveW, aliceW, theW, carW, thatW, sheW, drivesW, stopW)

    override val sentenceCount: Int
        get() = 1

    override fun getGraph(sentenceIdx: Int): Graph<GraphNode, GraphEdge> {

        //val root = GNode(0, rootW)
        val bob = GNode(0, bobW)
        val gave = GNode(1, gaveW)
        val alice = GNode(2, aliceW)
        val the = GNode(3, theW)
        val car = GNode(4, carW)
        val that = GNode(5, thatW)
        val she = GNode(6, sheW)
        val drives = GNode(7, drivesW)
        val stop = GNode(8, stopW)
        val nodes = listOf(/*root,*/ bob, gave, alice, the, car, that, she, drives, stop)

        // val rootEdge = GEdge(gave, root, "root", 0)
        //val nsubjEdge = GEdge(bob, gave, "nsubj", 0)
        //val iobjEdge = GEdge(alice, gave, "iobj", 1)
        //val dobjEdge = GEdge(car, gave, "dobj", 2)
        //val detEdge = GEdge(the, car, "det", 3)
        //val ccompEdge = GEdge(drives, gave, "ccomp", 4)
        //val nsubj2Edg = GEdge(she, drives, "nsubj2", 5)
        //val markEdge = GEdge(that, drives, "mark", 6)
        //val puncEdge = GEdge(stop, gave, "punc", 7)

        val nsubjEdge = GEdge(gave, bob, "nsubj", 0)
        val iobjEdge = GEdge(gave, alice, "iobj", 1)
        val dobjEdge = GEdge(gave, car, "dobj", 2)
        val detEdge = GEdge(car, the, "det", 3)
        val ccompEdge = GEdge(gave, drives, "ccomp", 4)
        val nsubj2Edg = GEdge(drives, she, "nsubj2", 5)
        val markEdge = GEdge(drives, that, "mark", 6)
        val puncEdge = GEdge(gave, stop, "punc", 7)
        val edges = listOf( /*rootEdge,*/ nsubjEdge, iobjEdge, dobjEdge, detEdge, ccompEdge, nsubj2Edg, markEdge, puncEdge)

        return GGraph(nodes, edges)
    }

    // S E G M E N T   L I S T S

    /**
     * Make intermediate segment list
     *
     * @param leftSegment  start segment
     * @param rightSegment finish segment
     * @return list of segments
     */
    override fun split(leftSegment: Segment, rightSegment: Segment): MutableList<Segment> {
        val segment = merge(leftSegment, rightSegment)
        return split(segment)
    }

    /**
     * Split segment into word segments
     *
     * @param segment segment
     * @return list of word segments
     */
    fun split(segment: Segment): MutableList<Segment> {
        val list: MutableList<Segment> = ArrayList<Segment>()
        for (word in this.words) {
            val l = word.segment.from.compareTo(segment.from)
            val r = word.segment.to.compareTo(segment.to)
            if (l >= 0 && r <= 0) {
                list.add(word.segment)
            }
        }
        return list
    }

    /**
     * Merge segments to segment
     *
     * @param segments segments
     * @return segment
     */
    fun merge(segments: List<Segment>): Segment {
        return Segment.merge(segments)
    }

    /**
     * Merge segments to segment
     *
     * @param segments segments
     * @return segment
     */
    fun merge(vararg segments: Segment): Segment {
        return Segment.merge(*segments)
    }
}