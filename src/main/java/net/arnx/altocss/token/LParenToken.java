package net.arnx.altocss.token;

public class LParenToken extends Token {
    public LParenToken(String text, int line, int column) {
        super(text, line, column, line, column);
    }
}
