package com.hironytic.kiretan0a.view.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt

fun <T> LiveData<T>.toObservableField(owner: LifecycleOwner) : ObservableField<T> =
        ObservableField<T>().apply { observeSafely(owner) { this.set(it) } }

fun LiveData<Int>.toObservableInt(owner: LifecycleOwner) : ObservableInt =
        ObservableInt().apply { observeSafely(owner) { this.set(it) } }

fun LiveData<Boolean>.toObservableBoolean(owner: LifecycleOwner) : ObservableBoolean =
        ObservableBoolean().apply { observeSafely(owner) { this.set(it) } }
