package net.arnx.altocss.token;

public class RBracketToken extends Token {
    public RBracketToken(String text, int line, int column) {
        super(text, line, column, line, column);
    }

    @Override
    protected String type() {
        return "rbracket";
    }
}
