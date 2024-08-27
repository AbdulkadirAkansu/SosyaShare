package com.akansu.sosyashare.presentation.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.UserPrivacy
import com.akansu.sosyashare.domain.repository.UserPrivacyRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrivacyRepository: UserPrivacyRepository,
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
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Initialization failed: ${e.message}", e)
            }
        }
    }

    private fun loadUserPrivacySetting(userId: String) {
        viewModelScope.launch {
            try {
                val userPrivacy = userPrivacyRepository.getUserPrivacy(userId)
                userPrivacy?.let {
                    if (_isPrivate.value == null) {
                        _isPrivate.value = it.isPrivate
                    }
                } ?: createDefaultUserPrivacy(userId)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to load user privacy setting: ${e.message}", e)
            }
        }
    }

    private fun createDefaultUserPrivacy(userId: String) {
        viewModelScope.launch {
            val defaultUserPrivacy = UserPrivacy(
                userId = userId,
                isPrivate = false,
                allowedFollowers = emptyList()
            )
            try {
                userPrivacyRepository.updateUserPrivacy(defaultUserPrivacy)
                _isPrivate.value = defaultUserPrivacy.isPrivate
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to create default user privacy: ${e.message}", e)
            }
        }
    }

    private fun setupRealTimeUpdates(userId: String) {
        privacyListener?.remove()
        privacyListener = userPrivacyRepository.addUserPrivacySettingListener(userId) { isPrivate ->
            _isPrivate.value = isPrivate
        }
    }

    fun updateUserPrivacySetting(isPrivate: Boolean) {
        viewModelScope.launch {
            _userId.value?.let { id ->
                privacyListener?.remove()
                try {
                    userPrivacyRepository.updateUserPrivacySetting(id, isPrivate)
                    _isPrivate.value = isPrivate
                } catch (e: Exception) {
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
