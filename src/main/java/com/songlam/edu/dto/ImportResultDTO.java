package com.songlam.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ImportResultDTO {

    private int success;
    private List<String> errors;

    public ImportResultDTO() {
        this.success = 0;
        this.errors = new ArrayList<>();
    }

    public void addError(String err) {
        this.errors.add(err);
    }

    public void incrementSuccess() {
        this.success++;
    }

    public int getFailCount() {
        return errors.size();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getErrorsAsText() {
        if (errors.isEmpty()) {
            return "";
        }
        return String.join("\n", errors);
    }
}
