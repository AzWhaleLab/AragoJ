package equation.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import equation.Equation;
import javafx.application.Platform;
import session.EquationFileManager;
import equation.model.EquationItem;
import equation.model.Variable;
import equation.model.VariableValueRow;
import equation.ui.custom.CustomComboBoxTableCell;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import ui.custom.LineGroup;
import ui.model.LabeledComboOption;
import ui.model.ScaleRatio;
import utils.Translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EquationDialogController extends Dialog<Void> {
    private Stage stage;

//    @FXML private JFXButton saveButton;
    @FXML private JFXButton okButton;
    @FXML private JFXButton removeButton;

    @FXML private JFXTextField nameTextField;
    @FXML private JFXTextField expressionTextField;
    //    @FXML private TextArea descriptionTextField;
    @FXML private JFXListView<EquationItem> equationListView;
    @FXML private TableView<VariableValueRow> variablesValueTableView;

    private Timeline validationScheduler;

    private OnActionListener listener;
    private List<LineGroup> lines;
    private ScaleRatio currentScale;

    private int currentIndex = -1;

    public void init(Window owner, OnActionListener listener, List<LineGroup> lines, ScaleRatio currentScale){
        this.listener = listener;
        this.lines = lines;
        this.currentScale = currentScale;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/EquationDialog.fxml"), Translator.getBundle());
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 420, 305);
            final ObservableList<String> stylesheets = scene.getStylesheets();
            stylesheets.addAll(getClass().getResource("/css/MainApplication.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(Translator.getString("newExpression"));
            stage.setScene(scene);
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            this.stage = stage;
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize(){
        initExpressionTable();
        initEquationList();
        initExpressionValidator();
        importEquations();
    }

    private void importEquations() {
        equationListView.getItems().setAll(EquationFileManager.importEquationsXml());
        if(equationListView.getItems().size() > 0){
            equationListView.getSelectionModel().select(0);
        }
    }

    private void initEquationList(){
        equationListView.setCellFactory(new Callback<ListView<EquationItem>, ListCell<EquationItem>>() {
            @Override
            public ListCell<EquationItem> call(ListView<EquationItem> param) {
                return new EquationListViewCell();
            }
        });

        equationListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EquationItem>() {
            @Override
            public void changed(ObservableValue<? extends EquationItem> observable, EquationItem oldValue, EquationItem newValue) {
                if(removeButton.isDisable()) removeButton.setDisable(false);
//                if(saveButton.isDisable()) saveButton.setDisable(false);
                if(nameTextField.isDisable()) nameTextField.setDisable(false);
                if(expressionTextField.isDisable()) expressionTextField.setDisable(false);
                if(variablesValueTableView.isDisable()) variablesValueTableView.setDisable(false);

                currentIndex = equationListView.getSelectionModel().getSelectedIndex();
                if(newValue != null) loadEquation(newValue);
            }
        });
    }

    private void initExpressionValidator() {
        expressionTextField.setDisable(true);
        nameTextField.setDisable(true);
        validationScheduler = new Timeline(new KeyFrame(
                Duration.millis(1500),
                ae -> {
                    List<VariableValueRow> values = variablesValueTableView.getItems();
                    validateExpression(values);
                }));

        nameTextField.setOnKeyTyped(event -> saveItem(currentIndex));
        expressionTextField.setOnKeyTyped(event -> {
            expressionTextField.setFocusColor(new Color(0, 150.0/255, 201.0/255, 1));
            if(validationScheduler != null){
                // Reset the scheduler when typed
                validationScheduler.stop();
                validationScheduler.play();
            }
        });
    }

    private void initExpressionTable() {
        variablesValueTableView.setDisable(true);
        variablesValueTableView.setPlaceholder(new Label(Translator.getString("noVariable")));
        TableColumn<VariableValueRow, String> variableNameColumn = new TableColumn<>(Translator.getString("variable"));
        TableColumn<VariableValueRow, LabeledComboOption> variableValueColumn = new TableColumn<>(Translator.getString("value"));
        variableNameColumn.setCellValueFactory(param -> param.getValue().variableNameProperty());
        variableValueColumn.setCellValueFactory(param -> param.getValue().variableValueProperty());

        variableValueColumn.setCellFactory(new Callback<TableColumn<VariableValueRow, LabeledComboOption>, TableCell<VariableValueRow, LabeledComboOption>>() {
            @Override
            public TableCell<VariableValueRow, LabeledComboOption> call(TableColumn<VariableValueRow, LabeledComboOption> param) {
                CustomComboBoxTableCell<VariableValueRow, LabeledComboOption> cell = new CustomComboBoxTableCell<VariableValueRow, LabeledComboOption>();
                cell.setConverter(new StringConverter<LabeledComboOption>() {
                    @Override
                    public String toString(LabeledComboOption object) {
                        if(object != null){
                            return object.getIdentifier();
                        }
                        return null;
                    }

                    @Override
                    public LabeledComboOption fromString(String string) {
                        LabeledComboOption labeledComboOption = cell.getItems().stream().filter(item -> string.contains(item.getValue())).findFirst().orElse(new LabeledComboOption(string, string));
                        return labeledComboOption;
                    }
                });
                cell.setListCell(new ListCell<LabeledComboOption>(){
                    @Override
                    protected void updateItem(LabeledComboOption item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.getIdentifier());
                        }
                    }
                });

//                cell.textProperty().addListener(new ChangeListener<String>() {
//                    @Override
//                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                        if(newValue == null && oldValue != null){
//                            cell.setText(oldValue);
//                            return;
//                        }
//                        if(!newValue.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && newValue.length() > 0){
//                            cell.setText(oldValue);
//                        }
//                    }
//                });
                //TODO
                cell.setValuePropertyListener(new ChangeListener<LabeledComboOption>() {
                    @Override
                    public void changed(ObservableValue<? extends LabeledComboOption> observable, LabeledComboOption oldValue, LabeledComboOption newValue) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                String val = newValue.getValue();
                                String ident = newValue.getIdentifier();
                                if(ident.contains(": ")){
                                    ident = ident.split(": ")[1];
                                    ident = ident.split(" ")[0];
                                    if(!ident.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && ident.length() > 0){
                                        cell.setComboBoxValue(oldValue);
                                        param.getTableView().getItems().get(cell.getIndex()).setVariableValue(oldValue);
                                    } else {
                                        LabeledComboOption option = new LabeledComboOption(ident, ident);
                                        cell.setComboBoxValue(option);
                                        param.getTableView().getItems().get(cell.getIndex()).setVariableValue(option);
                                    }
                                } else{
                                    if(!ident.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && ident.length() > 0){
                                        cell.setComboBoxValue(oldValue);
                                        param.getTableView().getItems().get(cell.getIndex()).setVariableValue(oldValue);
                                    }
                                }
                            }
                        });

                    }
                });

                cell.setComboBoxEditable(true);

                for(LineGroup line : lines){
                    cell.getItems().add(new LabeledComboOption(line.getName() + ": " + line.getLength() + " px", String.valueOf(line.getLength())));
                    if(currentScale != null){
                        double length = currentScale.getRoundedScaledValue(line.getLength());
                        cell.getItems().add(new LabeledComboOption(line.getName() + ": " + length + " "+ currentScale.getUnits(), String.valueOf(length)));
                    }
                }
                return cell;
            }
        });

        variableValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<VariableValueRow, LabeledComboOption>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<VariableValueRow, LabeledComboOption> event) {
                event.getTableView().getItems().get(event.getTablePosition().getRow()).setVariableValue(event.getNewValue());
            }
        });

        variablesValueTableView.setEditable(true);
        variableNameColumn.impl_setReorderable(false);
        variableValueColumn.impl_setReorderable(false);
        variableNameColumn.setSortable(false);
        variableValueColumn.setSortable(false);
        variablesValueTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        variablesValueTableView.getColumns().addAll(variableNameColumn, variableValueColumn);
    }

    private void validateExpression(List<?> eqVariables){
        if(expressionTextField.getText() == null || expressionTextField.getText().length() <= 0) return;
        Equation equation = new Equation(new EquationItem(nameTextField.getText(), expressionTextField.getText(), ""));
        try {
            List<String> variables = equation.getUserDefinedVariables();
            ArrayList<VariableValueRow> valueRows = new ArrayList<>();
            for(String s : variables){
                String variableValue = "";
                if(eqVariables != null && eqVariables.size() > 0){
                    if(eqVariables.get(0) instanceof Variable){
                        variableValue = getVariableValue((List<Variable>) eqVariables, s, variableValue);
                    } else if(eqVariables.get(0) instanceof VariableValueRow){
                        variableValue = getVariableValueRowValue((List<VariableValueRow>) eqVariables, s, variableValue);
                    }

                }
                valueRows.add(new VariableValueRow(s, new LabeledComboOption(variableValue, variableValue)));
            }
            ObservableList<VariableValueRow> data = FXCollections.observableArrayList(valueRows);
            variablesValueTableView.setItems(data);
            expressionTextField.setFocusColor(Color.GREEN);
//            saveButton.setDisable(false);
            okButton.setDisable(false);
            saveItem(currentIndex);
        } catch (Equation.SyntaxError syntaxError) {
            expressionTextField.setFocusColor(Color.RED);
//            saveButton.setDisable(true);
            okButton.setDisable(true);
        }
    }

    private String getVariableValue(List<Variable> eqVariables, String s, String variableValue) {
        for(Variable variable : eqVariables){
            if(variable.getName().equals(s)){
                variableValue = String.valueOf(variable.getValue());
            }
        }
        return variableValue;
    }

    private String getVariableValueRowValue(List<VariableValueRow> eqVariables, String s, String variableValue) {
        for(VariableValueRow variable : eqVariables){
            if(variable.getVariableName().equals(s)){
                variableValue = variable.getVariableValue().getValue();
            }
        }
        return variableValue;
    }

    private void loadEquation(EquationItem equation) {
        nameTextField.setText(equation.getName());
        expressionTextField.setText(equation.getExpression());
        variablesValueTableView.getItems().clear();
        validateExpression(equation.getVariables());
    }

    public void onCloseAction(ActionEvent actionEvent) {
        stage.close();
    }


    public void onNewAction(ActionEvent actionEvent) {
        EquationItem equation = new EquationItem();
        equationListView.getItems().addAll(equation);
        equationListView.getSelectionModel().selectLast();
        loadEquation(equation);
    }

    public void onRemoveAction(ActionEvent actionEvent) {
        equationListView.getItems().remove(equationListView.getSelectionModel().getSelectedItem());
        if(equationListView.getItems().size() <= 0){
            setDefaultState();
            variablesValueTableView.getItems().clear();
        }
        //TODO: Remove this from storage
    }

    private void setDefaultState() {
        nameTextField.setDisable(true);
        nameTextField.setText("");
        expressionTextField.setDisable(true);
        expressionTextField.setText("");
//        saveButton.setDisable(true);
        removeButton.setDisable(true);
        variablesValueTableView.setDisable(true);
    }

    public void onSaveAction(ActionEvent actionEvent) {


        //TODO: Store this somewhere (only name/equation, NOT VALUES)
    }

    public void saveItem(int index){
        equationListView.getItems().get(index).setName(nameTextField.getText());
        equationListView.getItems().get(index).setExpression(expressionTextField.getText());
        equationListView.getItems().get(index).setVariableValueRows(variablesValueTableView.getItems());
        equationListView.refresh();
    }


    public void onOkAction(ActionEvent actionEvent) {
        saveItem(currentIndex);
        EquationItem equation = new EquationItem(nameTextField.getText(), expressionTextField.getText(), "");
        List<VariableValueRow> items = variablesValueTableView.getItems();
        ArrayList<Variable> variables = new ArrayList<>();
        for(VariableValueRow value : items){
            variables.add(new Variable(value));
        }
        equation.setVariables(variables);
        listener.onAddEquation(equation);
        stage.close();

        EquationFileManager.exportEquationsToXml(equationListView.getItems());
    }


    public interface OnActionListener{
        void onAddEquation(EquationItem equation);
    }

}
