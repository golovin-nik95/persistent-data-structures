package ru.nsu.ccfit.golovin.pds

interface PersistentSequence<E> : PersistentCollection<E> {
    override fun plus(element: E): PersistentSequence<E>

    override fun plus(elements: Collection<E>): PersistentSequence<E>

    override fun minus(element: E): PersistentSequence<E>

    override fun minus(elements: Collection<E>): PersistentSequence<E>

    fun plusAt(index: Int, element: E): PersistentSequence<E>

    fun plusAt(index: Int, elements: Collection<E>): PersistentSequence<E>

    fun minusAt(index: Int): PersistentSequence<E>

    operator fun get(index: Int): E

    fun with(index: Int, element: E): PersistentSequence<E>

    fun subList(fromIndex: Int, toIndex: Int): PersistentSequence<E>
}