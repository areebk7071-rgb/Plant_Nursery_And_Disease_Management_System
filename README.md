# 🌿 Botanical Treatment Advisor

A JavaFX desktop application for managing garden plants, tracking diseases, and monitoring treatment progress. Built as an OOP Lab project demonstrating all four core OOP principles through a real, working application.

---

## 📸 Screenshots

> *(Add screenshots here after first run — login screen, main plant table, dashboard, treatment panel)*

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 Login System | Password-hashed authentication with session management |
| 🌱 Plant CRUD | Add, edit, delete Fruit, Flower, and Herb plants with type-specific fields |
| 🦠 Disease Library | 8 predefined diseases with symptoms, causative agents, and treatment plans |
| 💊 Treatment Tracking | Log treatment applications, mark effectiveness, track day-by-day progress |
| 📊 Dashboard | Live garden health stats, insights, and disease breakdown |
| 🧪 Assign Disease | Link any disease from the library to a plant |
| 🔍 Search & Filter | Filter plants by name, species, disease, or type |
| 📄 PDF Export | Generate a printable Garden Health Report via PDFBox |
| 📱 QR Share | Share exported files via local QR code server |
| 💾 CSV Persistence | All data stored in flat CSV files — no database setup needed |

---

## 🏗️ Architecture & OOP Principles

This project deliberately implements all four OOP pillars:

### 🔒 Encapsulation
Every model class (`Plant`, `Disease`, `User`, `TreatmentRecord`) uses **private fields** with validated getters/setters. Invalid data throws `IllegalArgumentException` at the setter level — e.g. `setName()` rejects blank strings, `setPlantedDate()` rejects future dates, `setExpectedYield()` rejects negatives.

### 🧱 Abstraction
- `Plant` is an **abstract class** with abstract methods `getPlantType()`, `getCareInstructions()`, and `toCsvRow()` — each subclass must define its own behavior.
- `Treatable` is an **interface** with default methods `hasDisease()` and `applyTreatment()`, abstracting over anything that can hold a treatment plan.

### 🌳 Inheritance
```
Plant (abstract, implements Treatable)
├── FruitPlant      → adds expectedYield, harvestPeriod
├── FlowerPlant     → adds bloomColor, isPerennial
└── HerbPlant       → adds culinaryUse, isMedicinal
```

### 🔁 Polymorphism
- `ObservableList<Plant>` holds all three subclasses — the UI calls `getCareInstructions()` and `toCsvRow()` polymorphically.
- `PlantFactory.fromCsv()` uses a `switch` on plant type to reconstruct the right subclass.
- `TreatmentRecommendation` (inner record in `TreatmentTrackingService`) polymorphically computes weighted effectiveness across different treatment histories.

### 🏭 Design Patterns
- **Factory Pattern** — `PlantFactory` centralizes CSV → object reconstruction
- **Singleton-style** — `DiseaseLibrary` uses a static map as an in-memory registry
- **Service Layer** — `AuthService`, `TreatmentTrackingService`, `DashboardStats`, `PdfReportService` are all stateless utility classes

---

## 📁 Project Structure

```
src/main/java/com/plantmanager/
│
├── MainApp.java                        # JavaFX entry point, scene routing
│
├── model/                              # Domain objects
│   ├── Plant.java                      # Abstract base class
│   ├── FruitPlant.java
│   ├── FlowerPlant.java
│   ├── HerbPlant.java
│   ├── Treatable.java                  # Interface
│   ├── Disease.java
│   ├── Treatment.java
│   ├── DiseaseLibrary.java             # In-memory disease registry
│   ├── PlantFactory.java               # Factory pattern
│   ├── TreatmentRecord.java
│   ├── TreatmentStatus.java            # Enum: ACTIVE, COMPLETED, CANCELLED
│   ├── TreatmentEffectiveness.java     # Enum: NONE, LOW, MEDIUM, HIGH
│   ├── PlantIcon.java
│   └── User.java
│
├── controller/                         # JavaFX controllers (MVC)
│   ├── MainController.java
│   ├── LoginController.java
│   ├── DashboardController.java
│   ├── PlantDialogController.java
│   ├── TreatmentPanelController.java
│   ├── TreatmentDialogController.java
│   ├── AssignDiseaseDialogController.java
│   └── AddDiseaseDialogController.java
│
├── service/                            # Business logic layer
│   ├── AuthService.java
│   ├── PasswordHasher.java
│   ├── TreatmentTrackingService.java
│   ├── DashboardStats.java
│   ├── PdfReportService.java
│   ├── PlantImageService.java
│   ├── QrShareService.java
│   └── LocalFileShareServer.java
│
├── repository/                         # CSV-based data access layer
│   ├── PlantRepository.java
│   ├── DiseaseRepository.java
│   ├── TreatmentRecordRepository.java
│   ├── UserRepository.java
│   └── CsvUtils.java
│
└── view/                               # SplashScreen (programmatic JavaFX)
    └── SplashScreen.java

src/main/resources/com/plantmanager/view/
├── main-view.fxml
├── login-view.fxml
├── dashboard-view.fxml
├── plant-dialog.fxml
├── treatment-dialog.fxml
├── treatment-panel.fxml
├── assign-disease-dialog.fxml
├── add-disease-dialog.fxml
└── styles.css

plants.csv                  # Plant data (auto-created on first run)
diseases.csv                # Disease library (persisted)
users.csv                   # Hashed user credentials
treatment_records.csv       # Treatment history
plant-images/               # Optional plant image folder
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.6+

### Run
```bash
git clone <your-repo-url>
cd JAVAFXPRO
mvn clean javafx:run
```

### Default Login
On first launch, a default user is created automatically:

| Username | Password |
|----------|----------|
| `admin`  | `admin123` |

> ⚠️ Change this in production use.

### First Launch Behaviour
- If `plants.csv` doesn't exist, four sample plants are seeded (Tomato, Rose, Basil, Apple Tree).
- If `diseases.csv` doesn't exist, 8 diseases are loaded from the hardcoded `DiseaseLibrary` defaults.

---

## 💾 CSV Data Format

### plants.csv
```
id,name,species,plantedDate,plantType,assignedDiseaseName,expectedYield,harvestPeriod,bloomColor,isPerennial,culinaryUse,isMedicinal,imageRef
```

### diseases.csv
```
name,symptoms,causativeAgent,chemicalName,applicationMethod,organicAlternative,durationDays,precautions
```

### treatment_records.csv
```
id,plantId,plantName,plantType,diseaseName,treatmentName,durationDays,startDate,endDate,status,effectiveness,applicationsCount
```

---

## 📦 Dependencies

| Library | Version | Purpose |
|---|---|---|
| JavaFX Controls | 21.0.2 | GUI framework |
| JavaFX FXML | 21.0.2 | FXML layout loading |
| Apache PDFBox | 3.0.2 | PDF report generation |
| ZXing Core | 3.5.3 | QR code generation |
| ZXing JavaSE | 3.5.3 | QR code rendering |

---

## 👥 Group Members

| Name | Role |
|------|------|
| *(Member 1)* | *(e.g. Model & Repository layer)* |
| *(Member 2)* | *(e.g. Controllers & FXML views)* |
| *(Member 3)* | *(e.g. Services & PDF/QR export)* |
| *(Member 4)* | *(e.g. Dashboard & Treatment tracking)* |

---

## 📚 Course

**Object-Oriented Programming Lab**  
Submission Date: 15th June 2026
