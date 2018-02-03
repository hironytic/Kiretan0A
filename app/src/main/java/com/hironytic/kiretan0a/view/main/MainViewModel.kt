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
import com.hironytic.kiretan0a.model.util.CollectionEvent
import com.hironytic.kiretan0a.view.util.UpdateHint

class MainViewItemList(val viewModels: List<MainItemViewModel>, val hint: UpdateHint<MainItemViewModel>)

class MainViewModel : ViewModel() {
    val title = MutableLiveData<String>()
    val itemList = MutableLiveData<MainViewItemList>()
    
    fun onAdd() {
        val viewModels = itemList.value!!.viewModels.toMutableList()
        val moved = viewModels.removeAt(2)
        viewModels.add(6, moved)

        val newVM = MainItemViewModel().apply { name.value = "Item New"}
        viewModels.add(4, newVM)

        itemList.value = MainViewItemList(viewModels.toList(), UpdateHint.Partial(
                listOf(CollectionEvent.Moved(2, 6), CollectionEvent.Inserted(4, newVM))
        ))
    }
    
    init {
        title.value = "Wao!"


        val list = listOf(
                MainItemViewModel().apply { name.value = "Item 1" },
                MainItemViewModel().apply { name.value = "Item 2" },
                MainItemViewModel().apply { name.value = "Item 3" },
                MainItemViewModel().apply { name.value = "Item 4" },
                MainItemViewModel().apply { name.value = "Item 5" },
                MainItemViewModel().apply { name.value = "Item 6" },
                MainItemViewModel().apply { name.value = "Item 7" },
                MainItemViewModel().apply { name.value = "Item 8" },
                MainItemViewModel().apply { name.value = "Item 9" },
                MainItemViewModel().apply { name.value = "Item 10" },
                MainItemViewModel().apply { name.value = "Item 11" },
                MainItemViewModel().apply { name.value = "Item 12" },
                MainItemViewModel().apply { name.value = "Item 12" },
                MainItemViewModel().apply { name.value = "Item 14" },
                MainItemViewModel().apply { name.value = "Item 15" },
                MainItemViewModel().apply { name.value = "Item 16" },
                MainItemViewModel().apply { name.value = "Item 17" },
                MainItemViewModel().apply { name.value = "Item 18" },
                MainItemViewModel().apply { name.value = "Item 19" },
                MainItemViewModel().apply { name.value = "Item 20" }
        )
        itemList.value = MainViewItemList(list, UpdateHint.Whole())
    }
}
