//
// DataStore.kt
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

package com.hironytic.kiretan0a.model.util

import io.reactivex.Completable
import io.reactivex.Observable
import java.util.*

interface DataStore {
    val deletePlaceHolder: Any
    val serverTimestampPlaceholder: Any

    fun collection(collectionID: String): CollectionPath

    fun <T: Entity> observeDocument(documentPath: DocumentPath): Observable<Optional<T>>
    fun <T: Entity> observeCollection(query: DataStoreQuery): Observable<CollectionChange<T>>

    fun write(block: (DocumentWriter) -> Unit): Completable
}

interface DataStoreQuery {
    fun whereEqualTo(field: String, value: Any): DataStoreQuery
    fun whereLessThan(field: String, value: Any): DataStoreQuery
    fun whereLessThanOrEqualTo(field: String, value: Any): DataStoreQuery
    fun whereGreaterThan(field: String, value: Any): DataStoreQuery
    fun whereGreaterThanOrEqualTo(field: String, value: Any): DataStoreQuery

    fun orderBy(field: String): DataStoreQuery
    fun orderBy(field: String, descending: Boolean): DataStoreQuery
}

interface CollectionPath : DataStoreQuery {
    val collectionID: String
    fun document(): DocumentPath
    fun document(documentID: String): DocumentPath
}

interface DocumentPath {
    val documentID: String
    fun collection(collectionID: String): CollectionPath
}

interface DocumentWriter {
    fun set(documentPath: DocumentPath, documentData: Map<String, Any>)
    fun update(documentPath: DocumentPath, documentData: Map<String, Any>)
    fun merge(documentPath: DocumentPath, documentData: Map<String, Any>)
    fun delete(documentPath: DocumentPath)
}

