package ru.nsu.ccfit.golovin.pds.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.nsu.ccfit.golovin.pds.PersistentList

internal class FullyPersistentListTest {
    private lateinit var emptyList: PersistentList<Int>
    private lateinit var singletonList: PersistentList<Int>
    private lateinit var persistentList: PersistentList<Int>

    @BeforeEach
    internal fun setUp() {
        emptyList = FullyPersistentList.empty()
        singletonList = FullyPersistentList.singleton(42)
        persistentList = FullyPersistentList.from(listOf(1, 2, 3, 4, 5))
    }

    @Test
    fun `test size`() {
        assertEquals(0, emptyList.size)

        assertEquals(1, singletonList.size)

        assertEquals(5, persistentList.size)
    }

    @Test
    fun `test version`() {
        assertEquals(0, emptyList.version)

        assertEquals(1, singletonList.version)
        assertEquals(2, singletonList.plus(43).version)
        assertEquals(1, singletonList.version)

        assertEquals(1, persistentList.version)
        assertEquals(2, persistentList.plus(listOf(6, 7, 8)).version)
        assertEquals(1, persistentList.version)
    }

    @Test
    fun `test plus one element`() {
        val first = emptyList.plus(1)
        assertEquals(1, first[0])
        assertEquals(1, first.size)
        assertEquals(1, first.version)

        val second = emptyList.plus(2)
        assertEquals(2, second[0])
        assertEquals(1, second.size)
        assertEquals(2, second.version)

        val third = first.plus(3)
        assertEquals(1, third[0])
        assertEquals(3, third[1])
        assertEquals(2, third.size)
        assertEquals(3, third.version)

        assertEquals(1, first[0])
        assertEquals(1, first.size)
        assertEquals(1, first.version)
    }

    @Test
    fun `test plus collection elements`() {
        val first = emptyList.plus(listOf(1, 2, 3))
        assertEquals(1, first[0])
        assertEquals(2, first[1])
        assertEquals(3, first[2])
        assertEquals(3, first.size)
        assertEquals(1, first.version)

        val second = emptyList.plus(listOf(4, 5, 6))
        assertEquals(4, second[0])
        assertEquals(5, second[1])
        assertEquals(6, second[2])
        assertEquals(3, second.size)
        assertEquals(2, second.version)

        val third = first.plus(listOf(4, 5, 6))
        assertEquals(1, third[0])
        assertEquals(2, third[1])
        assertEquals(3, third[2])
        assertEquals(4, third[3])
        assertEquals(5, third[4])
        assertEquals(6, third[5])
        assertEquals(6, third.size)
        assertEquals(3, third.version)

        assertEquals(1, first[0])
        assertEquals(2, first[1])
        assertEquals(3, first[2])
        assertEquals(3, first.size)
        assertEquals(1, first.version)
    }

    @Test
    fun `test minus one element`() {
        val first = emptyList.minus(0)
        assertEquals(0, first.size)
        assertEquals(1, first.version)

        val second = singletonList.minus(42)
        assertEquals(0, second.size)
        assertEquals(2, second.version)

        val third = persistentList.minus(3)
        assertFalse(third.contains(3))
        assertEquals(4, third.size)
        assertEquals(2, third.version)

        val fourth = persistentList.minus(10)
        assertEquals(5, fourth.size)
        assertEquals(3, fourth.version)

        val fifth = third.minus(1)
        assertFalse(fifth.contains(1))
        assertEquals(3, fifth.size)
        assertEquals(4, fifth.version)
    }

