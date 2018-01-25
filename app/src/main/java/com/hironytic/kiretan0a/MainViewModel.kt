package com.hironytic.kiretan0a

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainViewModel() : ViewModel() {
    val message = MutableLiveData<String>()
}
