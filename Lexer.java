package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are * helpers you need to use, they will make the implementation a lot easier. */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {

        List<Token> listToken = new ArrayList<>();
        while(chars.has(0)) {

            if (match("[ \b\n\r\t]")) {
                chars.skip();
            }
            else {
                listToken.add(lexToken());
            }
        }
        return listToken;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {

        if (match("[A-Za-z]") || match("@","[A-Za-z]")) {   //Identifier
            return lexIdentifier();
        }
        //@ Exception

        else if (match("-","[1-9]")|| match("[1-9]")|| (match("0") && peek("[^0-9]"))) {   //Number
            return lexNumber();
        }

        else if (match("[']")) {   //Character
            return  lexCharacter();
        }

        else if (match("\"")) {   //String
            return lexString();
        }

        else {   //Operator
            return lexOperator();
        }

        // lexToken() will check the first character
        // lexToken() will call lexIdentifier, lexNumber, lexCharacter, etc. based on whats needed
    }

    public Token lexIdentifier() {

        while (peek("[A-Za-z0-9_-]")) {
            match("[A-Za-z0-9_-]");
        }

        return chars.emit(Token.Type.valueOf("IDENTIFIER"));
    }

    public Token lexNumber() {

        while(match("[0-9]")) {
        }
        if (match("\\.","[0-9]") ) {
            while(match("[0-9]")) {
            }
            return chars.emit(Token.Type.valueOf("DECIMAL"));
        }
        return chars.emit(Token.Type.valueOf("INTEGER"));

    }

    public Token lexCharacter() {

        if (!match("[^'\\n\\r\\\\]") && !peek("\\\\")) {
            throw new ParseException("Invalid Character", chars.index);
        }

        if (match("\\\\"))
            lexEscape();

        if (peek("[']")) {
            match("[']");
        }
        else {
            throw new ParseException("Missing Quote", chars.index);
        }
        return chars.emit(Token.Type.valueOf("CHARACTER"));
    }

    public Token lexString() {

        while (match("([^\"\\n\\r\\\\])") || peek("\\\\")) {

            if (match("\\\\")) {
                lexEscape();
            }
        }

        if (!match("\"")) {
            throw new ParseException("Missing Quote", chars.index);
        }
        return chars.emit(Token.Type.valueOf("STRING"));
    }

    public void lexEscape() {

        if (!match("[bnrt'\"\\\\]"))
            throw new ParseException("Invalid Escape Sequence", chars.index);

    }

    public Token lexOperator() {

        if (peek("[!=]","=")) {
            match("[!=]","=");
        }
        else if (peek("[!=]")) {
            match("[!=]");
        }
        else if (peek("&&")) {
            match("&&");
        }
        else if (peek("||")) {
            match("||");
        }
        else {
            match("[^\b\n\r\t]");
        }

        return chars.emit(Token.Type.valueOf("OPERATOR"));
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {

        for (int i = 0; i < patterns.length; i++) {

            if (!chars.has(i) ||
                !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek=peek(patterns);
        if (peek){
            for(int i=0; i<patterns.length; i++){
                chars.advance();
            }
        }
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }
        //abc123
}
