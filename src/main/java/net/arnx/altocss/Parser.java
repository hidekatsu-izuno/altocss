package net.arnx.altocss;

import net.arnx.altocss.nodes.RootNode;

public interface Parser {
	public RootNode parse(String file, String css);
}
