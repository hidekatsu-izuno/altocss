package net.arnx.altocss.token;

public class LBraceToken extends Token {
    public LBraceToken(String text, int line, int column) {
        super("lbrace", text, line, column, line, column);
    }
}
