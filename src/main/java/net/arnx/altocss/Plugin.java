package net.arnx.altocss;

import net.arnx.altocss.node.RootNode;

public interface Plugin {
	public default void init(PluginContext context) {

	}

	public default Parser parser(String file) {
		return null;
	}

	public default void process(RootNode root) {
	}

	public default void validate(RootNode root) throws SyntaxException {
	}

	public default void minify(RootNode root) {
	}

	public default Stringifier stringifier(String file) {
		return null;
	}
}
