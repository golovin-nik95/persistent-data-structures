package ru.nsu.ccfit.golovin.pds

interface PersistentList<E> : PersistentSequence<E> {
    override fun plus(element: E): PersistentList<E>

    override fun plus(elements: Collection<E>): PersistentList<E>

    override fun minus(element: E): PersistentList<E>

    override fun minus(elements: Collection<E>): PersistentList<E>

    override fun plusAt(index: Int, element: E): PersistentList<E>

    override fun plusAt(index: Int, elements: Collection<E>): PersistentList<E>

    override fun minusAt(index: Int): PersistentList<E>

    override fun with(index: Int, element: E): PersistentList<E>

    override fun subList(fromIndex: Int, toIndex: Int): PersistentList<E>
}