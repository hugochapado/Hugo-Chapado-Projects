package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {  //TODO

        List<Ast.Global> globals = new ArrayList<Ast.Global>();
        List<Ast.Function> functions = new ArrayList<Ast.Function>();

        while(tokens.has(0) && !peek("FUN")) {
            globals.add(parseGlobal());
        }
        while(tokens.has(0)) {
            functions.add(parseFunction());
        }

        return new Ast.Source(globals, functions);
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Global parseGlobal() throws ParseException {  //TODO

        Ast.Global value=null;

        if (peek("LIST")) {
            value=parseList();
        }
        else if (peek("VAR")) {
            value=parseMutable();
        }
        else if (peek("VAL")) {
            value=parseImmutable();
        }

        if (!match(";")) {
            throw new ParseException("Missing semicolon", tokens.get(0).getIndex());
        }

        return value;
    }

    /**
     * Parses the {@code list} rule. This method should only be called if the
     * next token declares a list, aka {@code LIST}.
     */
    public Ast.Global parseList() throws ParseException { //TODO

        String typeString = null;
        match("LIST");
        String name = tokens.get(0).getLiteral();
        match(name);
        match(":");
        //match identifier
        match(Token.Type.IDENTIFIER);
        typeString = tokens.get(-2).getLiteral();

        match("=");
        match("[");

        Ast.Expression exp = parseExpression();
        List<Ast.Expression>express = new ArrayList<Ast.Expression>();

        while(tokens.has(0)){

            match(",");
            express.add(parseExpression());
        }
        //typeString = "Type";
        if(!match("]")) {
            throw new ParseException("Missing ']'", tokens.get(0).getIndex());
        }
        return new Ast.Global(name, typeString,false, Optional.of(exp));
    }

    /**
     * Parses the {@code mutable} rule. This method should only be called if the
     * next token declares a mutable global variable, aka {@code VAR}.
     */
    public Ast.Global parseMutable() throws ParseException {//TODO

        String typeString = null;

        match("VAR");
        Ast.Expression exp = null;
        String name = tokens.get(0).getLiteral();
        match(name);
        tokens.advance();

        match(":");
        //match identifier
        match(Token.Type.IDENTIFIER);

        typeString = tokens.get(-1).getLiteral();
        if (match("=")){
            exp = parseExpression();
        }
        return new Ast.Global(name, typeString,true,Optional.of(exp));
    }

    /**
     * Parses the {@code immutable} rule. This method should only be called if the
     * next token declares an immutable global variable, aka {@code VAL}.
     */
    public Ast.Global parseImmutable() throws ParseException {//TODO

        String typeString = null;
        match("VAL");
        String name = tokens.get(0).getLiteral();
        match(name);
        tokens.advance();

        match(":");
        //match identifier
        match(Token.Type.IDENTIFIER);
        typeString = tokens.get(-1).getLiteral();

        if (match("=")){
            Ast.Expression exp = parseExpression();
            return new Ast.Global(name, typeString,false, Optional.of(exp));
        }

        throw new ParseException("Missing equal", tokens.get(0).getIndex());
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Function parseFunction() throws ParseException {//TODO
        String returnType = null;
        match("FUN");
        String name = tokens.get(0).getLiteral();
        match(name);

        List<String> strings=new ArrayList<String>();
        List<String> paramsTypes=new ArrayList<String>();
        match("(");

        if (match(Token.Type.IDENTIFIER)) {

            strings.add(tokens.get(-2).getLiteral());
            match(":");
            //match identifier
            match(Token.Type.IDENTIFIER);
            tokens.advance();
            tokens.advance();
            paramsTypes.add(tokens.get(0).getLiteral());

            while(match(",")) {
                match(Token.Type.IDENTIFIER);
                strings.add(tokens.get(-1).getLiteral());
                match(":");
                //match identifier
                match(Token.Type.IDENTIFIER);
                tokens.advance();
                tokens.advance();
                paramsTypes.add(tokens.get(-1).getLiteral());
            }
        }
        match(")");

        if (match(":")) {
            match(Token.Type.IDENTIFIER);
            tokens.advance();

            returnType = tokens.get(-2).getLiteral();
        }
        match("DO");
        List<Ast.Statement>block = parseBlock();

        match("END");
        return new Ast.Function(name,strings,paramsTypes,Optional.of(returnType),block);
    }

    /**
     * Parses the {@code block} rule. This method should only be called if the
     * preceding token indicates the opening a block.
     */
    public List<Ast.Statement> parseBlock() throws ParseException {  //TODO

        List<Ast.Statement> block = new ArrayList<Ast.Statement>();

        while(tokens.has(0) && !peek("END")&& !peek("DEFAULT")&& !peek("ELSE")&& !peek("CASE")) {
            block.add(parseStatement());
        }
        return block;
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */

    public Ast.Statement parseStatement() throws ParseException {//TODO

        if (peek("LET")) {
            return parseDeclarationStatement();
        }
        else if (peek("SWITCH")) {
            return parseSwitchStatement();
        }
        else if (peek("IF")) {
            return parseIfStatement();
        }
        else if (peek("WHILE")) {
            return parseWhileStatement();
        }
        else if (peek("RETURN")) {
            return parseReturnStatement();
        }

        Ast.Expression exp = parseExpression();
        if (match("=")) {

            Ast.Expression exp2 = parseExpression();
            if (!match(";")) {
                throw new ParseException("Expected semicolon",tokens.get(0).getIndex());
            }
            return new Ast.Statement.Assignment(exp,exp2);
        }
        else {

            if (!match(";")) {
                throw new ParseException("Expected semicolon", tokens.get(-1).getIndex());
            }
            return new Ast.Statement.Expression(exp);
        }

    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {  //TODO
        String str = null;
        String typeString = null;
        match("LET");
        if (match(Token.Type.IDENTIFIER)){
            str = tokens.get(-1).getLiteral();
        }
        if (match(":")) {
            typeString = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);
        }

        if (match("=")) {
            Ast.Expression  exp = parseExpression();
            if (!match(";")) {
                throw new ParseException("Missing semicolon",tokens.get(0).getIndex());
            }
            return new Ast.Statement.Declaration(str,Optional.of(exp));
        }
        return new Ast.Statement.Declaration(str, Optional.ofNullable(typeString),Optional.empty());
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {  //TODO
        match("IF");
        Ast.Expression exp = parseExpression();
        match("DO");

        List<Ast.Statement> ifStatements = parseBlock();
        List<Ast.Statement> elseStatements = new ArrayList<>();

        if (match("ELSE")) {
            elseStatements = parseBlock();
        }

        match("END");

        return new Ast.Statement.If(exp, ifStatements, elseStatements);

    }

    /**
     * Parses a switch statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a switch statement, aka
     * {@code SWITCH}.
     */
    public Ast.Statement.Switch parseSwitchStatement() throws ParseException {  //TODO
        match("SWITCH");
        Ast.Expression exp = parseExpression();
        List<Ast.Statement.Case>cases=new ArrayList<Ast.Statement.Case>();
        while(match("CASE")){
            cases.add(parseCaseStatement());
        }
        match("DEFAULT");
        parseBlock();
        match("END");
        return new Ast.Statement.Switch(exp,cases);
    }

    /**
     * Parses a case or default statement block from the {@code switch} rule.
     * This method should only be called if the next tokens start the case or
     * default block of a switch statement, aka {@code CASE} or {@code DEFAULT}.
     */
    public Ast.Statement.Case parseCaseStatement() throws ParseException {  //TODO
        Ast.Expression  exp = parseExpression();
        match(":");
        List<Ast.Statement>block=parseBlock();
        return new Ast.Statement.Case(Optional.of(exp),block);
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {  //TODO
        match("WHILE");
        Ast.Expression exp = parseExpression();
        match("DO");

        List<Ast.Statement> block = parseBlock();
        if (!match("END")) {
            throw new ParseException("While loop doesn't end", tokens.get(0).getIndex());
        }

        return new Ast.Statement.While(exp, block);
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {  //TODO
        match("RETURN");
        Ast.Expression exp = parseExpression();
        if (!match(";")) {
            throw new ParseException("Missing semicolon", tokens.get(0).getIndex());
        }
        return new Ast.Statement.Return(exp);
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() throws ParseException  {

        Ast.Expression exp = parseComparisonExpression();
        while (peek("&&") || peek("||")) {

            String operator = tokens.get(0).getLiteral();

            match(Token.Type.OPERATOR);

            Ast.Expression exp2 = parseComparisonExpression();

            if (!peek("&&") && !peek("||"))
                return new Ast.Expression.Binary(operator, exp, exp2);
            else
                exp = new Ast.Expression.Binary(operator, exp, exp2);
        }

        return exp;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseComparisonExpression() throws ParseException {

        Ast.Expression exp = parseAdditiveExpression();

        while (peek("<") || peek(">")|| peek("==")|| peek("!=")) {

            String operator = tokens.get(0).getLiteral();

            match(Token.Type.OPERATOR);

            Ast.Expression exp2 = parseAdditiveExpression();

            if (!peek("<") && !peek(">") && !peek("==") && !peek("!=")) {
                return new plc.project.Ast.Expression.Binary(operator, exp, exp2);
            }
            else {
                exp = new Ast.Expression.Binary(operator, exp, exp2);
            }
        }

        return exp;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {

        Ast.Expression exp = parseMultiplicativeExpression();

        while (peek("+") || peek("-")) {

            String operator = tokens.get(0).getLiteral();

            match(Token.Type.OPERATOR);

            Ast.Expression exp2 = parseMultiplicativeExpression();

            if (!peek("+") && !peek("-") ) {
                return new plc.project.Ast.Expression.Binary(operator, exp, exp2);
            }
            else {
                exp = new Ast.Expression.Binary(operator, exp, exp2);
            }
        }

        return exp;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {

        Ast.Expression exp = parsePrimaryExpression();

        while (peek("*") || peek("/")|| peek("^")) {

            String operator = tokens.get(0).getLiteral();

            match(Token.Type.OPERATOR);

            Ast.Expression exp2 = parsePrimaryExpression();

            if (!peek("*") && !peek("/") && !peek("^")) {
                return new plc.project.Ast.Expression.Binary(operator, exp, exp2);
            }
            else {
                exp = new plc.project.Ast.Expression.Binary(operator, exp, exp2);
            }
        }

        return exp;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {

        if (match("NIL")) {
            return new Ast.Expression.Literal(null);//Null
        }

        else if (match("TRUE")) {
            return new Ast.Expression.Literal(new Boolean(true));//Boolean
        }

        else if (match("FALSE")) {
            return new Ast.Expression.Literal(new Boolean(false));//Boolean
        }

        else if (match(Token.Type.INTEGER)) {//We have token made

            return new Ast.Expression.Literal(new BigInteger(tokens.get(-1).getLiteral()));

        }

        else if (match(Token.Type.DECIMAL)) {
            return new Ast.Expression.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
        }

        else if (match(Token.Type.CHARACTER)) {
            String chars = tokens.get(-1).getLiteral();
            //Get rid of Double quotes
            chars = chars.substring(1,chars.length()-1);
            //Replace of escape characters
            chars = chars.replace("\\b","\b");
            chars = chars.replace("\\n","\n");
            chars = chars.replace("\\r","\r");
            chars = chars.replace("\\t","\t");
            chars = chars.replace("\\'","\'");
            chars = chars.replace("\\\"","\"");
            chars = chars.replace("\\\\","\\");
            //Character chr=chars.charAt(0);//Transform String to Char
            return new Ast.Expression.Literal(new Character(chars.charAt(0)));
        }

        else if (match(Token.Type.STRING)) {
            String str = tokens.get(-1).getLiteral();
            //Get rid of Double quotes
            str = str.substring(1,str.length()-1);//Remove first  and last
            //Replace of escape characters
            str = str.replace("\\b","\b");
            str = str.replace("\\n","\n");
            str = str.replace("\\r","\r");
            str = str.replace("\\t","\t");
            str = str.replace("\\'","\'");
            str = str.replace("\\\"","\"");
            str = str.replace("\\\\","\\");
            return new Ast.Expression.Literal(str);
        }

        else if (match("(")) {

            Ast.Expression exp = parseExpression();
            if (!match(")")) {
                throw new ParseException("Expected closing parenthesis.", -1);
            }
            return new Ast.Expression.Group(exp);

        }

        else if (match(Token.Type.IDENTIFIER)) {    //Incomplete

            String ide = tokens.get(-1).getLiteral();
            if (match("(")) {

                boolean isTrail = false;
                List<Ast.Expression> expList = new ArrayList<Ast.Expression>();
                while (!match(")")) {

                    expList.add(parseExpression());
                    isTrail = false;

                    if (!match(",")) {

                        match(")");
                        return new Ast.Expression.Function(ide, expList);
                    }
                    else {
                        isTrail = true;
                    }
                    match(",");
                }
                if (isTrail) {
                    throw new ParseException("Trailing comma", -1);
                }
                match(")");
                return new Ast.Expression.Function(ide,expList);

            }
            else if (match("[")) {

                Ast.Expression exp = parseExpression();
                if (!match("]")) {
                    throw new ParseException("Expected closing brackets", -1);
                }
                return new Ast.Expression.Access(Optional.of(exp),ide);
            }
            else {

                String name = tokens.get(-1).getLiteral();
                return new Ast.Expression.Access(Optional.empty(),ide);
            }

        }
        throw new ParseException("Invalid expression", -1);//Change message

    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++){
            if (!tokens.has(i)) {
                return false;
            }
            else if(patterns[i] instanceof Token.Type){
                if(patterns[i] != tokens.get(i).getType()){
                    return false;
                }
            }
            else if (patterns[i] instanceof String){
                if(!patterns[i].equals(tokens.get(i).getLiteral())){
                    return false;
                }
            }
            else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {

        boolean peek = peek(patterns);

        if (peek) {
            for (int i = 0; i < patterns.length; i++){
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
