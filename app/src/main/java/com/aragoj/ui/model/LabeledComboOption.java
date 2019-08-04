package com.aragoj.ui.model;

public class LabeledComboOption {
    private String identifier;
    private String value;

    public LabeledComboOption(String identifier, String value) {
        this.identifier = identifier;
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
