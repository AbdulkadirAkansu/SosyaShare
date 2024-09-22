package com.akansu.sosyashare.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.User
import com.akansu.sosyashare.domain.repository.AuthRepository
import com.akansu.sosyashare.domain.repository.BlockedUserRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val blockedUserRepository: BlockedUserRepository,
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    fun searchUsers(query: String) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUser()?.uid ?: return@launch


            val blockedUserIds = blockedUserRepository.getBlockedUsersByUserId(currentUserId)
                .map { it.blockedUserId }
            val usersWhoBlockedMe = blockedUserRepository.getUsersWhoBlockedUserId(currentUserId)
                .map { it.blockerUserId }


            val allBlockedIds = blockedUserIds + usersWhoBlockedMe

            if (query.isNotBlank()) {
                val users = userRepository.searchUsers(query)
                val filteredUsers = users.filter { it.id !in allBlockedIds }
                _searchResults.value = filteredUsers
            } else {
                _searchResults.value = emptyList()
            }
        }
    }


    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}
