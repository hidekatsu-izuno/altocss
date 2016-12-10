package net.arnx.altocss.nodes;

import java.io.IOException;
import java.util.Map;

import net.arnx.altocss.Source;
import net.arnx.altocss.util.JsonWriter;

public class CommentNode extends Node {
	public CommentNode() {
		super(false);
	}

	private String text;

	public CommentNode text(String text) {
		this.text = text;
		return this;
	}

	public String text() {
		return text;
	}

	@Override
	public CommentNode source(Source source) {
		return (CommentNode)super.source(source);
	}

	@Override
	public CommentRaws raws() {
		return (CommentRaws)super.raws();
	}

	@Override
	CommentRaws createRaws() {
		return new CommentRaws();
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("type").value("comment");
			writer.name("text").value(text);
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

	public static class CommentRaws extends NodeRaws {
		CommentRaws() {
		}

		public void left(String left) {
			put("left", left);
		}

		public String left() {
			Object left = get("left");
			if (left instanceof String) {
				return (String)left;
			}
			return null;
		}

		public void right(String right) {
			put("right", right);
		}

		public String right() {
			Object right = get("right");
			if (right instanceof String) {
				return (String)right;
			}
			return null;
		}
	}
}
