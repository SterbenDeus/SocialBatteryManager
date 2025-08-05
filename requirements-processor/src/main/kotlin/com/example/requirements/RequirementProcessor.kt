package com.example.requirements

import kotlinx.serialization.json.Json
import java.io.File

/**
 * A comprehensive requirements processing system that can handle different types of requirements
 * and generate base code accordingly.
 */
class RequirementProcessor {
    
    private val json = Json { prettyPrint = true }
    
    /**
     * Process uploaded requirements and generate base code structure
     */
    fun processRequirements(requirementsFile: File): CodeGenerationResult {
        val requirements = parseRequirements(requirementsFile)
        return generateBaseCode(requirements)
    }
    
    /**
     * Parse requirements from uploaded file (supports JSON, YAML, or plain text)
     */
    private fun parseRequirements(file: File): Requirements {
        val content = file.readText()
        
        return when {
            file.extension.lowercase() == "json" -> parseJsonRequirements(content)
            file.extension.lowercase() in listOf("yml", "yaml") -> parseYamlRequirements(content)
            else -> parseTextRequirements(content)
        }
    }
    
    private fun parseJsonRequirements(content: String): Requirements {
        return try {
            json.decodeFromString(Requirements.serializer(), content)
        } catch (e: Exception) {
            // Fallback to simple structure if JSON is malformed
            Requirements(
                projectName = "UnknownProject",
                description = "Parsed from JSON",
                features = listOf(Feature("GeneratedFeature", content, FeatureType.COMPONENT))
            )
        }
    }
    
    private fun parseYamlRequirements(content: String): Requirements {
        // Simple YAML parsing (for demonstration)
        val lines = content.lines()
        var projectName = "UnknownProject"
        var description = "Parsed from YAML"
        val features = mutableListOf<Feature>()
        
        lines.forEach { line ->
            when {
                line.startsWith("project:") -> projectName = line.substringAfter(":").trim()
                line.startsWith("description:") -> description = line.substringAfter(":").trim()
                line.startsWith("- feature:") -> {
                    val featureName = line.substringAfter("feature:").trim()
                    features.add(Feature(featureName, "", FeatureType.COMPONENT))
                }
            }
        }
        
        return Requirements(projectName, description, features)
    }
    
    private fun parseTextRequirements(content: String): Requirements {
        // Extract key information from plain text
        val lines = content.lines().filter { it.isNotBlank() }
        val projectName = lines.firstOrNull() ?: "UnknownProject"
        val description = lines.drop(1).joinToString(" ")
        
        // Generate features based on keywords
        val features = mutableListOf<Feature>()
        
        // Look for common patterns
        when {
            content.contains("login", true) || content.contains("authentication", true) -> {
                features.add(Feature("AuthenticationSystem", "User login and authentication", FeatureType.SERVICE))
            }
            content.contains("database", true) || content.contains("storage", true) -> {
                features.add(Feature("DatabaseLayer", "Data storage and retrieval", FeatureType.SERVICE))
            }
            content.contains("ui", true) || content.contains("interface", true) -> {
                features.add(Feature("UserInterface", "User interface components", FeatureType.COMPONENT))
            }
            content.contains("api", true) || content.contains("rest", true) -> {
                features.add(Feature("ApiLayer", "REST API endpoints", FeatureType.SERVICE))
            }
        }
        
        // If no specific features detected, create a generic one
        if (features.isEmpty()) {
            features.add(Feature("MainFeature", description, FeatureType.COMPONENT))
        }
        
        return Requirements(projectName, description, features)
    }
    
