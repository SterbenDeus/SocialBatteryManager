# Android Architecture Overview

This project follows Clean Architecture principles with the MVVM (Model-View-ViewModel) pattern.

## Package Structure

The project is organized into feature-based packages following this structure:

```
com.example.socialbatterymanager/
├── features/                      # Feature-based organization
│   ├── auth/                      # Authentication feature
│   │   ├── ui/                    # UI layer (Fragments, ViewModels, Adapters)
│   │   ├── data/                  # Data layer (Repositories, Data Sources)
│   │   └── domain/                # Domain layer (Use Cases, Business Logic)
│   ├── home/                      # Home feature
│   │   ├── ui/                    # UI components
│   │   ├── data/                  # Data handling
│   │   └── domain/                # Business logic
│   ├── people/                    # People management feature
│   │   ├── ui/                    # UI components including ViewModels
│   │   ├── data/                  # Repositories and data handling
│   │   └── domain/                # Business logic
│   └── profile/                   # User profile feature
│       ├── ui/                    # UI components
│       ├── data/                  # Data handling
│       └── domain/                # Business logic
├── shared/                        # Shared components across features
│   ├── utils/                     # Utility classes and functions
│   ├── preferences/               # Shared preferences management
│   └── network/                   # Network utilities
├── data/                          # Shared data layer
│   ├── database/                  # Room DAOs and database classes
│   ├── model/                     # Data entities and models
│   └── repository/                # Shared repositories
├── model/                         # Domain models with business logic
└── ui/                           # Shared UI components and themes
```

## Architecture Principles

### MVVM Pattern
- **View (Fragment/Activity)**: Displays UI and observes ViewModel state
- **ViewModel**: Manages UI state and handles user interactions
- **Model (Repository + Data Sources)**: Manages data operations and business logic

### Feature Organization
Each feature follows the same structure:
- `ui/` - Contains Fragments, ViewModels, Adapters, and other UI components
- `data/` - Contains Repositories, Data Sources, and data-related utilities
- `domain/` - Contains Use Cases, Business Logic, and domain models

### Data Layer
- **Entities** (`data/model/`): Room database entities
- **DAOs** (`data/database/`): Data access objects for database operations
- **Repositories** (`features/*/data/`): Abstract data access and coordinate between different data sources

### Shared Components
- **Utils** (`shared/utils/`): Common utility functions and helper classes
- **Preferences** (`shared/preferences/`): Application-wide preferences management
- **UI Components** (`ui/`): Reusable UI components, themes, and styles

## Code Standards

### Naming Conventions
- **Classes**: PascalCase (e.g., `MainActivity`, `UserViewModel`)
- **Functions and Variables**: camelCase (e.g., `getUserData()`, `userName`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages**: lowercase with underscores if needed

### Code Quality Tools
- **ktlint**: Enforces Kotlin code style and formatting
- **detekt**: Static code analysis for code smells and issues

Run the following commands to check and fix code quality:
```bash
./gradlew ktlintCheck    # Check for style violations
./gradlew ktlintFormat   # Auto-fix style violations
./gradlew detekt         # Run static analysis
```

## Adding New Features

When adding a new feature:

1. Create the feature package structure:
   ```
   features/your-feature/
   ├── ui/           # ViewModels, Fragments, Adapters
   ├── data/         # Repositories, Data Sources
   └── domain/       # Use Cases, Business Logic
   ```

2. Follow MVVM pattern:
   - Create Repository for data access
   - Create ViewModel for UI state management
   - Create Fragment/Activity for UI
   
3. Use dependency injection to provide dependencies

4. Write tests for each layer