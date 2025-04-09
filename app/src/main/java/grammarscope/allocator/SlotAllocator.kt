package grammarscope.allocator

import grammarscope.document.HasIndices

/**
 * Edge vertical slot allocator
 *
 * @author Bernard Bou
 */
class SlotAllocator<E : HasIndices> : SlotAllocatorForIndexRanges<E>() {
    /**
     * Allocate
     *
     * @param elements0  elements
     * @param comparator element allocation order
     */
    fun allocate(elements0: Collection<E>, comparator: Comparator<E>) {
        // sort elements
        val elements: MutableList<E> = ArrayList<E>(elements0)
        elements.sortWith(comparator)

        // allocate
        super.allocate(elements)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (entry in this.slots.entries) {
            val edge = entry.key
            sb.append("$edge index=(${edge.lowIndex}-${edge.highIndex}) = ${entry.value}\n")
        }
        return sb.toString()
    }
}
