package net.arnx.altocss.tokens;

public class SemiColonToken extends Token {
    public SemiColonToken(String text, int line, int column) {
        super(text, line, column, line, column);
    }
}
