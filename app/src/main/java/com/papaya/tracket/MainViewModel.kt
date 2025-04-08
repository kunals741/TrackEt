package com.papaya.tracket

import androidx.lifecycle.ViewModel
import com.papaya.tracket.domain.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
}