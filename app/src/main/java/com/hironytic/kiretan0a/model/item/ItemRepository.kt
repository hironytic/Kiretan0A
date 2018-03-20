//
// ItemRepository.kt
// Kiretan0A
//
// Copyright (c) 2018 Hironori Ichimiya <hiron@hironytic.com>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

package com.hironytic.kiretan0a.model.item

import com.hironytic.kiretan0a.model.util.CollectionChange
import com.hironytic.kiretan0a.model.util.DataStore
import com.hironytic.kiretan0a.model.util.DefaultDataStore
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface ItemRepository {
    fun items(teamID: String, insufficient: Boolean): Flowable<CollectionChange<Item>>

    fun createItem(teamID: String, item: Item): Single<String>
    fun updateItem(teamID: String, item: Item): Completable
    fun removeItem(teamID: String, itemID: String): Completable
}

sealed class ItemRepositoryError : Throwable() {
    object InvalidItem : ItemRepositoryError()
}

class DefaultItemRepository : ItemRepository {
    private val dataStore: DataStore = DefaultDataStore()
    
    override fun items(teamID: String, insufficient: Boolean): Flowable<CollectionChange<Item>> {
        val itemPath = dataStore.collection("team").document(teamID).collection("item")
        val itemQuery = itemPath
                .whereEqualTo("insufficient", insufficient)
                .orderBy("last_change")
        return dataStore.observeCollection(itemQuery, Item.Factory)
    }

    private fun updateLastChange(data: Map<String, Any>): Map<String, Any> {
        val result = data.toMutableMap()
        result["last_change"] = dataStore.serverTimestampPlaceholder
        return result
    }

    override fun createItem(teamID: String, item: Item): Single<String> {
        if (item.error != null) {
            return Single.error(ItemRepositoryError.InvalidItem)
        }

        val itemPath = dataStore.collection("team").document(teamID).collection("item").document()
        val itemID = itemPath.documentID
                return dataStore.write { writer ->
                    writer.set(itemPath, updateLastChange(item.raw().data))
                }.andThen(Single.just(itemID))
    }

    override fun updateItem(teamID: String, item: Item): Completable {
        if (item.error != null) {
            return Completable.error(ItemRepositoryError.InvalidItem)
        }

        val itemPath = dataStore.collection("team").document(teamID).collection("item").document(item.itemID)
        return dataStore.write { writer ->
            writer.update(itemPath, updateLastChange(item.raw().data))
        }
    }

    override fun removeItem(teamID: String, itemID: String): Completable {
        val itemPath = dataStore.collection("team").document(teamID).collection("item").document(itemID)
        return dataStore.write { writer ->
            writer.delete(itemPath)
        }
    }
}
