package com.axiel7.moelist.screens.magic

import kotlin.random.Random

/** Each unique item appears once per bag; avoid an immediate repeat at rollover. */
class ShuffleBag<T>(
    private val id: (T) -> Int,
    private val random: Random = Random.Default,
) {
    private var pool: List<T> = emptyList()
    private val remaining = mutableListOf<T>()
    private var lastId: Int? = null

    fun replace(items: List<T>) {
        // Keep one copy of each media ID and discard the previous cycle when the source pool changes.
        pool = items.distinctBy(id)
        remaining.clear()
    }

    fun next(): T? {
        if (pool.isEmpty()) return null
        if (remaining.isEmpty()) {
            // Refill only after every item in the previous shuffled cycle has been returned.
            remaining.addAll(pool.shuffled(random))
            // Avoid repeating the previous cycle's final item as the next cycle's first result.
            if (remaining.size > 1 && id(remaining.last()) == lastId) {
                val first = remaining[0]
                remaining[0] = remaining.last()
                remaining[remaining.lastIndex] = first
            }
        }
        // Remember the selected ID so the next refill can prevent an immediate boundary repeat.
        return remaining.removeAt(remaining.lastIndex).also { lastId = id(it) }
    }
}
