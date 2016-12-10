package net.arnx.altocss.plugins.postcss;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.arnx.altocss.AtRule;
import net.arnx.altocss.Comment;
import net.arnx.altocss.Declaration;
import net.arnx.altocss.Input;
import net.arnx.altocss.Node;
import net.arnx.altocss.Parser;
import net.arnx.altocss.Position;
import net.arnx.altocss.Root;
import net.arnx.altocss.Rule;
import net.arnx.altocss.Source;
import net.arnx.altocss.SyntaxException;
import net.arnx.altocss.util.SliceList;

public class PostCssParser implements Parser {
	@Override
	public Root parse(String file, String css) {
		try {
			Context context = new Context(file, css);

			PostCssTokenizer tokenizder = new PostCssTokenizer();
			context.tokens = tokenizder.tokenize(file, css);

			Root root = new Root();
			root.source(new Source(context.getInput(), new Position(1, 1), null));

			context.current = root;
			while (context.pos < context.tokens.size()) {
				PostCssToken token = context.tokens.get(context.pos);
				switch (token.type) {
				case SPACE:
				case SEMICOLON:
					context.spaces.append(token.text);
					break;
				case RBRACE:
					end(context, token);
					break;
				case COMMENT:
					comment(context, token);
					break;
				case AT_WORD:
					atrule(context, token);
					break;
				case LBRACE:
					emptyRule(context, token);
					break;
				default:
					other(context);
				}
				context.pos++;
			}
			endFile(context);
			return root;
		} catch (SyntaxException e) {
			String path = e.getPath();
			if (path != null) {
				if (path.endsWith(".scss")) {
					throw new SyntaxException(e.getPath(), e.getLine(), e.getColumn(),
							e.getText() + "\nYou tried to parse SCSS with the standard CSS parser; try again with the postcss-scss parser");
				} else if (path.endsWith(".less")) {
					throw new SyntaxException(e.getPath(), e.getLine(), e.getColumn(),
							e.getText() + "\nYou tried to parse Less with the standard CSS parser; try again with the postcss-less parser");
				}
			}
			throw e;
		}
	}

