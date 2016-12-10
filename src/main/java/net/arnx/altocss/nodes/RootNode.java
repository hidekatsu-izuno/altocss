package net.arnx.altocss.nodes;

import java.io.IOException;
import java.util.Map;

import net.arnx.altocss.Source;
import net.arnx.altocss.util.JsonWriter;

public class RootNode extends Node {
	public RootNode() {
		super(true);
	}

	@Override
	public RootNode source(Source source) {
		return (RootNode)super.source(source);
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
