package com.akansu.sosyashare.presentation.userprofile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.usecase.GetCurrentUserProfilePictureUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserProfilePictureUrlUseCase: GetCurrentUserProfilePictureUrlUseCase
) : ViewModel() {

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl

    init {
        getCurrentUserProfilePicture()
    }

    private fun getCurrentUserProfilePicture() {
        viewModelScope.launch {
            val profilePictureUrl = getCurrentUserProfilePictureUrlUseCase()
            _profilePictureUrl.value = profilePictureUrl
        }
    }
}
