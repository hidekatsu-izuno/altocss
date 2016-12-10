package net.arnx.altocss.token;

public class RBraceToken extends Token {
    public RBraceToken(String text, int line, int column) {
        super("rbrace", text, line, column, line, column);
    }
}
