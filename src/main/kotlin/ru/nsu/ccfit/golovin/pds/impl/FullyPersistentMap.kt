package ru.nsu.ccfit.golovin.pds.impl

import ru.nsu.ccfit.golovin.pds.PersistentMap
import java.util.concurrent.atomic.AtomicInteger

class FullyPersistentMap<K, V> private constructor(private val nodes: MutableMap<Int, MutableList<FatNode<K, V>>>,
                                                   private val nodeVersions: Map<Int, List<Int>>,
                                                   private val versionCounter: AtomicInteger) : PersistentMap<K, V> {
    override val entries: Set<Map.Entry<K, V>> = nodeVersions
        .flatMap { it.value.zip(nodes[it.key]!!) }
        .map { it.second.versionedData[it.first]!! }
        .toSet()
    override val keys: Set<K> = entries.map(Map.Entry<K, V>::key).toSet()
    override val values: Collection<V> = entries.map(Map.Entry<K, V>::value)
    override val size: Int = entries.size
    override val version: Int = versionCounter.get()

    override fun plus(pair: Pair<K, V>): FullyPersistentMap<K, V> = plusWithVersion(pair, versionCounter.incrementAndGet())

    override fun plus(map: Map<K, V>): FullyPersistentMap<K, V> {
        val newVersion = versionCounter.incrementAndGet()
        return map.entries.fold(this) { persistentMap, pair ->
                persistentMap.plusWithVersion(pair.toPair(), newVersion)
            }
    }

    override fun minus(key: K): FullyPersistentMap<K, V> = minusWithVersion(key, versionCounter.incrementAndGet())

    override fun minus(keys: Collection<K>): FullyPersistentMap<K, V> {
        val newVersion = versionCounter.incrementAndGet()
        return keys.fold(this) { persistentMap, key ->
                persistentMap.minusWithVersion(key, newVersion)
            }
    }

    override fun containsKey(key: K): Boolean = keys.contains(key)

    override fun containsValue(value: V): Boolean = values.contains(value)

    override fun get(key: K): V? = nodeVersions[key.hashCode()]
        ?.zip(nodes[key.hashCode()]!!)
        ?.map { it.second.versionedData[it.first]!! }
        ?.find { it.key == key}
        ?.value

    override fun isEmpty(): Boolean = size == 0

    override fun toString(): String = entries.joinToString(", ", "{", "}") { "${it.key}=${it.value}" }

    private fun plusWithVersion(pair: Pair<K, V>, version: Int): FullyPersistentMap<K, V> {
        val newEntry = pair.toEntry()
        val newNodeVersions: Map<Int, List<Int>>
        val chainIndex = pair.first.hashCode()
        val nodeVersionsChain = nodeVersions[chainIndex]
        if (nodeVersionsChain == null) {
            nodes
                .computeIfAbsent(chainIndex) { mutableListOf(FatNode()) }
                .first()
                .versionedData[version] = newEntry
            newNodeVersions = nodeVersions.plus(chainIndex to listOf(version))
        } else {
            val nodesChain = nodes[chainIndex]
            val oldEntryIndex = nodeVersionsChain
                .zip(nodesChain!!)
                .map { it.second.versionedData[it.first]!! }
                .indexOfFirst { it.key == pair.first }
            if (oldEntryIndex == -1) {
                if (nodesChain.size == nodeVersionsChain.size) {
                    nodesChain.add(FatNode())
                }
                nodesChain[nodeVersionsChain.size].versionedData[version] = newEntry
                newNodeVersions = nodeVersions.plus(chainIndex to nodeVersionsChain.plus(version))
            } else {
                nodesChain[oldEntryIndex].versionedData[version] = newEntry
                newNodeVersions = nodeVersions
                    .plus(chainIndex to (nodeVersionsChain.subList(0, oldEntryIndex) + version +
                                nodeVersionsChain.subList(oldEntryIndex + 1, nodeVersionsChain.size)))
            }
        }
        return FullyPersistentMap(nodes, newNodeVersions, versionCounter)
    }

    private fun minusWithVersion(key: K, version: Int): FullyPersistentMap<K, V> {
        val newNodeVersions: Map<Int, List<Int>>
        val chainIndex = key.hashCode()
        val nodeVersionsChain = nodeVersions[chainIndex]
        if (nodeVersionsChain == null) {
            newNodeVersions = nodeVersions
        } else {
            val nodesChain = nodes[chainIndex]
            val oldEntryIndex = nodeVersionsChain
                .zip(nodesChain!!)
                .map { it.second.versionedData[it.first]!! }
                .indexOfFirst { it.key == key }
            if (oldEntryIndex == -1) {
                newNodeVersions = nodeVersions
            } else {
                for (i in oldEntryIndex until nodeVersionsChain.size - 1) {
                    nodesChain[i].versionedData[version] = nodesChain[i + 1].versionedData[nodeVersionsChain[i + 1]]!!
                }
                newNodeVersions = nodeVersions
                    .plus(chainIndex to (nodeVersionsChain.subList(0, oldEntryIndex) +
                            List(nodeVersionsChain.size - oldEntryIndex - 1) { version }))
            }
        }
        return FullyPersistentMap(nodes, newNodeVersions, versionCounter)
    }

    private fun <K, V> Pair<K, V>.toEntry(): Map.Entry<K, V> = object : Map.Entry<K, V> {
        override val key: K = this@toEntry.first
        override val value: V = this@toEntry.second
    }

    private class FatNode<K, V>(val versionedData: MutableMap<Int, Map.Entry<K, V>> = hashMapOf())

    companion object {
        fun <K, V> empty(): FullyPersistentMap<K, V> = FullyPersistentMap(hashMapOf(), hashMapOf(), AtomicInteger(0))

        fun <K, V> singleton(pair: Pair<K, V>): FullyPersistentMap<K, V> = empty<K, V>().plus(pair)

        fun <K, V> from(map: Map<K, V>): FullyPersistentMap<K, V> = empty<K, V>().plus(map)
    }
}