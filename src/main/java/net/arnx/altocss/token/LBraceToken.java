package net.arnx.altocss.token;

public class LBraceToken extends Token {
    public LBraceToken(String text, int line, int column) {
        super(text, line, column, line, column);
    }

    @Override
    protected String type() {
        return "lbrace";
    }
}
