package net.arnx.altocss.plugins.postcss;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import net.arnx.altocss.SyntaxException;
import net.arnx.altocss.tokens.AtWordToken;
import net.arnx.altocss.tokens.ColonToken;
import net.arnx.altocss.tokens.CommentToken;
import net.arnx.altocss.tokens.LBraceToken;
import net.arnx.altocss.tokens.LBracketToken;
import net.arnx.altocss.tokens.LParenToken;
import net.arnx.altocss.tokens.RBraceToken;
import net.arnx.altocss.tokens.RBracketToken;
import net.arnx.altocss.tokens.RParenToken;
import net.arnx.altocss.tokens.SemiColonToken;
import net.arnx.altocss.tokens.SpaceToken;
import net.arnx.altocss.tokens.StringToken;
import net.arnx.altocss.tokens.Token;
import net.arnx.altocss.tokens.WordToken;
import net.arnx.altocss.util.InputSource;

public class PostCssTokenizer {

	private static final int SPACE_FLAG = 0B00000001;
	private static final int AT_END_FLAG = 0B00000010;
	private static final int WORD_END_FLAG = 0B00000100;
	private static final int NO_URL_FLAG = 0B00001000;
	private static final int[] FLAGS = new int[128];

	static {
		FLAGS[' '] = AT_END_FLAG | WORD_END_FLAG | SPACE_FLAG | NO_URL_FLAG;
		FLAGS['\n'] = AT_END_FLAG | WORD_END_FLAG | SPACE_FLAG | NO_URL_FLAG;
		FLAGS['\t'] = AT_END_FLAG | WORD_END_FLAG | SPACE_FLAG | NO_URL_FLAG;
		FLAGS['\r'] = AT_END_FLAG | WORD_END_FLAG | SPACE_FLAG | NO_URL_FLAG;
		FLAGS['\f'] = AT_END_FLAG | WORD_END_FLAG | SPACE_FLAG | NO_URL_FLAG;
		FLAGS['{'] = AT_END_FLAG | WORD_END_FLAG;
		FLAGS['}'] = WORD_END_FLAG;
		FLAGS[':'] = WORD_END_FLAG;
		FLAGS[';'] = AT_END_FLAG | WORD_END_FLAG;
		FLAGS['@'] = WORD_END_FLAG;
		FLAGS['!'] = WORD_END_FLAG;
		FLAGS['('] = AT_END_FLAG | WORD_END_FLAG | NO_URL_FLAG;
		FLAGS[')'] = AT_END_FLAG | WORD_END_FLAG | NO_URL_FLAG;
		FLAGS['\''] = AT_END_FLAG | WORD_END_FLAG | NO_URL_FLAG;
		FLAGS['"'] = AT_END_FLAG | WORD_END_FLAG | NO_URL_FLAG;
		FLAGS['\\'] = AT_END_FLAG | WORD_END_FLAG;
		FLAGS['/'] = AT_END_FLAG;
		FLAGS['['] = AT_END_FLAG | WORD_END_FLAG;
		FLAGS[']'] = AT_END_FLAG | WORD_END_FLAG;
		FLAGS['#'] = AT_END_FLAG | WORD_END_FLAG;
	}

