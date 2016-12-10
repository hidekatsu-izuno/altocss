package net.arnx.altocss.plugins.postcss;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.arnx.altocss.Input;
import net.arnx.altocss.Stringifier;
import net.arnx.altocss.node.AtRuleNode;
import net.arnx.altocss.node.CommentNode;
import net.arnx.altocss.node.DeclarationNode;
import net.arnx.altocss.node.Node;
import net.arnx.altocss.node.RootNode;
import net.arnx.altocss.node.RuleNode;
import net.arnx.altocss.util.SourceMapBuilder;

public class PostCssStringifier implements Stringifier {

	@Override
	public void stringify(Node node, Appendable out, SourceMapBuilder builder) throws IOException {
		Context context = new Context(out, builder);
		stringify(context, node, false);
	}

	private void stringify(Context context, Node node, boolean semicolon) throws IOException {
		context.addSource(node.source().input());

		if (node instanceof RootNode) {
			root(context, (RootNode)node);
		} else if (node instanceof CommentNode) {
			comment(context, (CommentNode)node);
		} else if (node instanceof DeclarationNode) {
			decl(context, (DeclarationNode)node, semicolon);
		} else if (node instanceof RuleNode) {
			rule(context, (RuleNode)node);
		} else if (node instanceof AtRuleNode) {
			atrule(context, (AtRuleNode)node, semicolon);
		} else {
			throw new IllegalArgumentException("unknown node type: " + node.getClass());
		}
	}

	protected void root(Context context, RootNode root) throws IOException {
		body(context, root);
		context.append(root.raws().after());
	}

	protected void comment(Context context, CommentNode comment) throws IOException {
		Object left = raw(context, comment, "left", "commentLeft");
		Object right = raw(context, comment, "right", "commentRight");
		context.append("/*").append(left).append(comment.text()).append(right).append("*/");
		context.addMapping(comment, null);
	}

	protected void decl(Context context, DeclarationNode decl, boolean semicolon)  throws IOException {
		Object between = raw(context, decl, "between", "colon");
		context.append(decl.prop()).append(between).append(rawValue(decl, "value", decl.value()));

		if (decl.important()) {
			String important = decl.raws().important();
            context.append((important != null) ? important : " !important");
		}

		if (semicolon) {
			context.append(";");
		}

		context.addMapping(decl, null);
	}

	protected void rule(Context context, RuleNode rule)  throws IOException {
		block(context, rule, rawValue(rule, "selector", rule.selector()));
	}

	protected void atrule(Context context, AtRuleNode atrule, boolean semicolon)  throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("@").append(atrule.name());
		Object params = atrule.params() != null ? rawValue(atrule, "params", atrule.params()) : null;

		String afterName = atrule.raws().afterName();
		if (afterName != null) {
		    sb.append(afterName);
		} else if (params == null || (params instanceof String && ((String)params).isEmpty())) {
			sb.append(" ");
		}
		sb.append(params);

