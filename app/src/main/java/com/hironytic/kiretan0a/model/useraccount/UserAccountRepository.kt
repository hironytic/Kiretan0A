//
// UserAccountRepository.kt
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

package com.hironytic.kiretan0a.model.useraccount

import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.util.*

interface UserAccountRepository {
    val currentUser: Observable<Optional<UserAccount>>

    fun signInAnonymously(): Completable
    fun signOut(): Completable
}

class DefaultUserAccountRepository : UserAccountRepository {
    override val currentUser: Observable<Optional<UserAccount>>
    private val auth = FirebaseAuth.getInstance()

    init {
        class AuthStateListener : FirebaseAuth.AuthStateListener {
            var observer: ObservableEmitter<Optional<UserAccount>>? = null
            override fun onAuthStateChanged(auth: FirebaseAuth) {
                observer?.onNext(Optional.ofNullable(auth.currentUser?.let { UserAccount(it) }))
            }
        }
        currentUser = Observable
                .using({
                    AuthStateListener()
                }, { listener ->
                    Observable.create<Optional<UserAccount>> { observer ->
                        listener.observer = observer
                        auth.addAuthStateListener(listener)
                    }
                }, { listener ->
                    auth.removeAuthStateListener(listener)
                })
    }

    override fun signInAnonymously(): Completable {
        return Completable.create { observer ->
            auth.signInAnonymously()
                    .addOnSuccessListener { _ -> observer.onComplete() }
                    .addOnFailureListener { ex -> observer.onError(ex) }
                    .addOnCompleteListener { _ -> observer.onComplete() }
        }
    }

    override fun signOut(): Completable {
        return Completable.create { observer ->
            auth.signOut()
            observer.onComplete()
        }
    }
}