    /**
     * Generate base code structure based on requirements
     */
    private fun generateBaseCode(requirements: Requirements): CodeGenerationResult {
        val generatedFiles = mutableListOf<GeneratedFile>()
        
        // Generate main project structure
        generatedFiles.add(generateMainActivity(requirements))
        
        // Generate feature-specific code
        requirements.features.forEach { feature ->
            when (feature.type) {
                FeatureType.COMPONENT -> generatedFiles.addAll(generateComponentCode(feature))
                FeatureType.SERVICE -> generatedFiles.addAll(generateServiceCode(feature))
                FeatureType.UTILITY -> generatedFiles.addAll(generateUtilityCode(feature))
            }
        }
        
        // Generate configuration files
        generatedFiles.add(generateBuildConfig(requirements))
        generatedFiles.add(generateManifest(requirements))
        
        return CodeGenerationResult(
            projectName = requirements.projectName,
            generatedFiles = generatedFiles,
            summary = "Generated ${generatedFiles.size} files for ${requirements.features.size} features"
        )
    }
    
    private fun generateMainActivity(requirements: Requirements): GeneratedFile {
        val code = """
            package com.example.${requirements.projectName.lowercase()}
            
            import android.os.Bundle
            import androidx.appcompat.app.AppCompatActivity
            import androidx.fragment.app.Fragment
            
            /**
             * Main activity for ${requirements.projectName}
             * Generated from requirements: ${requirements.description}
             */
            class MainActivity : AppCompatActivity() {
                
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContentView(R.layout.activity_main)
                    
                    // Initialize features
                    ${requirements.features.joinToString("\n        ") { "// Initialize ${it.name}" }}
                    
                    setupUI()
                }
                
                private fun setupUI() {
                    // UI setup code will be generated here
                }
                
                ${requirements.features.joinToString("\n\n    ") { generateFeatureMethod(it) }}
            }
        """.trimIndent()
        
        return GeneratedFile(
            path = "app/src/main/java/com/example/${requirements.projectName.lowercase()}/MainActivity.kt",
            content = code,
            type = FileType.KOTLIN
        )
    }
    
    private fun generateFeatureMethod(feature: Feature): String {
        return """
            private fun initialize${feature.name}() {
                // ${feature.description}
                // Implementation will be generated based on feature type: ${feature.type}
            }
        """.trimIndent()
    }
    
    private fun generateComponentCode(feature: Feature): List<GeneratedFile> {
        val files = mutableListOf<GeneratedFile>()
        
        // Generate Fragment
        val fragmentCode = """
            package com.example.requirements.components
            
            import android.os.Bundle
            import android.view.LayoutInflater
            import android.view.View
            import android.view.ViewGroup
            import androidx.fragment.app.Fragment
            
            /**
             * ${feature.name} Fragment
             * ${feature.description}
             */
            class ${feature.name}Fragment : Fragment() {
                
                override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?
                ): View? {
                    // Inflate layout for ${feature.name}
                    return inflater.inflate(R.layout.fragment_${feature.name.lowercase()}, container, false)
                }
                
                override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                    super.onViewCreated(view, savedInstanceState)
                    setup${feature.name}()
                }
                
                private fun setup${feature.name}() {
                    // Implementation for ${feature.description}
                }
            }
        """.trimIndent()
        
        files.add(GeneratedFile(
            path = "app/src/main/java/com/example/requirements/components/${feature.name}Fragment.kt",
            content = fragmentCode,
            type = FileType.KOTLIN
        ))
        
        // Generate layout file
        val layoutCode = """
            <?xml version="1.0" encoding="utf-8"?>
            <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="vertical"
                android:padding="16dp">
                
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="${feature.name}"
                    android:textSize="24sp"
                    android:textStyle="bold" />
                
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="${feature.description}"
                    android:layout_marginTop="8dp" />
                
                <!-- Add more UI components here -->
                
            </LinearLayout>
        """.trimIndent()
        
        files.add(GeneratedFile(
            path = "app/src/main/res/layout/fragment_${feature.name.lowercase()}.xml",
            content = layoutCode,
            type = FileType.XML
        ))
        
        return files
    }
    
