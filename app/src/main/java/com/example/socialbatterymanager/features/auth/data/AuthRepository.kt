package com.example.socialbatterymanager.features.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return auth.signInWithEmailAndPassword(email, password).await()
    }
    
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult {
        return auth.createUserWithEmailAndPassword(email, password).await()
    }
    
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await()
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }
}