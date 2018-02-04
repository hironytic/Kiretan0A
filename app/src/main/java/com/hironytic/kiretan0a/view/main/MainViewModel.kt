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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.hironytic.kiretan0a.model.item.DefaultItemRepository
import com.hironytic.kiretan0a.model.item.ItemRepository
import com.hironytic.kiretan0a.model.team.DefaultTeamRepository
import com.hironytic.kiretan0a.model.team.TeamRepository
import com.hironytic.kiretan0a.model.util.CollectionEvent
import com.hironytic.kiretan0a.view.util.UpdateHint
import com.hironytic.kiretan0a.view.util.toLiveData

class MainViewItemList(val viewModels: List<MainItemViewModel>, val hint: UpdateHint<MainItemViewModel>)

class MainViewModel : ViewModel() {
    val title: LiveData<String>
    val itemList: LiveData<MainViewItemList>

    private val teamRepository: TeamRepository = DefaultTeamRepository()
    private val itemRepository: ItemRepository = DefaultItemRepository()
    
    private val TEAMID = "TEST_TEAM_ID"
    
    fun onAdd() {
    }
    
    init {
        title = teamRepository
                .team(TEAMID)
                .map { it.map({ it.name }).orElse("") }
                .toLiveData()

        itemList = itemRepository
                .items(TEAMID, false)
                .scan(MainViewItemList(listOf(), UpdateHint.Whole())) { acc, change ->
                    val viewModels = acc.viewModels.toMutableList()
                    val hintEvents = ArrayList<CollectionEvent<MainItemViewModel>>()
                    for (event in change.events) {
                        when (event) {
                            is CollectionEvent.Inserted -> {
                                val viewModel = MainItemViewModel().apply { name.value = event.item.name }
                                viewModels.add(event.index, viewModel)
                                hintEvents.add(CollectionEvent.Inserted(event.index, viewModel))
                            }
                            is CollectionEvent.Deleted -> {
                                viewModels.removeAt(event.index)
                                hintEvents.add(CollectionEvent.Deleted(event.index))
                            }
                            is CollectionEvent.Moved -> {
                                viewModels.add(event.newIndex, viewModels.removeAt(event.oldIndex))
                                hintEvents.add(CollectionEvent.Moved(event.oldIndex, event.newIndex))
                            }
                        }
                    }
                    MainViewItemList(viewModels, UpdateHint.Partial(hintEvents))
                }
                .toLiveData()
    }
}
