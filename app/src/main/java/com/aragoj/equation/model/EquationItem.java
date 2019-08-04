package com.aragoj.equation.model;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import com.aragoj.equation.Equation;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import com.aragoj.session.model.EditorItemLayer;
import com.aragoj.ui.model.LayerListItem;
import com.aragoj.utils.Utility;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "com/aragoj/equation")
@XmlType(propOrder={"name", "expression", "variables"})
public class EquationItem implements LayerListItem, EditorItemLayer{
    private String name;
//    private String description;
    private String expression;
    private List<Variable> variables;


    public EquationItem() {
        this("Unnammed expression", "", "");
    }

    public EquationItem(String name, String expression, String description) {
        this.name = name;
        this.expression = expression;
//        this.description = description;
        this.variables = new ArrayList<>();
    }

    public void addVariable(String variable, double value){
        variables.add(new Variable(variable, value));
    }

    public void setVariables(List<Variable> variables){
        this.variables = variables;
    }

    public void setVariableValueRows(List<VariableValueRow> variablesValues){
        this.variables.clear();
        for(VariableValueRow row : variablesValues){
            String value = row.getVariableValue().getValue();
            if(value == null || value.isEmpty()) value = "0";
            variables.add(new Variable(row.getVariableName(), Double.valueOf(value)));
        }
    }

    @XmlElement
    public String getName() {
        return name;
    }

//    public String getDescription() {
//        return description;
//    }

    @XmlElement
    public String getExpression() {
        return expression;
    }

    @XmlElement
    public List<Variable> getVariables(){
        return variables;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

//    public void setDescription(String description) {
//        this.description = description;
//    }

    public double getResult() throws Equation.ValidationError {
        Equation equation = new Equation(this);
        if(equation.isValid() && variables != null && variables.size() > 0){
            return equation.solveExpression(variables);
        }
        return 0;
    }

    @Override
    public SVGGlyph getSVG() throws IOException {
        SVGGlyph glyph = SVGGlyphLoader.loadGlyph(getClass().getClassLoader().getResource("svg/1-equation_f.svg"));


        Stop[] stops = new Stop[] { new Stop(0, Color.valueOf("#008c8c")), new Stop(1, Color.valueOf("#0096C9"))};
        LinearGradient lg1 = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        glyph.setFill(lg1);
        glyph.setSize(32,32);
        return glyph;
    }

    @XmlTransient
    @Override
    public String getPrimaryText() {
        return name;
    }

    @Override
    public void setPrimaryText(String primaryText) {
        setName(primaryText);
    }

    @Override
    public String getSecondaryText() {
        try {
            return String.valueOf(Utility.roundTwoDecimals(getResult()));
        } catch (Equation.ValidationError validationError) {
            return "";
        }
    }

    @Override
    public Type getType() {
        return Type.EQUATION;
    }

    @Override
    public boolean isVisualElement() {
        return false;
    }
}
