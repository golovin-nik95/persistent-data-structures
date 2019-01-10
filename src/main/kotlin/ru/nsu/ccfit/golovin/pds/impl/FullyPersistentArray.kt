package ru.nsu.ccfit.golovin.pds.impl

import ru.nsu.ccfit.golovin.pds.PersistentArray
import java.util.concurrent.atomic.AtomicInteger

class FullyPersistentArray<E> private constructor(private val nodes: MutableList<FatNode<E>>,
                                                  private val nodeVersions: List<Int>,
                                                  private val versionCounter: AtomicInteger) : PersistentArray<E> {
    override val size: Int = nodeVersions.size
    override val version: Int = versionCounter.get()

    override fun plus(element: E): FullyPersistentArray<E> = plusAt(size, element)

    override fun plus(elements: Collection<E>): FullyPersistentArray<E> = plusAt(size, elements)

    override fun minus(element: E): FullyPersistentArray<E> = minusWithVersion(element, versionCounter.incrementAndGet())

    override fun minus(elements: Collection<E>): FullyPersistentArray<E> {
        val newVersion = versionCounter.incrementAndGet()
        return elements.fold(this) { persistentArray, element ->
                persistentArray.minusWithVersion(element, newVersion)
            }
    }

    override fun plusAt(index: Int, element: E): FullyPersistentArray<E> {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException(index)
        }
        return plusAtWithVersion(index, element, versionCounter.incrementAndGet())
    }

    override fun plusAt(index: Int, elements: Collection<E>): FullyPersistentArray<E> {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException(index)
        }
        val newVersion = versionCounter.incrementAndGet()
        return elements
            .reversed()
            .fold(this) { persistentArray, element ->
                persistentArray.plusAtWithVersion(index, element, newVersion)
            }
    }

    override fun minusAt(index: Int): FullyPersistentArray<E> {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index)
        }
        return minusAtWithVersion(index, versionCounter.incrementAndGet())
    }

    override fun with(index: Int, element: E): FullyPersistentArray<E> {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index)
        }
        val newVersion = versionCounter.incrementAndGet()
        nodes[index].versionedData[newVersion] = element
        val newNodeVersions = nodeVersions.subList(0, index) + newVersion + nodeVersions.subList(index + 1, size)
        return FullyPersistentArray(nodes, newNodeVersions, versionCounter)
    }

    override fun subList(fromIndex: Int, toIndex: Int): FullyPersistentArray<E> {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
        }
        val newVersion = versionCounter.incrementAndGet()
        for (i in fromIndex until toIndex) {
            nodes[i - fromIndex].versionedData[newVersion] = nodes[i].versionedData[nodeVersions[i]]!!
        }
        val newNodeVersions = List(toIndex - fromIndex) { newVersion }
        return FullyPersistentArray(nodes, newNodeVersions, versionCounter)
    }

    override fun get(index: Int): E = nodes[index].versionedData[nodeVersions[index]]!!

    override fun contains(element: E): Boolean = indexOf(element) != -1

    override fun containsAll(elements: Collection<E>): Boolean = elements.all(::contains)

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<E> = nodeVersions.zip(nodes).map { it.second.versionedData[it.first]!! }.iterator()

    override fun toString(): String = iterator().asSequence().joinToString(", ", "[", "]")

    private fun indexOf(element: E): Int = iterator().asSequence().indexOf(element)

    private fun plusAtWithVersion(index: Int, element: E, version: Int): FullyPersistentArray<E> {
        if (nodes.size == size) {
            nodes += FatNode()
        }
        for (i in size -1 downTo index) {
            nodes[i + 1].versionedData[version] = nodes[i].versionedData[nodeVersions[i]]!!
        }
        nodes[index].versionedData[version] = element
        val newNodeVersions = nodeVersions.subList(0, index) + List(size - index + 1) { version }
        return FullyPersistentArray(nodes, newNodeVersions, versionCounter)
    }

    private fun minusWithVersion(element: E, version: Int): FullyPersistentArray<E> {
        val elementIndex = indexOf(element)
        return if (elementIndex == -1) FullyPersistentArray(nodes, nodeVersions, versionCounter)
        else minusAtWithVersion(elementIndex, version)
    }

    private fun minusAtWithVersion(index: Int, version: Int): FullyPersistentArray<E> {
        for (i in index until size - 1) {
            nodes[i].versionedData[version] = nodes[i + 1].versionedData[nodeVersions[i + 1]]!!
        }
        val newNodeVersions = nodeVersions.subList(0, index) + List(size - index - 1) { version }
        return FullyPersistentArray(nodes, newNodeVersions, versionCounter)
    }

    private class FatNode<E>(val versionedData: MutableMap<Int, E> = hashMapOf())

    companion object {
        fun <E> empty(): FullyPersistentArray<E> = FullyPersistentArray(mutableListOf(), emptyList(), AtomicInteger(0))

        fun <E> singleton(element: E): FullyPersistentArray<E> = empty<E>().plus(element)

        fun <E> from(elements: Collection<E>): FullyPersistentArray<E> = empty<E>().plus(elements)
    }
}