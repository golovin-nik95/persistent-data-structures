package ru.nsu.ccfit.golovin.pds.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.nsu.ccfit.golovin.pds.PersistentMap

internal class FullyPersistentMapTest {
    private lateinit var emptyMap: PersistentMap<Int, String>
    private lateinit var singletonMap: PersistentMap<Int, String>
    private lateinit var persistentMap: PersistentMap<Int, String>

    @BeforeEach
    internal fun setUp() {
        emptyMap = FullyPersistentMap.empty()
        singletonMap = FullyPersistentMap.singleton(42 to "42")
        persistentMap = FullyPersistentMap.from(mapOf(1 to "1", 2 to "2", 3 to "3", 4 to "4", 5 to "5"))
    }

    @Test
    fun `test size`() {
        assertEquals(0, emptyMap.size)

        assertEquals(1, singletonMap.size)

        assertEquals(5, persistentMap.size)
    }

    @Test
    fun `test version`() {
        assertEquals(0, emptyMap.version)

        assertEquals(1, singletonMap.version)

        assertEquals(1, persistentMap.version)
    }

    @Test
    fun `test plus one pair`() {
        val first = emptyMap.plus(1 to "1")
        assertEquals("1", first[1])
        assertEquals(1, first.size)
        assertEquals(1, first.version)

        val second = emptyMap.plus(1 to "2")
        assertEquals("2", second[1])
        assertEquals(1, second.size)
        assertEquals(2, second.version)

        val third = first.plus(3 to "3")
        assertEquals("1", third[1])
        assertEquals("3", third[3])
        assertEquals(2, third.size)
        assertEquals(3, third.version)

        assertEquals("1", first[1])
        assertEquals(1, first.size)
        assertEquals(1, first.version)

        val fourth = third.plus(3 to "5")
        assertEquals("5", fourth[3])
        assertEquals(2, fourth.size)
        assertEquals(4, fourth.version)
    }

    @Test
    fun `test plus collection pairs`() {
        val first = emptyMap.plus(mapOf(1 to "1", 2 to "2", 3 to "3"))
        assertEquals("1", first[1])
        assertEquals("2", first[2])
        assertEquals("3", first[3])
        assertEquals(3, first.size)
        assertEquals(1, first.version)

        val second = emptyMap.plus(mapOf(1 to "4", 2 to "5", 3 to "6"))
        assertEquals("4", second[1])
        assertEquals("5", second[2])
        assertEquals("6", second[3])
        assertEquals(3, second.size)
        assertEquals(2, second.version)

        val third = first.plus(mapOf(4 to "4", 5 to "5", 6 to "6"))
        assertEquals("1", third[1])
        assertEquals("2", third[2])
        assertEquals("3", third[3])
        assertEquals("4", third[4])
        assertEquals("5", third[5])
        assertEquals("6", third[6])
        assertEquals(6, third.size)
        assertEquals(3, third.version)

        assertEquals("1", first[1])
        assertEquals("2", first[2])
        assertEquals("3", first[3])
        assertEquals(3, first.size)
        assertEquals(1, first.version)

        val fourth = third.plus(mapOf(1 to "2", 3 to "4", 5 to "6"))
        assertEquals("2", fourth[1])
        assertEquals("2", fourth[2])
        assertEquals("4", fourth[3])
        assertEquals("4", fourth[4])
        assertEquals("6", fourth[5])
        assertEquals("6", fourth[6])
        assertEquals(6, fourth.size)
        assertEquals(4, fourth.version)
    }

    @Test
    fun `test minus one key`() {
        val first = emptyMap.minus(0)
        assertEquals(0, first.size)
        assertEquals(1, first.version)

        val second = singletonMap.minus(42)
        assertEquals(0, second.size)
        assertEquals(2, second.version)

        val third = persistentMap.minus(3)
        assertFalse(third.containsKey(3))
        assertFalse(third.containsValue("3"))
        assertEquals(4, third.size)
        assertEquals(2, third.version)

        val fourth = persistentMap.minus(10)
        assertEquals(5, fourth.size)
        assertEquals(3, fourth.version)

        val fifth = third.minus(1)
        assertFalse(fifth.containsKey(1))
        assertFalse(fifth.containsValue("1"))
        assertEquals(3, fifth.size)
        assertEquals(4, fifth.version)
    }