    @Test
    fun `test minus collection elements`() {
        val first = emptyList.minus(listOf(0, 1))
        assertEquals(0, first.size)
        assertEquals(1, first.version)

        val second = singletonList.minus(listOf(42))
        assertEquals(0, second.size)
        assertEquals(2, second.version)

        val third = singletonList.minus(listOf(42, 42, 43))
        assertEquals(0, third.size)
        assertEquals(3, third.version)

        val fourth = persistentList.minus(listOf(2, 3))
        assertFalse(fourth.contains(2))
        assertFalse(fourth.contains(3))
        assertEquals(3, fourth.size)
        assertEquals(2, fourth.version)

        val fifth = persistentList.minus(listOf(6, 7, 8))
        assertEquals(5, fifth.size)
        assertEquals(3, fifth.version)

        val sixth = fourth.minus(listOf(0, 1, 2, 5, 1, 5))
        assertFalse(sixth.contains(1))
        assertFalse(sixth.contains(5))
        assertEquals(1, sixth.size)
        assertEquals(4, sixth.version)
    }

    @Test
    fun `test plusAt one element`() {
        assertThrows(IndexOutOfBoundsException::class.java) { emptyList.plusAt(1, 1) }

        val first = emptyList.plusAt(0, 1)
        assertEquals(1, first[0])
        assertEquals(1, first.size)
        assertEquals(1, first.version)

        assertThrows(IndexOutOfBoundsException::class.java) { singletonList.plusAt(-1, 0) }

        val second = singletonList.plusAt(0, 0)
        assertEquals(0, second[0])
        assertEquals(42, second[1])
        assertEquals(2, second.size)
        assertEquals(2, second.version)

        assertThrows(IndexOutOfBoundsException::class.java) { persistentList.plusAt(6, 6) }

        val third = persistentList.plusAt(0, 0)
        assertEquals(0, third[0])
        assertEquals(1, third[1])
        assertEquals(2, third[2])
        assertEquals(3, third[3])
        assertEquals(4, third[4])
        assertEquals(5, third[5])
        assertEquals(6, third.size)
        assertEquals(2, third.version)

        val fourth = persistentList.plusAt(2, 6)
        assertEquals(6, fourth[2])
        assertEquals(3, fourth[3])
        assertEquals(4, fourth[4])
        assertEquals(5, fourth[5])
        assertEquals(6, fourth.size)
        assertEquals(3, fourth.version)
    }

    @Test
    fun `test plusAt collection elements`() {
        assertThrows(IndexOutOfBoundsException::class.java) { emptyList.plusAt(1, listOf(1)) }

        val first = emptyList.plusAt(0, listOf(1, 2))
        assertEquals(1, first[0])
        assertEquals(2, first[1])
        assertEquals(2, first.size)
        assertEquals(1, first.version)

        assertThrows(IndexOutOfBoundsException::class.java) { singletonList.plusAt(-1, listOf(0, 1)) }

        val second = singletonList.plusAt(0, listOf(0, 1))
        assertEquals(0, second[0])
        assertEquals(1, second[1])
        assertEquals(42, second[2])
        assertEquals(3, second.size)
        assertEquals(2, second.version)

        assertThrows(IndexOutOfBoundsException::class.java) { persistentList.plusAt(6, listOf(6, 7, 8)) }

        val third = persistentList.plusAt(0, listOf(-1, 0))
        assertEquals(-1, third[0])
        assertEquals(0, third[1])
        assertEquals(1, third[2])
        assertEquals(2, third[3])
        assertEquals(3, third[4])
        assertEquals(4, third[5])
        assertEquals(5, third[6])
        assertEquals(7, third.size)
        assertEquals(2, third.version)

        val fourth = persistentList.plusAt(2, listOf(6, 7, 8))
        assertEquals(6, fourth[2])
        assertEquals(7, fourth[3])
        assertEquals(8, fourth[4])
        assertEquals(3, fourth[5])
        assertEquals(4, fourth[6])
        assertEquals(5, fourth[7])
        assertEquals(8, fourth.size)
        assertEquals(3, fourth.version)
    }

