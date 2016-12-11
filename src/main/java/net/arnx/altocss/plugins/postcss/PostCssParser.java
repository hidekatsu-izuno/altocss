package net.arnx.altocss.plugins.postcss;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.arnx.altocss.Input;
import net.arnx.altocss.Parser;
import net.arnx.altocss.Position;
import net.arnx.altocss.Source;
import net.arnx.altocss.SyntaxException;
import net.arnx.altocss.node.AtRuleNode;
import net.arnx.altocss.node.CommentNode;
import net.arnx.altocss.node.DeclarationNode;
import net.arnx.altocss.node.Node;
import net.arnx.altocss.node.RootNode;
import net.arnx.altocss.node.RuleNode;
import net.arnx.altocss.token.AtWordToken;
import net.arnx.altocss.token.ColonToken;
import net.arnx.altocss.token.CommentToken;
import net.arnx.altocss.token.LBraceToken;
import net.arnx.altocss.token.LBracketToken;
import net.arnx.altocss.token.LParenToken;
import net.arnx.altocss.token.OpeToken;
import net.arnx.altocss.token.RBraceToken;
import net.arnx.altocss.token.RBracketToken;
import net.arnx.altocss.token.RParenToken;
import net.arnx.altocss.token.SemicolonToken;
import net.arnx.altocss.token.SpaceToken;
import net.arnx.altocss.token.Token;
import net.arnx.altocss.token.WordToken;
import net.arnx.altocss.util.SliceList;

