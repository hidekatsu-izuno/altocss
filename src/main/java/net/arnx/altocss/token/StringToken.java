package net.arnx.altocss.token;

public class StringToken extends Token {
    public StringToken(String text, int startLine, int startColumn, int endLine, int endColumn) {
        super(text, startLine, startColumn, endLine, endColumn);
    }
}
