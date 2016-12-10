package net.arnx.altocss;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.arnx.altocss.util.JsonWriter;
import net.arnx.altocss.util.Jsonable;
import net.arnx.altocss.util.TreeWalker;

public abstract class Node extends AbstractList<Node> implements Jsonable {
	private boolean hasBody;
	private Node parent;
	private List<Node> children;

	private Source source;
	private NodeRaws raws = createRaws();

	public Node(boolean hasBody) {
		this.hasBody = hasBody;
		this.children = hasBody ? new ArrayList<>() : Collections.emptyList();
	}

	public boolean hasBody() {
		return hasBody;
	}

	public Root root() {
        Node current = this;
        while (current.parent != null) {
        	current = current.parent;
        }
        if (current instanceof Root) {
        	return (Root)current;
        }
        return null;
    }

	public Node parent() {
		return parent;
	}

	@Override
	public Node get(int index) {
		return children.get(index);
	}

	@Override
	public void add(int index, Node node) {
		children.add(index, node);
		node.parent = this;
	}

	@Override
	public Node set(int index, Node node) {
		Node prev = children.set(index, node);
		node.parent = this;
		prev.parent = null;
		return prev;
	}

	@Override
	public Node remove(int index) {
		Node prev = children.remove(index);
		prev.parent = null;
		return prev;
	}

	@Override
	public int size() {
		return children.size();
	}

	public TreeWalker<Node> walker() {
	    return new TreeWalker<>(this);
	}

	public Node source(Source source) {
		this.source = source;
		return this;
	}

	public Source source() {
		return source;
	}

	public NodeRaws raws() {
		return raws;
	}

	NodeRaws createRaws() {
		return new NodeRaws();
	}

	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		JsonWriter writer = new JsonWriter(sb);
		writer.prettyPrint(true);
		try {
			jsonize(writer);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return toJSON();
	}

	public static class NodeRaws extends HashMap<String, Object> {
		NodeRaws() {
		}

		public void before(String before) {
			put("before", before);
		}

		public String before() {
			Object before = get("before");
			if (before instanceof String) {
				return (String)before;
			}
			return null;
		}

		public void semicolon(Boolean semicolon) {
			put("semicolon", semicolon);
		}

		public Boolean semicolon() {
			Object semicolon = get("semicolon");
			if (semicolon instanceof Boolean) {
				return (Boolean)semicolon;
			}
			return null;
		}

		public void after(String after) {
			put("after", after);
		}

		public String after() {
			Object after = get("after");
			if (after instanceof String) {
				return (String)after;
			}
			return null;
		}
	}
}
