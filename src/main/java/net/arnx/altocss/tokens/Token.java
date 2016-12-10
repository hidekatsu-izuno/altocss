package net.arnx.altocss.tokens;

public abstract class Token {
	private final String text;
	private final int startLine;
	private final int startColumn;
	private final int endLine;
	private final int endColumn;

	public Token(String text, int startLine, int startColumn, int endLine, int endColumn) {
	    this.text = text;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	public String text() {
	    return text;
	}

	public int startLine() {
	    return startLine;
	}

	public int startColumn() {
	    return startColumn;
	}

	public int endLine() {
	    return endLine;
	}

	public int endColumn() {
	    return endColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getClass().hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + startLine;
		result = prime * result + startColumn;
		result = prime * result + endLine;
		result = prime * result + endColumn;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (getClass() != other.getClass())
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (startLine != other.startLine)
			return false;
		if (startColumn != other.startColumn)
			return false;
		if (endLine != other.endLine)
			return false;
		if (endColumn != other.endColumn)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ " [text=" + text
				+ ", startLine=" + startLine
				+ ", startColumn=" + startColumn
				+ ", endLine=" + endLine
				+ ", endColumn=" + endColumn + "]";
	}
}
