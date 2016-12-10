package net.arnx.altocss;

import java.io.IOException;

import net.arnx.altocss.util.JsonWriter;
import net.arnx.altocss.util.Jsonable;

public class Source implements Jsonable {
	private Input input;
	private Position start;
	private Position end;

	public Source() {
	}

	public Source(Input input, Position start, Position end) {
		this.input = input;
		this.start = start;
		this.end = end;
	}

	public Input input() {
		return input;
	}

	public void start(Position start) {
		this.start = start;
	}

	public Position start() {
		return start;
	}

	public void end(Position end) {
		this.end = end;
	}

	public Position end() {
		return end;
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			if (input != null) {
				writer.name("input");
				input().jsonize(writer);
			}
			if (start != null) {
				writer.name("start");
				start().jsonize(writer);
			}
			if (end != null) {
				writer.name("end");
				end().jsonize(writer);
			}
		}
		writer.endObject();
	}
}
