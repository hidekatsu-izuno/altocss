package net.arnx.altocss.plugins.postcss;

public class PostCssToken {
	public final PostCssTokenType type;
	public final String text;
	public final int startLine;
	public final int startColumn;
	public final int endLine;
	public final int endColumn;

	public PostCssToken(PostCssTokenType type, String text, int line, int column) {
		this(type, text, line, column, line, column);
	}

	public PostCssToken(PostCssTokenType type, String text, int startLine, int startColumn, int endLine, int endColumn) {
		this.type = type;
		this.text = text;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		PostCssToken other = (PostCssToken) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
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
		return "Token [type=" + type
				+ ", text=" + text
				+ ", startLine=" + startLine
				+ ", startColumn=" + startColumn
				+ ", endLine=" + endLine
				+ ", endColumn=" + endColumn
				+ "]";
	}



}
