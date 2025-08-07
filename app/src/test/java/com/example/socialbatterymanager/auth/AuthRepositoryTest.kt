package com.example.socialbatterymanager.auth

import com.example.socialbatterymanager.features.auth.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.Test

class AuthRepositoryTest {

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val authRepository = AuthRepository(firebaseAuth)

    @Test
    fun `currentUser returns value from FirebaseAuth`() {
        val user: FirebaseUser = mockk()
        every { firebaseAuth.currentUser } returns user

        assertEquals(user, authRepository.currentUser)
    }

    @Test
    fun `signOut delegates to FirebaseAuth`() {
        authRepository.signOut()

        verify { firebaseAuth.signOut() }
    }
}
