package net.arnx.altocss.util;

import java.io.IOException;
import java.io.Reader;

public interface InputSource extends AutoCloseable {
	public int lookup() throws IOException;

	public void unlookup();

	public int next() throws IOException;

	public String text();

	public int getLine();

	public int getColumn();

	@Override
	public void close() throws IOException;

	public static InputSource from(CharSequence cs) {
		return new InputSource() {
			private int pos = 0;
			private int lookup = 0;
			private int next = 0;
			private int breakstate = 0; // 0 CR 1 LF/FF/CR 2
			private int line = 1;
			private int column = 0;

			@Override
			public int lookup() {
				int n;
				if (pos + next + lookup >= cs.length()) {
					n = -1;
				} else {
					n = cs.charAt(pos + next + lookup);
				}
				lookup++;
				return n;
			}

			@Override
			public void unlookup() {
				if (lookup <= 0) {
					throw new IllegalStateException("lookup count is not positive.");
				}
				lookup--;
			}

			@Override
			public int next() {
				int n;
				if (pos + next >= cs.length()) {
					n = -1;
				} else {
					n = cs.charAt(pos + next++);
				}

				lookup = 0;

				if (breakstate == 2 || breakstate == 1 && n != '\n') {
					line++;
					column = 0;
				}
				if (n != -1) {
					column++;
				}

				if (n == '\n') {
					breakstate = 2;
				} else if (n == '\r') {
					breakstate = 1;
				} else {
					breakstate = 0;
				}

				return  n;
			}

			@Override
			public String text() {
				String text = cs.subSequence(pos, pos + next).toString();
				pos += next;
				lookup = 0;
				next = 0;
				return text;
			}

			@Override
			public int getLine() {
				return line;
			}

			@Override
			public int getColumn() {
				return column;
			}

			@Override
			public void close() {
			}
		};
	}

	public static InputSource from(Reader reader) {
		return new InputSource() {
			private StringBuilder sb = new StringBuilder(1000);
			private int lookup = 0;
			private int next = 0;
			private int end = Integer.MAX_VALUE;
			private int breakstate = 0; // 0 CR 1 LF/FF/CR 2
			private int line = 1;
			private int column = 0;

			@Override
			public int lookup() throws IOException {
				int n;
				if (next + lookup >= end) {
					n = -1;
				} else if (next + lookup < sb.length()) {
					n = sb.charAt(next + lookup);
				} else {
					n = reader.read();
					if (n == -1) {
						end = next + lookup;
					} else {
						sb.append((char)n);
					}
				}
				lookup++;
				return n;
			}

			@Override
			public void unlookup() {
				if (lookup <= 0) {
					throw new IllegalStateException("lookup count is not positive.");
				}
				lookup--;
			}

			@Override
			public int next() throws IOException {
				int n;
				if (next >= end) {
					n = -1;
				} else if (next < sb.length()) {
					n = sb.charAt(next++);
				} else {
					n = reader.read();
					if (n == -1) {
						end = next;
					} else {
						sb.append((char)n);
						next++;
					}
				}

				lookup = 0;

				if (breakstate == 2 || breakstate == 1 && n != '\n') {
					line++;
					column = 0;
				}
				if (n != -1) {
					column++;
				}

				if (n == '\n') {
					breakstate = 2;
				} else if (n == '\r') {
					breakstate = 1;
				} else {
					breakstate = 0;
				}

				return n;
			}

			@Override
			public String text() {
				String result = sb.substring(0, next);
				sb.delete(0, next);
				lookup = 0;
				next = 0;
				return result;
			}

			@Override
			public int getLine() {
				return line;
			}

			@Override
			public int getColumn() {
				return column;
			}

			@Override
			public void close() throws IOException {
				reader.close();
			}
		};
	}
}
