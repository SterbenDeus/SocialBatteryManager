package com.example.socialbatterymanager.data.model

data class PrivacySettings(
    val socialEnergyVisibility: VisibilityLevel = VisibilityLevel.FRIENDS,
    val moodVisibility: VisibilityLevel = VisibilityLevel.CLOSE_FRIENDS,
    val activityVisibility: VisibilityLevel = VisibilityLevel.EVERYONE,
    val profileVisibility: VisibilityLevel = VisibilityLevel.EVERYONE
)

enum class VisibilityLevel {
    EVERYONE,       // All contacts can see
    FRIENDS,        // Friends and Close Friends can see
    CLOSE_FRIENDS,  // Only Close Friends can see
    PRIVATE         // Only user can see
}

object PrivacyManager {

    fun canViewSocialEnergy(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.socialEnergyVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS -> viewerLabel in listOf(PersonLabel.FRIEND, PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.PRIVATE -> false
        }
    }

    fun canViewMood(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.moodVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS -> viewerLabel in listOf(PersonLabel.FRIEND, PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.PRIVATE -> false
        }
    }

    fun canViewActivity(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.activityVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS -> viewerLabel in listOf(PersonLabel.FRIEND, PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.PRIVATE -> false
        }
    }

    fun canViewProfile(viewerLabel: PersonLabel, privacySettings: PrivacySettings): Boolean {
        return when (privacySettings.profileVisibility) {
            VisibilityLevel.EVERYONE -> true
            VisibilityLevel.FRIENDS -> viewerLabel in listOf(PersonLabel.FRIEND, PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.CLOSE_FRIENDS -> viewerLabel in listOf(PersonLabel.CLOSE_FRIEND, PersonLabel.FAMILY)
            VisibilityLevel.PRIVATE -> false
        }
    }
}
