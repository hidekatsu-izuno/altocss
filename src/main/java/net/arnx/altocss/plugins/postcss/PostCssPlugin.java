package net.arnx.altocss.plugins.postcss;

import java.util.regex.Pattern;

import net.arnx.altocss.Parser;
import net.arnx.altocss.Plugin;
import net.arnx.altocss.Stringifier;

public class PostCssPlugin implements Plugin {
	private static final Pattern CSS_PATTERN = Pattern.compile("\\.css$", Pattern.CASE_INSENSITIVE);

	@Override
	public Parser parser(String file) {
		if (CSS_PATTERN.matcher(file).find()) {
			return new PostCssParser();
		}
		return null;
	}

	@Override
	public Stringifier stringifier(String file) {
		if (CSS_PATTERN.matcher(file).find()) {
			return new PostCssStringifier();
		}
		return null;
	}
}
