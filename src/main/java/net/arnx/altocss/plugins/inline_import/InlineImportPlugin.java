package net.arnx.altocss.plugins.inline_import;

import java.util.Iterator;

import net.arnx.altocss.Plugin;
import net.arnx.altocss.node.AtRuleNode;
import net.arnx.altocss.node.Node;
import net.arnx.altocss.node.RootNode;

public class InlineImportPlugin implements Plugin {
    @Override
    public void process(RootNode root) {
        Iterator<Node> i = root.walker().iterator();
        while (i.hasNext()) {
            Node node = i.next();
            if (node instanceof AtRuleNode) {
                AtRuleNode atrule = (AtRuleNode)node;
                if (atrule.name().equals("charset")) {
                    i.remove();
                } else if (atrule.name().equals("import")) {
                    atrule.params();
                }
            }
        }
    }
}
