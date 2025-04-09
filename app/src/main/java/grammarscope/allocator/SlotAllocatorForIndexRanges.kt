package grammarscope.allocator

import grammarscope.document.HasIndices
import java.util.Hashtable

/**
 * Vertical slot allocator for indexed ranges (each having low and index)
 */
open class SlotAllocatorForIndexRanges<T : HasIndices> : SlotAllocatorForSequences<Int>() {
    /**
     * Each element has one slot map attached to it. Each slot is represented by one bit in the slot map (0 to 63 slots)
     */
    @JvmField
	protected val slots: MutableMap<T, Int> = Hashtable<T, Int>()

    /**
     * Allocate slots and cache them
     *
     * @param elements elements in range
     */
    fun allocate(elements: MutableCollection<T>) {
        // iterate on elements and allocate
        for (element in elements) {
            allocateSlot(element)
        }
    }

    /**
     * Get element's slot from cache
     *
     * @param element element to get slot of
     * @return this element's slot
     */
    fun getSlot(element: T): Int {
        return this.slots[element]!!
    }

    /**
     * Allocate slot
     *
     * @param element element
     */
    private fun allocateSlot(element: T) {
        val low = element.lowIndex
        val hi = element.highIndex

        // each range is represented by sequence [low, low+1, ..., hi-1]
        val elements: MutableList<Int> = ArrayList<Int>()
        for (i in low..<hi) {
            elements.add(i)
        }

        val slot = super.allocateSlot(elements)
        this.slots.put(element, slot)
    }
}
