package net.arnx.altocss.token;

public class RParenToken extends Token {
    public RParenToken(String text, int line, int column) {
        super(text, line, column, line, column);
    }

    @Override
    protected String type() {
        return "rparen";
    }
}
