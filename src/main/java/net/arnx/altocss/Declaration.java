package net.arnx.altocss;

import java.io.IOException;
import java.util.Map;

import net.arnx.altocss.util.JsonWriter;

public class Declaration extends Node {
	public Declaration() {
		super(false);
	}

	private String prop;

	public Declaration prop(String prop) {
		this.prop = prop;
		return this;
	}

	public String prop() {
		return prop;
	}

	private String value;

	public Declaration value(String value) {
		this.value = value;
		return this;
	}

	public String value() {
		return value;
	}

	private boolean important;

	public Declaration important(boolean important) {
		this.important = important;
		return this;
	}

	public boolean important() {
		return important;
	}

	@Override
	public Declaration source(Source source) {
		return (Declaration)super.source(source);
	}

	@Override
	public DeclarationRaws raws() {
		return (DeclarationRaws)super.raws();
	}

	@Override
	DeclarationRaws createRaws() {
		return new DeclarationRaws();
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("type").value("decl");
			writer.name("prop").value(prop);
			writer.name("value").value(value);
			if (important) {
				writer.name("important").value(important);
			}
			writer.name("raws").beginObject();
			for (Map.Entry<String, Object> entry : raws().entrySet()) {
				writer.name(entry.getKey()).value(entry.getValue());
			}
			writer.endObject();
			Source source = source();
			if (source != null) {
				writer.name("source");
				source.jsonize(writer);
			}
		}
		writer.endObject();
	}

	public static class DeclarationRaws extends NodeRaws {
		DeclarationRaws() {
		}

		public void between(String between) {
			put("between", between);
		}

		public String between() {
			Object between = get("between");
			if (between instanceof String) {
				return (String)between;
			}
			return null;
		}

		public void important(String important) {
			put("important", important);
		}

		public String important() {
			Object important = get("important");
			if (important instanceof String) {
				return (String)important;
			}
			return null;
		}

		public void value(String value) {
			put("value", value);
		}

		public String value() {
			Object value = get("value");
			if (value instanceof String) {
				return (String)value;
			}
			return null;
		}

		public void _value(String value) {
			put("_value", value);
		}

		public String _value() {
			Object value = get("_value");
			if (value instanceof String) {
				return (String)value;
			}
			return null;
		}
	}
}
