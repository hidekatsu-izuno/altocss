package net.arnx.altocss.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SourceMapBuilder {
	private String file;
	private String sourcesRoot;
	private Map<String, String> sources = new LinkedHashMap<>();
	private List<Mapping> mappings = new ArrayList<>();

	public void file(String file) {
		this.file = file;
	}

	public String file() {
		return file;
	}

	public void sourcesRoot(String sourcesRoot) {
		this.sourcesRoot = sourcesRoot;
	}

	public String sourcesRoot() {
		return sourcesRoot;
	}

	public void addSource(String file, String content) {
		sources.putIfAbsent(file, content);
	}

	public void addMapping(String file, int srcLine, int srcColumn, int destLine, int destColumn) {
		Mapping mapping = new Mapping();
		mapping.file = file;
		mapping.srcLine = srcLine;
		mapping.srcColumn = srcColumn;
		mapping.destLine = destLine;
		mapping.destColumn = destColumn;
		mappings.add(mapping);
	}

	public void stringify(Appendable out) throws IOException {
		Map<String, Integer> sourcesIndex = new HashMap<>();
		int index = 0;
		for (Map.Entry<String, String> entry : sources.entrySet()) {
			sourcesIndex.put(entry.getKey(), index);
		}

		JsonWriter writer = new JsonWriter(out);
		writer.beginObject();
		{
			writer.name("version").value(3);

			if (file != null) {
				writer.name("file").value(file);
			}

			if (sourcesRoot != null) {
				writer.name("sourcesRoot").value(sourcesRoot);
			}

			boolean hasSourcesContent = false;
			writer.name("sources").beginArray();
			for (Map.Entry<String, String> entry : sources.entrySet()) {
				writer.value(entry.getKey());
				if (entry.getValue() != null) {
					hasSourcesContent = true;
				}
			}
			writer.endArray();

			if (hasSourcesContent) {
				writer.name("sourcesContent").beginArray();
				for (Map.Entry<String, String> entry : sources.entrySet()) {
					writer.value(entry.getValue());
				}
				writer.endArray();
			}

			writer.name("names").beginArray();
			writer.endArray();

			StringBuilder sb = new StringBuilder(mappings.size() * 6);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			int lastDestLine = 1;
			int lastDestColumn = 0;
			int lastSrcLine = 1;
			int lastSrcColumn = 0;
			for (Mapping mapping : mappings) {
				if (mapping.destLine > lastDestLine) {
					for (int i = 0; i < mapping.destLine - lastDestLine; i++) {
						sb.append(';');
					}
					lastDestColumn = 0;
				} else if (lastDestColumn > 0) {
					sb.append(',');
				}

				int destColumn = mapping.destColumn - lastDestColumn;
				int sourceId = sourcesIndex.getOrDefault(mapping.file, 0);
				int srcLine = mapping.srcLine - lastSrcLine;
				int srcColumn = mapping.srcColumn - lastSrcColumn;

				bout.reset();
				encodeVLQ(bout, destColumn);
				encodeVLQ(bout, sourceId);
				encodeVLQ(bout, srcLine);
				encodeVLQ(bout, srcColumn);
				sb.append(Base64.getEncoder().encodeToString(bout.toByteArray()));

				lastDestLine = mapping.destLine;
				lastDestColumn = mapping.destColumn;
				lastSrcLine = mapping.srcLine;
				lastSrcColumn = mapping.srcColumn;
			}
			writer.name("mappings").value(sb.toString());
		}
		writer.endObject();
	}

	private static void encodeVLQ(ByteArrayOutputStream bout, int value) {
		int sign = value >= 0 ? 0 : 1;
		int rest = Math.abs(value);

		int data = (rest << 1) & 0b11110 | sign;
		rest >>>= 4;
		bout.write((rest > 0) ? 0b100000 | data : data);

		while (rest > 0) {
			data = rest & 0b11111;
			rest >>>= 5;
			bout.write((rest > 0) ? 0b100000 | data : data);
		}
	}

	private static class Mapping {
		String file;
		int srcLine;
		int srcColumn;
		int destLine;
		int destColumn;
	}
}
