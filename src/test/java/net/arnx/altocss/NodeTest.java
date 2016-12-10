package net.arnx.altocss;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

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
        Root root = parser.parse("example.css", EXAMPLE);

        Iterator<Node> i = root.walker().iterator();
        assertEquals(Rule.class, i.next().getClass());
        assertEquals(Declaration.class, i.next().getClass());
        assertEquals(Declaration.class, i.next().getClass());
        assertEquals(Comment.class, i.next().getClass());
        assertEquals(AtRule.class, i.next().getClass());
        assertEquals(Comment.class, i.next().getClass());
        assertEquals(Rule.class, i.next().getClass());
        i.remove();
        assertEquals(Declaration.class, i.next().getClass());
        assertEquals(AtRule.class, i.next().getClass());
        assertEquals(Rule.class, i.next().getClass());
        assertEquals(Declaration.class, i.next().getClass());
        assertEquals(AtRule.class, i.next().getClass());
        assertEquals(Declaration.class, i.next().getClass());
        assertEquals(Comment.class, i.next().getClass());
    }

}
