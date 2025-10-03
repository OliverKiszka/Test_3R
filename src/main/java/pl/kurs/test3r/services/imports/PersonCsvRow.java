package pl.kurs.test3r.services.imports;

import pl.kurs.test3r.exceptions.ImportProcessingException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;

public class PersonCsvRow {

    private final Map<String, String> values;
    private final int rowNumber;

    public PersonCsvRow(Map<String, String> values, int rowNumber) {
        this.values = values;
        this.rowNumber = rowNumber;
    }

    public String type() {
        return required("type").toUpperCase(Locale.ROOT);
    }

    public String firstName() {
        return required("firstname");
    }

    public String lastName() {
        return required("lastname");
    }

    public String pesel() {
        return required("pesel");
    }

    public double height() {
        return parseDouble(required("height"), "height");
    }

    public double weight() {
        return parseDouble(required("weight"), "weight");
    }

    public String email() {
        return required("email");
    }

    public String required(String column) {
        String value = values.get(column);
        if (value == null || value.isBlank()) {
            throw new ImportProcessingException("Missing value for column '" + column + "' at row " + rowNumber);
        }
        return value.trim();
    }

    public String optional(String column) {
        String value = values.get(column);
        return value == null ? null : value.trim();
    }

    public double requiredDouble(String column) {
        return parseDouble(required(column), column);
    }

    public Integer requiredInteger(String column) {
        String value = required(column);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new ImportProcessingException("Invalid integer value in column '" + column + "' at row " + rowNumber);
        }
    }

    public LocalDate requiredDate(String column) {
        String value = required(column);
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ImportProcessingException("Invalid date value in column '" + column + "' at row " + rowNumber + "'. Expected format ISO-8601 (yyyy-MM-dd)");
        }
    }

    private double parseDouble(String value, String column) {
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new ImportProcessingException("Invalid decimal value in column '" + column + "' at row " + rowNumber);
        }
    }
}