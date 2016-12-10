package net.arnx.altocss.token;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TokenList extends AbstractList<Token> {
    private List<Token> tokens;

    public TokenList(List<Token> tokens, int start, int end) {
        this.tokens = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            this.tokens.add(tokens.get(i));
        }
    }

    @Override
    public Token get(int index) {
        return tokens.get(index);
    }

    @Override
    public int size() {
        return tokens.size();
    }
}
