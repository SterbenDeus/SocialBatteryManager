# Requirements-Based Code Generation System

## Overview

This system allows you to upload requirements in various formats (JSON, YAML, or plain text) and automatically generate a complete Android application base code structure. The system can handle requirements of varying complexity levels and generates appropriate code components based on the features specified.

## Features

- **Multi-format Support**: Accepts requirements in JSON, YAML, or plain text format
- **Intelligent Parsing**: Automatically detects and extracts features from requirements
- **Complete Code Generation**: Generates full Android app structure including:
  - Activities and Fragments
  - Services and background tasks
  - Utility classes
  - Layout files
  - Build configurations
  - Manifest files
- **Complexity Assessment**: Evaluates requirement complexity and provides recommendations
- **Scalable Architecture**: Handles simple to very complex requirements

## How Complex Can You Get?

The system can handle requirements of varying complexity levels:

### Simple Requirements (1-2 features, < 100 words)
- Basic mobile apps with login functionality
- Simple CRUD applications
- Single-feature utilities

### Moderate Requirements (3-5 features, < 500 words)
- E-commerce platforms with cart functionality
- Social media apps with basic features
- Multi-screen applications

### Complex Requirements (6-10 features, < 1000 words)
- Enterprise applications with role-based access
- Multi-tenant systems
- Advanced analytics platforms

### Very Complex Requirements (10+ features, 1000+ words)
- Full-featured enterprise platforms
- Multi-service architectures
- Advanced AI/ML applications
- Comprehensive management systems

## Usage

### Command Line Interface

1. **Process a requirements file:**
   ```bash
   ./gradlew run --args="process requirements.json output-directory"
   ```

2. **Run demo with sample requirements:**
   ```bash
   ./gradlew demo
   ```

3. **Run tests:**
   ```bash
   ./gradlew test-requirements
   ```

### Web Interface

The system includes a web interface component that can be integrated into web applications:

```kotlin
val webInterface: RequirementsWebInterface = RequirementsWebInterfaceImpl()
val result = webInterface.processWebRequirements(
    fileContent = requirementsText,
    fileName = "requirements.json"
)
```

## Supported Formats

### JSON Format
```json
{
    "projectName": "MyApp",
    "description": "Application description",
    "features": [
        {
            "name": "Authentication",
            "description": "User login system",
            "type": "SERVICE"
        },
        {
            "name": "Dashboard",
            "description": "Main dashboard",
            "type": "COMPONENT"
        }
    ]
}
```

### YAML Format
```yaml
project: MyApp
description: Application description
features:
  - feature: Authentication
  - feature: Dashboard
```

### Plain Text Format
```text
My Application Name

Create a mobile application with the following features:
- User authentication and login
- Dashboard with analytics
- Data synchronization
- Push notifications
```

## Feature Types

The system supports three types of features:

1. **COMPONENT**: UI components, fragments, activities
2. **SERVICE**: Background services, data processing, API clients
3. **UTILITY**: Helper classes, utilities, common functions

## Generated Structure

For each project, the system generates:

```
generated-project/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/example/projectname/
│           │   └── MainActivity.kt
│           ├── java/com/example/requirements/
│           │   ├── components/
│           │   │   ├── FeatureFragment.kt
│           │   │   └── ...
│           │   ├── services/
│           │   │   ├── FeatureService.kt
│           │   │   └── ...
│           │   └── utils/
│           │       ├── FeatureUtil.kt
│           │       └── ...
│           └── res/
│               └── layout/
│                   ├── activity_main.xml
│                   ├── fragment_feature.xml
│                   └── ...
```

## Example: Complex E-commerce Platform

Here's an example of a complex e-commerce platform requirements:

```json
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
```

This would generate:
- 11 files total
- 6 Kotlin classes
- 4 XML layout files
- 1 build configuration
- Complete Android app structure

## Complexity Assessment

The system provides automatic complexity assessment:

- **Estimated files**: Based on feature count and complexity
- **Estimated lines of code**: Rough estimate for planning
- **Recommendations**: Specific suggestions based on complexity level
- **Feature analysis**: Breakdown of detected features

## Integration

### Android App Integration

The requirements processor can be integrated into the existing Social Battery Manager app:

```kotlin
// In your Android activity
val processor = RequirementProcessor()
val result = processor.processRequirements(requirementsFile)

// Use the generated code structure
result.generatedFiles.forEach { file ->
    // Save or display generated code
}
```

### Web Service Integration

Can be deployed as a web service for online requirements processing:

```kotlin
@RestController
class RequirementsController {
    
    @PostMapping("/process")
    fun processRequirements(@RequestBody requirements: String): WebProcessingResult {
        val webInterface: RequirementsWebInterface = RequirementsWebInterfaceImpl()
        return webInterface.processWebRequirements(requirements, "requirements.json")
    }
}
```

## Building and Running

1. **Build the project:**
   ```bash
   ./gradlew build
   ```

2. **Run tests:**
   ```bash
   ./gradlew test
   ```

3. **Generate JAR:**
   ```bash
   ./gradlew jar
   ```

4. **Run standalone:**
   ```bash
   java -jar build/libs/requirements-processor-1.0.0.jar process requirements.json
   ```

## Future Enhancements

- **AI-powered feature extraction**: Use NLP to better understand requirements
- **Code quality metrics**: Analyze and improve generated code quality
- **Multiple platform support**: Generate code for iOS, web, etc.
- **Real-time collaboration**: Multi-user requirements editing
- **Template system**: Customizable code generation templates
- **Integration plugins**: IDE plugins for seamless development

## Contributing

The system is designed to be extensible. You can:

1. Add new file format parsers
2. Enhance code generation templates
3. Add new feature types
4. Improve complexity assessment algorithms
5. Add new output formats

## License

This requirements-based code generation system is part of the Social Battery Manager project and follows the same licensing terms.