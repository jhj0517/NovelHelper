# Novel Helper (WIP)

Novel Helper is an Android app to help writers manage their novel documents with version control integration.

## Modules

- **app**: Main application module
- **feature:document-editor**: Document editing screen
- **feature:document-selection**: Document selection/home screen
- **core:model**: Domain models
- **core:data**: Repositories and data sources
- **core:ui**: Common UI components
- **core:database**: Room database and DAOs
- **core:network**: Network and cloud sync

## Directory Structure
```
NovelHelper
├── 📁 app                           // Main application module
├── 📁 core                          // Core modules for shared functionality
│   ├── 📁 data                      // Data layer
│   ├── 📁 database                  // Local database
│   ├── 📁 model                     // Domain models
│   ├── 📁 network                   // Network and cloud services
│   └── 📁 ui                        // Shared UI components
├── 📁 feature                       // Feature modules
│   ├── 📁 document-editor           // Document editing screen feature
│   └── 📁 document-selection        // Document selection screen feature
```

## WIP