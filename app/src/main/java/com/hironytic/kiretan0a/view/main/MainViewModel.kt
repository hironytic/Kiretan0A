package com.hironytic.kiretan0a.view.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

sealed class UpdateHint {
    object Whole: UpdateHint()
    data class Partial(val changes: List<Change>): UpdateHint()
    object None: UpdateHint()

    sealed class Change {
        data class Deleted(val index: Int) : Change()
        data class Inserted(val index: Int) : Change()
        data class Moved(val oldIndex: Int, val newIndex: Int) : Change()
    }
}

class MainViewItemList(val viewModels: List<MainItemViewModel>, val hint: UpdateHint)

class MainViewModel : ViewModel() {
    val title = MutableLiveData<String>()
    val itemList = MutableLiveData<MainViewItemList>()
    
    init {
        title.value = "Wao!"
        itemList.value = MainViewItemList(emptyList(), UpdateHint.Whole)
    }
}
