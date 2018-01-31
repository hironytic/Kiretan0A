package com.hironytic.kiretan0a.view.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.hironytic.kiretan0a.view.util.UpdateHint

class MainViewItemList(val viewModels: List<MainItemViewModel>, val hint: UpdateHint)

class MainViewModel : ViewModel() {
    val title = MutableLiveData<String>()
    val itemList = MutableLiveData<MainViewItemList>()
    
    init {
        title.value = "Wao!"
        itemList.value = MainViewItemList(emptyList(), UpdateHint.Whole)
    }
}
