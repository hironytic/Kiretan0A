package com.hironytic.kiretan0a.view.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T?) -> Unit) =
        observe(owner, Observer<T> { observer(it) })

fun <T> LiveData<T>.observe(owner: LifecycleOwner, defaultValue: T, observer: (T) -> Unit) =
        observe(owner, Observer<T> { observer(it ?: defaultValue) })

fun <T> LiveData<T>.observeSafely(owner: LifecycleOwner, observer: (T) -> Unit) =
        observe(owner, Observer<T> { it?.apply { observer(this) }})