	protected void comment(Context context, PostCssToken token) {
		Comment node = new Comment();
		this.init(context, node, token.startLine, token.startColumn);
		node.source().end(new Position(token.endLine, token.endColumn));

		int start = -1;
		for (int i = 2; i < token.text.length() - 2; i++) {
			char c = token.text.charAt(i);
			if (!(c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f')) {
				start = i;
				break;
			}
		}
		if (start == -1) {
			node.text("");
			node.raws().left(token.text.substring(2, token.text.length() - 2));
			node.raws().right("");
		} else {
			int end = start + 1;
			for (int i = token.text.length() - 2 - 1; i > start; i--) {
				char c = token.text.charAt(i);
				if (!(c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f')) {
					end = i + 1;
					break;
				}
			}
			node.text(token.text.substring(start, end));
			node.raws().left(token.text.substring(2, start));
			node.raws().right(token.text.substring(end, token.text.length() - 2));
		}
	}

	protected void emptyRule(Context context, PostCssToken token) {
		Rule node = new Rule();
		this.init(context, node, token.startLine, token.startColumn);
		node.selector("");
		node.raws().between("");
		context.current = node;
	}

	protected void other(Context context) {
		boolean end = false;
		boolean colon = false;
		PostCssToken bracket = null;
		List<PostCssTokenType> brackets = new ArrayList<>();

		int start = context.pos;
		while (context.pos < context.tokens.size()) {
			PostCssToken token = context.tokens.get(context.pos);
			if (token.type == PostCssTokenType.LPAREN || token.type == PostCssTokenType.LBRACKET) {
				if (bracket == null) {
					bracket = token;
				}
				brackets.add(token.type == PostCssTokenType.LPAREN ? PostCssTokenType.RPAREN : PostCssTokenType.RBRACKET);
			} else if (brackets.isEmpty()) {
				if (token.type == PostCssTokenType.SEMICOLON) {
					if (colon) {
						decl(context, new SliceList<>(context.tokens, start, context.pos + 1));
						return;
					} else {
						break;
					}
				} else if (token.type == PostCssTokenType.LBRACE) {
					rule(context, new SliceList<>(context.tokens, start, context.pos + 1));
					return;
				} else if (token.type == PostCssTokenType.RBRACE) {
					context.pos --;
					end = true;
					break;
				} else if (token.type == PostCssTokenType.COLON) {
					colon = true;
				}
			} else if (token.type == brackets.get(brackets.size() - 1)) {
				brackets.remove(brackets.size() - 1);
				if (brackets.isEmpty()) {
					bracket = null;
				}
			}

			context.pos++;
		}
		if (context.pos == context.tokens.size()) {
			context.pos--;
			end = true;
		}

		if (!brackets.isEmpty()) {
			unclosedBracket(context, bracket);
		}

		if (end && colon) {
			while (context.pos > start) {
				PostCssToken token = context.tokens.get(context.pos);
				if (token.type != PostCssTokenType.SPACE && token.type != PostCssTokenType.COMMENT) {
					break;
				}
				context.pos--;
			}
			decl(context, new SliceList<>(context.tokens, start, context.pos + 1));
			return;
		}

		unknownWord(context, start);
	}

	private void rule(Context context, SliceList<PostCssToken> tokens) {
		tokens.pop();

		Rule node = new Rule();
		PostCssToken token = tokens.get(0);
		init(context, node, token.startLine, token.startColumn);

		node.raws().between(spacesFromEnd(context.getCachedBuilder(), tokens).toString());
		node.selector(raw(context, node, "selector", tokens));
		context.current = node;
	}

	private void decl(Context context, SliceList<PostCssToken> tokens) {
		Declaration node = new Declaration();
		init(context, node, -1, -1);

		PostCssToken last = tokens.get(tokens.size() - 1);
		if (last.type == PostCssTokenType.SEMICOLON) {
			context.semicolon = true;
			tokens.pop();
		}
		node.source().end(new Position(last.endLine, last.endColumn));

		StringBuilder sb = context.getCachedBuilder();
		String before = node.raws().before();
		if (before != null) {
			sb.append(before);
		}
		while (!tokens.isEmpty()) {
			PostCssToken token = tokens.get(0);
			if (token.type == PostCssTokenType.WORD) {
				break;
			}
			sb.append(tokens.shift().text);
		}
		node.raws().before(sb.toString());

		PostCssToken first = tokens.get(0);
		node.source().start(new Position(first.startLine, first.startColumn));

		sb = context.getCachedBuilder();
		while (!tokens.isEmpty()) {
			PostCssToken token = tokens.get(0);
			if (token.type == PostCssTokenType.COLON || token.type == PostCssTokenType.SPACE || token.type == PostCssTokenType.COMMENT) {
				break;
			}
			sb.append(tokens.shift().text);
		}
		node.prop(sb.toString());

		sb = context.getCachedBuilder();
		while (!tokens.isEmpty()) {
			PostCssToken token = tokens.shift();

			if (token.type == PostCssTokenType.COLON) {
				sb.append(token.text);
				break;
			} else {
				sb.append(token.text);
			}
		}
		node.raws().between(sb.toString());

		char c = node.prop().charAt(0);
		if (c == '_' || c == '*') {
			sb = context.getCachedBuilder();
			before = node.raws().before();
			if (before != null) {
				sb.append(before);
			}
			sb.append(c);
			node.raws().before(sb.toString());
			node.prop(node.prop().substring(1));
		}
		sb = context.getCachedBuilder();
		String between = node.raws().between();
		if (between != null) {
			sb.append(between);
		}
		spacesFromStart(sb, tokens);
		node.raws().between(sb.toString());
		precheckMissedSemicolon(context, tokens);

		for (int i = tokens.size() - 1; i >= 0; i--) {
			PostCssToken token = tokens.get(i);
			if (Objects.equals(token.text, "!important")) {
				node.important(true);
				String string = stringFrom(context.getCachedBuilder(), tokens, i);
				string = spacesFromEnd(context.getCachedBuilder(), tokens).append(string).toString();
				if (!Objects.equals(string, " !important")) {
					node.raws().important(string);
				}
				break;
			} else if (Objects.equals(token.text, "important")) {
				boolean exclamation = false;
				int j;
				for (j = i - 1; j > 0; j--) {
					token = tokens.get(j);
					if (!exclamation && token.type == PostCssTokenType.WORD && Objects.equals(token.text, "!")) {
						exclamation = true;
					} else if (token.type != PostCssTokenType.SPACE && token.type != PostCssTokenType.COMMENT) {
						break;
					}
				}
				if (exclamation) {
					node.important(true);
					node.raws().important(stringFrom(context.getCachedBuilder(), tokens, j + 1));
					i = j;
				}
			}

			if (token.type != PostCssTokenType.SPACE && token.type != PostCssTokenType.COMMENT) {
				break;
			}
		}

		node.value(raw(context, node, "value", tokens));

		if (node.value().indexOf(':') != -1) {
			checkMissedSemicolon(context, tokens);
		}
	}

	protected void atrule(Context context, PostCssToken token) {
		List<PostCssToken> params = new ArrayList<>();
		boolean open = false;
		PostCssToken lastToken = null;
		int pos = context.pos + 1;
		while (pos < context.tokens.size()) {
			PostCssToken ptoken = context.tokens.get(pos);
			if (ptoken.type == PostCssTokenType.SEMICOLON || ptoken.type == PostCssTokenType.RBRACE) {
				lastToken = ptoken;
				break;
			} else if (ptoken.type == PostCssTokenType.LBRACE) {
				lastToken = ptoken;
				open = true;
				break;
			} else {
				params.add(ptoken);
			}
			pos++;
		}

		AtRule node = new AtRule(open);
		node.name(token.text.substring(1));
		if (node.name().isEmpty()) {
			unnamedAtrule(context, node, token);
		}
		init(context, node, token.startLine, token.startColumn);

		context.pos = pos;

		boolean last = false;
		if (lastToken == null) {
			last = true;
		} else if (lastToken.type == PostCssTokenType.SEMICOLON) {
			node.source().end(new Position(lastToken.startLine, lastToken.startColumn));
			context.semicolon = true;
		} else if (lastToken.type == PostCssTokenType.RBRACE) {
		    PostCssToken lastPrevToken = context.tokens.get(pos - 1);
		    node.source().end(new Position(lastPrevToken.endLine, lastPrevToken.endColumn));
			end(context, lastToken);
		}

		SliceList<PostCssToken> tokens = new SliceList<>(params, 0, params.size());

		node.raws().between(spacesFromEnd(context.getCachedBuilder(), tokens).toString());
		if (!tokens.isEmpty()) {
			node.raws().afterName(spacesFromStart(context.getCachedBuilder(), tokens).toString());
			node.params(raw(context, node, "params", tokens));
			if (last) {
				token = tokens.get(tokens.size() - 1);
				node.source().end(new Position(token.endLine, token.endColumn));

				String between = node.raws().between();
				context.spaces.setLength(0);
				if (between != null) {
					context.spaces.append(between);
				}
				node.raws().between("");
			}
		} else {
			node.raws().afterName("");
			node.params("");
		}

		if (node.hasBody()) {
			context.current = node;
		}
	}

	protected void end(Context context, PostCssToken token) {
		if (!context.current.isEmpty()) {
			context.current.raws().semicolon(context.semicolon);
		}
		context.semicolon = false;

		StringBuilder sb = context.getCachedBuilder();
		String after = context.current.raws().after();
		if (after != null) {
			sb.append(after);
		}
		sb.append(context.spaces);
		context.current.raws().after(sb.toString());
		context.spaces.setLength(0);

		if (context.current.parent() != null) {
			context.current.source().end(new Position(token.startLine, token.startColumn));
			context.current = context.current.parent();
		} else {
			this.unexpectedClose(context, token);
		}
	}

	protected void endFile(Context context) {
		if (context.current.parent() != null) {
			unclosedBlock(context);
		}

		if (!context.current.isEmpty()) {
			context.current.raws().semicolon(context.semicolon);
		}

		StringBuilder sb = context.getCachedBuilder();
		String after = context.current.raws().after();
		if (after != null) {
			sb.append(after);
		}
		sb.append(context.spaces);
		context.current.raws().after(sb.toString());
		context.spaces.setLength(0);

		if (!context.tokens.isEmpty()) {
		    PostCssToken token = context.tokens.get(context.tokens.size() - 1);
		    context.current.source().end(new Position(token.endLine, token.endColumn));
		} else {
		    context.current.source().end(context.current.source().start());
		}
	}

	// Helpers

	private void init(Context context, Node node, int line, int column) {
		context.current.add(node);

		node.source(new Source(context.getInput(), new Position(line, column), null));

		node.raws().before(context.spaces.toString());
		context.spaces.setLength(0);
		if (!(node instanceof Comment)) {
			context.semicolon = false;
		}
	}

	private String raw(Context context, Node node, String prop, List<PostCssToken> tokens) {
		StringBuilder sb = context.getCachedBuilder();
		boolean clean = true;
		for (int i = 0; i < tokens.size(); i++) {
			PostCssToken token = tokens.get(i);
			if (token.type == PostCssTokenType.COMMENT || token.type == PostCssTokenType.SPACE && i == tokens.size() - 1) {
				clean = false;
			} else {
				sb.append(token.text);
			}
		}
		String value = sb.toString();
		if (!clean) {
			sb = context.getCachedBuilder();
			for (int i = 0; i < tokens.size(); i++) {
				PostCssToken token = tokens.get(i);
				sb.append(token.text);
			}

			node.raws().put(prop, sb.toString());
			node.raws().put("_" + prop, value);
		}
		return value;
	}

	private StringBuilder spacesFromStart(StringBuilder sb, SliceList<PostCssToken> tokens) {
		int to = tokens.size();
		for (int i = 0; i < tokens.size(); i++) {
			PostCssToken token = tokens.get(i);
			if (token.type != PostCssTokenType.SPACE && token.type != PostCssTokenType.COMMENT) {
				to = i;
				break;
			}
		}
		for (int i = 0; i < to; i++) {
			sb.append(tokens.shift().text);
		}
		return sb;
	}

	private StringBuilder spacesFromEnd(StringBuilder sb, SliceList<PostCssToken> tokens) {
		int from = 0;
		for (int i = tokens.size() - 1; i >= 0; i--) {
			PostCssToken token = tokens.get(i);
			if (token.type != PostCssTokenType.SPACE && token.type != PostCssTokenType.COMMENT) {
				from = i + 1;
				break;
			}
		}
		int count = tokens.size() - from;
		for (int i = 0; i < count; i++) {
			sb.append(tokens.get(from + i).text);
		}
		for (int i = 0; i < count; i++) {
			tokens.pop();
		}
		return sb;
	}

	private String stringFrom(StringBuilder sb, SliceList<PostCssToken> tokens, int from) {
		int count = 0;
		for (int i = from; i < tokens.size(); i++) {
			sb.append(tokens.get(i).text);
			count++;
		}
		for (int i = 0; i < count; i++) {
			tokens.pop();
		}
		return sb.toString();
	}

	private int colon(Context context, List<PostCssToken> tokens) {
		int brackets = 0;
		PostCssToken prev = null;
		for (int i = 0; i < tokens.size(); i++) {
			PostCssToken token = tokens.get(i);
			if (token.type == PostCssTokenType.LPAREN) {
				brackets++;
			} else if (token.type == PostCssTokenType.RPAREN) {
				brackets--;
			} else if (brackets == 0 && token.type == PostCssTokenType.COLON) {
				if (prev == null) {
					doubleColon(context, token);
				} else if (prev.type == PostCssTokenType.WORD && Objects.equals(prev.text, "progid")) {
					continue;
				} else {
					return i;
				}
			}

			prev = token;
		}
		return -1;
	}

	// Errors

	public void unclosedBracket(Context context, PostCssToken bracket) {
		throw new SyntaxException(context.getInput().file(), bracket.startLine, bracket.startColumn, "Unexpected bracket");
	}

	public void unknownWord(Context context, int start) {
		PostCssToken token = context.tokens.get(start);
		throw new SyntaxException(context.getInput().file(), token.startLine, token.startColumn, "Unknown word");
	}

	public void unexpectedClose(Context context, PostCssToken token) {
		throw new SyntaxException(context.getInput().file(), token.startLine, token.startColumn, "Unexpected }");
	}

	public void unclosedBlock(Context context) {
		Position pos = context.current.source().start();
		throw new SyntaxException(context.getInput().file(), pos.line(), pos.column(), "Unclosed block");
	}

	public void doubleColon(Context context, PostCssToken token) {
		throw new SyntaxException(context.getInput().file(), token.startLine, token.startColumn, "Double colon");
	}

	public void unnamedAtrule(Context context, Node node, PostCssToken token) {
		throw new SyntaxException(context.getInput().file(), token.startLine, token.startColumn, "At-rule without name");
	}

	public void precheckMissedSemicolon(Context context, List<PostCssToken> tokens) {

	}

	public void checkMissedSemicolon(Context context, List<PostCssToken> tokens) {
		int colon = colon(context, tokens);
		if (colon == -1) {
			return;
		}

		int founded = 0;
		PostCssToken token = null;
		for (int j = colon - 1; j >= 0; j--) {
			token = tokens.get(j);
			if (token.type != PostCssTokenType.SPACE) {
				founded += 1;
				if (founded == 2) {
					break;
				}
			}
		}
		throw new SyntaxException(context.getInput().file(), token.startLine, token.startColumn, "Missed semicolon");
	}

	protected class Context {
		private Input input;
		private StringBuilder spaces = new StringBuilder();

		List<PostCssToken> tokens;
		int pos = 0;
		private Node current;
		boolean semicolon;

		private Context(String file, String css) {
			this.input = new Input(file, css);
		}

		public Input getInput() {
			return input;
		}

		private StringBuilder sb = new StringBuilder(1000);

		StringBuilder getCachedBuilder() {
			sb.setLength(0);
			return sb;
		}
	}
}
