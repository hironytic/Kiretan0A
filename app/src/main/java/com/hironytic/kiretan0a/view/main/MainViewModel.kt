package com.hironytic.kiretan0a.view.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.hironytic.kiretan0a.view.util.UpdateHint

class MainViewItemList(val viewModels: List<MainItemViewModel>, val hint: UpdateHint)

class MainViewModel : ViewModel() {
    val title = MutableLiveData<String>()
    val itemList = MutableLiveData<MainViewItemList>()
    
    fun onAdd() {
        val viewModels = ArrayList<MainItemViewModel>()
        viewModels.addAll(itemList.value!!.viewModels)

        val newViewModel = MainItemViewModel()
        newViewModel.name.value = "Wao!"
        viewModels.add(newViewModel)

        itemList.value = MainViewItemList(viewModels, UpdateHint.Partial(listOf(UpdateHint.Change.Inserted(viewModels.size - 1))))
    }
    
    init {
        title.value = "Wao!"
        itemList.value = MainViewItemList(emptyList(), UpdateHint.Whole)
    }
}
