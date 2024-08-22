package com.akansu.sosyashare.presentation.userprofile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl

    // Burada username alanını ekliyoruz
    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    init {
        getCurrentUserProfilePicture()
        loadCurrentUsername()
    }

    private fun getCurrentUserProfilePicture() {
        viewModelScope.launch {
            val profilePictureUrl = userRepository.getCurrentUserProfilePictureUrl()
            _profilePictureUrl.value = profilePictureUrl
        }
    }

    private fun loadCurrentUsername() {
        viewModelScope.launch {
            val username = userRepository.getCurrentUserName()
            _username.value = username
        }
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            _userId.value = userId
        }
    }
}