    private fun generateServiceCode(feature: Feature): List<GeneratedFile> {
        val files = mutableListOf<GeneratedFile>()
        
        // Generate Service class
        val serviceCode = """
            package com.example.requirements.services
            
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.withContext
            
            /**
             * ${feature.name} Service
             * ${feature.description}
             */
            class ${feature.name}Service {
                
                suspend fun execute(): Result<String> = withContext(Dispatchers.IO) {
                    try {
                        // Implementation for ${feature.description}
                        Result.success("${feature.name} executed successfully")
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
                
                companion object {
                    @Volatile
                    private var INSTANCE: ${feature.name}Service? = null
                    
                    fun getInstance(): ${feature.name}Service {
                        return INSTANCE ?: synchronized(this) {
                            INSTANCE ?: ${feature.name}Service().also { INSTANCE = it }
                        }
                    }
                }
            }
        """.trimIndent()
        
        files.add(GeneratedFile(
            path = "app/src/main/java/com/example/requirements/services/${feature.name}Service.kt",
            content = serviceCode,
            type = FileType.KOTLIN
        ))
        
        return files
    }
    
    private fun generateUtilityCode(feature: Feature): List<GeneratedFile> {
        val files = mutableListOf<GeneratedFile>()
        
        val utilityCode = """
            package com.example.requirements.utils
            
            /**
             * ${feature.name} Utility
             * ${feature.description}
             */
            object ${feature.name}Util {
                
                fun process(input: String): String {
                    // Implementation for ${feature.description}
                    return "Processed: ${'$'}input"
                }
                
                fun validate(input: String): Boolean {
                    // Validation logic for ${feature.name}
                    return input.isNotBlank()
                }
            }
        """.trimIndent()
        
        files.add(GeneratedFile(
            path = "app/src/main/java/com/example/requirements/utils/${feature.name}Util.kt",
            content = utilityCode,
            type = FileType.KOTLIN
        ))
        
        return files
    }
    
    private fun generateBuildConfig(requirements: Requirements): GeneratedFile {
        val buildConfig = """
            plugins {
                id("com.android.application")
                id("kotlin-android")
            }
            
            android {
                compileSdk = 34
                
                defaultConfig {
                    applicationId = "com.example.${requirements.projectName.lowercase()}"
                    minSdk = 24
                    targetSdk = 34
                    versionCode = 1
                    versionName = "1.0"
                }
                
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
                
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
            
            dependencies {
                implementation("androidx.core:core-ktx:1.9.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("com.google.android.material:material:1.8.0")
                implementation("androidx.constraintlayout:constraintlayout:2.1.4")
                implementation("androidx.fragment:fragment-ktx:1.5.6")
                implementation("kotlinx.coroutines:kotlinx-coroutines-android:1.6.4")
                
                testImplementation("junit:junit:4.13.2")
                androidTestImplementation("androidx.test.ext:junit:1.1.5")
                androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
            }
        """.trimIndent()
        
        return GeneratedFile(
            path = "app/build.gradle.kts",
            content = buildConfig,
            type = FileType.GRADLE
        )
    }
    
    private fun generateManifest(requirements: Requirements): GeneratedFile {
        val manifest = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.example.${requirements.projectName.lowercase()}">
                
                <application
                    android:allowBackup="true"
                    android:icon="@mipmap/ic_launcher"
                    android:label="@string/app_name"
                    android:theme="@style/Theme.${requirements.projectName}">
                    
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                    
                </application>
                
            </manifest>
        """.trimIndent()
        
        return GeneratedFile(
            path = "app/src/main/AndroidManifest.xml",
            content = manifest,
            type = FileType.XML
        )
    }
}

data class CodeGenerationResult(
    val projectName: String,
    val generatedFiles: List<GeneratedFile>,
    val summary: String
)

data class GeneratedFile(
    val path: String,
    val content: String,
    val type: FileType
)

enum class FileType {
    KOTLIN,
    XML,
    GRADLE,
    JSON
}