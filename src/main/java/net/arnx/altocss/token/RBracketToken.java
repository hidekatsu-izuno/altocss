package net.arnx.altocss.token;

public class RBracketToken extends Token {
    public RBracketToken(String text, int line, int column) {
        super("rbracket", text, line, column, line, column);
    }
}
