package ru.nsu.ccfit.golovin.pds

interface PersistentArray<E> : PersistentSequence<E> {
    override fun plus(element: E): PersistentArray<E>

    override fun plus(elements: Collection<E>): PersistentArray<E>

    override fun minus(element: E): PersistentArray<E>

    override fun minus(elements: Collection<E>): PersistentArray<E>

    override fun plusAt(index: Int, element: E): PersistentArray<E>

    override fun plusAt(index: Int, elements: Collection<E>): PersistentArray<E>

    override fun minusAt(index: Int): PersistentArray<E>

    override fun with(index: Int, element: E): PersistentArray<E>

    override fun subList(fromIndex: Int, toIndex: Int): PersistentArray<E>
}