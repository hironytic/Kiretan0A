package com.hironytic.kiretan0a.view.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hironytic.kiretan0a.R
import com.hironytic.kiretan0a.databinding.ActivityMainBinding
import com.hironytic.kiretan0a.databinding.ItemMainBinding

class MainActivity : AppCompatActivity() {
    class BindingModel {
        val title = ObservableField<String>()
    }
    
    class ItemBindingModel {
        val name = ObservableField<String>()
    }
    
    class Handlers(private val viewModel: MainViewModel) {
        @Suppress("UNUSED_PARAMETER")
        fun onClickFab(view: View) {
            viewModel.title.value = viewModel.title.value + "!"            
            
            val viewModels = ArrayList<MainItemViewModel>()
            viewModels.addAll(viewModel.itemList.value!!.viewModels)
            
            val newViewModel = MainItemViewModel()
            newViewModel.name.value = "Wao!"
            viewModels.add(newViewModel)

            viewModel.itemList.value = MainViewItemList(viewModels, UpdateHint.Partial(listOf(UpdateHint.Change.Inserted(viewModels.size - 1))))
        }
    }

    class ItemViewHolder(val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root)
    
    class ItemListAdapter : RecyclerView.Adapter<ItemViewHolder>() {
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
        
        val bindingModel = BindingModel()
        binding.model = bindingModel
        binding.handlers = Handlers(viewModel)
        
        viewModel.title.observe(this, Observer<String> {
            if (it != null) {
                bindingModel.title.set(it)
            }
        })
        
        viewModel.itemList.observe(this, Observer<MainViewItemList> {
            if (it != null) {
                if (binding.itemList.adapter == null) {
                    val itemListAdapter = ItemListAdapter()
                    itemListAdapter.updateItems(it.viewModels.map { convertItemViewModel(it) })
                    binding.itemList.adapter = itemListAdapter
                } else {
                    val itemListAdapter = binding.itemList.adapter as ItemListAdapter
                    when (it.hint) {
                        UpdateHint.Whole -> {
                            itemListAdapter.updateItems(it.viewModels.map { convertItemViewModel(it) })
                        }
                        is UpdateHint.Partial -> {
                            for (change in it.hint.changes) {
                                when (change) {
                                    is UpdateHint.Change.Inserted -> {
                                        itemListAdapter.insertItem(change.index, convertItemViewModel(it.viewModels[change.index]))
                                    }
                                    is UpdateHint.Change.Deleted -> {
                                        itemListAdapter.removeItem(change.index)
                                    }
                                    is UpdateHint.Change.Moved -> {
                                        itemListAdapter.moveItem(change.oldIndex, change.newIndex)
                                    }
                                }
                            }
                        }
                        UpdateHint.None -> {
                            // do nothing
                        }
                    }
                }
            }
        })
    }

    private fun convertItemViewModel(itemViewModel: MainItemViewModel): ItemBindingModel {
        val converted = ItemBindingModel()
        itemViewModel.name.observe(this, Observer<String> {
            converted.name.set(it)
        })
        return converted
    }

}
