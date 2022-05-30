package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {     //TODO
        //create a "class Main {"

        //declare globals -> properties

        //  declare "public static void main(String[] args) {
        //                 System.exit(new Main().main());
        //           }

        //  declare each of our functions -> methods
        //  one of our functions -> methods is called main()
        //print "}" to close the class Main

        print("public class Main {");
        indent++;
        newline(0);
        if (!ast.getGlobals().isEmpty()) {
            for (int i = 0; i < ast.getGlobals().size(); i++) {
                newline(indent);
                print(ast.getGlobals().get(i));
            }
            newline(0); // blank line
        }

        // Java main method
        newline(indent);
        print("public static void main(String[] args) {");
        indent++;
        newline(indent);
        print("System.exit(new Main().main());");
        indent--;
        newline(indent);
        print("}");
        newline(0);
        for (int i = 0; i < ast.getFunctions().size(); i++) {
            newline(indent);
            print(ast.getFunctions().get(i));
        }
        newline(0);

        indent--;
        newline(indent);
        print("}");

        return null;
    }   //TODO

    @Override
    public Void visit(Ast.Global ast) {

        boolean isList = false;

        if (!ast.getMutable()) {
            print("final ");
        }

        if (ast.getValue().get() instanceof Ast.Expression.PlcList) {     //Needs to change
            isList = true;
        }

        if (ast.getTypeName().equals("Integer")) {
            print("int");
        }
        else if (ast.getTypeName().equals("Decimal")) {
            print("double");
        }
        else if (ast.getTypeName().equals("Boolean")) {
            print("boolean");
        }
        else if (ast.getTypeName().equals("Character")) {
            print("char");
        }
        else if (ast.getTypeName().equals("String")) {
            print("String");
        }

        if (isList) {
            print("[]");
        }

        print(" ",ast.getName());
        if (ast.getValue().isPresent()) {
            print(" = ");

            if (isList) {
                visit(ast.getValue().get());
            }
            else {
                print(ast.getValue().get());
            }
        }
        print(";");
        return null;
    }   //TODO

    @Override
    public Void visit(Ast.Function ast) {
        print(ast.getFunction().getReturnType().getJvmName());
        print(" ",ast.getName(),"(");
        for (int i = 0; i < ast.getParameters().size(); i++) {
            print(ast.getParameterTypeNames().get(i));
            print(" ");
            print(ast.getParameters().get(i));
            if (i != ast.getParameters().size() - 1) {
                print(", ");
            }
        }
        print(") {");
        if (!ast.getStatements().isEmpty()) {
            indent++;
            for (int i = 0; i < ast.getStatements().size(); i++) {
                newline(indent);
                print(ast.getStatements().get(i));
            }
            indent--;
            newline(indent);
        }
        print("}");

        return null;
    }   //TODO

    @Override
    public Void visit(Ast.Statement.Expression ast) {

        print(ast.getExpression(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {

        //LET x: INTEGER = 3;

        //write: TYPE variable_name
        print(ast.getVariable().getType().getJvmName()," ", ast.getVariable().getJvmName());

        //is there an assigned value?
        if (ast.getValue().isPresent()) {
            //if so, write: = and the value
            print(" = ", ast.getValue().get());
        }
        //write ;
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {

        visit(ast.getReceiver());
        print(" = ");
        visit(ast.getValue());
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        print("if (");
        print(ast.getCondition());
        print(") {");
        indent++;
        for (int i = 0; i < ast.getThenStatements().size(); i++) {
            newline(indent);
            print(ast.getThenStatements().get(i));
        }
        indent--;
        newline(indent);
        print("}");

        if (!ast.getElseStatements().isEmpty()) {
            print(" else {");
            indent++;
            for (int i = 0; i < ast.getElseStatements().size(); i++) {
                newline(indent);
                print(ast.getElseStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        }

        return null;

    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        print("switch ");
        print("(",ast.getCondition(), ") ","{");
        int count = 0 ;
        for (int i = 0; i < ast.getCases().size(); i++) {
            indent = 1;
            newline(indent);
            //indent++;
            count++;
            if(ast.getCases().size() > count){
                print("case ");
            }
            else {
                //newline(1);
                print("default");
            }
            visit(ast.getCases().get(i));
        }
        newline(0);
        print("}");
        return null;

    }

    @Override
    public Void visit(Ast.Statement.Case ast) {

        if (!ast.getValue().isPresent()) {
            print(":");
        }
        else {
            print(ast.getValue().get(), ":");
        }

        int temp = indent;
        newline(++indent);
        for (int i = 0; i < ast.getStatements().size(); i++) {
            visit(ast.getStatements().get(i));
            if (i != ast.getStatements().size() - 1) {
                newline(indent);
            }

        }

        indent = temp;
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        //print the while structure, including condition
        print("while (", ast.getCondition(), ") {");

        //determine if there are statements to process
        if (!ast.getStatements().isEmpty()) {

            //setup the next line
            newline(++indent);
            //handle all statements in the while statement body
            for (int i = 0; i < ast.getStatements().size(); i++) {

                //check if newline and indent are needed
                if (i != 0) {
                    //setup the next line
                    newline(indent);
                }
                //print the next statement
                print(ast.getStatements().get(i));
            }
            //setup the next line
            newline(--indent);
        }
        //close the while
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {

        print("return ");
        visit(ast.getValue());
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {

        if (ast.getType() == Environment.Type.CHARACTER) {
            print("'");
            print(ast.getLiteral());
            print("'");
        }
        else if (ast.getType() == Environment.Type.STRING) {
            print("\"");
            print(ast.getLiteral());
            print("\"");
        }
        else if (ast.getType() == Environment.Type.INTEGER) {
            BigInteger integer = BigInteger.class.cast(ast.getLiteral());
            print(integer.intValue());
        }
        else if (ast.getType() == Environment.Type.DECIMAL) {
            BigDecimal decimal = BigDecimal.class.cast(ast.getLiteral());
            print(decimal.doubleValue());
        }

        else {
            print(ast.getLiteral());
        }
        return null;
    } //Might need to change

    @Override
    public Void visit(Ast.Expression.Group ast) {

        print("(");
        visit(ast.getExpression());
        print(")");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {

        boolean leftString = ast.getLeft().getType().toString().equals("String");   //Need to change
        boolean rightString = ast.getRight().getType().toString().equals("String"); //Need to change
        if (ast.getOperator().equals("^")) {
            print("Math.pow(", ast.getLeft(), ", ", ast.getRight(), ")");
        }
        else {

            if (leftString) {
                print("\"");
            }
            visit(ast.getLeft());
            if (leftString) {
                print("\"");
            }

            print(" ", ast.getOperator(), " ");

            if (rightString) {
                print("\"");
            }
            visit(ast.getRight());
            if (rightString) {
                print("\"");
            }
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {

        if (ast.getOffset().isPresent()) {
            print(ast.getVariable().getJvmName(), "[");
            visit(ast.getOffset().get());
            print("]");
        }
        else {
            print(ast.getVariable().getJvmName());
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {

        print(ast.getFunction().getJvmName());
        print("(");

        for (int i = 0; i < ast.getArguments().size() - 1; i++) {
            visit(ast.getArguments().get(i));
            print(", ");
        }

        if (ast.getArguments().size() != 0) {
            visit(ast.getArguments().get(ast.getArguments().size() - 1));
        }

        print(")");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        print("{");
        int count = 0;
        List<Ast.Expression> list = ast.getValues();
        for (Ast.Expression expression : list) {
            count++;
            print(expression);
            if(list.size() > count){
                print(", ");
            }
        }
        print("}");
        return null;
    }

}