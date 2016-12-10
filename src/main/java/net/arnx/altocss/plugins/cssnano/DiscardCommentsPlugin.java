package net.arnx.altocss.plugins.cssnano;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import net.arnx.altocss.AtRule;
import net.arnx.altocss.Comment;
import net.arnx.altocss.Declaration;
import net.arnx.altocss.Environment;
import net.arnx.altocss.Node;
import net.arnx.altocss.Option;
import net.arnx.altocss.Plugin;
import net.arnx.altocss.Root;
import net.arnx.altocss.Rule;

public class DiscardCommentsPlugin implements Plugin {
    public static final Option<Boolean> COMMENT_REMOVE_ALL = Option.of("discard_comments.remove_all", Boolean.class);

    private boolean removeAll;

    @Override
    public void init(Environment env, Map<Option<?>, Object> options) {
        removeAll = Boolean.TRUE.equals(options.get(COMMENT_REMOVE_ALL));
    }

	@Override
	public void minify(Root root) {
	    Iterator<Node> i = root.walker().iterator();
	    while (i.hasNext()) {
	        Node node = i.next();
            if (node instanceof Comment && canRemove(((Comment)node).text())) {
                i.remove();
                continue;
            }

            Object between = node.raws().get("between");
            if (between instanceof String) {
                node.raws().put("between", replaceComments((String)between, " "));
            }

            if (node instanceof Declaration) {
                Declaration decl = (Declaration)node;
                if (Objects.equals(decl.raws()._value(), decl.value())) {
                    decl.value(replaceComments(decl.raws().value(), " "));
                } else {
                    decl.value(replaceComments(decl.value(), " "));
                }
                String important = decl.raws().important();
                if (important != null) {
                    important = replaceComments(important, " ");
                    if (important.contains("/*")) {
                        decl.raws().put("important", important);
                    } else{
                        decl.raws().put("important", "!important");
                    }
                }
                continue;
            }

            if (node instanceof Rule) {
                Rule rule = (Rule)node;
                String selector = rule.raws().selector();
                if (selector != null) {
                    rule.raws().selector(replaceComments(selector, ""));
                }
                continue;
            }

            if (node instanceof AtRule) {
                AtRule atRule = (AtRule)node;
                String afterName = atRule.raws().afterName();
                if (afterName != null) {
                    String commentsReplaced = replaceComments(afterName, " ");
                    if (commentsReplaced.isEmpty()) {
                        node.raws().put("afterName", " ");
                    } else {
                        node.raws().put("afterName", " " + commentsReplaced + " ");
                    }
                }
                String params = atRule.raws().params();
                if (params != null) {
                    atRule.raws().params(replaceComments(params, " "));
                }
            }
	    }
	}

	private boolean canRemove(String comment) {
	    return removeAll || !comment.startsWith("!");
	}

	private String replaceComments(String text, String replacement) {
	    if (text.isEmpty()) {
	        return text;
	    }

	    StringBuilder sb = new StringBuilder(text.length());
	    int start = 0;
	    int end;
	    while ((end = text.indexOf("/*", start)) != -1) {
	        sb.append(text, start, end);
	        start = end;
	        end = text.indexOf("*/", start);
	        if (end != -1) {
	            String comment = text.substring(start + 2, end);
	            if (canRemove(comment)) {
	                sb.append(replacement);
	            } else {
	                sb.append(comment);
	            }
	            start = end + 2;
	        } else {
	            start = text.length();
	            break;
	        }
	    }
	    if (start < text.length()) {
	        sb.append(text, start, text.length());
	    }
		return sb.toString();
	}
}
