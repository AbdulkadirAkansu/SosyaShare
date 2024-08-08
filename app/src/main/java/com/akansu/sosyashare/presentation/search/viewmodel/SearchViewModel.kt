package com.akansu.sosyashare.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.usecase.search.SearchUsersUseCase
import com.akansu.sosyashare.domain.usecase.search.GetCurrentUserUseCase
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                val users = searchUsersUseCase(query)
                _searchResults.value = users
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return getCurrentUserUseCase()
    }
}
