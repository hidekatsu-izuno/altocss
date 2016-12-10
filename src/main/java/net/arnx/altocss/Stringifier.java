package net.arnx.altocss;

import java.io.IOException;

import net.arnx.altocss.nodes.Node;
import net.arnx.altocss.util.SourceMapBuilder;

public interface Stringifier {
	public void stringify(Node node, Appendable out, SourceMapBuilder builder) throws IOException;
}
