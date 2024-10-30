package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
// test@email.com
//test123
class LoginViewModel : ViewModel() {
    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    fun onLoginComplete() {
        _isLoggedIn.value = true
    }
}