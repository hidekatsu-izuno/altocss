package net.arnx.altocss.node;

import java.io.IOException;
import java.util.Map;

import net.arnx.altocss.Source;
import net.arnx.altocss.util.JsonWriter;

public class AtRuleNode extends Node {
	public AtRuleNode(boolean hasBody) {
		super(hasBody);
	}

	private String name;

	public AtRuleNode name(String name) {
		this.name = name;
		return this;
	}

	public String name() {
		return name;
	}

	private String params;

	public AtRuleNode params(String params) {
		this.params = params;
		return this;
	}

	public String params() {
		return params;
	}

	@Override
	public AtRuleNode source(Source source) {
		return (AtRuleNode)super.source(source);
	}

	@Override
	public AtRuleRaws raws() {
		return (AtRuleRaws)super.raws();
	}

	@Override
	AtRuleRaws createRaws() {
		return new AtRuleRaws();
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("type").value("atrule");
			writer.name("name").value(name);
			writer.name("params").value(params);
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

	public static class AtRuleRaws extends NodeRaws {
		AtRuleRaws() {
		}

		public void afterName(String afterName) {
			put("afterName", afterName);
		}

		public String afterName() {
			Object afterName = get("afterName");
			if (afterName instanceof String) {
				return (String)afterName;
			}
			return null;
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

		public void params(String params) {
			put("params", params);
		}

		public String params() {
			Object params = get("params");
			if (params instanceof String) {
				return (String)params;
			}
			return null;
		}

		public void _params(String params) {
			put("_params", params);
		}

		public String _params() {
			Object params = get("_params");
			if (params instanceof String) {
				return (String)params;
			}
			return null;
		}
	}
}
