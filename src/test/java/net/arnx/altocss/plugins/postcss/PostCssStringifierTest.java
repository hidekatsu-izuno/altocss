package net.arnx.altocss.plugins.postcss;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.Test;

import net.arnx.altocss.Root;
import net.arnx.altocss.plugins.postcss.PostCssParser;
import net.arnx.altocss.plugins.postcss.PostCssStringifier;
import net.arnx.altocss.util.SourceMapBuilder;

public class PostCssStringifierTest {
	@Test
	public void testIndentsByDefault() {
		String css = "@page {\n" +
                "    a {\n" +
                "        color: black\n" +
                "    }\n" +
                "}";
		assertEquals(css, stringify(css));
	}

	private String stringify(String css) {
		StringBuilder sb = new StringBuilder();
		PostCssStringifier stringifier = new PostCssStringifier();
		PostCssParser parser = new PostCssParser();
		SourceMapBuilder builder = new SourceMapBuilder();
		try {
			Root root = parser.parse("<empty>", css);
			stringifier.stringify(root, sb, builder);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return sb.toString();
	}
}
