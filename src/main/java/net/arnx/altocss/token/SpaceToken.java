package net.arnx.altocss.token;

public class SpaceToken extends Token {
    public SpaceToken(String text, int startLine, int startColumn, int endLine, int endColumn) {
        super(text, startLine, startColumn, endLine, endColumn);
    }

    @Override
    protected String type() {
        return "space";
    }
}