	public List<Token> tokenize(String file, CharSequence cs) {
		try (InputSource css = InputSource.from(cs)) {
			return tokenize(file, css);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public List<Token> tokenize(String file, Reader reader) throws IOException {
		try (InputSource css = InputSource.from(reader)) {
			return tokenize(file, css);
		}
	}

	public List<Token> tokenize(String file, InputSource css) throws IOException {
		List<Token> tokens = new ArrayList<>();

		boolean inUrl = false;
		int n;
		while ((n = css.next()) != -1) {
			char code = (char)n;

			int startLine = css.getLine();
			int startColumn = css.getColumn();

			if (inUrl && !(n < FLAGS.length && (FLAGS[n] & NO_URL_FLAG) != 0)) {
                boolean escape = (n == '\\');
                int space = 0;
                while ((n = css.lookup()) != -1) {
                    if (n < FLAGS.length && (FLAGS[n] & SPACE_FLAG) != 0) {
                        escape = false;
                        space++;
                        continue;
                    }

                    if (n == '\\') {
                        escape = !escape;
                    } else if (n == ')' && !escape) {
                        break;
                    } else {
                        escape = false;
                    }

                    for (int i = 0; i < space; i++) {
                        css.next();
                    }
                    space = 0;
                    css.next();
                }

                if (n == -1) {
                    throw new SyntaxException(file, startLine, startColumn, "Unclosed bracket");
                }

                tokens.add(new WordToken(css.text(), startLine, startColumn, css.getLine(), css.getColumn()));
                continue;
			}

			switch (code) {
			case ' ':
			case '\n':
			case '\t':
			case '\r':
			case '\f': {
				while ((n = css.lookup()) != -1) {
					if (n < FLAGS.length && (FLAGS[n] & SPACE_FLAG) != 0) {
                        css.next();
					} else {
                        break;
					}
				}

				tokens.add(new SpaceToken(css.text(), startLine, startColumn, css.getLine(), css.getColumn()));
				break;
			}
			case '[': {
				tokens.add(new LBracketToken(css.text(), startLine, startColumn));
				break;
			}
			case ']': {
				tokens.add(new RBracketToken(css.text(), startLine, startColumn));
				break;
			}
			case '{': {
				tokens.add(new LBraceToken(css.text(), startLine, startColumn));
				break;
			}
			case '}': {
				tokens.add(new RBraceToken(css.text(), startLine, startColumn));
				break;
			}
			case ':': {
				tokens.add(new ColonToken(css.text(), startLine, startColumn));
				break;
			}
			case ';': {
				tokens.add(new SemiColonToken(css.text(), startLine, startColumn));
				break;
			}
			case '(': {
			    if (!tokens.isEmpty() && "url".equals(tokens.get(tokens.size() - 1).text())) {
			        inUrl = true;
			        if (css.lookup() == -1) {
			            throw new SyntaxException(file, startLine, startColumn, "Unclosed bracket");
			        } else {
			            css.unlookup();
			        }
			    }
                tokens.add(new LParenToken(css.text(), startLine, startColumn));
				break;
			}
			case ')': {
			    inUrl = false;
				tokens.add(new RParenToken(css.text(), startLine, startColumn));
				break;
			}
			case '\'':
			case '"': {
				boolean escape = false;
				while ((n = css.next()) != -1) {
					if (n == '\\') {
						escape = !escape;
					} else if (n == code && !escape) {
						break;
					} else {
						escape = false;
					}
				}

				if (n == -1) {
					throw new SyntaxException(file, startLine, startColumn, "Unclosed string");
				}

				tokens.add(new StringToken(css.text(), startLine, startColumn, css.getLine(), css.getColumn()));
				break;
			}
			case '@': {
				while ((n = css.lookup()) != -1) {
					if (n < FLAGS.length && (FLAGS[n] & AT_END_FLAG) != 0) {
						break;
					} else {
						css.next();
					}
				}

				tokens.add(new AtWordToken(css.text(), startLine, startColumn, css.getLine(), css.getColumn()));
				break;
			}
			case '\\': {
				boolean escape = true;
				while ((n = css.lookup()) != -1) {
					if (n == '\\') {
						escape = !escape;
						css.next();
					} else {
						if (escape && !(n < FLAGS.length && (FLAGS[n] & SPACE_FLAG) != 0) && n != '/') {
							css.next();
						}
						break;
					}
				}
				tokens.add(new WordToken(css.text(), startLine, startColumn, css.getLine(), css.getColumn()));
				break;
			}
			default: {
				if (code == '/' && css.lookup() == '*') {
					css.next();

					while ((n = css.lookup()) != -1) {
						if (n == '*') {
							if ((n = css.lookup()) == '/') {
								css.next();
								css.next();
								break;
							} else {
								css.next();
							}
						}
						css.next();
					}

					if (n == -1) {
						throw new SyntaxException(file, startLine, startColumn, "Unclosed comment");
					}

					tokens.add(new CommentToken(css.text(), startLine, startColumn, css.getLine(), css.getColumn()));
				} else {
					while ((n = css.lookup()) != -1) {
						if (n < FLAGS.length && (FLAGS[n] & WORD_END_FLAG) != 0) {
							break;
						} else if (n == '/') {
							if ((n = css.lookup()) == '*') {
								break;
							} else {
								css.next();
							}
						} else {
							css.next();
						}
					}
					tokens.add(new WordToken(css.text(), startLine, startColumn, css.getLine(), css.getColumn()));
				}

				break;
			}

			}
		}

		return tokens;
	}
}
