package net.arnx.altocss.token;

public class LBracketToken extends Token {
    public LBracketToken(String text, int line, int column) {
        super("lbracket", text, line, column, line, column);
    }
}
