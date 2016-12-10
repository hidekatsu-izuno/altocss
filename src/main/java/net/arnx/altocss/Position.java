package net.arnx.altocss;

import java.io.IOException;

import net.arnx.altocss.util.JsonWriter;
import net.arnx.altocss.util.Jsonable;

public class Position implements Jsonable {
	private int line;
	private int column;

	public Position() {
	}

	public Position(int line, int column) {
		this.line = line;
		this.column = column;
	}

	public Position line(int line) {
		this.line = line;
		return this;
	}

	public int line() {
		return line;
	}

	public Position column(int column) {
		this.column = column;
		return this;
	}

	public int column() {
		return column;
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("line").value(line > 0 ? line : 1);
			writer.name("column").value(column > 0 ? column : 1);
		}
		writer.endObject();
	}
}