    @Test
    fun `test minus collection keys`() {
        val first = emptyMap.minus(listOf(0, 1))
        assertEquals(0, first.size)
        assertEquals(1, first.version)

        val second = singletonMap.minus(listOf(42))
        assertEquals(0, second.size)
        assertEquals(2, second.version)

        val third = singletonMap.minus(listOf(42, 42, 43))
        assertEquals(0, third.size)
        assertEquals(3, third.version)

        val fourth = persistentMap.minus(listOf(2, 3))
        assertFalse(fourth.containsKey(2))
        assertFalse(fourth.containsValue("2"))
        assertFalse(fourth.containsKey(3))
        assertFalse(fourth.containsValue("3"))
        assertEquals(3, fourth.size)
        assertEquals(2, fourth.version)

        val fifth = persistentMap.minus(listOf(6, 7, 8))
        assertEquals(5, fifth.size)
        assertEquals(3, fifth.version)

        val sixth = fourth.minus(listOf(0, 1, 2, 5, 1, 5))
        assertFalse(sixth.containsKey(1))
        assertFalse(sixth.containsValue("1"))
        assertFalse(sixth.containsKey(5))
        assertFalse(sixth.containsValue("5"))
        assertEquals(1, sixth.size)
        assertEquals(4, sixth.version)
    }

    @Test
    fun `test containsKey`() {
        assertFalse(emptyMap.containsKey(0))

        assertTrue(singletonMap.containsKey(42))
        assertFalse(singletonMap.containsKey(43))

        assertTrue(persistentMap.containsKey(1))
        assertTrue(persistentMap.containsKey(2))
        assertTrue(persistentMap.containsKey(3))
        assertTrue(persistentMap.containsKey(4))
        assertTrue(persistentMap.containsKey(5))
        assertFalse(persistentMap.containsKey(0))
    }

    @Test
    fun `test containsValue`() {
        assertFalse(emptyMap.containsValue("0"))

        assertTrue(singletonMap.containsValue("42"))
        assertFalse(singletonMap.containsValue("43"))

        assertTrue(persistentMap.containsValue("1"))
        assertTrue(persistentMap.containsValue("2"))
        assertTrue(persistentMap.containsValue("3"))
        assertTrue(persistentMap.containsValue("4"))
        assertTrue(persistentMap.containsValue("5"))
        assertFalse(persistentMap.containsValue("0"))
    }

    @Test
    fun `test get`() {
        assertNull(emptyMap[0])

        assertNull(singletonMap[-1])
        assertEquals("42", singletonMap[42])
        assertNull(singletonMap[1])

        assertNull(persistentMap[-1])
        assertEquals("1", persistentMap[1])
        assertEquals("2", persistentMap[2])
        assertEquals("3", persistentMap[3])
        assertEquals("4", persistentMap[4])
        assertEquals("5", persistentMap[5])
        assertNull(persistentMap[6])
    }

    @Test
    fun `test isEmpty`() {
        assertTrue(emptyMap.isEmpty())

        assertFalse(singletonMap.isEmpty())

        assertFalse(persistentMap.isEmpty())
    }

    @Test
    fun `test toString`() {
        assertEquals("{}", emptyMap.toString())

        assertEquals("{42=42}", singletonMap.toString())

        assertEquals("{1=1, 2=2, 3=3, 4=4, 5=5}", persistentMap.toString())
    }

    @Test
    fun `test all`() {
        val first = emptyMap.plus(1 to "1")
        assertEquals("1", first[1])
        assertEquals(1, first.size)
        assertEquals(1, first.version)

        val second = first.plus(mapOf(1 to "2", 2 to "3", 3 to "4", 4 to "5"))
        assertEquals("2", second[1])
        assertEquals("3", second[2])
        assertEquals("4", second[3])
        assertEquals("5", second[4])
        assertEquals(4, second.size)
        assertEquals(2, second.version)

        val third = second.minus(3)
        assertNull(third[3])
        assertEquals(3, third.size)
        assertEquals(3, third.version)

        val fourth = third.minus(listOf(1, 4))
        assertNull(fourth[1])
        assertNull(fourth[4])
        assertEquals(1, fourth.size)
        assertEquals(4, fourth.version)
    }
}