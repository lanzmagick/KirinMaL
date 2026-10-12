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
        pool = items.distinctBy(id)
        remaining.clear()
    }

    fun next(): T? {
        if (pool.isEmpty()) return null
        if (remaining.isEmpty()) {
            remaining.addAll(pool.shuffled(random))
            if (remaining.size > 1 && id(remaining.last()) == lastId) {
                val first = remaining[0]
                remaining[0] = remaining.last()
                remaining[remaining.lastIndex] = first
            }
        }
        return remaining.removeAt(remaining.lastIndex).also { lastId = id(it) }
    }
}
