package com.hironytic.kiretan0a

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hironytic.kiretan0a.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val info = Info()
        binding.info = info
        
        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.message.observe(this, Observer<String> {
            if (it != null) {
                info.message.set(it)
            }
        })
        
        viewModel.message.value = "Wao!"
    }
}
