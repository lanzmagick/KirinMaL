package com.axiel7.moelist.screens.magic

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ShuffleBagTest {
    @Test
    fun eachBagContainsEveryUniqueTitleOnce() {
        val pool = (1..7).toList()
        val bag = ShuffleBag<Int>(id = { it }, random = Random(27))
        bag.replace(pool + pool)

        repeat(20) {
            val picks = List(pool.size) { bag.next() }
            assertEquals(pool.toSet(), picks.toSet())
            assertEquals(pool.size, picks.distinct().size)
        }
    }

    @Test
    fun refillingDoesNotRepeatThePreviousTitle() {
        val bag = ShuffleBag<Int>(id = { it }, random = Random(11))
        bag.replace(listOf(1, 2))
        var previous = bag.next()

        repeat(100) {
            val next = bag.next()
            assertNotEquals(previous, next)
            previous = next
        }
    }

    @Test
    fun replacingThePoolDropsOldTitlesAndAvoidsAnImmediateRepeat() {
        val bag = ShuffleBag<Int>(id = { it }, random = Random(8))
        bag.replace(listOf(1))
        assertEquals(1, bag.next())

        bag.replace(listOf(1, 2))
        assertEquals(2, bag.next())
        assertEquals(1, bag.next())

        bag.replace(listOf(3, 4))
        assertEquals(setOf(3, 4), setOf(bag.next(), bag.next()))
    }

    @Test
    fun deduplicatesByIdInsteadOfObjectEquality() {
        data class Title(val id: Int, val name: String)
        val bag = ShuffleBag<Title>(id = { it.id }, random = Random(2))
        bag.replace(listOf(Title(1, "First"), Title(1, "Duplicate"), Title(2, "Second")))

        val picks = List(2) { bag.next() }
        assertEquals(setOf(1, 2), picks.map { it?.id }.toSet())
        assertEquals("First", picks.first { it?.id == 1 }?.name)
    }

    @Test
    fun emptyPoolHasNoSelectionAndOneTitleCanRepeat() {
        val bag = ShuffleBag<Int>(id = { it })
        assertNull(bag.next())
        bag.replace(listOf(9))
        repeat(3) { assertEquals(9, bag.next()) }
        bag.replace(emptyList())
        assertNull(bag.next())
    }
}
