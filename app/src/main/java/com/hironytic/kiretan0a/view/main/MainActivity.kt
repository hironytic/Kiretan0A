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
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hironytic.kiretan0a.R
import com.hironytic.kiretan0a.databinding.ActivityMainBinding
import com.hironytic.kiretan0a.databinding.ItemMainBinding
import com.hironytic.kiretan0a.model.useraccount.DefaultUserAccountRepository
import com.hironytic.kiretan0a.model.util.CollectionEvent
import com.hironytic.kiretan0a.view.util.UpdateHint
import com.hironytic.kiretan0a.view.util.observeSafely
import com.hironytic.kiretan0a.view.welcome.WelcomeActivity
import io.reactivex.rxkotlin.subscribeBy

class MainActivity : AppCompatActivity() {
    class Handlers(private val viewModel: MainViewModel) {
        @Suppress("UNUSED_PARAMETER")
        fun onAdd(view: View) = viewModel.onAdd()
    }

    private class ItemViewHolder(private val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(itemViewModel: MainItemViewModel) {
            binding.model = itemViewModel
            binding.executePendingBindings()
        }
    }
    
    private class ItemListAdapter(private val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<ItemViewHolder>() {
        private val items = ArrayList<MainItemViewModel>()
        
        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val binding: ItemMainBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_main, parent, false)
            binding.setLifecycleOwner(lifecycleOwner)
            return ItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val bm = items[position]
            holder.bind(bm)
        }
        
        fun insertItem(index: Int, viewModel: MainItemViewModel) {
            items.add(index, viewModel)
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
        
        fun updateItems(items: List<MainItemViewModel>) {
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
        binding.setLifecycleOwner(this)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        binding.model = viewModel
        binding.handlers = Handlers(viewModel)
        
        viewModel.itemList.observeSafely(this) { itemList ->
            if (binding.itemList.adapter == null) {
                val itemListAdapter = ItemListAdapter(this)
                itemListAdapter.updateItems(itemList.viewModels)
                binding.itemList.adapter = itemListAdapter
            } else {
                val itemListAdapter = binding.itemList.adapter as ItemListAdapter
                when (itemList.hint) {
                    is UpdateHint.Whole -> {
                        itemListAdapter.updateItems(itemList.viewModels)
                    }
                    is UpdateHint.Partial -> {
                        for (change in itemList.hint.events) {
                            when (change) {
                                is CollectionEvent.Inserted -> {
                                    itemListAdapter.insertItem(change.index, change.item)
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

        // FIXME:
        val userAccountRepository = DefaultUserAccountRepository()
        userAccountRepository.currentUser
                .subscribeBy { optUserAccount ->
                    if (!optUserAccount.isPresent) {
                        startActivity(Intent(this, WelcomeActivity::class.java))
                    }
                }
    }
}
