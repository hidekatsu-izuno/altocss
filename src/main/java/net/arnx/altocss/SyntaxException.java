package net.arnx.altocss;

public class SyntaxException extends RuntimeException {
	private final String path;
	private final int line;
	private final int column;
	private final String text;

	public SyntaxException(String path, int line, int column, String text) {
		super((path != null ? path : "") +  ":" + line + ":" + column + ": " + text);
		this.path = path;
		this.line = line;
		this.column = column;
		this.text = text;
	}

	public String getPath() {
		return path;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public String getText() {
		return text;
	}
}
