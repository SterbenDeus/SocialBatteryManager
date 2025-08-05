package com.example.requirements

import java.io.File

/**
 * Simple test runner for the requirements processor
 */
object RequirementsTest {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Requirements Processor Test Suite ===")
        
        testBasicProcessing()
        testComplexRequirements()
        testWebInterface()
        testComplexityAssessment()
        
        println("\n=== All tests completed ===")
    }
    
    private fun testBasicProcessing() {
        println("\n1. Testing basic requirements processing...")
        
        val simpleRequirements = """
            {
                "projectName": "TestApp",
                "description": "A simple test application",
                "features": [
                    {
                        "name": "MainScreen",
                        "description": "Main application screen",
                        "type": "COMPONENT"
                    },
                    {
                        "name": "DataService",
                        "description": "Data processing service",
                        "type": "SERVICE"
                    }
                ]
            }
        """.trimIndent()
        
        val tempFile = File.createTempFile("test_requirements", ".json")
        tempFile.writeText(simpleRequirements)
        
        val processor = RequirementProcessor()
        val result = processor.processRequirements(tempFile)
        
        println("✓ Project: ${result.projectName}")
        println("✓ Generated ${result.generatedFiles.size} files")
        println("✓ Summary: ${result.summary}")
        
        // Show sample generated code
        result.generatedFiles.take(2).forEach { file ->
            println("\n--- ${file.path} ---")
            println(file.content.take(200) + "...")
        }
        
        tempFile.delete()
    }
    
    private fun testComplexRequirements() {
        println("\n2. Testing complex requirements processing...")
        
        val complexRequirements = """
            Advanced E-commerce Platform
            
            Create a comprehensive e-commerce platform with the following features:
            
            User Management:
            - User registration and authentication
            - User profiles and preferences
            - Admin user management
            
            Product Management:
            - Product catalog with categories
            - Product search and filtering
            - Product reviews and ratings
            - Inventory management
            
            Shopping Experience:
            - Shopping cart functionality
            - Wishlist management
            - Order processing and tracking
            - Payment integration
            
            Additional Features:
            - Push notifications
            - Analytics and reporting
            - Multi-language support
            - Offline functionality
            
            Technical Requirements:
            - Modern Android UI
            - REST API integration
            - Local database storage
            - Real-time updates
        """.trimIndent()
        
        val tempFile = File.createTempFile("complex_requirements", ".txt")
        tempFile.writeText(complexRequirements)
        
        val processor = RequirementProcessor()
        val result = processor.processRequirements(tempFile)
        
        println("✓ Complex project: ${result.projectName}")
        println("✓ Generated ${result.generatedFiles.size} files")
        println("✓ Features detected and processed")
        
        // Show file types generated
        val fileTypes = result.generatedFiles.groupBy { it.type }
        fileTypes.forEach { (type, files) ->
            println("  - ${type.name}: ${files.size} files")
        }
        
        tempFile.delete()
    }
    
    private fun testWebInterface() {
        println("\n3. Testing web interface...")
        
        val webInterface: RequirementsWebInterface = RequirementsWebInterfaceImpl()
        
        // Test with sample requirements
        val sampleRequirements = webInterface.generateSampleRequirements()
        
        println("✓ Generated ${sampleRequirements.size} sample requirements")
        
        // Process one of the samples
        val sample = sampleRequirements[1] // E-commerce platform
        val result = webInterface.processWebRequirements(
            sample.content,
            "ecommerce.json"
        )
        
        if (result.success) {
            println("✓ Web processing successful")
            println("  - Project: ${result.projectName}")
            println("  - Files: ${result.files.size}")
            println("  - Message: ${result.message}")
        } else {
            println("✗ Web processing failed: ${result.message}")
        }
    }
    
    private fun testComplexityAssessment() {
        println("\n4. Testing complexity assessment...")
        
        val webInterface: RequirementsWebInterface = RequirementsWebInterfaceImpl()
        
        val testCases = listOf(
            "Simple app with login" to "Simple mobile app with user login feature",
            "Moderate complexity" to """
                Social media app with user profiles, post sharing, messaging, 
                notifications, and friend management features
            """.trimIndent(),
            "High complexity" to """
                Enterprise-level application with user management, role-based access control,
                reporting dashboard, data analytics, real-time notifications, multi-tenant support,
                API integration, advanced search, file management, audit logging,
                backup and recovery, and comprehensive admin panel
            """.trimIndent()
        )
        
        testCases.forEach { (name, requirements) ->
            val assessment = webInterface.assessComplexity(requirements)
            println("✓ $name:")
            println("  - Level: ${assessment.level}")
            println("  - Estimated files: ${assessment.estimatedFiles}")
            println("  - Features: ${assessment.features}")
            println("  - Recommendations: ${assessment.recommendations.size}")
        }
    }
}

/**
 * Demo runner that shows the capabilities of the system
 */
object RequirementsDemo {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Requirements-Based Code Generation Demo ===")
        println("This system can handle requirements of varying complexity levels.")
        println()
        
        showCapabilities()
        runInteractiveDemo()
    }
    
    private fun showCapabilities() {
        println("System Capabilities:")
        println("• Parse requirements from JSON, YAML, or plain text")
        println("• Generate complete Android app structure")
        println("• Create components, services, and utilities")
        println("• Generate build configurations and manifests")
        println("• Assess complexity and provide recommendations")
        println("• Handle simple to very complex requirements")
        println()
        
        println("Supported Features:")
        println("• User authentication systems")
        println("• Database integration")
        println("• REST API clients")
        println("• UI components and layouts")
        println("• Background services")
        println("• Utility classes")
        println("• Navigation and routing")
        println("• And much more...")
        println()
    }
    
    private fun runInteractiveDemo() {
        val webInterface: RequirementsWebInterface = RequirementsWebInterfaceImpl()
        val samples = webInterface.generateSampleRequirements()
        
        println("Available Sample Requirements:")
        samples.forEachIndexed { index, sample ->
            println("${index + 1}. ${sample.name}")
            println("   ${sample.description}")
            println("   Format: ${sample.format}")
            println()
        }
        
        // Process all samples
        samples.forEach { sample ->
            println("=== Processing: ${sample.name} ===")
            
            val assessment = webInterface.assessComplexity(sample.content)
            println("Complexity: ${assessment.level}")
            println("Estimated files: ${assessment.estimatedFiles}")
            println("Features detected: ${assessment.features}")
            
            val result = webInterface.processWebRequirements(sample.content, "sample.${sample.format.lowercase()}")
            
            if (result.success) {
                println("✓ Processing successful!")
                println("  Project: ${result.projectName}")
                println("  Files generated: ${result.files.size}")
                println("  Summary: ${result.summary}")
                
                // Show some generated files
                result.files.take(3).forEach { file ->
                    println("  Generated: ${file.path} (${file.type})")
                }
            } else {
                println("✗ Processing failed: ${result.message}")
            }
            
            println()
        }
    }
}