public class PostCssParser implements Parser {
	@Override
	public RootNode parse(String file, String css) {
		try {
			Context context = new Context(file, css);

			PostCssTokenizer tokenizder = new PostCssTokenizer();
			context.tokens = tokenizder.tokenize(file, css);

			RootNode root = new RootNode();
			root.source(new Source(context.getInput(), new Position(1, 1), null));

			context.current = root;
			while (context.pos < context.tokens.size()) {
				Token token = context.tokens.get(context.pos);
				if (token instanceof SpaceToken || token instanceof SemicolonToken) {
                    context.spaces.append(token.text());
				} else if (token instanceof CommentToken) {
				    comment(context, token);
				} else if (token instanceof RBraceToken) {
                    end(context, token);
                } else if (token instanceof LBraceToken) {
                    emptyRule(context, token);
                } else if (token instanceof AtWordToken) {
                    atrule(context, token);
                } else {
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

	protected void comment(Context context, Token token) {
		CommentNode node = new CommentNode();
		this.init(context, node, token.startLine(), token.startColumn());
		node.source().end(new Position(token.endLine(), token.endColumn()));

		int start = -1;
		for (int i = 2; i < token.text().length() - 2; i++) {
			char c = token.text().charAt(i);
			if (!(c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f')) {
				start = i;
				break;
			}
		}
		if (start == -1) {
			node.text("");
			node.raws().left(token.text().substring(2, token.text().length() - 2));
			node.raws().right("");
		} else {
			int end = start + 1;
			for (int i = token.text().length() - 2 - 1; i > start; i--) {
				char c = token.text().charAt(i);
				if (!(c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f')) {
					end = i + 1;
					break;
				}
			}
			node.text(token.text().substring(start, end));
			node.raws().left(token.text().substring(2, start));
			node.raws().right(token.text().substring(end, token.text().length() - 2));
		}
	}

	protected void emptyRule(Context context, Token token) {
		RuleNode node = new RuleNode();
		this.init(context, node, token.startLine(), token.startColumn());
		node.selector("");
		node.raws().between("");
		context.current = node;
	}

	protected void other(Context context) {
		boolean end = false;
		boolean colon = false;
		Token bracket = null;
		List<Class<? extends Token>> brackets = new ArrayList<>();

		int start = context.pos;
		while (context.pos < context.tokens.size()) {
			Token token = context.tokens.get(context.pos);
			if (token instanceof LParenToken || token instanceof LBracketToken) {
				if (bracket == null) {
					bracket = token;
				}
				brackets.add(token instanceof LParenToken ? RParenToken.class : RBracketToken.class);
			} else if (brackets.isEmpty()) {
				if (token instanceof SemicolonToken) {
					if (colon) {
						decl(context, new SliceList<>(context.tokens, start, context.pos + 1));
						return;
					} else {
						break;
					}
				} else if (token instanceof LBraceToken) {
					rule(context, new SliceList<>(context.tokens, start, context.pos + 1));
					return;
				} else if (token instanceof RBraceToken) {
					context.pos --;
					end = true;
					break;
				} else if (token instanceof ColonToken) {
					colon = true;
				}
			} else if (token.getClass().isAssignableFrom(brackets.get(brackets.size() - 1))) {
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
				Token token = context.tokens.get(context.pos);
				if (!(token instanceof SpaceToken || token instanceof CommentToken)) {
					break;
				}
				context.pos--;
			}
			decl(context, new SliceList<>(context.tokens, start, context.pos + 1));
			return;
		}

		unknownWord(context, start);
	}

	private void rule(Context context, SliceList<Token> tokens) {
		tokens.pop();

		RuleNode node = new RuleNode();
		Token token = tokens.get(0);
		init(context, node, token.startLine(), token.startColumn());

		node.raws().between(spacesFromEnd(context.getCachedBuilder(), tokens).toString());
		node.selector(raw(context, node, "selector", tokens));
		context.current = node;
	}

	private void decl(Context context, SliceList<Token> tokens) {
		DeclarationNode node = new DeclarationNode();
		init(context, node, -1, -1);

		Token last = tokens.get(tokens.size() - 1);
		if (last instanceof SemicolonToken) {
			context.semicolon = true;
			tokens.pop();
		}
		node.source().end(new Position(last.endLine(), last.endColumn()));

		StringBuilder sb = context.getCachedBuilder();
		String before = node.raws().before();
		if (before != null) {
			sb.append(before);
		}
		while (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token instanceof WordToken || token instanceof OpeToken) {
				break;
			}
			sb.append(tokens.shift().text());
		}
		node.raws().before(sb.toString());

		Token first = tokens.get(0);
		node.source().start(new Position(first.startLine(), first.startColumn()));

		sb = context.getCachedBuilder();
		while (!tokens.isEmpty()) {
			Token token = tokens.get(0);
			if (token instanceof ColonToken || token instanceof SpaceToken || token instanceof CommentToken) {
				break;
			}
			sb.append(tokens.shift().text());
		}
		node.prop(sb.toString());

		sb = context.getCachedBuilder();
		while (!tokens.isEmpty()) {
			Token token = tokens.shift();

			if (token instanceof ColonToken) {
				sb.append(token.text());
				break;
			} else {
				sb.append(token.text());
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
			Token token = tokens.get(i);
			if (Objects.equals(token.text(), "!important")) {
				node.important(true);
				String string = stringFrom(context.getCachedBuilder(), tokens, i);
				string = spacesFromEnd(context.getCachedBuilder(), tokens).append(string).toString();
				if (!Objects.equals(string, " !important")) {
					node.raws().important(string);
				}
				break;
			} else if (Objects.equals(token.text(), "important")) {
				boolean exclamation = false;
				int j;
				for (j = i - 1; j > 0; j--) {
					token = tokens.get(j);
					if (!exclamation && token instanceof WordToken && Objects.equals(token.text(), "!")) {
						exclamation = true;
					} else if (!(token instanceof SpaceToken || token instanceof CommentToken)) {
						break;
					}
				}
				if (exclamation) {
					node.important(true);
					node.raws().important(stringFrom(context.getCachedBuilder(), tokens, j + 1));
					i = j;
				}
			}

			if (!(token instanceof SpaceToken || token instanceof CommentToken)) {
				break;
			}
		}

		node.value(raw(context, node, "value", tokens));

		if (node.value().indexOf(':') != -1) {
			checkMissedSemicolon(context, tokens);
		}
	}

	protected void atrule(Context context, Token token) {
		List<Token> params = new ArrayList<>();
		boolean open = false;
		Token lastToken = null;
		int pos = context.pos + 1;
		while (pos < context.tokens.size()) {
			Token ptoken = context.tokens.get(pos);
			if (ptoken instanceof SemicolonToken || ptoken instanceof RBraceToken) {
				lastToken = ptoken;
				break;
			} else if (ptoken instanceof LBraceToken) {
				lastToken = ptoken;
				open = true;
				break;
			} else {
				params.add(ptoken);
			}
			pos++;
		}

		AtRuleNode node = new AtRuleNode(open);
		node.name(token.text().substring(1));
		if (node.name().isEmpty()) {
			unnamedAtrule(context, node, token);
		}
		init(context, node, token.startLine(), token.startColumn());

		context.pos = pos;

		boolean last = false;
		if (lastToken == null) {
			last = true;
		} else if (lastToken instanceof SemicolonToken) {
			node.source().end(new Position(lastToken.startLine(), lastToken.startColumn()));
			context.semicolon = true;
		} else if (lastToken instanceof RBraceToken) {
		    Token lastPrevToken = context.tokens.get(pos - 1);
		    node.source().end(new Position(lastPrevToken.endLine(), lastPrevToken.endColumn()));
			end(context, lastToken);
		}

		SliceList<Token> tokens = new SliceList<>(params, 0, params.size());

		node.raws().between(spacesFromEnd(context.getCachedBuilder(), tokens).toString());
		if (!tokens.isEmpty()) {
			node.raws().afterName(spacesFromStart(context.getCachedBuilder(), tokens).toString());
			node.params(raw(context, node, "params", tokens));
			if (last) {
				token = tokens.get(tokens.size() - 1);
				node.source().end(new Position(token.endLine(), token.endColumn()));

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

	protected void end(Context context, Token token) {
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
			context.current.source().end(new Position(token.startLine(), token.startColumn()));
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
		    Token token = context.tokens.get(context.tokens.size() - 1);
		    context.current.source().end(new Position(token.endLine(), token.endColumn()));
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
		if (!(node instanceof CommentNode)) {
			context.semicolon = false;
		}
	}

	private String raw(Context context, Node node, String prop, List<Token> tokens) {
		StringBuilder sb = context.getCachedBuilder();
		boolean clean = true;
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token instanceof CommentToken || token instanceof SpaceToken && i == tokens.size() - 1) {
				clean = false;
			} else {
				sb.append(token.text());
			}
		}
		String value = sb.toString();
		if (!clean) {
			sb = context.getCachedBuilder();
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				sb.append(token.text());
			}

			node.raws().put(prop, sb.toString());
			node.raws().put("_" + prop, value);
		}
		return value;
	}

	private StringBuilder spacesFromStart(StringBuilder sb, SliceList<Token> tokens) {
		int to = tokens.size();
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (!(token instanceof SpaceToken || token instanceof CommentToken)) {
				to = i;
				break;
			}
		}
		for (int i = 0; i < to; i++) {
			sb.append(tokens.shift().text());
		}
		return sb;
	}

	private StringBuilder spacesFromEnd(StringBuilder sb, SliceList<Token> tokens) {
		int from = 0;
		for (int i = tokens.size() - 1; i >= 0; i--) {
			Token token = tokens.get(i);
			if (!(token instanceof SpaceToken || token instanceof CommentToken)) {
				from = i + 1;
				break;
			}
		}
		int count = tokens.size() - from;
		for (int i = 0; i < count; i++) {
			sb.append(tokens.get(from + i).text());
		}
		for (int i = 0; i < count; i++) {
			tokens.pop();
		}
		return sb;
	}

	private String stringFrom(StringBuilder sb, SliceList<Token> tokens, int from) {
		int count = 0;
		for (int i = from; i < tokens.size(); i++) {
			sb.append(tokens.get(i).text());
			count++;
		}
		for (int i = 0; i < count; i++) {
			tokens.pop();
		}
		return sb.toString();
	}

	private int colon(Context context, List<Token> tokens) {
		int brackets = 0;
		Token prev = null;
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token instanceof LParenToken) {
				brackets++;
			} else if (token instanceof RParenToken) {
				brackets--;
			} else if (brackets == 0 && token instanceof ColonToken) {
				if (prev == null) {
					doubleColon(context, token);
				} else if (prev instanceof WordToken && Objects.equals(prev.text(), "progid")) {
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

	public void unclosedBracket(Context context, Token bracket) {
		throw new SyntaxException(context.getInput().file(), bracket.startLine(), bracket.startColumn(), "Unexpected bracket");
	}

	public void unknownWord(Context context, int start) {
		Token token = context.tokens.get(start);
		throw new SyntaxException(context.getInput().file(), token.startLine(), token.startColumn(), "Unknown word");
	}

	public void unexpectedClose(Context context, Token token) {
		throw new SyntaxException(context.getInput().file(), token.startLine(), token.startColumn(), "Unexpected }");
	}

	public void unclosedBlock(Context context) {
		Position pos = context.current.source().start();
		throw new SyntaxException(context.getInput().file(), pos.line(), pos.column(), "Unclosed block");
	}

	public void doubleColon(Context context, Token token) {
		throw new SyntaxException(context.getInput().file(), token.startLine(), token.startColumn(), "Double colon");
	}

	public void unnamedAtrule(Context context, Node node, Token token) {
		throw new SyntaxException(context.getInput().file(), token.startLine(), token.startColumn(), "At-rule without name");
	}

	public void precheckMissedSemicolon(Context context, List<Token> tokens) {

	}

	public void checkMissedSemicolon(Context context, List<Token> tokens) {
		int colon = colon(context, tokens);
		if (colon == -1) {
			return;
		}

		int founded = 0;
		Token token = null;
		for (int j = colon - 1; j >= 0; j--) {
			token = tokens.get(j);
			if (!(token instanceof SpaceToken)) {
				founded += 1;
				if (founded == 2) {
					break;
				}
			}
		}
		throw new SyntaxException(context.getInput().file(), token.startLine(), token.startColumn(), "Missed semicolon");
	}

	protected class Context {
		private Input input;
		private StringBuilder spaces = new StringBuilder();

		List<Token> tokens;
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
