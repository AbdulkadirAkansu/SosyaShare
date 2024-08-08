package com.akansu.sosyashare.presentation.login.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.domain.repository.AuthRepository
import com.akansu.sosyashare.domain.usecase.*
import com.akansu.sosyashare.domain.usecase.login.LogoutUserUseCase
import com.akansu.sosyashare.domain.usecase.login.ReloadUserUseCase
import com.akansu.sosyashare.domain.usecase.login.SendEmailVerificationUseCase
import com.akansu.sosyashare.domain.usecase.login.SyncAllUsersUseCase
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val reloadUserUseCase: ReloadUserUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val syncAllUsersUseCase: SyncAllUsersUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun isUsernameUnique(username: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isUnique = authRepository.isUsernameUnique(username)
            onResult(isUnique)
        }
    }

    fun registerUser(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                authRepository.registerUser(email, password, username)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun sendEmailVerification(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                sendEmailVerificationUseCase()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.resetPassword(email)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun getUserDetails(userId: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val userDetails = authRepository.getUserDetails(userId)
            onResult(userDetails)
        }
    }

    fun reloadUser(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                reloadUserUseCase()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                authRepository.loginUser(email, password)
                reloadUserUseCase()
                syncAllUsersUseCase() // Kullanıcı verilerini senkronize et
                val user = authRepository.getCurrentUser()
                if (user?.isEmailVerified == true) {
                    saveLoginState(true)
                    onSuccess()
                } else {
                    throw Exception("Email not verified")
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun logoutUser(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                logoutUserUseCase()
                saveLoginState(false)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}
