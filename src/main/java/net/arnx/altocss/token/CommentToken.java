package net.arnx.altocss.token;

public class CommentToken extends Token {
    public CommentToken(String text, int startLine, int startColumn, int endLine, int endColumn) {
        super(text, startLine, startColumn, endLine, endColumn);
    }

    @Override
    protected String type() {
        return "comment";
    }
}
