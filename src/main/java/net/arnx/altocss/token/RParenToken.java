package net.arnx.altocss.token;

public class RParenToken extends Token {
    public RParenToken(String text, int line, int column) {
        super("rparen", text, line, column, line, column);
    }
}
