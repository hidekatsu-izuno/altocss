package net.arnx.altocss;

import java.io.IOException;
import java.util.Map;

public interface Plugin {
	public default void init(Environment env, Map<Option<?>, Object> options) {

	}

	public default Parser parser(String file) throws IOException {
		return null;
	}

	public default void process(Root root) {
	}

	public default void validate(Root root) throws SyntaxException {
	}

	public default void minify(Root root) {
	}

	public default Stringifier stringifier(String file) throws IOException {
		return null;
	}
}
