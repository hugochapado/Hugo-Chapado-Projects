package plc.project;

import javax.lang.model.element.ElementVisitor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);
    private Object obj;

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        //Globals followed by functions
        ast.getGlobals().forEach(global ->{visit(global);});
        ast.getFunctions().forEach(function -> {visit(function);});
        List<Environment.PlcObject> listObj = new ArrayList<Environment.PlcObject>();
        return scope.lookupFunction("main",0).invoke(listObj);
    }

    @Override
    public Environment.PlcObject visit(Ast.Global ast) {
        //Variable in the current Scope
        if (ast.getValue().isPresent()) {
            scope.defineVariable(ast.getName(),false, visit(ast.getValue().get()));
        }
        //Not initialized at declaration use nil
        else {
            scope.defineVariable(ast.getName(), false,Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Function ast) {  //TODO
        //Define a function in current scope

            scope.defineFunction(ast.getName(), ast.getParameters().size(), lambdda -> {
                try {
                    scope = new Scope(scope);
                    ast.getParameters().forEach(param -> {
                        lambdda.forEach(var -> {
                            scope.defineVariable(param, false, var);
                        });
                    });
                    ast.getStatements().forEach(stat -> {
                        visit(stat);
                    });
                }
                catch (Return ret) {
                    return ret.value;
                }
                finally {
                    scope = scope.getParent();
                }

                return Environment.NIL;
            });


        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Expression ast) {
        //Evaluate the expression
        visit(ast.getExpression());
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Declaration ast) {
        if (ast.getValue().isPresent()) {
            scope.defineVariable(ast.getName(),true, visit(ast.getValue().get()));
        }
        else {
            scope.defineVariable(ast.getName(), true, Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Assignment ast) {      //TODO
        //Ensure receiver is an Access
        if (ast.getReceiver().getClass() != Ast.Expression.Access.class){
            throw new RuntimeException("Receiver is not Access");
        }
        //Set Variable in current scope

        Ast.Expression.Access var = Ast.Expression.Access.class.cast(ast.getReceiver());
        if(((Ast.Expression.Access) ast.getReceiver()).getOffset().isPresent()){
            List<Object> objList = (List<Object>)scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue().getValue();

            Object val=visit(ast.getValue()).getValue();
            Ast.Expression.Literal lit= (Ast.Expression.Literal) ((Ast.Expression.Access) ast.getReceiver()).getOffset().get();
            BigInteger off=(BigInteger)lit.getLiteral();

            objList.set(off.intValue(),val);

        }
        else {

            scope.lookupVariable(var.getName()).setValue(visit(ast.getValue()));
        }

        //Doubt


        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.If ast) {
        //If condition is true evaluate then statements
        if (requireType(Boolean.class,visit(ast.getCondition()))){
            try {
                scope = new Scope(scope);

                for (Ast.Statement statement : ast.getThenStatements()) {
                    visit(statement);
                }
            }
            finally {
                scope = scope.getParent();
            }
        }
        //if not  Evaluate Else statement
        else if (!requireType(Boolean.class,visit(ast.getCondition()))){
            try {
                scope = new Scope(scope);
                for (Ast.Statement statement : ast.getElseStatements()) {
                    visit(statement);
                }
            }
            finally{
                scope = scope.getParent();
            }
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Switch ast) {
        try {
            scope = new Scope(scope);
            boolean isDefault = true;

            List<Ast.Statement.Case> caseList = ast.getCases();
            //If condition is equivalent to a CASE
            for (Ast.Statement.Case aCase : caseList) {

                if (aCase.getValue().isPresent()) {
                    if (visit(aCase.getValue().get()).getValue().equals(visit(ast.getCondition()).getValue())) { //Logic is correct, execution isn't
                        visit(aCase);
                        isDefault = false;
                    }
                }
            }

            //Else, evaluate statements of DEFAULT
            if (isDefault) {
                visit(caseList.get(caseList.size() - 1));
            }
        }
        finally {
            scope = scope.getParent();
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Case ast) {

        // if the condition is equivalent to a CASE, evaluate the statements for that case
        for (Ast.Statement statement: ast.getStatements()) {
            visit(statement);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.While ast) {
        while(requireType(Boolean.class, visit(ast.getCondition()))) {
            try { // enter new scope
                scope = new Scope(scope);
                ast.getStatements().forEach(this::visit);
            } finally { // restore scope
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Return ast) {
        throw new Return(visit(ast.getValue()));
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Literal ast) {

        if(ast.getLiteral() == null) {
            return Environment.NIL;
        }
        return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Group ast) {
        //TODO
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Binary ast) {
        String bin = ast.getOperator();
        if (bin.equals("&&")) {
            if (requireType(Boolean.class, visit(ast.getLeft())).equals(requireType(Boolean.class, visit(ast.getRight())))) {
                return visit(ast.getLeft());
            } else {
                return Environment.create(Boolean.FALSE);
            }
        }
        if (bin.equals("||")) {
            if (requireType(Boolean.class, visit(ast.getLeft())).equals((Boolean.TRUE))) {
                return visit(ast.getLeft());
            } else if (requireType(Boolean.class, visit(ast.getRight())).equals((Boolean.TRUE))) {
                return visit(ast.getRight());
            } else {
                return Environment.create(Boolean.FALSE);
            }
        } else if (bin.equals("<") || bin.equals(">")) {

            //must be Comparable
            if (visit(ast.getLeft()).getValue().getClass().equals(visit(ast.getRight()).getValue().getClass()) && visit(ast.getLeft()).getValue() instanceof Comparable) {
                Comparable<Object> left = (Comparable<Object>) visit(ast.getLeft()).getValue();
                Comparable<Object> right = (Comparable<Object>) visit(ast.getRight()).getValue();
                int comp = left.compareTo(right);

                if (bin.equals("<")) {
                    if (comp < 0) {
                        return Environment.create(Boolean.TRUE);
                    } else {
                        return Environment.create(Boolean.FALSE);
                    }
                } else {
                    if (comp > 0) {
                        return Environment.create(Boolean.TRUE);
                    } else {
                        return Environment.create(Boolean.FALSE);
                    }
                }
            }
        } else if (ast.getOperator().equals("==") || ast.getOperator().equals("!=")) {

            //Evaluate both operands

            if (visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue())) {
                if (bin.equals("==")) {
                    return Environment.create(Boolean.TRUE);
                } else {
                    return Environment.create(Boolean.FALSE);
                }
            } else {
                if (bin.equals("!=")) {
                    return Environment.create(Boolean.TRUE);
                } else {
                    return Environment.create(Boolean.FALSE);
                }
            }

        } else if (bin.equals("+")) {

            //Evaluate both the LHS and RHS

            //If either expression is a String, the result is their concatenation
            if (visit(ast.getLeft()).getValue().getClass().equals(String.class) || visit(ast.getRight()).getValue().getClass().equals(String.class)) {
                return Environment.create(visit(ast.getLeft()).getValue().toString() + visit(ast.getRight()).getValue().toString());
            } else if (visit(ast.getLeft()).getValue().getClass().equals(BigInteger.class) && visit(ast.getRight()).getValue().getClass().equals(BigInteger.class)) {
                return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).add(BigInteger.class.cast(visit(ast.getRight()).getValue())));
            } else if (visit(ast.getLeft()).getValue().getClass().equals(BigDecimal.class) && visit(ast.getRight()).getValue().getClass().equals(BigDecimal.class)) {
                return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).add(BigDecimal.class.cast(visit(ast.getRight()).getValue())));
            }
            //Otherwise throw an exception
            else {
                throw new RuntimeException("Invalid operands.");
            }
        } else if (bin.equals("-") || bin.equals("*")) {

            //Evaluate both the LHS and RHS
            if (visit(ast.getLeft()).getValue().getClass().equals(BigDecimal.class) && visit(ast.getRight()).getValue().getClass().equals(BigDecimal.class)) {
                if (bin.equals("-")) {
                    return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).subtract(BigDecimal.class.cast(visit(ast.getRight()).getValue())));
                } else {
                    return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).multiply(BigDecimal.class.cast(visit(ast.getRight()).getValue())));
                }
            }
            else if(visit(ast.getLeft()).getValue().getClass().equals(BigInteger.class) && visit(ast.getRight()).getValue().getClass().equals(BigInteger.class)) {
                if (bin.equals("-")) {
                    return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).subtract(BigInteger.class.cast(visit(ast.getRight()).getValue())));
                } else {
                    return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).multiply(BigInteger.class.cast(visit(ast.getRight()).getValue())));
                }
            }
            else {
                throw new RuntimeException("Invalid operands.");
            }
        } else if (ast.getOperator().equals("/")) {

            //Evaluate both the LHS and RHS expressions
            if (visit(ast.getRight()).getValue().equals(BigInteger.ZERO) && visit(ast.getRight()).getValue().getClass().equals(BigInteger.class)) {
                throw new RuntimeException("Dividing by Zero");
            } else if (visit(ast.getRight()).getValue().equals(BigDecimal.ZERO) && visit(ast.getRight()).getValue().getClass().equals(BigDecimal.class)) {
                throw new RuntimeException("Dividing by Zero");
            } else if (visit(ast.getLeft()).getValue().getClass().equals(BigDecimal.class) && visit(ast.getRight()).getValue().getClass().equals(BigDecimal.class)) {
                return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).divide(BigDecimal.class.cast(visit(ast.getRight()).getValue()), RoundingMode.HALF_EVEN));
            } else if (visit(ast.getLeft()).getValue().getClass().equals(BigInteger.class) && visit(ast.getRight()).getValue().getClass().equals(BigInteger.class)) {
                return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).divide(BigInteger.class.cast(visit(ast.getRight()).getValue())));
            }
        } else if (ast.getOperator().equals("^")) {

            //The exponent is required to a BigInteger
            // if (requireType(BigInteger.class, visit(ast.getRight()))) {   //Needs to change
            if (visit(ast.getLeft()).getValue().getClass().equals(BigInteger.class) && visit(ast.getRight()).getValue().getClass().equals(BigInteger.class)) {

                BigInteger bigint1 = (BigInteger.class.cast(visit(ast.getLeft()).getValue()));
                BigInteger bigint2 = (BigInteger.class.cast(visit(ast.getLeft()).getValue()));
                BigInteger bigint3 = (BigInteger.class.cast(visit(ast.getRight()).getValue()));
                while (!bigint3.equals(BigInteger.ZERO)) {
                    bigint1 = bigint1.multiply(bigint2);
                    bigint3 = bigint3.subtract(BigInteger.ONE);
                }
            } else if (visit(ast.getLeft()).getValue().getClass().equals(BigDecimal.class) && visit(ast.getRight()).getValue().getClass().equals(BigInteger.class)) {
                BigDecimal bigint1 = (BigDecimal.class.cast(visit(ast.getLeft()).getValue()));
                BigDecimal bigint2 = (BigDecimal.class.cast(visit(ast.getLeft()).getValue()));
                BigInteger bigint3 = (BigInteger.class.cast(visit(ast.getRight()).getValue()));
                while (!bigint3.equals(BigInteger.ZERO)) {
                    bigint1 = bigint1.multiply(bigint2);
                    bigint3 = bigint3.subtract(BigInteger.ONE);
                }
                //Evaluate both the LHS (base) and RHS (exponent) expressions
                //The result is the LHS (base) raised to the power of the RHS (exponent)
            } else {
                throw new RuntimeException("Invalid Binary Operator");
            }
        }
        throw new RuntimeException("Invalid Binary Operator");
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Access ast) {     //TODO

        //Return the value of the appropriate variable in the current scope
        if(ast.getName().toString().equals("list")){
            if (visit(ast.getOffset().get()).getValue().getClass().equals(BigInteger.class)) {

                Environment.PlcObject off = visit(ast.getOffset().get());
               Scope value=getScope();
               //Object obj=value.lookupVariable(ast.getName()).getValue().getValue();
                Object list= (value.lookupVariable(ast.getName()).getValue().getValue());
               BigInteger i= (BigInteger) off.getValue();
                int n=i.intValue();
                List<Object> objList = (List<Object>) list;

               return Environment.create(objList.get(n));

            }
            else {
            throw  new RuntimeException("Not BIGInteger Offset");
            }

        }
        return scope.lookupVariable(ast.getName()).getValue();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Function ast) {

        List<Environment.PlcObject> arguments = new ArrayList<Environment.PlcObject>();
        for (int i = 0; i < ast.getArguments().size(); i++) {
            arguments.add(visit(ast.getArguments().get(i)));
        }
        //Use Environment.Function.invoke and return it\
        Environment.Function func = scope.lookupFunction(ast.getName(),ast.getArguments().size());
        return func.invoke(arguments);
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.PlcList ast) {

        List<Object> valList = new ArrayList<>();
        for (int i = 0; i < ast.getValues().size(); i++) {
            if (ast.getValues().get(i).getClass() == Ast.Expression.Literal.class) {
                valList.add(visit(ast.getValues().get(i)).getValue());
            }
        }
        return Environment.create(valList);
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