		if (atrule.hasBody()) {
			block(context, atrule, sb);
		} else {
			context.append(sb).append(atrule.raws().between());
			if (semicolon) {
				context.append(";");
			}

			context.addMapping(atrule, null);
		}
	}

	private void body(Context context, Node node) throws IOException {
		int last = node.size() - 1;
		while (last > 0) {
			if (!(node.get(last) instanceof CommentNode)) {
				break;
			}
			last--;
		}

		boolean semicolon = Boolean.TRUE.equals(raw(context, node, "semicolon", "semicolon"));
		for (int i = 0; i < node.size(); i++) {
			Node child = node.get(i);
			context.append(raw(context, child, "before", "before"));
			stringify(context, child, last != i || semicolon);
		}
	}

	private void block(Context context, Node node, Object start) throws IOException {
		Object between = raw(context, node, "between", "beforeOpen");
		context.append(start).append(between).append("{");
		context.addMapping(node, MappingType.START);

		if (node.hasBody()) {
			body(context, node);
			context.append(raw(context, node, "after", "after"));
		} else {
			context.append(raw(context, node, "after", "emptyBody"));
		}

		context.append("}");
		context.addMapping(node, MappingType.END);
	}

	private Object raw(Context context, Node node, String own, String detect) {
		// Already had
		if (own != null) {
			Object value = node.raws().get(own);
			if (value != null){
				return value;
			}
		}

		Node parent = node.parent();

		// Hack for first rule in CSS
		if (Objects.equals(detect, "before")) {
			if (parent == null || (parent instanceof RootNode && !parent.isEmpty() && parent.get(0) == node)) {
				return "";
			}
		}

		// Floating child without parent
		if (parent == null) {
		    switch (detect) {
	        case "colon": return ": ";
	        case "indent": return "    ";
	        case "beforeDecl": return "\n";
	        case "beforeRule": return "\n";
	        case "beforeOpen": return " ";
	        case "beforeClose": return "\n";
	        case "beforeComment": return "\n";
	        case "after": return "\n";
	        case "emptyBody": return "";
	        case "commentLeft": return " ";
	        case "commentRight": return " ";
	        default: return null;
	        }
		}

		// Detect style by other nodes
		if (context.rawCache.containsKey(detect)) {
			return context.rawCache.get(detect);
		}

		if (Objects.equals(detect, "before") || Objects.equals(detect, "after")) {
			return beforeAfter(context, node, detect);
		}

		RootNode root = node.root();
		Object value = null;
		switch (detect) {
		case "semicolon": {
		    for (Node current : root.walker()) {
		        if (!current.isEmpty() && current.get(current.size() -1) instanceof DeclarationNode) {
		            value = current.raws().semicolon();
		            break;
	            }
		    }
			break;
		}
		case "emptyBody": {
		    for (Node current : root.walker()) {
				if (!current.isEmpty()) {
				    value = current.raws().after();
				    break;
				}
			}
			break;
		}
		case "indent": {
		    for (Node current : root.walker()) {
				Node p = current.parent();
				if (p != null && !(p instanceof RootNode) && p.parent() != null && p.parent() == root) {
					String before = current.raws().before();
					if (before != null) {
						String[] parts = before.split("\n");
						value = parts[parts.length - 1].replaceAll("[^\\s]", "");
						break;
					}
				}
			}
			break;
		}
		case "beforeComment": {
		    for (Node current : root.walker()) {
				if (current instanceof CommentNode) {
					String before = current.raws().before();
					if (before != null) {
						if (before.indexOf('\n') != -1) {
							before = before.replaceFirst("[^\\n]+$", "");
						}
						value = before;
						break;
					}
				}
			}
			if (value == null) {
				value = raw(context, node, null, "beforeDecl");
			}
			break;
		}
		case "beforeDecl": {
		    for (Node current : root.walker()) {
				if (current instanceof DeclarationNode) {
					String before = current.raws().before();
					if (before != null) {
						if (before.indexOf('\n') != -1) {
							before = before.replaceAll("[^\\n]+", "");
						}
						value = before;
						break;
					}
				}
			}
			if (value == null) {
				value = raw(context, node, null, "beforeRule");
			}
			break;
		}
		case "beforeRule": {
		    for (Node current : root.walker()) {
				if (current.hasBody() && (current.parent() != root || root.isEmpty() || root.get(0) != current)) {
					String before = current.raws().before();
					if (before != null) {
						if (before.indexOf('\n') != -1) {
							before = before.replaceFirst("[^\\n]+$", "");
						}
						value = before;
						break;
					}
				}
			}
			break;
		}
		case "beforeClose": {
		    for (Node current : root.walker()) {
				if (!current.isEmpty()) {
					String after = current.raws().after();
					if (after != null) {
						if (after.indexOf('\n') != -1) {
							after = after.replaceFirst("[^\n]+$", "");
						}
						value = after;
						break;
					}
				}
			}
			break;
		}
		case "beforeOpen": {
		    for (Node current : root.walker()) {
				if (current instanceof DeclarationNode) {
					value = ((DeclarationNode)current).raws().between();
					break;
				}
			}
			break;
		}
		case "colon": {
		    for (Node current : root.walker()) {
				if (current instanceof DeclarationNode) {
					String between = ((DeclarationNode)current).raws().between();
					if (between != null) {
						value = between.replaceAll("[^\\s:]", "");
						break;
					}
				}
			}
			break;
		}
		default: {
		    for (Node current : root.walker()) {
				value = current.raws().get(own);
				break;
			}
			break;
		}
		}

		context.rawCache.put(detect, value);
		return value;
	}

	private Object beforeAfter(Context context, Node node, String detect) {
		Object value;
		if (node instanceof DeclarationNode) {
			value = raw(context, node, null, "beforeDecl");
		} else if (node instanceof CommentNode) {
			value = raw(context, node, null, "beforeComment");
		} else if (Objects.equals(detect, "before")) {
			value = raw(context, node, null, "beforeRule");
		} else {
			value = raw(context, node, null, "beforeClose");
		}

		Node buf = node.parent();
		int depth = 0;
		while (buf != null && !(buf instanceof RootNode)) {
			depth++;
			buf = buf.parent();
		}

		if (value instanceof String && ((String)value).indexOf('\n') != -1) {
			Object indent = raw(context, node, null, "indent");
			if (indent instanceof String) {
				StringBuilder sb = new StringBuilder();
				sb.append((String)value);
				for (int step = 0; step < depth; step++) {
					sb.append(indent);
				}
				value = sb.toString();
			}
		}

		return value;
	}

	private Object rawValue(Node node, String prop, Object value) {
		if (Objects.equals(node.raws().get("_" + prop), value)) {
			return node.raws().get(prop);
		} else {
			return value;
		}
	}

	private static class Context {
		private Appendable out;
		private SourceMapBuilder builder;

		private int lastLine = 1;
		private int lastColumn = 0;

		private int line = 1;
		private int column = 0;
		private int breakstate = 0; // 0 CR 1 LF/FF/CR 2

		Map<String, Object> rawCache = new HashMap<>();

		public Context(Appendable out, SourceMapBuilder builder) {
			this.out = out;
			this.builder = builder;
		}

		public Context append(Object o) throws IOException {
			CharSequence cs = (o instanceof CharSequence) ? (CharSequence)o :
				(o != null) ? o.toString() :
					"";
			for (int i = 0; i < cs.length(); i++) {
				char c = cs.charAt(i);

				if (breakstate == 2 || breakstate == 1 && c != '\n') {
					line++;
					column = 0;
				}
				column++;
				if (c == '\n') {
					breakstate = 2;
				} else if (c == '\r') {
					breakstate = 1;
				} else {
					breakstate = 0;
				}
			}

			out.append(cs);
			return this;
		}

		public void addSource(Input input) {
			if (builder == null) {
				return;
			}
			builder.addSource(input.file(), input.css());
		}

		public void addMapping(Node node, MappingType type) {
			if (builder == null) {
				return;
			}

			if (type != MappingType.END) {
				builder.addMapping(node.source().input().file(),
						node.source().start().line(),
						node.source().start().column() - 1,
						lastLine,
						lastColumn - 1);
			}

			if (type != MappingType.START) {
				builder.addMapping(node.source().input().file(),
						node.source().end().line(),
						node.source().end().column() - 1,
						line,
						column - 1);
			}

			lastLine = line;
			lastColumn = column;
		}
	}

	private static enum MappingType {
		START,
		END
	}
}
