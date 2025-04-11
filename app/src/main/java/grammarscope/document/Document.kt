package grammarscope.document

import grammarscope.Segment

interface IsVertex

interface HasIndex{
    val index: Int
}

interface HasIndices {
    val lowIndex: Int
    val highIndex: Int
}

/**
 * Indexed vertex
 *
 * @param <E> type of incoming/outgoing edges
 * @author Bernard Bou
 */
interface IsIndexedVertex : HasIndex, IsVertex

interface Graph<N, E> {
    val nodes: Collection<N>
    val edges: Collection<E>
}

interface GraphNode : IsIndexedVertex {
    val segment: Segment
}

interface GraphEdge : HasIndex, HasIndices {
    val source: GraphNode
    val target: GraphNode
    val label: String?
}

/**
 * Document interface
 *
 * @author Bernard Bou
 */
interface Document {
    val text: String

    val sentenceCount: Int

    fun getGraph(sentenceIdx: Int): Graph<GraphNode, GraphEdge>

    fun split(segment1: Segment, segment2: Segment): MutableList<Segment>
}
