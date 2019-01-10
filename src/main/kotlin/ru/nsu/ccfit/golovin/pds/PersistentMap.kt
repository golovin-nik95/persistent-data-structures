package ru.nsu.ccfit.golovin.pds

interface PersistentMap<K, V> : Map<K, V> {
    val version: Int

    fun plus(pair: Pair<K, V>): PersistentMap<K, V>

    fun plus(map: Map<K, V>): PersistentMap<K, V>

    fun minus(key: K): PersistentMap<K, V>

    fun minus(keys: Collection<K>): PersistentMap<K, V>
}