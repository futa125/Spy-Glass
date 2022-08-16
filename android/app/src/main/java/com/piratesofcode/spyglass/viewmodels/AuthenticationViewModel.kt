package com.piratesofcode.spyglass.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.piratesofcode.spyglass.api.AuthenticationRepository
import com.piratesofcode.spyglass.api.DocumentRepository
import com.piratesofcode.spyglass.api.NotificationRepository
import com.piratesofcode.spyglass.utils.AuthHelper
import com.piratesofcode.spyglass.utils.NotificationHelper
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {

    private var loginResultLiveData: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    private var registrationResultLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    fun getLoginResultLiveData(): LiveData<Boolean?> {
        return loginResultLiveData
    }

    fun getRegistrationResultLiveData(): LiveData<Boolean> {
        return registrationResultLiveData
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginResultLiveData.value = try {
                AuthenticationRepository.login(email, password)
                val userId = AuthenticationRepository.getCurrentFirebaseUserId()
                val user = AuthenticationRepository.getUserFromFirestore(userId)

                AuthenticationRepository.user = user

                true

            } catch (e: FirebaseAuthException) {
                false
            }
        }
    }

    fun register(
        email: String, password: String, firstName: String, lastName: String,
        role: String, specialization: String?
    ) {
        viewModelScope.launch {
            registrationResultLiveData.value = try {
                AuthenticationRepository.register(email, password)
                val userId = AuthenticationRepository.getCurrentFirebaseUserId()
                val user =
                    AuthHelper.createUser(userId, email, firstName, lastName, role, specialization)

                AuthenticationRepository.saveUserToFirestore(user)
                AuthenticationRepository.user = user

                true

            } catch (e: FirebaseAuthException) {
                false
            }
        }
    }

    fun checkUserLoggedIn() {
        viewModelScope.launch {
            loginResultLiveData.value =
                if (AuthenticationRepository.getCurrentFirebaseUser() != null) {
                    val userId = AuthenticationRepository.getCurrentFirebaseUserId()
                    AuthenticationRepository.user =
                        AuthenticationRepository.getUserFromFirestore(userId)
                    true

                } else {
                    null
                }
        }
    }

    fun logout() {
        AuthenticationRepository.logout()
        AuthenticationRepository.user = null

        //delete token since user is now logged in and must not receive notifications
        NotificationHelper.deleteToken()
        DocumentRepository.firstRun = true
    }

    fun storeFCMTokenToDB() {
        //store FCM token to database
        NotificationRepository.storeTokenToDatabase(AuthenticationRepository.user!!.id)
    }
}
