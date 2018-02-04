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

import com.google.firebase.firestore.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import java.util.*

interface DataStore {
    val deletePlaceHolder: Any
    val serverTimestampPlaceholder: Any

    fun collection(collectionID: String): CollectionPath

    fun <E : Entity> observeDocument(documentPath: DocumentPath, factory: EntityFactory<E>): Flowable<Optional<E>>
    fun <E : Entity> observeCollection(query: DataStoreQuery, factory: EntityFactory<E>): Flowable<CollectionChange<E>>

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

class DefaultDataStore : DataStore {
    override val deletePlaceHolder: Any
        get() = FieldValue.delete()

    override val serverTimestampPlaceholder: Any
        get() = FieldValue.serverTimestamp()

    override fun collection(collectionID: String): CollectionPath =
            DefaultCollectionPath(FirebaseFirestore.getInstance().collection(collectionID))

    private class RegistrationBox {
        var registration: ListenerRegistration? = null
    }

    override fun <E : Entity> observeDocument(documentPath: DocumentPath, factory: EntityFactory<E>): Flowable<Optional<E>> {
        val resourceSupplier = { RegistrationBox() }
        val disposer: (RegistrationBox) -> Unit = { box -> box.registration?.remove() }
        val sourceSupplier = { box: RegistrationBox ->
            Flowable.create<Optional<E>>({ emitter ->
                box.registration = (documentPath as DefaultDocumentPath).documentRef.addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        emitter.onError(error)
                    } else {
                        if (documentSnapshot.exists()) {
                            try {
                                val entity = factory.fromRawEntity(RawEntity(documentSnapshot.id, documentSnapshot.data))
                                emitter.onNext(Optional.of(entity))
                            } catch (error: Throwable) {
                                emitter.onError(error)
                            }
                        } else {
                            emitter.onNext(Optional.empty())
                        }
                    }
                }
            }, BackpressureStrategy.BUFFER)
        }
        return Flowable.using(resourceSupplier, sourceSupplier, disposer)
    }

    override fun <E : Entity> observeCollection(query: DataStoreQuery, factory: EntityFactory<E>): Flowable<CollectionChange<E>> {
        val resourceSupplier = { RegistrationBox() }
        val disposer: (RegistrationBox) -> Unit = { box -> box.registration?.remove() }
        val sourceSupplier = { box: RegistrationBox ->
            Flowable.create<CollectionChange<E>>({ emitter ->
                box.registration = (query as DefaultDataStoreQuery).query.addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        emitter.onError(error)
                    } else {
                        try {
                            val generatedEntities = mutableMapOf<String, E>()
                            val entity: (DocumentSnapshot) -> E = { doc ->
                                generatedEntities.getOrPut(doc.id, { factory.fromRawEntity(RawEntity(doc.id, doc.data)) })
                            }

                            val result = querySnapshot.documents.map { entity(it) }
                            val events = querySnapshot.documentChanges.map<DocumentChange, CollectionEvent<E>> { change ->
                                when (change.type) {
                                    DocumentChange.Type.ADDED -> {
                                        CollectionEvent.Inserted(change.newIndex, entity(change.document))
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        CollectionEvent.Moved(change.oldIndex, change.newIndex)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        CollectionEvent.Deleted(change.oldIndex)
                                    }
                                }
                            }
                            emitter.onNext(CollectionChange(result, events))
                        } catch (error: Throwable) {
                            emitter.onError(error)
                        }
                    }
                }
            }, BackpressureStrategy.BUFFER)
        }
        return Flowable.using(resourceSupplier, sourceSupplier, disposer)
    }

    override fun write(block: (DocumentWriter) -> Unit): Completable {
        return Completable.create { observer ->
            val batch = FirebaseFirestore.getInstance().batch()
            val writer = DefaultDocumentWriter(batch)
            try {
                block(writer)
            } catch (error: Throwable) {
                observer.onError(error)
            }

            batch.commit()
                    .addOnSuccessListener { observer.onComplete() }
                    .addOnFailureListener { observer.onError(it) }
        }
    }
}

private open class DefaultDataStoreQuery(val query: Query) : DataStoreQuery {
    override fun whereEqualTo(field: String, value: Any): DataStoreQuery =
            DefaultDataStoreQuery(query.whereEqualTo(field, value))

    override fun whereLessThan(field: String, value: Any): DataStoreQuery =
            DefaultDataStoreQuery(query.whereLessThan(field, value))

    override fun whereLessThanOrEqualTo(field: String, value: Any): DataStoreQuery =
            DefaultDataStoreQuery(query.whereLessThanOrEqualTo(field, value))

    override fun whereGreaterThan(field: String, value: Any): DataStoreQuery =
            DefaultDataStoreQuery(query.whereGreaterThan(field, value))

    override fun whereGreaterThanOrEqualTo(field: String, value: Any): DataStoreQuery =
            DefaultDataStoreQuery(query.whereGreaterThanOrEqualTo(field, value))

    override fun orderBy(field: String): DataStoreQuery =
            DefaultDataStoreQuery(query.orderBy(field))

    override fun orderBy(field: String, descending: Boolean): DataStoreQuery =
            DefaultDataStoreQuery(query.orderBy(field,
                    if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING))
}

private class DefaultCollectionPath(val collectionRef: CollectionReference) : DefaultDataStoreQuery(collectionRef), CollectionPath {
    override val collectionID: String
        get() = collectionRef.id

    override fun document(): DocumentPath =
            DefaultDocumentPath(collectionRef.document())

    override fun document(documentID: String): DocumentPath =
            DefaultDocumentPath(collectionRef.document(documentID))
}

private class DefaultDocumentPath(val documentRef: DocumentReference) : DocumentPath {
    override val documentID: String
        get() = documentRef.id

    override fun collection(collectionID: String): CollectionPath =
            DefaultCollectionPath(documentRef.collection(collectionID))
}

private class DefaultDocumentWriter(val writeBatch: WriteBatch) : DocumentWriter {
    override fun set(documentPath: DocumentPath, documentData: Map<String, Any>) {
        writeBatch.set((documentPath as DefaultDocumentPath).documentRef, documentData)
    }

    override fun update(documentPath: DocumentPath, documentData: Map<String, Any>) {
        writeBatch.update((documentPath as DefaultDocumentPath).documentRef, documentData)
    }

    override fun merge(documentPath: DocumentPath, documentData: Map<String, Any>) {
        writeBatch.set((documentPath as DefaultDocumentPath).documentRef, documentData, SetOptions.merge())
    }

    override fun delete(documentPath: DocumentPath) {
        writeBatch.delete((documentPath as DefaultDocumentPath).documentRef)
    }
}
