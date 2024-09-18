package com.akansu.sosyashare.presentation.login.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
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
                // Önce kullanıcı adının benzersiz olup olmadığını kontrol et
                val isUnique = authRepository.isUsernameUnique(username)
                if (!isUnique) {
                    onFailure(Exception("This username is already taken. Please choose a different one."))
                    return@launch
                }

                // Kullanıcı adı benzersizse, kayıt işlemini gerçekleştir
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
                authRepository.sendEmailVerification()
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

    fun getUserDetails(onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val userDetails = authRepository.getUserDetails()
            onResult(userDetails)
        }
    }

    fun reloadUser(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.reloadUser()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun updateEmailVerifiedStatus(userId: String) {
        viewModelScope.launch {
            try {
                authRepository.updateEmailVerifiedStatus(userId)
            } catch (e: Exception) {
            }
        }
    }


    fun reloadUserAndCheckVerification(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.reloadUser()
                val currentUser = authRepository.getCurrentUser()

                if (currentUser?.isEmailVerified == true) {
                    // Email doğrulandıysa, Firestore'da güncelle
                    authRepository.updateEmailVerifiedStatus(currentUser.uid)
                    onSuccess()
                } else {
                    throw Exception("Email not verified")
                }
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
                val currentUser = authRepository.getCurrentUser()

                // Email doğrulandı mı diye kontrol et
                if (currentUser?.isEmailVerified == true) {
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
                authRepository.logoutUser()
                saveLoginState(false)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun getCurrentUserProfilePictureUrl(): String? {
        return runBlocking { authRepository.getCurrentUserProfilePictureUrl() }
    }
}
