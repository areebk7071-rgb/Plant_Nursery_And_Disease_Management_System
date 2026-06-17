# Setup Guide

## Prerequisites

| Tool | Minimum Version | Check |
|------|----------------|-------|
| Java JDK | 17 | `java -version` |
| Apache Maven | 3.6 | `mvn -version` |

Java 21 is recommended (LTS). The project will not compile on Java 11 or earlier due to record types and switch expressions.

---

## Installation

### 1. Clone the repository
```bash
git clone <repo-url>
cd JAVAFXPRO
```

### 2. Build the project
```bash
mvn clean compile
```

### 3. Run the application
```bash
mvn javafx:run
```

That's it. No database setup, no environment variables, no extra configuration.

---

## First Launch

On first launch:

1. **users.csv** is auto-created with a default admin account.
2. **plants.csv** is auto-created with 4 sample plants (Tomato, Rose, Basil, Apple Tree).
3. **diseases.csv** is populated from the hardcoded `DiseaseLibrary` defaults.
4. **treatment_records.csv** is created empty and filled as you use the app.

### Default credentials
```
Username: admin
Password: admin
```

---

## Common Issues

### `mvn javafx:run` fails with "JavaFX runtime components are missing"
This usually means you're running the JAR directly instead of through Maven. Always use `mvn javafx:run` — the plugin handles the module path for you.

### CSV files get corrupted
Plant names or disease names that contain commas must be quoted. The app handles this automatically via `CsvUtils.quote()`. If you manually edit CSVs, wrap any field containing a comma in double quotes.

### Images not showing
Plant images are looked up from the `plant-images/` folder relative to the working directory (where you run Maven from). The sample Tomato image (`plant-images/1_tomato.jpg`) ships with the project. For other plants, either upload an image through the UI or use the built-in icon fallbacks.

---

## Building a Runnable JAR (optional)

The project doesn't include a fat-jar plugin by default. For a self-contained executable, add the `maven-shade-plugin` or `javafx-maven-plugin` packaging config to `pom.xml`. Ask your lab instructor if this is required for submission.

---

## IDE Setup

### IntelliJ IDEA
1. File → Open → select the `JAVAFXPRO` folder
2. IntelliJ auto-detects the Maven project
3. Run `MainApp` directly or use the Maven sidebar → `javafx:run`

### VS Code
1. Install the "Extension Pack for Java" and "JavaFX Support" extensions
2. Open the folder
3. Use the Maven extension panel to run `javafx:run`
