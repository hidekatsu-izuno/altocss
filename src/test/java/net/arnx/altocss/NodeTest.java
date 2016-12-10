package net.arnx.altocss;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import net.arnx.altocss.nodes.AtRuleNode;
import net.arnx.altocss.nodes.CommentNode;
import net.arnx.altocss.nodes.DeclarationNode;
import net.arnx.altocss.nodes.Node;
import net.arnx.altocss.nodes.RootNode;
import net.arnx.altocss.nodes.RuleNode;
import net.arnx.altocss.plugins.postcss.PostCssParser;

public class NodeTest {
    private static final String EXAMPLE = "" +
            "a { a: 1; b: 2 }" +
            "/* a */" +
            "@keyframes anim {" +
                "/* b */" +
                "to { c: 3 }" +
            "}" +
            "@media all and (min-width: 100) {" +
                "em { d: 4 }" +
                "@page {" +
                    "e: 5;" +
                    "/* c */" +
                "}" +
            "}";

    @Test
    public void testWalkIterates() {
        PostCssParser parser = new PostCssParser();
        RootNode root = parser.parse("example.css", EXAMPLE);

        Iterator<Node> i = root.walker().iterator();
        assertEquals(RuleNode.class, i.next().getClass());
        assertEquals(DeclarationNode.class, i.next().getClass());
        assertEquals(DeclarationNode.class, i.next().getClass());
        assertEquals(CommentNode.class, i.next().getClass());
        assertEquals(AtRuleNode.class, i.next().getClass());
        assertEquals(CommentNode.class, i.next().getClass());
        assertEquals(RuleNode.class, i.next().getClass());
        i.remove();
        assertEquals(DeclarationNode.class, i.next().getClass());
        assertEquals(AtRuleNode.class, i.next().getClass());
        assertEquals(RuleNode.class, i.next().getClass());
        assertEquals(DeclarationNode.class, i.next().getClass());
        assertEquals(AtRuleNode.class, i.next().getClass());
        assertEquals(DeclarationNode.class, i.next().getClass());
        assertEquals(CommentNode.class, i.next().getClass());
    }

}
