package com.aragoj.equation.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used for data store
 * @see VariableValueRow
 */
@XmlRootElement(name = "variable")
public class Variable {
    private String name;
    private double value;


    public Variable(){}

    public Variable(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public Variable(VariableValueRow valueRow){
        this.name = valueRow.getVariableName();
        String variableValue = valueRow.getVariableValue().getValue();
        if(variableValue.contains(":")){
            variableValue = variableValue.split(": ")[1];
        }
        if(!variableValue.isEmpty()) {
            this.value = Double.parseDouble(variableValue);
        }
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
