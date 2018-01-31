//
// MainViewModel.kt
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
