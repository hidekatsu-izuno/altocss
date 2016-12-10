package net.arnx.altocss.token;

public class CommentToken extends Token {
    public CommentToken(String text, int startLine, int startColumn, int endLine, int endColumn) {
        super("comment", text, startLine, startColumn, endLine, endColumn);
    }
}
