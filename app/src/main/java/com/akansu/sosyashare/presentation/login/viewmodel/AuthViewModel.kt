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
import com.google.firebase.firestore.FirebaseFirestoreException
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
            try {
                val isUnique = authRepository.isUsernameUnique(username)
                onResult(isUnique)
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    // İnternet bağlantısı yok
                    Log.e("NetworkError", "İnternet bağlantısı kontrolünde hata: ${e.message}")
                }
                // Hata durumunda varsayılan olarak false dön (benzersiz değil)
                onResult(false)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "isUsernameUnique hata: ${e.message}")
                onResult(false)
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("GoogleSignIn", "Google hesabı alındı: ${account.email}")
        
        val idToken = account.idToken
        if (idToken == null) {
            Log.e("GoogleSignIn", "ID Token bulunamadı")
            onFailure(Exception("Google kimlik bilgileri eksik."))
            return
        }
        
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        
        Log.d("GoogleSignIn", "Firebase kimlik doğrulama başlatılıyor...")
        
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "Firebase kimlik doğrulama başarılı!")
                    val user = FirebaseAuth.getInstance().currentUser
                    
                    if (user != null) {
                        viewModelScope.launch {
                            try {
                                // Kullanıcı bilgilerini Firestore'a kaydet (eğer yoksa)
                                val userEntity = authRepository.firebaseAuthWithGoogle(account)
                                Log.d("GoogleSignIn", "Kullanıcı Firestore'a kaydedildi: ${userEntity.id}")
                                
                                // Oturum durumunu kaydet
                                saveLoginState(true)
                                
                                // Başarılı callback'i çağır
                                onSuccess()
                            } catch (e: FirebaseFirestoreException) {
                                // Firestore hatası aldık, internet bağlantısı kontrol et
                                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                                    Log.e("NetworkError", "İnternet bağlantısı yok, ancak kimlik doğrulama başarılı")
                                    // Yine de giriş yapabilmesine izin ver
                                    saveLoginState(true)
                                    onSuccess()
                                } else {
                                    Log.e("FirestoreError", "Firestore işleminde hata: ${e.message}")
                                    // Diğer Firestore hataları için kullanıcıya bildirim göster
                                    onFailure(e)
                                }
                            } catch (e: Exception) {
                                Log.e("GoogleSignIn", "Firestore işleminde beklenmeyen hata: ${e.message}")
                                // Yine de giriş yapabilmesine izin ver
                                saveLoginState(true)
                                onSuccess()
                            }
                        }
                    } else {
                        Log.e("GoogleSignIn", "Firebase kullanıcısı null")
                        onFailure(Exception("Giriş başarısız: Kullanıcı bulunamadı"))
                    }
                } else {
                    Log.e("GoogleSignIn", "Firebase kimlik doğrulama başarısız: ${task.exception?.message}")
                    onFailure(task.exception ?: Exception("Google ile giriş yapılamadı"))
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
                    onFailure(Exception("Bu kullanıcı adı zaten alınmış. Lütfen farklı bir kullanıcı adı seçin."))
                    return@launch
                }

                authRepository.registerUser(email, password, username)
                onSuccess()
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    onFailure(Exception("İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."))
                } else {
                    onFailure(e)
                }
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
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    onFailure(Exception("İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."))
                } else {
                    onFailure(e)
                }
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
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    onFailure(Exception("İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."))
                } else {
                    onFailure(e)
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun getUserDetails(onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val userDetails = authRepository.getUserDetails()
                onResult(userDetails)
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    Log.e("NetworkError", "İnternet bağlantısı yok, kullanıcı detayları alınamadı")
                }
                onResult(null)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "getUserDetails hata: ${e.message}")
                onResult(null)
            }
        }
    }

    fun reloadUser(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.reloadUser()
                onSuccess()
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    onFailure(Exception("İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."))
                } else {
                    onFailure(e)
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun updateEmailVerifiedStatus(userId: String) {
        viewModelScope.launch {
            try {
                authRepository.updateEmailVerifiedStatus(userId)
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    Log.e("NetworkError", "İnternet bağlantısı yok, email doğrulama durumu güncellenemedi")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "updateEmailVerifiedStatus hata: ${e.message}")
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
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    onFailure(Exception("İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."))
                } else {
                    onFailure(e)
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
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    onFailure(Exception("İnternet bağlantısı yok. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."))
                } else {
                    onFailure(e)
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
