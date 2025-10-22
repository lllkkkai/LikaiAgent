# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI-powered Java log analysis system that parses exception logs and uses Kimi AI to analyze root causes and provide fix suggestions. Built with Spring Boot 3.5.6 and Java 21 following Domain-Driven Design (DDD) architecture.

## Essential Commands

### Build and Run
```bash
# Build the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Package the application
./mvnw package

# Run tests
./mvnw test

# Run specific test
./mvnw test -Dtest=ClassName
```

### Initial Setup
```bash
# Run setup script for configuration
./setup.sh

# Manual configuration copy
cp src/main/resources/application-template.yml src/main/resources/application.yml
```

## Architecture Overview

The project follows DDD architecture with clear separation of concerns:

```
src/main/java/com/lllkkk/ai/agent/modules/log/handle/
├── application/          # Application orchestration layer
│   └── LogProcessingPipeline.java  # Main processing pipeline (currently commented out)
├── domain/               # Domain layer with business logic
│   ├── model/           # Domain models (LogRecord, StackFrame, AnalysisResult)
│   └── service/         # Domain services and their implementations
│       ├── LogParser.java      # Interface for log parsing
│       ├── LogFilter.java      # Interface for log filtering
│       ├── AIAnalyzer.java     # Interface for AI analysis
│       ├── CodeLocator.java    # Interface for code location
│       └── impl/               # Implementations
└── infrastructure/      # Infrastructure layer
    ├── client/          # External API clients (KimiAIClient)
    └── config/          # Configuration classes (AIConfig)
```

### Data Flow
1. **Raw Log String** → RegexLogParser → **LogRecord** (structured)
2. **LogRecord** → LogFilterImpl → **Filtered LogRecord** (business-focused)
3. **Filtered LogRecord** → KimiAnalyzerImpl → **AnalysisResult** (AI insights)
4. **AnalysisResult** includes: rootCause, summary, fixSuggestion, relatedLocation

## Key Configuration

### AI Configuration (application.yml)
```yaml
ai:
  kimi:
    api-key: "your-kimi-api-key"  # Get from https://platform.moonshot.cn/
    base-url: "https://api.moonshot.cn/v1"
    model: "moonshot-v1-8k"
    max-tokens: 2000
    temperature: 0.3
```

### Business Package Configuration
In `LogFilterImpl.java`, configure business package prefixes:
```java
private static final String[] BUSINESS_PACKAGES = {
    "com.dyyl", "com.wkb"  // Add your business package prefixes
};
```

## Security Considerations

- **NEVER commit real configuration files** - application.yml is gitignored
- **Use template files** for configuration examples
- **API keys must be kept secure** - use environment variables when possible
- **Run setup.sh** for safe initial configuration

## Development Notes

### Current Status
- ✅ Core components implemented (parsing, filtering, AI analysis, code location)
- ⚠️ LogProcessingPipeline orchestration is commented out and needs completion
- ⚠️ Test coverage is basic - expand tests for production use

### Working with the Code
- The codebase uses Chinese comments and documentation
- Follow existing patterns when adding new components
- Use interfaces in domain/service and implementations in domain/service/impl
- Configuration is managed through Spring Boot's application.yml

### Testing Approach
- Unit tests exist for core components (RegexLogParser, LogFilterImpl, LocalCodeLocator)
- Run tests before committing changes
- Add tests for new functionality following existing test patterns

### AI Integration
- Kimi AI client is implemented in `infrastructure/client/KimiAIClient.java`
- AI analysis logic is in `domain/service/impl/KimiAnalyzerImpl.java`
- The system constructs prompts with exception context and code snippets for analysis