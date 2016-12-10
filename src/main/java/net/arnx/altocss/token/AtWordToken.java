package net.arnx.altocss.token;

public class AtWordToken extends Token {
    public AtWordToken(String text, int startLine, int startColumn, int endLine, int endColumn) {
        super("atword", text, startLine, startColumn, endLine, endColumn);
    }
}
