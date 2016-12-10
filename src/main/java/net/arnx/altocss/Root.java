package net.arnx.altocss;

import java.io.IOException;
import java.util.Map;

import net.arnx.altocss.util.JsonWriter;

public class Root extends Node {
	public Root() {
		super(true);
	}

	@Override
	public Root source(Source source) {
		return (Root)super.source(source);
	}

	@Override
	public RootRaws raws() {
		return (RootRaws)super.raws();
	}

	@Override
	RootRaws createRaws() {
		return new RootRaws();
	}

	@Override
	public void jsonize(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("type").value("root");
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

	public static class RootRaws extends NodeRaws {
		RootRaws() {
		}
	}
}
