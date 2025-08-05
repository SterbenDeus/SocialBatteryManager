package com.example.socialbatterymanager.features.people

import com.example.socialbatterymanager.data.model.PersonLabel
import com.example.socialbatterymanager.data.model.PrivacyManager
import com.example.socialbatterymanager.data.model.PrivacySettings
import com.example.socialbatterymanager.data.model.VisibilityLevel
import org.junit.Test
import org.junit.Assert.*

class PrivacyTest {

    @Test
    fun testSocialEnergyVisibility() {
        val settings = PrivacySettings(
            socialEnergyVisibility = VisibilityLevel.FRIENDS_ONLY
        )

        // Close friends should be able to see
        assertTrue(PrivacyManager.canViewSocialEnergy(PersonLabel.CLOSE_FRIEND, settings))
        
        // Friends should be able to see
        assertTrue(PrivacyManager.canViewSocialEnergy(PersonLabel.FRIEND, settings))
        
        // Coworkers should not be able to see
        assertFalse(PrivacyManager.canViewSocialEnergy(PersonLabel.COWORKER, settings))
        
        // Acquaintances should not be able to see
        assertFalse(PrivacyManager.canViewSocialEnergy(PersonLabel.ACQUAINTANCE, settings))
    }

    @Test
    fun testMoodVisibilityCloseFriendsOnly() {
        val settings = PrivacySettings(
            moodVisibility = VisibilityLevel.CLOSE_FRIENDS
        )

        // Close friends should be able to see
        assertTrue(PrivacyManager.canViewMood(PersonLabel.CLOSE_FRIEND, settings))
        
        // Regular friends should not be able to see
        assertFalse(PrivacyManager.canViewMood(PersonLabel.FRIEND, settings))
        
        // Coworkers should not be able to see
        assertFalse(PrivacyManager.canViewMood(PersonLabel.COWORKER, settings))
    }

    @Test
    fun testEveryoneCanSeeProfile() {
        val settings = PrivacySettings(
            profileVisibility = VisibilityLevel.EVERYONE
        )

        // Everyone should be able to see profile
        assertTrue(PrivacyManager.canViewProfile(PersonLabel.CLOSE_FRIEND, settings))
        assertTrue(PrivacyManager.canViewProfile(PersonLabel.FRIEND, settings))
        assertTrue(PrivacyManager.canViewProfile(PersonLabel.COWORKER, settings))
        assertTrue(PrivacyManager.canViewProfile(PersonLabel.ACQUAINTANCE, settings))
    }

    @Test
    fun testPrivateSettings() {
        val settings = PrivacySettings(
            socialEnergyVisibility = VisibilityLevel.ONLY_ME,
            moodVisibility = VisibilityLevel.ONLY_ME
        )

        // No one should be able to see private information
        assertFalse(PrivacyManager.canViewSocialEnergy(PersonLabel.CLOSE_FRIEND, settings))
        assertFalse(PrivacyManager.canViewMood(PersonLabel.CLOSE_FRIEND, settings))
        assertFalse(PrivacyManager.canViewSocialEnergy(PersonLabel.FRIEND, settings))
        assertFalse(PrivacyManager.canViewMood(PersonLabel.FRIEND, settings))
    }
}