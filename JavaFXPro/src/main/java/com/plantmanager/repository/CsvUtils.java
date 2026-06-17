package com.plantmanager.repository;

/**
 * Utility methods for CSV reading and writing.
 */
public final class CsvUtils {

    private CsvUtils() {
    }

    public static String quote(String value) {
        if (value == null) {
            return "\"\"";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Splits a CSV line respecting quoted fields.
     */
    public static String[] parseLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}
