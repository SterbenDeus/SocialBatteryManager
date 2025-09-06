package com.example.requirements

import java.io.File

/**
 * Web interface for requirements processing.
 * Can be integrated into the Android app or run as a standalone service.
 */
class RequirementsWebInterfaceImpl : RequirementsWebInterface {

    private val processor = RequirementProcessor()

    override fun processWebRequirements(
        fileContent: String,
        fileName: String,
        outputDir: String
    ): WebProcessingResult {
        return try {
            val tempFile = File.createTempFile("web_requirements", getFileExtension(fileName))
            tempFile.writeText(fileContent)

            val result = processor.processRequirements(tempFile)
            val outputFiles = result.generatedFiles.map { generatedFile ->
                WebOutputFile(
                    path = generatedFile.path,
                    content = generatedFile.content,
                    type = generatedFile.type.name,
                    size = generatedFile.content.length
                )
            }

            tempFile.delete()

            WebProcessingResult(
                success = true,
                projectName = result.projectName,
                summary = result.summary,
                files = outputFiles,
                message = "Requirements processed successfully"
            )
        } catch (e: Exception) {
            WebProcessingResult(
                success = false,
                projectName = "",
                summary = "",
                files = emptyList(),
                message = "Error processing requirements: ${e.message}"
            )
        }
    }

    override fun generateSampleRequirements(): List<SampleRequirement> = listOf(
        SampleRequirement(
            name = "Simple Mobile App",
            description = "Basic mobile application with login and main features",
            format = "JSON",
            content = """
                {
                    "projectName": "MyApp",
                    "description": "A simple mobile application",
                    "features": [
                        {
                            "name": "Login",
                            "description": "User authentication system",
                            "type": "SERVICE"
                        },
                        {
                            "name": "Dashboard",
                            "description": "Main application dashboard",
                            "type": "COMPONENT"
                        }
                    ]
                }
            """.trimIndent()
        ),
        SampleRequirement(
            name = "E-commerce Platform",
            description = "Complex e-commerce application with multiple features",
            format = "JSON",
            content = """
                {
                    "projectName": "EcommerceApp",
                    "description": "Full-featured e-commerce mobile application",
                    "features": [
                        {
                            "name": "UserAuthentication",
                            "description": "User registration, login, and profile management",
                            "type": "SERVICE"
                        },
                        {
                            "name": "ProductCatalog",
                            "description": "Product listing and search functionality",
                            "type": "COMPONENT"
                        },
                        {
                            "name": "ShoppingCart",
                            "description": "Shopping cart management",
                            "type": "COMPONENT"
                        },
                        {
                            "name": "OrderManagement",
                            "description": "Order processing and tracking",
                            "type": "SERVICE"
                        },
                        {
                            "name": "PaymentProcessor",
                            "description": "Payment integration and processing",
                            "type": "SERVICE"
                        },
                        {
                            "name": "DatabaseHelper",
                            "description": "Local data storage utilities",
                            "type": "UTILITY"
                        }
                    ]
                }
            """.trimIndent()
        ),
        SampleRequirement(
            name = "Social Media App",
            description = "Social networking application with posts and messaging",
            format = "YAML",
            content = """
                project: SocialApp
                description: A social media application with messaging and posts
                features:
                  - feature: UserProfile
                  - feature: PostFeed
                  - feature: Messaging
                  - feature: NotificationSystem
                  - feature: MediaUpload
            """.trimIndent()
        ),
        SampleRequirement(
            name = "Fitness Tracker",
            description = "Health and fitness tracking application",
            format = "Text",
            content = """
                Fitness Tracker Application

                Create a comprehensive fitness tracking application with the following capabilities:

                Core Features:
                - User authentication and profile management
                - Activity tracking (steps, distance, calories)
                - Workout planning and logging
                - Progress tracking and analytics
                - Goal setting and achievement tracking

                Advanced Features:
                - Integration with wearable devices
                - Social features for sharing achievements
                - Nutritional tracking and meal planning
                - Sleep tracking and analysis
                - GPS tracking for outdoor activities

                Technical Requirements:
                - Modern Android UI with Material Design
                - Local database for offline functionality
                - API integration for data synchronization
                - Push notifications for reminders
                - Data visualization for progress tracking
            """.trimIndent()
        )
    )

    override fun assessComplexity(requirements: String): ComplexityAssessment {
        val words = requirements.split("\\s+".toRegex()).size
        val features = countFeatures(requirements)
        val complexity = when {
            features <= 2 && words <= 100 -> ComplexityLevel.SIMPLE
            features <= 5 && words <= 500 -> ComplexityLevel.MODERATE
            features <= 10 && words <= 1000 -> ComplexityLevel.COMPLEX
            else -> ComplexityLevel.VERY_COMPLEX
        }
        return ComplexityAssessment(
            level = complexity,
            estimatedFiles = features * 3 + 5,
            estimatedLines = words * 2,
            features = features,
            recommendations = getRecommendations(complexity)
        )
    }

    private fun countFeatures(requirements: String): Int {
        val featureKeywords = listOf(
            "feature", "component", "service", "module", "functionality",
            "login", "authentication", "database", "api", "ui", "interface"
        )
        var count = 0
        featureKeywords.forEach { keyword ->
            count += requirements.lowercase().split(keyword).size - 1
        }
        return maxOf(1, count / 2)
    }

    private fun getRecommendations(complexity: ComplexityLevel): List<String> = when (complexity) {
        ComplexityLevel.SIMPLE -> listOf(
            "Perfect for getting started",
            "Should generate quickly",
            "Good for learning the system"
        )
        ComplexityLevel.MODERATE -> listOf(
            "Good balance of features",
            "May require some customization",
            "Consider breaking into phases"
        )
        ComplexityLevel.COMPLEX -> listOf(
            "Consider modular architecture",
            "Plan for iterative development",
            "May need additional testing"
        )
        ComplexityLevel.VERY_COMPLEX -> listOf(
            "Break into smaller components",
            "Consider microservices architecture",
            "Plan for extensive testing and documentation",
            "May require multiple development phases"
        )
    }

    private fun getFileExtension(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) ".$extension" else ".txt"
    }
}
