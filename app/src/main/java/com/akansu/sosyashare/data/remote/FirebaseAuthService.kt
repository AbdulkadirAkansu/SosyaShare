package com.akansu.sosyashare.data.remote

import android.util.Log
import com.akansu.sosyashare.data.model.UserEntity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun registerUser(email: String, password: String, username: String): UserEntity {
        try {
            Log.d("RegisterUser", "Kayıt işlemi başladı: $email")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")
            Log.d("RegisterUser", "Firebase Authentication'da kullanıcı oluşturuldu. UID: $userId")

            val userMap = mapOf(
                "username" to username,
                "email" to email,
                "isEmailVerified" to false,
                "following" to emptyList<String>(),
                "followers" to emptyList<String>()
            )
            firestore.collection("users").document(userId).set(userMap).await()
            Log.d("RegisterUser", "Kullanıcı Firestore'da kaydedildi. UID: $userId")

            sendEmailVerification()
            Log.d("RegisterUser", "Email doğrulama gönderildi: $email")

            val privacyMap = mapOf(
                "userId" to userId,
                "isPrivate" to false,
                "allowedFollowers" to emptyList<String>()
            )
            firestore.collection("user_privacy").document(userId).set(privacyMap).await()
            Log.d(
                "RegisterUser",
                "Kullanıcı gizlilik ayarları Firestore'a kaydedildi. UID: $userId"
            )

            return UserEntity(
                id = userId,
                username = username,
                email = email,
                profilePictureUrl = null
            )
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreError", "Kayıt işlemi sırasında Firestore hatası: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("RegisterError", "Kayıt işlemi sırasında hata: ${e.message}")
            throw e
        }
    }

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): UserEntity {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val user = authResult.user ?: throw Exception("Kullanıcı bulunamadı")

        val userId = user.uid
        val username = account.displayName ?: "Unknown"
        val email = user.email ?: throw Exception("E-posta bulunamadı")

        val userMap = mapOf(
            "username" to username,
            "email" to email,
            "isEmailVerified" to true,
            "following" to emptyList<String>(),
            "followers" to emptyList<String>()
        )
        firestore.collection("users").document(userId).set(userMap).await()

        val privacyMap = mapOf(
            "userId" to userId,
            "isPrivate" to false,
            "allowedFollowers" to emptyList<String>()
        )
        firestore.collection("user_privacy").document(userId).set(privacyMap).await()

        return UserEntity(
            id = userId,
            username = username,
            email = email,
            profilePictureUrl = user.photoUrl?.toString()
        )
    }


    suspend fun getUserDetails(): UserEntity? {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("GetUserDetails", "Giriş yapmış kullanıcı bulunamadı.")
                throw Exception("Giriş yapmış kullanıcı bulunamadı.")
            }
            val userId = currentUser.uid  // Oturum açmış kullanıcının UID'sini al
            Log.d("GetUserDetails", "Kullanıcı bilgileri alınıyor: $userId")

            val document = firestore.collection("users").document(userId).get().await()
            Log.d("GetUserDetails", "Kullanıcı Firestore'dan alındı. UID: $userId")

            return document.toObject(UserEntity::class.java)?.copy(id = userId)
        } catch (e: Exception) {
            Log.e("GetUserDetailsError", "Kullanıcı bilgilerini alırken hata: ${e.message}")
            throw e
        }
    }


    suspend fun sendEmailVerification() {
        try {
            val user = auth.currentUser ?: throw Exception("No authenticated user")
            Log.d("EmailVerification", "Email doğrulaması gönderiliyor UID: ${user.uid}")
            user.sendEmailVerification().await()
            Log.d("EmailVerification", "Email doğrulaması başarıyla gönderildi.")
        } catch (e: Exception) {
            Log.e("EmailVerificationError", "Email doğrulaması gönderilemedi: ${e.message}")
            throw e
        }
    }

    suspend fun isUsernameUnique(username: String): Boolean {
        try {
            Log.d("UsernameCheck", "Kullanıcı adı kontrol ediliyor: $username")
            val user = getUserByUsername(username)
            val isUnique = user == null
            Log.d("UsernameCheck", "Kullanıcı adı benzersiz mi: $isUnique")
            return isUnique
        } catch (e: Exception) {
            Log.e("UsernameCheckError", "Kullanıcı adı kontrol hatası: ${e.message}")
            throw e
        }
    }

    private suspend fun getUserByUsername(username: String): Map<String, Any>? {
        try {
            Log.d("GetUserByUsername", "Kullanıcı adıyla kullanıcı aranıyor: $username")
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            val user = if (!querySnapshot.isEmpty) {
                querySnapshot.documents.first().data
            } else {
                null
            }
            Log.d("GetUserByUsername", "Kullanıcı adı araması sonucu: ${user != null}")
            return user
        } catch (e: Exception) {
            Log.e(
                "GetUserByUsernameError",
                "Kullanıcı adıyla kullanıcı aranırken hata: ${e.message}"
            )
            throw e
        }
    }

    suspend fun loginUser(email: String, password: String): UserEntity? {
        try {
            Log.d("LoginUser", "Giriş işlemi başladı: $email")
            auth.signInWithEmailAndPassword(email, password).await()
            val currentUser = auth.currentUser ?: throw Exception("Kullanıcı bulunamadı.")
            Log.d("LoginUser", "Firebase Authentication ile giriş yapıldı. UID: ${currentUser.uid}")

            currentUser.reload().await() // Kullanıcı bilgilerini güncelle
            if (!currentUser.isEmailVerified) {
                Log.e("LoginUser", "Email doğrulanmamış. UID: ${currentUser.uid}")
                throw Exception("Email doğrulanmamış. Lütfen giriş yapmadan önce email adresinizi doğrulayın.")
            }

            val userId = currentUser.uid
            val document = firestore.collection("users").document(userId).get().await()
            Log.d("LoginUser", "Kullanıcı Firestore'dan alındı. UID: $userId")

            updateEmailVerifiedStatus(userId)

            return document.toObject(UserEntity::class.java)?.copy(id = userId)
        } catch (e: FirebaseFirestoreException) {
            Log.e("FirestoreError", "İzin eksikliği veya başka bir sorun: ${e.message}")
            throw Exception("İzin eksikliği: Verilere erişim sağlanamıyor.")
        } catch (e: Exception) {
            Log.e("LoginError", "Giriş sırasında hata: ${e.message}")
            throw e
        }
    }

    suspend fun updateEmailVerifiedStatus(userId: String) {
        try {
            Log.d("UpdateEmailVerified", "Email doğrulama durumu güncelleniyor UID: $userId")
            val user = auth.currentUser ?: throw Exception("No authenticated user")
            user.reload().await() // Kullanıcı bilgisini güncelle

            if (user.isEmailVerified) {
                firestore.collection("users").document(userId)
                    .update("isEmailVerified", true)
                    .await()  // Email doğrulanmışsa Firestore'da güncelle
                Log.d("UpdateEmailVerified", "Firestore'da email doğrulama durumu güncellendi.")
            }
        } catch (e: Exception) {
            Log.e(
                "UpdateEmailVerifiedError",
                "Email doğrulama durumu güncellenirken hata: ${e.message}"
            )
            throw e
        }
    }

    suspend fun resetPassword(email: String) {
        try {
            Log.d("ResetPassword", "Şifre sıfırlama işlemi başlatıldı: $email")
            auth.sendPasswordResetEmail(email).await()
            Log.d("ResetPassword", "Şifre sıfırlama emaili gönderildi: $email")
        } catch (e: Exception) {
            Log.e("ResetPasswordError", "Şifre sıfırlama emaili gönderilirken hata: ${e.message}")
            throw e
        }
    }

    suspend fun reloadUser() {
        try {
            val user = auth.currentUser ?: throw Exception("No authenticated user")
            Log.d("ReloadUser", "Kullanıcı bilgileri yeniden yükleniyor UID: ${user.uid}")
            user.reload().await() // Refresh user data
            Log.d("ReloadUser", "Kullanıcı bilgileri başarıyla yeniden yüklendi.")
        } catch (e: Exception) {
            Log.e("ReloadUserError", "Kullanıcı bilgileri yeniden yüklenirken hata: ${e.message}")
            throw e
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun logoutUser() {
        try {
            Log.d("LogoutUser", "Kullanıcı çıkış işlemi başlatıldı.")
            auth.signOut()
            Log.d("LogoutUser", "Kullanıcı başarıyla çıkış yaptı.")
        } catch (e: Exception) {
            Log.e("LogoutUserError", "Çıkış sırasında hata: ${e.message}")
            throw e
        }
    }

    suspend fun syncAllUsers(): List<UserEntity> {
        try {
            Log.d("SyncAllUsers", "Tüm kullanıcılar Firestore'dan alınıyor.")
            val result = firestore.collection("users").get().await()
            val users = mutableListOf<UserEntity>()
            for (document in result.documents) {
                val user = document.toObject(UserEntity::class.java)?.copy(id = document.id)
                if (user != null) {
                    users.add(user)
                }
            }
            Log.d("SyncAllUsers", "Tüm kullanıcılar başarıyla alındı: ${users.size} kullanıcı")
            return users
        } catch (e: Exception) {
            Log.e("SyncAllUsersError", "Kullanıcılar alınırken hata: ${e.message}")
            throw e
        }
    }
}
