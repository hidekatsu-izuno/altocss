package net.arnx.altocss.token;

public class SemicolonToken extends Token {
    public SemicolonToken(String text, int line, int column) {
        super(text, line, column, line, column);
    }

    @Override
    protected String type() {
        return "semicolon";
    }
}
