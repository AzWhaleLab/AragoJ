package equation.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import ui.model.LabeledComboOption;

/**
 * Used for UI representation
 * @see Variable
 */
public class VariableValueRow {
    private final SimpleStringProperty variableName;
    private final SimpleObjectProperty<LabeledComboOption> variableValue;

    public VariableValueRow(String variableName, LabeledComboOption variableValue) {
        this.variableName = new SimpleStringProperty(variableName);
        this.variableValue = new SimpleObjectProperty<>(variableValue);
    }

    public String getVariableName() {
        return variableName.get();
    }

    public SimpleStringProperty variableNameProperty() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName.set(variableName);
    }

    public LabeledComboOption getVariableValue() {
        return variableValue.getValue();
    }

    public SimpleObjectProperty<LabeledComboOption> variableValueProperty() {
        return variableValue;
    }

    public void setVariableValue(LabeledComboOption variableValue) {
        this.variableValue.set(variableValue);
    }

}