    @Test
    fun `test minusAt`() {
        assertThrows(IndexOutOfBoundsException::class.java) { emptyList.minusAt(0) }

        assertThrows(IndexOutOfBoundsException::class.java) { singletonList.minusAt(1) }

        val second = singletonList.minusAt(0)
        assertEquals(0, second.size)
        assertEquals(2, second.version)

        val third = persistentList.minusAt(3)
        assertFalse(third.contains(4))
        assertEquals(4, third.size)
        assertEquals(2, third.version)

        assertThrows(IndexOutOfBoundsException::class.java) { persistentList.minusAt(10) }

        val fifth = third.minusAt(1)
        assertFalse(fifth.contains(2))
        assertEquals(3, fifth.size)
        assertEquals(3, fifth.version)
    }

    @Test
    fun `test with`() {
        assertThrows(IndexOutOfBoundsException::class.java) { emptyList.with(0, 1) }

        assertThrows(IndexOutOfBoundsException::class.java) { singletonList.with(-1, 2) }

        val first = singletonList.with(0, 1)
        assertEquals(1, first[0])
        assertEquals(1, first.size)
        assertEquals(2, first.version)

        assertThrows(IndexOutOfBoundsException::class.java) { singletonList.with(1, 2) }

        assertThrows(IndexOutOfBoundsException::class.java) { persistentList.with(-1, 0) }

        val second = persistentList.with(2, 7)
        assertEquals(7, second[2])
        assertEquals(5, second.size)
        assertEquals(2, second.version)

        val third = persistentList.with(0, 7)
        assertEquals(7, third[0])
        assertEquals(5, third.size)
        assertEquals(3, third.version)

        val fourth = persistentList.with(4, 7)
        assertEquals(7, fourth[4])
        assertEquals(5, fourth.size)
        assertEquals(4, fourth.version)

        assertThrows(IndexOutOfBoundsException::class.java) { singletonList.with(5, 6) }
    }

    @Test
    fun `test subList`() {
        val first = persistentList.subList(2, 4)
        assertEquals(3, first[0])
        assertEquals(4, first[1])
        assertEquals(2, first.size)
        assertEquals(2, first.version)

        val second = persistentList.subList(0, 3)
        assertEquals(1, second[0])
        assertEquals(2, second[1])
        assertEquals(3, second[2])
        assertEquals(3, second.size)
        assertEquals(3, second.version)

        val third = persistentList.subList(3, 5)
        assertEquals(4, third[0])
        assertEquals(5, third[1])
        assertEquals(2, third.size)
        assertEquals(4, third.version)

        val fourth = persistentList.subList(0, 5)
        assertEquals(5, fourth.size)
        assertEquals(5, fourth.version)

        val fifth = persistentList.subList(2, 2)
        assertEquals(0, fifth.size)
        assertEquals(6, fifth.version)

        assertThrows(IndexOutOfBoundsException::class.java) { persistentList.subList(-1, 2) }

        assertThrows(IndexOutOfBoundsException::class.java) { persistentList.subList(3, 6) }
    }

    @Test
    fun `test get`() {
        assertThrows(IndexOutOfBoundsException::class.java) { emptyList[0] }

        assertThrows(IndexOutOfBoundsException::class.java) { singletonList[-1] }
        assertEquals(42, singletonList[0])
        assertThrows(IndexOutOfBoundsException::class.java) { singletonList[1] }

        assertThrows(IndexOutOfBoundsException::class.java) { persistentList[-1] }
        assertEquals(1, persistentList[0])
        assertEquals(2, persistentList[1])
        assertEquals(3, persistentList[2])
        assertEquals(4, persistentList[3])
        assertEquals(5, persistentList[4])
        assertThrows(IndexOutOfBoundsException::class.java) { persistentList[5] }
    }

    @Test
    fun `test contains`() {
        assertFalse(emptyList.contains(0))

        assertTrue(singletonList.contains(42))
        assertFalse(singletonList.contains(43))

        assertTrue(persistentList.contains(1))
        assertTrue(persistentList.contains(2))
        assertTrue(persistentList.contains(3))
        assertTrue(persistentList.contains(4))
        assertTrue(persistentList.contains(5))
        assertFalse(singletonList.contains(0))
    }

