package net.arnx.altocss.util;

import java.io.IOException;
import java.io.UncheckedIOException;

public interface Jsonable {
	void jsonize(JsonWriter writer) throws IOException;

	public static String stringify(Object o) {
	    StringBuilder sb = new StringBuilder();
	    JsonWriter writer = new JsonWriter(sb);
	    try {
            writer.value(o);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
	    return sb.toString();
	}
}
