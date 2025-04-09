package grammarscope.allocator

import java.util.Hashtable

/**
 * Vertical slot allocator for sequences of elements. Allocates height for consecutive elements so that sequences don't overlap.
 */
open class SlotAllocatorForSequences<T> {
    /**
     * Each element has one slot map attached to it. Each slot is represented by one bit in the slot map (0 to 63 slots)
     */
    private val slotMaps: MutableMap<T, Long> = Hashtable<T, Long>()

    /**
     * Allocate slot
     *
     * @param elements elements in range
     * @return allocated slot index
     */
    fun allocateSlot(elements: MutableList<T>): Int {
        // merge slot bitmaps for all elements in range into or synthetic bitmap
        val rangeSlotBitMap = mergeSlots(elements)

        // search slot bitmap for first free slot
        var mask: Long = 0
        var slot = 0
        while (slot < 64) {
            mask = 1L shl slot
            if ((rangeSlotBitMap and mask) == 0L) {
                break
            }
            slot++
        }

        // here we have slot index and its mask

        // allocate free slot, propagate 'taken' to each individual element
        for (element in elements) {
            // set slot bit to 1
            val slots = this.slotMaps[element]
            var bitMap = slots ?: 0
            bitMap = bitMap or mask
            this.slotMaps.put(element, bitMap)
        }
        return slot
    }

    /**
     * Maximum number of slots allocated
     */
    val maxSlot: Int
        get() {
            val bitMap = mergeAllSlots()

            // count allocated slots
            var count = 0
            for (slot in 0..63) {
                val mask = 1L shl slot
                if ((bitMap and mask) != 0L) {
                    count++
                }
            }
            return count
        }

    /**
     * Get merged slot bitmap (combine allocations for all elements into synthetic bitmap)
     *
     * @return slot bitmap (long representing slots with one bit, 64 max)
     */
    private fun mergeAllSlots(): Long {
        var mergedBitMap = 0L
        for (slots in this.slotMaps.values) {
            val bitMap = slots
            mergedBitMap = mergedBitMap or bitMap
        }
        return mergedBitMap
    }

    /**
     * Get merged slot bitmap (combine allocations for some elements into synthetic bitmap)
     *
     * @param elements elements in range
     * @return slot bitmap (long representing slots with one bit, 64 max)
     */
    private fun mergeSlots(elements: MutableList<T>): Long {
        var mergedBitmap = 0L
        for (element in elements) {
            val slots = this.slotMaps[element]
            val bitMap = slots ?: 0L
            mergedBitmap = mergedBitmap or bitMap
        }
        return mergedBitmap
    }
}
