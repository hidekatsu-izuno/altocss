package net.arnx.altocss.token;

public class SemiColonToken extends Token {
    public SemiColonToken(String text, int line, int column) {
        super("semicolon", text, line, column, line, column);
    }
}
