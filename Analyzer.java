package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Function function;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {

        boolean found  = false;
        if (!ast.getGlobals().isEmpty()){
            for (int i = 0; i < ast.getFunctions().size(); i++){
                visit(ast.getGlobals().get(i));
            }
        }
        if (!ast.getFunctions().isEmpty()){
            for (int i = 0; i < ast.getFunctions().size(); i++) {
                visit(ast.getFunctions().get(i));
            }
            for (int i = 0; i < ast.getFunctions().size(); i++) {
                Ast.Function func = ast.getFunctions().get(i);
                if (func.getName().equals("main") && func.getParameters().isEmpty() && func.getReturnTypeName().equals("Integer")){
                    found = true;
                }
            }
        }
        if (!found){
            throw new RuntimeException("No function with name main/0");
        }
        return null;
    }


    @Override
    public Void visit(Ast.Global ast) {
        if(ast.getValue().isPresent()) {
            visit(ast.getValue().get());
            requireAssignable(Environment.getType(ast.getTypeName()),ast.getValue().get().getType());
            scope.defineVariable(ast.getName(), ast.getName(), ast.getValue().get().getType(), false, Environment.NIL);
            ast.setVariable(scope.lookupVariable(ast.getName()));
        }
        else{
            scope.defineVariable(ast.getName(), ast.getName(),Environment.getType(ast.getTypeName()),true, Environment.NIL);
            ast.setVariable(scope.lookupVariable(ast.getName()));
        }
        return null;
    }

    @Override
    public Void visit(Ast.Function ast) {
        Environment.Type returnType;
        if(ast.getReturnTypeName().isPresent()){
            returnType = Environment.getType(ast.getReturnTypeName().get());
        }
        else{
            returnType = Environment.Type.NIL;
        }
        scope.defineVariable("returntype","returntype",returnType,true,Environment.NIL);
        List<String> params = ast.getParameterTypeNames();
        Environment.Type[] paramtypes = new Environment.Type[params.size()];
        if(!params.isEmpty()){
            for (int i = 0 ; i< params.size(); i++){
                paramtypes[i] = Environment.getType(params.get(i));
            }
        }
        scope.defineFunction(ast.getName(), ast.getName(),Arrays.asList(paramtypes), returnType, args -> Environment.NIL);

        if(!ast.getStatements().isEmpty()){
            for(int i = 0; i < ast.getStatements().size(); i++){
                try{
                    scope = new Scope(scope);
                    visit(ast.getStatements().get(i));
                }
                finally {
                    scope = scope.getParent();
                }
            }
        }
        ast.setFunction(scope.lookupFunction(ast.getName(), ast.getParameters().size()));
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        if(ast.getExpression().getClass() != Ast.Expression.Function.class){
            throw new RuntimeException("No Expression");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {

            if(ast.getValue().isPresent()) {//If present
                visit(ast.getValue().get());
                scope.defineVariable(ast.getName(),ast.getName(),ast.getValue().get().getType(), true,Environment.NIL);
                ast.setVariable(scope.lookupVariable(ast.getName()));
            }
            else{
                scope.defineVariable(ast.getName(), ast.getName(),Environment.getType(ast.getTypeName().get()),true, Environment.NIL);
                ast.setVariable(scope.lookupVariable(ast.getName()));
            }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        try {
            if (ast.getReceiver().getClass() != Ast.Expression.Access.class) {
                throw new RuntimeException("Not Access Expression");
            }
            //First we visit
            visit(ast.getValue());
            visit(ast.getReceiver());
            requireAssignable(ast.getReceiver().getType(),ast.getValue().getType());
        }
        catch(RuntimeException exc){
            throw new RuntimeException(exc);
        }
            return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {

        if(ast.getThenStatements().isEmpty()){
            throw new RuntimeException("Thenstatement is empty");
        }
        visit(ast.getCondition());
        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());
        if(!ast.getThenStatements().isEmpty()) {
            //Else statements
            for (int i = 0; i < ast.getElseStatements().size(); i++) {
                try {
                    scope = new Scope(scope);
                    visit(ast.getElseStatements().get(i));
                } finally {
                    scope = scope.getParent();
                }
            }
        }
        //Then Statements
        for(int i = 0; i < ast.getThenStatements().size(); i++){
            try {
                scope = new Scope(scope);
                visit(ast.getThenStatements().get(i));
            }
            finally{
                scope = scope.getParent();
            }
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        //check conditions for RuntimeException
        visit(ast.getCondition());
        List<Ast.Statement.Case> cases= ast.getCases();
        for (int i = 0; i < cases.size(); i++) {
            visit(cases.get(i));
            if(cases.get(i).getValue().isPresent()){
                visit(cases.get(i).getValue().get());
                requireAssignable(ast.getCases().get(i).getValue().get().getType(), ast.getCondition().getType());
            }

            if (i == (ast.getCases().size() - 1)) {
                if (cases.get(i).getValue().isPresent()) {
                    throw new RuntimeException("Invalid default case");
                }
            }



        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        try {
            scope = new Scope(scope);
            for (int i = 0; i < ast.getStatements().size(); i++) {
                visit(ast.getStatements().get(i));

            }
        }
        finally {
            scope = scope.getParent();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {

            visit(ast.getCondition());
            requireAssignable(Environment.Type.BOOLEAN,ast.getCondition().getType());
            try{
                scope = new Scope(scope);
                for(Ast.Statement statement : ast.getStatements()){
                    visit(statement);
                }
            }
            finally {
                scope = scope.getParent();
            }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        try{
            visit(ast.getValue());
            Environment.Variable ret = scope.lookupVariable("returntype");
            requireAssignable(ret.getType(),ast.getValue().getType());
        }
        catch(RuntimeException exc){
            throw new RuntimeException(exc);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if(ast.getLiteral()instanceof  String){
            ast.setType(Environment.Type.STRING);
        }
        else if(ast.getLiteral() instanceof Character){
            ast.setType(Environment.Type.CHARACTER);
        }
        else if(ast.getLiteral() instanceof Boolean){
            ast.setType(Environment.Type.BOOLEAN);
        }
        else if(ast.getLiteral() == Environment.NIL){
            ast.setType(Environment.Type.NIL);
        }
        else if(ast.getLiteral() instanceof BigInteger){
            BigInteger integer = BigInteger.class.cast(ast.getLiteral());
            if ((integer.intValueExact() > Integer.MAX_VALUE) || (integer.intValueExact() < Integer.MIN_VALUE)){
                throw new RuntimeException("Int Out of Range");
            }
            ast.setType(Environment.Type.INTEGER);
        }
        else if (ast.getLiteral() instanceof BigDecimal) {
            BigDecimal decimal = BigDecimal.class.cast(ast.getLiteral());
            if ((decimal.doubleValue() > Double.MAX_VALUE) || (decimal.doubleValue() < Double.MIN_VALUE)) {
                throw new RuntimeException("Decimal outside range");
            }
            ast.setType(Environment.Type.DECIMAL);

        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        visit(ast.getExpression());
        if(ast.getExpression().getClass() != Ast.Expression.Binary.class){
            throw new RuntimeException("Not Binary Expression");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        String operator = ast.getOperator();
        visit(ast.getLeft());
        visit(ast.getRight());
        if(operator.equals("&&") || operator.equals("||")){
            requireAssignable(Environment.Type.BOOLEAN, ast.getLeft().getType());
            requireAssignable(Environment.Type.BOOLEAN, ast.getRight().getType());
            ast.setType(Environment.Type.BOOLEAN);
        }
        else if(operator.equals("<") || operator.equals(">") || operator.equals("==") || operator.equals("!=")){
            requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
            requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
            ast.setType(Environment.Type.BOOLEAN);
        }
        else if(operator.equals("+")){
            if(ast.getLeft().getType() == Environment.Type.STRING || ast.getRight().getType() == Environment.Type.STRING){
                ast.setType(Environment.Type.STRING);
            }
            else if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL){
                if(ast.getLeft().getType() != ast.getRight().getType()) {
                    throw new RuntimeException("Not same types");
                }
                ast.setType(ast.getLeft().getType());
            }
            else{
                throw new RuntimeException("Not valid types");
            }
        }
        else if (operator.equals("-") || operator.equals("*") || operator.equals("/")) {
            if(ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL){
                if(ast.getLeft().getType() != ast.getRight().getType()) {
                    throw new RuntimeException("Not same types");
                }
                ast.setType(ast.getLeft().getType());
            }
            else{
                throw new RuntimeException("Not valid types");
            }
        }
        else if (operator.equals("^")){
            if((ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL) && ast.getRight().getType() == Environment.Type.INTEGER){
                ast.setType(ast.getLeft().getType());
            }
            else{
                throw new RuntimeException("Not valid types");
            }
        }
        else{
            throw new RuntimeException("Not Binary Types");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        if(ast.getOffset().isPresent()){
            //Ast.Expression.Access acc = Ast.Expression.Access.class.cast(ast.get)
            try{
                //scope = scope.lookupVariable();
                ast.setVariable(scope.lookupVariable(ast.getName()));
            }
            finally {
                scope = scope.getParent();
            }
        }
        else{
            ast.setVariable(scope.lookupVariable(ast.getName()));
        }
        //throw new RuntimeException("Offset No integer");

        return null;
    }


    @Override
    public Void visit(Ast.Expression.Function ast) {
        List<Environment.Type> param = scope.lookupFunction(ast.getName(), ast.getArguments().size()).getParameterTypes();
        for(int i = 0 ; i < ast.getArguments().size(); i++){
            visit(ast.getArguments().get(i));
            requireAssignable(param.get(i), ast.getArguments().get(i).getType());
        }
        ast.setFunction(scope.lookupFunction(ast.getName(), ast.getArguments().size()));
        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {

        List<Ast.Expression> list = ast.getValues();
        for (Ast.Expression expression : list) {
            visit(expression);
            requireAssignable(ast.getType(), expression.getType());
        }

        return null;
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
      if(target != type && target != Environment.Type.ANY && target != Environment.Type.COMPARABLE){
          throw new RuntimeException("Require Assignable error");
      }
    }

}
