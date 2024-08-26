package com.akansu.sosyashare.presentation.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.repository.PrivateAccountRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val privateAccountRepository: PrivateAccountRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isPrivate = MutableStateFlow<Boolean?>(null)
    val isPrivate: StateFlow<Boolean?> = _isPrivate

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private var privacyListener: ListenerRegistration? = null

    init {
        initialize()
    }

    fun initialize() {
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId()
                if (userId != null) {
                    _userId.value = userId
                    loadUserPrivacySetting(userId)
                    setupRealTimeUpdates(userId)
                } else {
                    Log.e("SettingsViewModel", "Failed to get current user ID")
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Initialization failed", e)
            }
        }
    }

    private fun loadUserPrivacySetting(userId: String) {
        viewModelScope.launch {
            try {
                val privateAccount = privateAccountRepository.getPrivateAccount(userId)
                privateAccount?.let {
                    if (_isPrivate.value == null) {
                        _isPrivate.value = it.isPrivate
                        Log.d("SettingsViewModel", "Loaded isPrivate: ${_isPrivate.value}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to load user privacy setting", e)
            }
        }
    }

    private fun setupRealTimeUpdates(userId: String) {
        privacyListener?.remove()
        privacyListener = privateAccountRepository.addUserPrivacySettingListener(userId) { isPrivate ->
            _isPrivate.value = isPrivate
            Log.d("SettingsViewModel", "Real-time isPrivate updated: $isPrivate")
        }
    }

    fun updateUserPrivacySetting(isPrivate: Boolean) {
        viewModelScope.launch {
            _userId.value?.let { id ->
                privacyListener?.remove()
                try {
                    privateAccountRepository.updateUserPrivacySetting(id, isPrivate)
                    _isPrivate.value = isPrivate
                    Log.d("SettingsViewModel", "Successfully updated isPrivate to $isPrivate")
                } catch (e: Exception) {
                    Log.e("SettingsViewModel", "Failed to update isPrivate: ${e.message}")
                    loadUserPrivacySetting(id)
                } finally {
                    setupRealTimeUpdates(id)
                }
            }
        }
    }

    override fun onCleared() {
        privacyListener?.remove()
        super.onCleared()
    }
}
