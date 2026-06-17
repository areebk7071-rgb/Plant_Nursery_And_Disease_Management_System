# OOP Principles — Botanical Treatment Advisor

This document maps every OOP concept used in the project to specific classes and methods, intended for the project report.

---

## 1. Encapsulation

**Definition:** Bundling data (fields) and the methods that operate on them inside a class, and restricting direct access to the data using access modifiers.

### Where it's applied

| Class | Fields | Validation in setters |
|---|---|---|
| `Plant` | `id`, `name`, `species`, `plantedDate`, `assignedDisease`, `imageRef` | ✅ All setters validate (blank check, null check, future date rejection, negative ID) |
| `FruitPlant` | `expectedYield`, `harvestPeriod` | ✅ Yield cannot be negative; harvest period cannot be blank |
| `FlowerPlant` | `bloomColor`, `isPerennial` | ✅ Bloom color cannot be blank |
| `HerbPlant` | `culinaryUse`, `isMedicinal` | ✅ Culinary use cannot be blank |
| `Disease` | `name`, `symptoms`, `causativeAgent`, `treatment` | ✅ Private fields |
| `User` | `username`, `passwordHash`, `displayName` | ✅ All final — immutable after construction |
| `TreatmentRecord` | All tracking fields | ✅ `setApplicationsCount` clamps to ≥ 0 |

**Code example:**
```java
// Plant.java
public void setPlantedDate(LocalDate plantedDate) {
    if (plantedDate == null)
        throw new IllegalArgumentException("Planted date cannot be null");
    if (plantedDate.isAfter(LocalDate.now()))
        throw new IllegalArgumentException("Planted date cannot be in the future");
    this.plantedDate = plantedDate;
}
```

---

## 2. Abstraction

**Definition:** Hiding internal implementation details and exposing only what's necessary through abstract classes and interfaces.

### Abstract class: `Plant`

`Plant` is declared `abstract` — you can never instantiate it directly. It defines three abstract methods that each subclass **must** implement:

```java
public abstract String getPlantType();        // "Fruit", "Flower", or "Herb"
public abstract String getCareInstructions(); // type-specific advice string
public abstract String toCsvRow();            // serialization format varies by subclass
```

Callers don't need to know which subclass they have — they just call these methods.

### Interface: `Treatable`

```java
public interface Treatable {
    Disease getAssignedDisease();
    void setAssignedDisease(Disease disease);

    default boolean hasDisease() {
        return getAssignedDisease() != null;
    }

    default String applyTreatment() {
        if (!hasDisease()) return "No disease assigned – no treatment required.";
        return getAssignedDisease().getTreatment().getFormattedDetails();
    }
}
```

`Treatable` abstracts over "anything that can hold and apply a treatment" — today it's `Plant`, but the contract could apply to any other entity in the future.

---

## 3. Inheritance

**Definition:** A subclass inherits fields and methods from a parent class, adding or overriding behaviour.

### Hierarchy

```
java.lang.Object
└── Plant (abstract, implements Treatable)
    ├── FruitPlant
    │     + expectedYield : double
    │     + harvestPeriod : String
    ├── FlowerPlant
    │     + bloomColor : String
    │     + isPerennial : boolean
    └── HerbPlant
          + culinaryUse : String
          + isMedicinal : boolean
```

All shared behaviour (CRUD fields, disease assignment, CSV header, `toString`) is defined **once** in `Plant` and inherited by all three subclasses. Each subclass only adds what's unique to it.

**Code example:**
```java
// FruitPlant inherits Plant's id, name, species, plantedDate,
// assignedDisease, imageRef fields and all their getters/setters.
public class FruitPlant extends Plant {
    private double expectedYield;
    private String harvestPeriod;
    // ...
}
```

---

## 4. Polymorphism

**Definition:** The same interface or method call behaves differently depending on the actual object type at runtime.

### Method overriding (runtime polymorphism)

```java
ObservableList<Plant> plants = ...; // contains FruitPlant, FlowerPlant, HerbPlant

for (Plant p : plants) {
    System.out.println(p.getPlantType());       // "Fruit", "Flower", or "Herb"
    System.out.println(p.getCareInstructions()); // completely different text per type
    System.out.println(p.toCsvRow());            // different column layout per type
}
```

The loop doesn't know (or care) what concrete type each plant is — the correct method is dispatched at runtime.

### Factory polymorphism (`PlantFactory`)

```java
// PlantFactory.java
return switch (plantType) {
    case "FruitPlant"  -> new FruitPlant(id, name, species, plantedDate, yield, harvest);
    case "FlowerPlant" -> new FlowerPlant(id, name, species, plantedDate, color, perennial);
    case "HerbPlant"   -> new HerbPlant(id, name, species, plantedDate, culinary, medicinal);
    default -> null;
};
```

The switch acts as a polymorphic dispatcher — one call to `PlantFactory.fromCsv()` returns the right subtype without the caller needing any `instanceof` checks.

### Interface polymorphism

Anything that implements `Treatable` can have `applyTreatment()` called on it — the caller doesn't need to know the concrete class.

---

## 5. Additional Design Patterns

### Factory Pattern
`PlantFactory` centralises the construction of `Plant` subclasses from CSV data. Without it, every caller would need its own `if/else` type-dispatch logic.

### Singleton-style Registry
`DiseaseLibrary` uses a static `LinkedHashMap` as a global, in-memory registry. It provides controlled access (`getAll()`, `findByName()`, `add()`) and prevents direct mutation.

### Service Layer (Separation of Concerns)
Business logic lives in dedicated service classes, not in controllers:
- `AuthService` — login session management
- `TreatmentTrackingService` — treatment lifecycle and effectiveness scoring
- `DashboardStats` — statistics and insight generation
- `PdfReportService` — report generation

Controllers call services; services call repositories. This makes each layer independently testable.

### MVC (Model-View-Controller)
JavaFX FXML enforces MVC:
- **Model** → `model/` package
- **View** → `.fxml` files + `styles.css`
- **Controller** → `controller/` package

---

## Summary Table

| Principle | Key Classes |
|---|---|
| Encapsulation | `Plant`, `FruitPlant`, `FlowerPlant`, `HerbPlant`, `Disease`, `User`, `TreatmentRecord` |
| Abstraction | `Plant` (abstract class), `Treatable` (interface) |
| Inheritance | `FruitPlant`, `FlowerPlant`, `HerbPlant` extend `Plant` |
| Polymorphism | `ObservableList<Plant>`, `PlantFactory.fromCsv()`, `Treatable.applyTreatment()` |
| Factory | `PlantFactory` |
| Singleton-style | `DiseaseLibrary` |
| Service Layer | `AuthService`, `TreatmentTrackingService`, `DashboardStats`, `PdfReportService` |
| MVC | Full JavaFX FXML separation |
