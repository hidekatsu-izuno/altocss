package net.arnx.altocss.token;

public class ColonToken extends Token {
    public ColonToken(String text, int line, int column) {
        super("colon", text, line, column, line, column);
    }
}
