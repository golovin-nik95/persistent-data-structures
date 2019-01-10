package ru.nsu.ccfit.golovin.pds

interface PersistentCollection<E> : Collection<E> {
    val version: Int

    fun plus(element: E): PersistentCollection<E>

    fun plus(elements: Collection<E>): PersistentCollection<E>

    fun minus(element: E): PersistentCollection<E>

    fun minus(elements: Collection<E>): PersistentCollection<E>
}