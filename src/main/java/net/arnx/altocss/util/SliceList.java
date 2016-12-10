package net.arnx.altocss.util;

import java.util.AbstractList;
import java.util.List;

public class SliceList<E> extends AbstractList<E> {
	private List<E> list;
	private int start;
	private int end;

	public SliceList(List<E> list, int start, int end) {
		this.list = list;
		this.start = start;
		this.end = end;
	}

	@Override
	public E get(int index) {
		if (start + index >= end) {
			throw new IndexOutOfBoundsException();
		}
		return list.get(start + index);
	}

	@Override
	public int size() {
		return Math.max(end - start, 0);
	}

	public E shift() {
		E elem = list.get(start);
		start++;
		return elem;
	}

	public E pop() {
		E elem = list.get(end-1);
		end--;
		return elem;
	}
}
