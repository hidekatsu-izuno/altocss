package net.arnx.altocss.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TokenList extends AbstractList<CharSequence> implements CharSequence {
    private List<CharSequence> list = new ArrayList<>();
    private String text;

    @Override
    public void add(int index, CharSequence cs) {
        text = null;
        list.add(index, cs);
    }

    @Override
    public CharSequence set(int index, CharSequence cs) {
        text = null;
        return list.set(index, cs);
    }

    @Override
    public CharSequence remove(int index) {
        text = null;
        return list.remove(index);
    }

    @Override
    public CharSequence get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public String toString() {
        if (text == null) {
            int length = 0;
            for (CharSequence cs : list) {
                length += cs.length();
            }
            StringBuilder sb = new StringBuilder(length);
            for (CharSequence cs : list) {
                sb.append(cs);
            }
            text = sb.toString();
        }
        return text;
    }
}
