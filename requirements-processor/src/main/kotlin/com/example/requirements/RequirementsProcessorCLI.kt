package com.example.requirements

import java.io.File

/**
 * Command-line interface for the requirements processor
 */
object RequirementsProcessorCLI {
    
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            showUsage()
            return
        }
        
        val command = args[0]
        
        when (command) {
            "process" -> {
                if (args.size < 2) {
                    println("Error: Please provide a requirements file path")
                    showUsage()
                    return
                }
                
                val filePath = args[1]
                val outputDir = if (args.size > 2) args[2] else "generated"
                
                processRequirementsFile(filePath, outputDir)
            }
            "demo" -> {
                runDemo()
            }
            "help" -> {
                showUsage()
            }
            else -> {
                println("Unknown command: $command")
                showUsage()
            }
        }
    }
    
    private fun processRequirementsFile(filePath: String, outputDir: String) {
        val file = File(filePath)
        if (!file.exists()) {
            println("Error: File not found: $filePath")
            return
        }
        
        println("Processing requirements from: $filePath")
        println("Output directory: $outputDir")
        
        val processor = RequirementProcessor()
        val result = processor.processRequirements(file)
        
        // Create output directory
        val outputDirFile = File(outputDir)
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs()
        }
        
        // Write generated files
        result.generatedFiles.forEach { generatedFile ->
            val outputFile = File(outputDir, generatedFile.path)
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(generatedFile.content)
            println("Generated: ${generatedFile.path}")
        }
        
        println("\nGeneration complete!")
        println("Summary: ${result.summary}")
        println("Project: ${result.projectName}")
        println("Files generated: ${result.generatedFiles.size}")
    }
    
    private fun runDemo() {
        println("Running requirements processor demo...")
        
        // Create sample requirements files
        val sampleFiles = createSampleRequirements()
        
        sampleFiles.forEach { (name, file) ->
            println("\n=== Processing $name ===")
            val processor = RequirementProcessor()
            val result = processor.processRequirements(file)
            
            println("Project: ${result.projectName}")
            println("Summary: ${result.summary}")
            println("Generated files:")
            result.generatedFiles.forEach { 
                println("  - ${it.path} (${it.type})")
            }
        }
    }
    
    private fun createSampleRequirements(): List<Pair<String, File>> {
        val samples = mutableListOf<Pair<String, File>>()
        
        // Sample 1: JSON requirements
        val jsonRequirements = """
            {
                "projectName": "TaskManager",
                "description": "A task management application with user authentication and task tracking",
                "features": [
                    {
                        "name": "Authentication",
                        "description": "User login and registration system",
                        "type": "SERVICE"
                    },
                    {
                        "name": "TaskList",
                        "description": "Display and manage tasks",
                        "type": "COMPONENT"
                    },
                    {
                        "name": "TaskStorage",
                        "description": "Local database for task persistence",
                        "type": "SERVICE"
                    }
                ]
            }
        """.trimIndent()
        
        val jsonFile = File.createTempFile("sample_json", ".json")
        jsonFile.writeText(jsonRequirements)
        samples.add("JSON Requirements" to jsonFile)
        
        // Sample 2: YAML requirements
        val yamlRequirements = """
            project: WeatherApp
            description: A weather application with location services
            features:
              - feature: LocationService
              - feature: WeatherAPI
              - feature: WeatherDisplay
        """.trimIndent()
        
        val yamlFile = File.createTempFile("sample_yaml", ".yml")
        yamlFile.writeText(yamlRequirements)
        samples.add("YAML Requirements" to yamlFile)
        
        // Sample 3: Text requirements
        val textRequirements = """
            E-commerce Mobile App
            
            Create a mobile application for online shopping with the following features:
            - User authentication and login system
            - Product catalog with search functionality
            - Shopping cart and checkout process
            - Order history and tracking
            - Payment integration
            - User profile management
            
            The app should have a modern UI with smooth navigation and should work offline for basic features.
        """.trimIndent()
        
        val textFile = File.createTempFile("sample_text", ".txt")
        textFile.writeText(textRequirements)
        samples.add("Text Requirements" to textFile)
        
        return samples
    }
    
    private fun showUsage() {
        println("""
            Requirements Processor CLI
            
            Usage:
              java -jar requirements-processor.jar <command> [options]
            
            Commands:
              process <file> [output_dir]  Process requirements file and generate code
              demo                         Run demo with sample requirements
              help                         Show this help message
            
            Examples:
              java -jar requirements-processor.jar process requirements.json generated/
              java -jar requirements-processor.jar demo
              java -jar requirements-processor.jar help
            
            Supported file formats:
              - JSON (.json)
              - YAML (.yml, .yaml)
              - Plain text (.txt)
            
            The processor can handle complex requirements including:
              - Multi-feature applications
              - Different component types (UI, Services, Utilities)
              - Automatic code structure generation
              - Build configuration generation
        """.trimIndent())
    }
}
