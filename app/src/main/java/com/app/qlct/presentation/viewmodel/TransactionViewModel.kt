package com.app.qlct.presentation.viewmodel

import androidx.lifecycle.*
import com.app.qlct.data.entity.Transaction
import com.app.qlct.data.TransactionRepository
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions.asLiveData()

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type).asLiveData()
    }

    fun getTransactionsByMonth(start: Long, end: Long): LiveData<List<Transaction>> {
        return repository.getTransactionsByMonth(start, end).asLiveData()
    }

    fun getTransactionsByTypeInMonth(type: String, start: Long, end: Long): LiveData<List<Transaction>> {
        return repository.getTransactionsByTypeInMonth(type, start, end).asLiveData()
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
