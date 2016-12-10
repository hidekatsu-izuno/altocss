package net.arnx.altocss.token;

public class WordToken extends Token {
    public WordToken(String text, int startLine, int startColumn, int endLine, int endColumn) {
        super(text, startLine, startColumn, endLine, endColumn);
    }
}
