package net.arnx.altocss;

import java.io.IOException;

import net.arnx.altocss.util.JsonWriter;
import net.arnx.altocss.util.Jsonable;

public class Input implements Jsonable {
	private String file;
	private String css;

	public Input() {
	}

	public Input(String file, String css) {
		this.file = file;
		this.css = css;
	}

	public void file(String file) {
		this.file = file;
	}

	public String file() {
		return file;
	}

	public void css(String css) {
		this.css = css;
	}

	public String css() {
		return css;
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("file").value(file);
		}
		writer.endObject();
	}
}
