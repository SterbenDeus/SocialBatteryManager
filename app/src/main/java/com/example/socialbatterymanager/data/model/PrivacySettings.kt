package com.example.socialbatterymanager.data.model

data class PrivacySettings(
    val socialEnergyVisibility: VisibilityLevel = VisibilityLevel.FRIENDS_ONLY,
    val moodVisibility: VisibilityLevel = VisibilityLevel.CLOSE_FRIENDS,
    val activityVisibility: VisibilityLevel = VisibilityLevel.EVERYONE,
    val profileVisibility: VisibilityLevel = VisibilityLevel.EVERYONE
)

object PrivacyManager {

    fun canViewSocialEnergy(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.socialEnergyVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS_ONLY -> viewerLabel in listOf(
                PersonLabel.FRIEND,
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.ONLY_ME -> false
        }
    }

    fun canViewMood(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.moodVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS_ONLY -> viewerLabel in listOf(
                PersonLabel.FRIEND,
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.ONLY_ME -> false
        }
    }

    fun canViewActivity(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.activityVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS_ONLY -> viewerLabel in listOf(
                PersonLabel.FRIEND,
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.ONLY_ME -> false
        }
    }

    fun canViewProfile(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.profileVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS_ONLY -> viewerLabel in listOf(
                PersonLabel.FRIEND,
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(
                PersonLabel.CLOSE_FRIEND,
                PersonLabel.FAMILY
            )
            VisibilityLevel.ONLY_ME -> false
        }
    }
}
