package com.plantmanager.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory disease library. Loaded from diseases.csv at startup;
 * new diseases can be added at runtime via the UI.
 */
public final class DiseaseLibrary {

    private static final Map<String, Disease> DISEASES = new LinkedHashMap<>();

    private DiseaseLibrary() {
    }

    public static void loadDefaults() {
        DISEASES.clear();
        addInternal("Powdery Mildew", new Disease(
                "Powdery Mildew",
                "White powdery spots on leaves and stems",
                "Fungus (Erysiphe spp.)",
                new Treatment("Sulfur spray", "Spray every 7 days on affected areas",
                        "Neem oil solution", 21, "Apply in early morning; avoid during flowering")
        ));
        addInternal("Aphid Infestation", new Disease(
                "Aphid Infestation",
                "Curled yellow leaves, sticky honeydew residue on foliage",
                "Insect (Aphidoidea)",
                new Treatment("Insecticidal soap", "Spray every 4 days, covering undersides of leaves",
                        "Release ladybugs or lacewings", 14, "Repeat after rain")
        ));
        addInternal("Late Blight", new Disease(
                "Late Blight",
                "Dark water-soaked lesions on stems and leaves, white mold in humid conditions",
                "Oomycete (Phytophthora infestans)",
                new Treatment("Copper fungicide", "Spray weekly; remove and destroy infected parts",
                        "Compost tea spray", 28, "Do not compost infected material")
        ));
        addInternal("Rust", new Disease(
                "Rust",
                "Orange or brown pustules on leaf undersides, yellowing foliage",
                "Fungus (Puccinia spp.)",
                new Treatment("Myclobutanil spray", "Apply every 10 days until symptoms subside",
                        "Baking soda solution (1 tbsp per gallon)", 28, "Improve air circulation around plants")
        ));
        addInternal("Root Rot", new Disease(
                "Root Rot",
                "Wilting despite watering, yellowing leaves, mushy brown roots",
                "Fungus (Phytophthora, Pythium)",
                new Treatment("Trichoderma bio-fungicide", "Drench soil after improving drainage",
                        "Cinnamon powder on affected roots", 0, "Reduce watering; ensure well-draining soil")
        ));
        addInternal("Leaf Spot", new Disease(
                "Leaf Spot",
                "Circular brown or black spots on leaves, sometimes with yellow halos",
                "Fungus (Cercospora, Septoria)",
                new Treatment("Chlorothalonil fungicide", "Spray every 7–10 days",
                        "Remove affected leaves; spray with diluted milk", 21, "Avoid overhead watering")
        ));
        addInternal("Spider Mite Damage", new Disease(
                "Spider Mite Damage",
                "Fine webbing on leaves, stippled yellow/bronze foliage",
                "Arachnid (Tetranychus urticae)",
                new Treatment("Miticide spray", "Apply every 5 days for 3 applications",
                        "Increase humidity; spray with water", 15, "Mites thrive in dry conditions")
        ));
        addInternal("Bacterial Wilt", new Disease(
                "Bacterial Wilt",
                "Sudden wilting of entire plant, brown vascular tissue when stem is cut",
                "Bacterium (Ralstonia solanacearum)",
                new Treatment("Copper-based bactericide", "Soil drench at first sign of wilt",
                        "Crop rotation; remove infected plants immediately", 0, "No cure once established – prevention is key")
        ));
    }

    public static void replaceAll(List<Disease> diseases) {
        DISEASES.clear();
        for (Disease disease : diseases) {
            addInternal(disease.getName(), disease);
        }
    }

    public static void add(Disease disease) {
        if (disease == null) {
            throw new IllegalArgumentException("Disease cannot be null");
        }
        if (DISEASES.containsKey(disease.getName())) {
            throw new IllegalArgumentException("A disease named \"" + disease.getName() + "\" already exists");
        }
        addInternal(disease.getName(), disease);
    }

    private static void addInternal(String key, Disease disease) {
        DISEASES.put(key, disease);
    }

    public static List<Disease> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(DISEASES.values()));
    }

    public static Optional<Disease> findByName(String name) {
        if (name == null || name.isBlank() || "None".equalsIgnoreCase(name.trim())) {
            return Optional.empty();
        }
        return Optional.ofNullable(DISEASES.get(name.trim()));
    }

    public static boolean exists(String name) {
        return name != null && DISEASES.containsKey(name.trim());
    }
}
