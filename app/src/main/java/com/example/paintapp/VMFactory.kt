package com.example.paintapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//View-Model Factory needed because we need to pass in repository and default constructor doesn't allow that
class VMFactory(private val drawRepository: DrawRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass : Class<T>) : T {
        if(modelClass.isAssignableFrom(DrawViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return DrawViewModel(drawRepository) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}

//View-Model Factory needed because we need to pass in repository and default constructor doesn't allow that
class MainMenuVMFactory(private val drawRepository: DrawRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass : Class<T>) : T {
        if(modelClass.isAssignableFrom(MainMenuViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MainMenuViewModel(drawRepository) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}

