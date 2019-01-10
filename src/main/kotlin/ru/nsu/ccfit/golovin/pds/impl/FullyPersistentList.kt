package ru.nsu.ccfit.golovin.pds.impl

import ru.nsu.ccfit.golovin.pds.PersistentList
import java.util.concurrent.atomic.AtomicInteger

class FullyPersistentList<E> private constructor(private val first: FatNode<E>?,
                                                 private val last: FatNode<E>?,
                                                 private val nodeVersions: List<Int>,
                                                 private val versionCounter: AtomicInteger) : PersistentList<E> {
    override val size: Int = nodeVersions.size
    override val version: Int = versionCounter.get()

    override fun plus(element: E): FullyPersistentList<E> = plusAt(size, element)

    override fun plus(elements: Collection<E>): FullyPersistentList<E> = plusAt(size, elements)

    override fun minus(element: E): FullyPersistentList<E> = minusWithVersion(element, versionCounter.incrementAndGet())

    override fun minus(elements: Collection<E>): FullyPersistentList<E> {
        val newVersion = versionCounter.incrementAndGet()
        return elements.fold(this) { persistentList, element ->
            persistentList.minusWithVersion(element, newVersion)
        }
    }

    override fun plusAt(index: Int, element: E): FullyPersistentList<E> {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException(index)
        }
        return plusAtWithVersion(index, element, versionCounter.incrementAndGet())
    }

    override fun plusAt(index: Int, elements: Collection<E>): FullyPersistentList<E> {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException(index)
        }
        val newVersion = versionCounter.incrementAndGet()
        return elements
            .reversed()
            .fold(this) { persistentList, element ->
                persistentList.plusAtWithVersion(index, element, newVersion)
            }
    }

    override fun minusAt(index: Int): FullyPersistentList<E> {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index)
        }
        return minusAtWithVersion(index, versionCounter.incrementAndGet())
    }

    override fun with(index: Int, element: E): FullyPersistentList<E> {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException(index)
        }
        val newVersion = versionCounter.incrementAndGet()
        val newNode = FatNode(element)
        val newNodeVersions = nodeVersions.subList(0, index) + newVersion + nodeVersions.subList(index + 1, size)
        return when (index) {
            0 ->
                if (size == 1) FullyPersistentList(newNode, newNode, newNodeVersions, versionCounter)
                else {
                    newNode.versionedNext[nodeVersions[1]] = first!!.versionedNext[nodeVersions[1]]!!
                    FullyPersistentList(newNode, last, newNodeVersions, versionCounter)
                }
            size - 1 -> {
                val prevNodeVersion = nodeVersions[index - 1]
                linkNodes(prevNodeVersion to last!!.versionedPrev[prevNodeVersion]!!, newVersion to newNode)
                FullyPersistentList(first, newNode, newNodeVersions, versionCounter)
            }
            else -> {
                val replacedNode = nodes().elementAt(index)
                val prevNodeVersion = nodeVersions[index - 1]
                val nextNodeVersion = nodeVersions[index + 1]
                linkNodes(prevNodeVersion to replacedNode.versionedPrev[prevNodeVersion]!!,
                    newVersion to newNode,
                    nextNodeVersion to replacedNode.versionedNext[nextNodeVersion]!!)
                FullyPersistentList(first, last, newNodeVersions, versionCounter)
            }
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): FullyPersistentList<E> {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
        }
        versionCounter.incrementAndGet()
        val newNodes = nodes().drop(fromIndex).take(toIndex - fromIndex)
        val newNodeVersions = nodeVersions.subList(fromIndex, toIndex)
        return FullyPersistentList(newNodes.firstOrNull(), newNodes.lastOrNull(), newNodeVersions, versionCounter)
    }

    override fun get(index: Int): E = elementData().elementAt(index)

    override fun contains(element: E): Boolean = elementData().contains(element)

    override fun containsAll(elements: Collection<E>): Boolean = elements.all(::contains)

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<E> = elementData().iterator()

    override fun toString(): String = elementData().joinToString(", ", "[", "]")

    private fun nodes(): Sequence<FatNode<E>> = when (first) {
        null -> emptySequence()
        else -> generateSequence(1 to first) { (i, node) ->
            if (i == size) null
            else (i + 1) to node.versionedNext[nodeVersions[i]]!!
        }.map { it.second }
    }

    private fun elementData(): Sequence<E> = nodes().map { it.item }

    private fun linkNodes(first: Pair<Int, FatNode<E>>, second: Pair<Int, FatNode<E>>) {
        val (firstVersion, firstNode) = first
        val (secondVersion, secondNode) = second
        firstNode.versionedNext[secondVersion] = secondNode
        secondNode.versionedPrev[firstVersion] = firstNode
    }

    private fun linkNodes(first: Pair<Int, FatNode<E>>, second: Pair<Int, FatNode<E>>, third: Pair<Int, FatNode<E>>) {
        linkNodes(first, second)
        linkNodes(second, third)
    }

    private fun plusAtWithVersion(index: Int, element: E, version: Int): FullyPersistentList<E> {
        val newNode = FatNode(element)
        val newNodeVersions = nodeVersions.subList(0, index) + version + nodeVersions.subList(index, size)
        return when (index) {
            0 ->
                if (isEmpty()) FullyPersistentList(newNode, newNode, newNodeVersions, versionCounter)
                else {
                    linkNodes(version to newNode, nodeVersions.first() to first!!)
                    FullyPersistentList(newNode, last, newNodeVersions, versionCounter)
                }
            size -> {
                linkNodes(nodeVersions.last() to last!!, version to newNode)
                FullyPersistentList(first, newNode, newNodeVersions, versionCounter)
            }
            else -> {
                val nextNode = nodes().elementAt(index)
                val nextNodeVersion = nodeVersions[index]
                val prevNodeVersion = nodeVersions[index - 1]
                linkNodes(prevNodeVersion to nextNode.versionedPrev[prevNodeVersion]!!,
                    version to newNode,
                    nextNodeVersion to nextNode)
                FullyPersistentList(first, last, newNodeVersions, versionCounter)
            }
        }
    }

    private fun minusWithVersion(element: E, version: Int): FullyPersistentList<E> {
        val elementIndex = elementData().indexOf(element)
        return if (elementIndex == -1) FullyPersistentList(first, last, nodeVersions, versionCounter)
        else minusAtWithVersion(elementIndex, version)
    }

    private fun minusAtWithVersion(index: Int, version: Int): FullyPersistentList<E> {
        return when (index) {
            0 ->
                if (size == 1) FullyPersistentList(null, null, emptyList(), versionCounter)
                else FullyPersistentList(first!!.versionedNext[nodeVersions[1]], last, nodeVersions.drop(1), versionCounter)
            size - 1 ->
                FullyPersistentList(first, last!!.versionedPrev[nodeVersions[index - 1]], nodeVersions.dropLast(1), versionCounter)
            else -> {
                val newNodeVersions = nodeVersions.subList(0, index) + List(size - index - 1) { version }
                val startsWithIndexNodes = nodes().drop(index)
                val deletedNode = startsWithIndexNodes.first()
                val afterIndexList = startsWithIndexNodes.drop(1).toList()
                val prevNodeVersion = nodeVersions[index - 1]
                val nextNodeVersion = nodeVersions[index + 1]
                linkNodes(prevNodeVersion to deletedNode.versionedPrev[prevNodeVersion]!!,
                    version to deletedNode.versionedNext[nextNodeVersion]!!)
                for (i in afterIndexList.indices.drop(1)) {
                    linkNodes(version to afterIndexList[i - 1], version to afterIndexList[i])
                }
                FullyPersistentList(first, last, newNodeVersions, versionCounter)
            }
        }
    }

    private class FatNode<E>(val item: E,
                             val versionedNext: MutableMap<Int, FatNode<E>> = hashMapOf(),
                             val versionedPrev: MutableMap<Int, FatNode<E>> = hashMapOf())

    companion object {
        fun <E> empty(): FullyPersistentList<E> = FullyPersistentList(null, null, emptyList(), AtomicInteger(0))

        fun <E> singleton(element: E): FullyPersistentList<E> = empty<E>().plus(element)

        fun <E> from(elements: Collection<E>): FullyPersistentList<E> = empty<E>().plus(elements)
    }
}