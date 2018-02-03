//
// MainActivity.kt
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

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hironytic.kiretan0a.R
import com.hironytic.kiretan0a.databinding.ActivityMainBinding
import com.hironytic.kiretan0a.databinding.ItemMainBinding
import com.hironytic.kiretan0a.model.util.CollectionEvent
import com.hironytic.kiretan0a.view.util.UpdateHint
import com.hironytic.kiretan0a.view.util.observeSafely
import com.hironytic.kiretan0a.view.util.toObservableField

class MainActivity : AppCompatActivity() {
    class BindingModel(owner: LifecycleOwner, viewModel: MainViewModel) {
        val title = viewModel.title.toObservableField(owner)
    }
    
    class ItemBindingModel(owner: LifecycleOwner, itemViewModel: MainItemViewModel) {
        val name = itemViewModel.name.toObservableField(owner)
    }
    
    class Handlers(private val viewModel: MainViewModel) {
        @Suppress("UNUSED_PARAMETER")
        fun onAdd(view: View) = viewModel.onAdd()
    }

    private class ItemViewHolder(val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root)
    
    private class ItemListAdapter : RecyclerView.Adapter<ItemViewHolder>() {
        private val items = ArrayList<ItemBindingModel>()
        
        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
            val binding: ItemMainBinding = DataBindingUtil.inflate(LayoutInflater.from(parent!!.context), R.layout.item_main, parent, false)
            return ItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder?, position: Int) {
            val bm = items[position]
            holder?.apply { 
                binding.model = bm
            }
        }
        
        fun insertItem(index: Int, bindingModel: ItemBindingModel) {
            items.add(index, bindingModel)
            notifyItemInserted(index)
        }
        
        fun removeItem(index: Int) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
        
        fun moveItem(oldIndex: Int, newIndex: Int) {
            items.add(newIndex, items.removeAt(oldIndex))
            notifyItemMoved(oldIndex, newIndex)
        }
        
        fun updateItems(items: List<ItemBindingModel>) {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
        }
    }
    

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        
        binding.model = BindingModel(this, viewModel)
        binding.handlers = Handlers(viewModel)
        
        viewModel.itemList.observeSafely(this) { itemList ->
            if (binding.itemList.adapter == null) {
                val itemListAdapter = ItemListAdapter()
                itemListAdapter.updateItems(itemList.viewModels.map { ItemBindingModel(this, it) })
                binding.itemList.adapter = itemListAdapter
            } else {
                val itemListAdapter = binding.itemList.adapter as ItemListAdapter
                when (itemList.hint) {
                    is UpdateHint.Whole -> {
                        itemListAdapter.updateItems(itemList.viewModels.map { ItemBindingModel(this, it) })
                    }
                    is UpdateHint.Partial -> {
                        for (change in itemList.hint.events) {
                            when (change) {
                                is CollectionEvent.Inserted -> {
                                    itemListAdapter.insertItem(change.index, ItemBindingModel(this, change.item))
                                }
                                is CollectionEvent.Deleted -> {
                                    itemListAdapter.removeItem(change.index)
                                }
                                is CollectionEvent.Moved -> {
                                    itemListAdapter.moveItem(change.oldIndex, change.newIndex)
                                }
                            }
                        }
                    }
                    is UpdateHint.None -> {
                        // do nothing
                    }
                }
            }
        }
    }
}
