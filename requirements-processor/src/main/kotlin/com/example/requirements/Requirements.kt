package com.example.requirements

import kotlinx.serialization.Serializable

@Serializable
data class Requirements(
    val projectName: String,
    val description: String,
    val features: List<Feature>
)

@Serializable
data class Feature(
    val name: String,
    val description: String,
    val type: FeatureType
)

@Serializable
enum class FeatureType {
    COMPONENT,  // UI components, fragments, activities
    SERVICE,    // Background services, data processing
    UTILITY     // Helper classes, utilities
}

