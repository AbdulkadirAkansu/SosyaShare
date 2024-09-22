package com.akansu.sosyashare.presentation.login.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.data.model.UserEntity
import com.akansu.sosyashare.domain.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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

    fun loginWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val email = account.email ?: throw Exception("Google account email is null")
                Log.d("LoginWithGoogle", "Email from Google: $email")

                val existingUser = authRepository.getUserByEmail(email)

                if (existingUser != null) {
                    Log.d("LoginWithGoogle", "Existing user found. Checking if Google account is already linked.")

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser == null) {
                        Log.e("LoginWithGoogle", "No current user to link Google account with.")
                        signInWithGoogleCredential(account, onSuccess, onFailure)
                        return@launch
                    }

                    val isGoogleLinked = currentUser.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }

                    if (isGoogleLinked) {
                        Log.d("LoginWithGoogle", "Google account is already linked. Signing in directly.")
                        saveLoginState(true)
                        onSuccess()
                    } else {
                        Log.d("LoginWithGoogle", "Google account not linked yet. Linking account.")
                        linkGoogleAccountWithEmailAccount(account, onSuccess, onFailure)
                    }
                } else {
                    Log.d("LoginWithGoogle", "No existing user found. Creating new user with Google.")
                    val userEntity = authRepository.firebaseAuthWithGoogle(account)
                    Log.d("LoginWithGoogle", "New user created: ${userEntity.email}")
                    saveLoginState(true)
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("LoginWithGoogle", "Google Sign-In failed: ${e.message}")
                onFailure(e)
            }
        }
    }

    fun signInWithGoogleCredential(account: GoogleSignInAccount, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "Successfully signed in with Google.")
                    saveLoginState(true)
                    onSuccess()
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Log.e("GoogleSignIn", "Failed to sign in with Google: $errorMessage")
                    onFailure(task.exception ?: Exception("Sign-in failed"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleSignIn", "Sign in failed: ${e.message}")
                onFailure(e)
            }
    }

    fun linkGoogleAccountWithEmailAccount(account: GoogleSignInAccount, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e("LinkGoogleAccount", "No current user to link Google account with.")
                    throw Exception("No current user to link Google account with.")
                }

                Log.d("LinkGoogleAccount", "Linking Google account to existing user.")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                currentUser.linkWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("LinkGoogleAccount", "Google account successfully linked.")
                        saveLoginState(true)
                        onSuccess()
                    } else {
                        val errorMessage = task.exception?.message ?: "Unknown error"
                        Log.e("LinkGoogleAccount", "Failed to link Google account: $errorMessage")
                        onFailure(task.exception ?: Exception("Linking failed"))
                    }
                }
            } catch (e: Exception) {
                Log.e("LinkGoogleAccount", "Linking Google account failed: ${e.message}")
                onFailure(e)
            }
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
