package com.example.socialbatterymanager.features.auth

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.features.auth.ui.LoginFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {
    @Test
    fun signInUiIsDisplayed() {
        launchFragmentInContainer<LoginFragment>(themeResId = R.style.Theme_SocialBatteryManager)

        onView(withId(R.id.emailEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.passwordEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.signInButton)).check(matches(isDisplayed()))
    }
}