    @Test
    fun `test containsAll`() {
        assertFalse(emptyList.containsAll(listOf(0)))

        assertTrue(singletonList.containsAll(listOf(42)))
        assertFalse(singletonList.containsAll(listOf(42, 43)))

        assertTrue(persistentList.containsAll(listOf(1)))
        assertTrue(persistentList.containsAll(listOf(2, 4)))
        assertTrue(persistentList.containsAll(listOf(4, 5)))
        assertTrue(persistentList.containsAll(listOf(1, 2, 3)))
        assertTrue(persistentList.containsAll(listOf(1, 2, 3, 4, 5)))
        assertFalse(persistentList.containsAll(listOf(0)))
        assertFalse(persistentList.containsAll(listOf(0, 1, 2, 3, 4, 5)))
        assertFalse(persistentList.containsAll(listOf(0, 2, 4, 5)))
    }

    @Test
    fun `test isEmpty`() {
        assertTrue(emptyList.isEmpty())

        assertFalse(singletonList.isEmpty())

        assertFalse(persistentList.isEmpty())
    }

    @Test
    fun `test iterator`() {
        assertFalse(emptyList.iterator().hasNext())

        singletonList.iterator().forEach {
            assertEquals(42, it)
        }

        val persistentArrayIterator = persistentList.iterator()
        repeat(persistentList.size) {
            assertEquals(it + 1, persistentArrayIterator.next())
        }
        assertFalse(persistentArrayIterator.hasNext())
    }

    @Test
    fun `test toString`() {
        assertEquals("[]", emptyList.toString())

        assertEquals("[42]", singletonList.toString())

        assertEquals("[1, 2, 3, 4, 5]", persistentList.toString())
    }

    @Test
    fun `test all`() {
        val first = emptyList.plus(1)
        assertEquals(1, first[0])
        assertEquals(1, first.size)
        assertEquals(1, first.version)

        val second = first.plus(listOf(2, 3, 4, 5))
        assertEquals(2, second[1])
        assertEquals(3, second[2])
        assertEquals(4, second[3])
        assertEquals(5, second[4])
        assertEquals(5, second.size)
        assertEquals(2, second.version)

        val third = second.minus(3)
        assertEquals(4, third[2])
        assertEquals(4, third.size)
        assertEquals(3, third.version)

        val fourth = third.minus(listOf(1, 5))
        assertEquals(2, fourth[0])
        assertEquals(4, fourth[1])
        assertEquals(2, fourth.size)
        assertEquals(4, fourth.version)

        val fifth = third.with(2, 3)
        assertEquals(3, fifth[2])
        assertEquals(4, fifth.size)
        assertEquals(5, fifth.version)

        val sixth = third.subList(2, 4)
        assertEquals(4, sixth[0])
        assertEquals(5, sixth[1])
        assertEquals(2, sixth.size)
        assertEquals(6, sixth.version)

        val seventh = third.plusAt(2, 3)
        assertEquals(3, seventh[2])
        assertEquals(5, seventh.size)
        assertEquals(7, seventh.version)

        val eighth = seventh.minusAt(1)
        assertEquals(3, eighth[1])
        assertEquals(4, eighth.size)
        assertEquals(8, eighth.version)

        val ninth = seventh.plusAt(1, listOf(6, 7))
        assertEquals(1, ninth[0])
        assertEquals(6, ninth[1])
        assertEquals(7, ninth[2])
        assertEquals(2, ninth[3])
        assertEquals(3, ninth[4])
        assertEquals(4, ninth[5])
        assertEquals(5, ninth[6])
        assertEquals(7, ninth.size)
        assertEquals(9, ninth.version)
    }
}