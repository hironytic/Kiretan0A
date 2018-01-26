package com.hironytic.kiretan0a

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.hironytic.kiretan0a.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    class ViewModel {
        val message = ObservableField<String>()
    }
    
    class Handlers(private val viewModel: MainViewModel) {
        @Suppress("UNUSED_PARAMETER")
        fun onClickFab(view: View) {
            viewModel.message.value = viewModel.message.value + "!"
        }
    }

    

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        
        val bindingViewModel = ViewModel()
        binding.viewModel = bindingViewModel
        binding.handlers = Handlers(viewModel)
        
        viewModel.message.observe(this, Observer<String> {
            if (it != null) {
                bindingViewModel.message.set(it)
            }
        })


        viewModel.message.value = "Wao!"
    }
}
