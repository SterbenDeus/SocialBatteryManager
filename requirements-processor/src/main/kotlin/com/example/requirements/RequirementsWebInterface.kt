package com.example.requirements

/**
 * Contract for web-based interactions with the requirements processor.
 * Implementations provide facilities for processing uploaded requirement
 * documents, supplying sample requirements and assessing project complexity.
 */
interface RequirementsWebInterface {

    /**
     * Process requirements supplied by a web client.
     *
     * @param fileContent contents of the uploaded requirements file
     * @param fileName name of the uploaded file for extension detection
     * @param outputDir optional directory for generated files
     * @return [WebProcessingResult] describing the processing outcome
     */
    fun processWebRequirements(
        fileContent: String,
        fileName: String,
        outputDir: String = "generated"
    ): WebProcessingResult

    /**
     * Provide example requirements that can be presented in a UI.
     *
     * @return list of available sample requirements
     */
    fun generateSampleRequirements(): List<SampleRequirement>

    /**
     * Estimate the complexity of a plain-text requirements description.
     *
     * @param requirements raw requirements text
     * @return [ComplexityAssessment] describing the project scale
     */
    fun assessComplexity(requirements: String): ComplexityAssessment
}

// Supporting data classes

/** Result returned from processing uploaded requirements. */
data class WebProcessingResult(
    val success: Boolean,
    val projectName: String,
    val summary: String,
    val files: List<WebOutputFile>,
    val message: String
)

/** Description of a generated file produced by the processor. */
data class WebOutputFile(
    val path: String,
    val content: String,
    val type: String,
    val size: Int
)

/** Sample requirement that can be offered to users for demonstration. */
data class SampleRequirement(
    val name: String,
    val description: String,
    val format: String,
    val content: String
)

/** Assessment of requirement complexity and related metrics. */
data class ComplexityAssessment(
    val level: ComplexityLevel,
    val estimatedFiles: Int,
    val estimatedLines: Int,
    val features: Int,
    val recommendations: List<String>
)

/**
 * Rough categories used when assessing a requirement's size and effort.
 */
enum class ComplexityLevel {
    SIMPLE,
    MODERATE,
    COMPLEX,
    VERY_COMPLEX
}

