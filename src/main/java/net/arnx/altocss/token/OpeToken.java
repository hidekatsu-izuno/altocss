package net.arnx.altocss.token;

public class OpeToken extends Token {
    public OpeToken(String text, int startLine, int startColumn, int endLine, int endColumn) {
        super(text, startLine, startColumn, endLine, endColumn);
    }

    @Override
    protected String type() {
        return "ope";
    }
}
