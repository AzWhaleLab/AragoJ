package equation;

import equation.model.EquationItem;
import equation.model.Variable;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.parsertokens.Token;

import java.util.ArrayList;
import java.util.List;

public class Equation {

    private Expression expression;

    public Equation(EquationItem item) {
        this.expression = new Expression(item.getExpression());
    }

    public List<String> getUserDefinedVariables() throws SyntaxError {
        ArrayList<String> tokensString = new ArrayList<>();
        boolean lexSyntax = expression.checkLexSyntax();

        if(lexSyntax) {
            List<Token> tokensList = expression.getCopyOfInitialTokens();
            for (Token t : tokensList)
                if (t.tokenTypeId == Token.NOT_MATCHED && t.looksLike.equals("argument") && !tokensString.contains(t.tokenStr))
                    tokensString.add(t.tokenStr);
        }
        else{
            // TODO: Specify where is the error!
            System.out.println(expression.getErrorMessage());
            throw new SyntaxError("There's a problem with the equation syntax.");
        }
        return tokensString;
    }

    public double solveExpression(List<Variable> arguments) throws ValidationError {

        for(Variable v : arguments){
            expression.addArguments(new Argument(v.getName(), v.getValue()));
        }
        boolean valid = expression.checkSyntax();
        if(valid){
            return expression.calculate();
        } else {
            throw new ValidationError();
        }
    }

    public boolean isValid(){
        return expression.checkLexSyntax();
    }

    public static class SyntaxError extends Exception {
        public SyntaxError() { super(); }
        public SyntaxError(String message) { super(message); }
    }
    public static class ValidationError extends Exception {
        public ValidationError() { super(); }
    }
}
