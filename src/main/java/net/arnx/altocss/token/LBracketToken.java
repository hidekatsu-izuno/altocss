package net.arnx.altocss.token;

public class LBracketToken extends Token {
    public LBracketToken(String text, int line, int column) {
        super(text, line, column, line, column);
    }

    @Override
    protected String type() {
        return "lbracket";
    }
}
