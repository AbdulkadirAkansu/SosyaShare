package com.akansu.sosyashare.presentation.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akansu.sosyashare.domain.model.Notification
import com.akansu.sosyashare.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Load notifications
    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            try {
                val loadedNotifications = notificationRepository.getNotificationsByUserId(userId)
                _notifications.value = loadedNotifications
            } catch (e: Exception) {
                _error.value = "Failed to load notifications: ${e.message}"
            }
        }
    }

    // Bildirimleri yükle ve okundu olarak işaretle
    fun loadNotificationsAndMarkAsRead(userId: String) {
        viewModelScope.launch {
            try {
                val loadedNotifications = notificationRepository.getNotificationsByUserId(userId)
                _notifications.value = loadedNotifications

                // Bildirimlerin tamamını okundu olarak işaretle
                loadedNotifications.forEach { notification ->
                    if (!notification.isRead) {
                        notificationRepository.markNotificationAsRead(notification.documentId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load notifications: ${e.message}"
            }
        }
    }

    // Mark notification as read
// NotificationViewModel.kt
    fun deleteNotification(notificationDocumentId: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "Silme işlemine başlandı: $notificationDocumentId")
                notificationRepository.deleteNotification(notificationDocumentId)
                Log.d("NotificationViewModel", "Bildirim silindi: $notificationDocumentId")
                loadNotifications(_notifications.value.firstOrNull()?.userId ?: "")
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Silme işlemi başarısız: ${e.message}")
            }
        }
    }

    fun markNotificationAsRead(notificationDocumentId: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "Okundu olarak işaretleniyor: $notificationDocumentId")
                notificationRepository.markNotificationAsRead(notificationDocumentId)
                loadNotifications(_notifications.value.firstOrNull()?.userId ?: "")
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Bildirimi okundu olarak işaretleme hatası id=$notificationDocumentId: ${e.message}")
            }
        }
    }
}