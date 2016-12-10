package net.arnx.altocss.node;

import java.io.IOException;
import java.util.Map;

import net.arnx.altocss.Source;
import net.arnx.altocss.util.JsonWriter;

public class RuleNode extends Node {
	public RuleNode() {
		super(true);
	}

	private String selector;

	public RuleNode selector(String value) {
		this.selector = value;
		return this;
	}

	public String selector() {
		return selector;
	}

	@Override
	public RuleNode source(Source source) {
		return (RuleNode)super.source(source);
	}

	@Override
	public RuleRaws raws() {
		return (RuleRaws)super.raws();
	}

	@Override
	RuleRaws createRaws() {
		return new RuleRaws();
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("type").value("rule");
			writer.name("selector").value(selector);
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
			if (hasBody()) {
				writer.name("nodes").beginArray();
				for (Node node : this) {
					node.jsonize(writer);
				}
				writer.endArray();
			}
		}
		writer.endObject();
	}

	public static class RuleRaws extends NodeRaws {
		RuleRaws() {
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

		public void selector(String selector) {
			put("selector", selector);
		}

		public String selector() {
			Object selector = get("selector");
			if (selector instanceof String) {
				return (String)selector;
			}
			return null;
		}

		public void _selector(String selector) {
			put("_selector", selector);
		}

		public String _selector() {
			Object selector = get("_selector");
			if (selector instanceof String) {
				return (String)selector;
			}
			return null;
		}
	}
}
