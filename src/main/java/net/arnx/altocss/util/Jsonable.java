package net.arnx.altocss.util;

import java.io.IOException;

public interface Jsonable {
	void jsonize(JsonWriter writer) throws IOException;
}
