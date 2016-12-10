package net.arnx.altocss;

import java.io.IOException;
import java.util.Map;

import net.arnx.altocss.nodes.RootNode;

public interface Plugin {
	public default void init(Environment env, Map<Option<?>, Object> options) {

	}

	public default Parser parser(String file) throws IOException {
		return null;
	}

	public default void process(RootNode root) {
	}

	public default void validate(RootNode root) throws SyntaxException {
	}

	public default void minify(RootNode root) {
	}

	public default Stringifier stringifier(String file) throws IOException {
		return null;
	}
}
