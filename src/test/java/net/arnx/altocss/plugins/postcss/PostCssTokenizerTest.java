package net.arnx.altocss.plugins.postcss;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.arnx.altocss.SyntaxException;

public class PostCssTokenizerTest {

	@Test
	public void tokenizesEmptyFile() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		assertEquals(tokens, tokenizer.tokenize("", toSource("")));
	}

	@Test
	public void tokenizesSpace() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\r\n \f\t", 1, 1, 2, 3));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\r\n \f\t")));
	}

	@Test
	public void tokenizesWord() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "ab", 1, 1, 1, 2));
		assertEquals(tokens, tokenizer.tokenize("", toSource("ab")));
	}

	@Test
	public void splitsWordByEx() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "aa", 1, 1, 1, 2));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "!bb", 1, 3, 1, 5));
		assertEquals(tokens, tokenizer.tokenize("", toSource("aa!bb")));
	}

	@Test
	public void changesLinesInSpace() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "a", 1, 1, 1, 1));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, " \n ", 1, 2, 2, 1));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "b", 2, 2, 2, 2));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a \n b")));
	}

	@Test
	public void tokenizesControlChars() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.LBRACE, "{", 1, 1));
		tokens.add(new PostCssToken(PostCssTokenType.COLON, ":", 1, 2));
		tokens.add(new PostCssToken(PostCssTokenType.SEMICOLON, ";", 1, 3));
		tokens.add(new PostCssToken(PostCssTokenType.RBRACE, "}", 1, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("{:;}")));
	}

	@Test
	public void escapesControlSymbols() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "\\(", 1, 1, 1, 2));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "\\{", 1, 3, 1, 4));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "\\\"", 1, 5, 1, 6));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "\\@", 1, 7, 1, 8));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "\\\\", 1, 9, 1, 10));
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"\"", 1, 11, 1, 12));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\\(\\{\\\"\\@\\\\\"\"")));
	}

	@Test
	public void escapesBackslash() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "\\\\\\\\", 1, 1, 1, 4));
		tokens.add(new PostCssToken(PostCssTokenType.LBRACE, "{", 1, 5));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\\\\\\\\{")));
	}

	@Test
	public void tokenizesSimpleBrackets() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
        tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 1, 1, 1));
        tokens.add(new PostCssToken(PostCssTokenType.WORD, "ab", 1, 2, 1, 3));
        tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 4, 1, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("(ab)")));
	}

	@Test
	public void tokenizesSquareBrackets() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "a", 1, 1, 1, 1));
		tokens.add(new PostCssToken(PostCssTokenType.LBRACKET, "[", 1, 2));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "bc", 1, 3, 1, 4));
		tokens.add(new PostCssToken(PostCssTokenType.RBRACKET, "]", 1, 5));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a[bc]")));
	}

	@Test
	public void tokenizesComlicatedBrackets() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 1));
        tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 2, 1, 2));
        tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 3, 1, 3));
		tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 4));
		tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 5));
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"\"", 1, 6, 1, 7));
		tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 8));
		tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 9));
		tokens.add(new PostCssToken(PostCssTokenType.COMMENT, "/**/", 1, 10, 1, 13));
		tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 14));
		tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 15));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "\\\\", 1, 16, 1, 17));
		tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 18));
		tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 19));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\n", 1, 20, 1, 20));
		tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 2, 1));
		tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 2, 2));
		assertEquals(tokens, tokenizer.tokenize("", toSource("(())(\"\")(/**/)(\\\\)(\n)(")));
	}

	@Test
	public void tokenizesString() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "'\"'", 1, 1, 1, 3));
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"\\\"\"", 1, 4, 1, 7));
		assertEquals(tokens, tokenizer.tokenize("", toSource("'\"'\"\\\"\"")));
	}

	@Test
	public void tokenizesEscapedString() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"\\\\\"", 1, 1, 1, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\"\\\\\"")));
	}

	@Test
	public void changesLineInStrings() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"\n\n\"", 1, 1, 3, 1));
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"\n\n\"", 3, 2, 5, 1));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\"\n\n\"\"\n\n\"")));
	}

	@Test
	public void tokenizesAtWord() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.AT_WORD, "@word", 1, 1, 1, 5));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, " ", 1, 6, 1, 6));
		assertEquals(tokens, tokenizer.tokenize("", toSource("@word ")));
	}

	@Test
	public void tokenizesAtWordEnd() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.AT_WORD, "@one", 1, 1, 1, 4));
		tokens.add(new PostCssToken(PostCssTokenType.LBRACE, "{", 1, 5));
		tokens.add(new PostCssToken(PostCssTokenType.AT_WORD, "@two", 1, 6, 1, 9));
        tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 10, 1, 10));
        tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 11, 1, 11));
		tokens.add(new PostCssToken(PostCssTokenType.AT_WORD, "@three", 1, 12, 1, 17));
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"\"", 1, 18, 1, 19));
		tokens.add(new PostCssToken(PostCssTokenType.AT_WORD, "@four", 1, 20, 1, 24));
		tokens.add(new PostCssToken(PostCssTokenType.SEMICOLON, ";", 1, 25));
		assertEquals(tokens, tokenizer.tokenize("", toSource("@one{@two()@three\"\"@four;")));
	}

	@Test
	public void tokenizesUrls() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "url", 1, 1, 1, 3));
        tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 4, 1, 4));
        tokens.add(new PostCssToken(PostCssTokenType.WORD, "/*\\)", 1, 5, 1, 8));
        tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 9, 1, 9));
		assertEquals(tokens, tokenizer.tokenize("", toSource("url(/*\\))")));
	}

	@Test
	public void tokenizesQuotedUrls() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "url", 1, 1, 1, 3));
		tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 1, 4));
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\")\"", 1, 5, 1, 7));
		tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 1, 8));
		assertEquals(tokens, tokenizer.tokenize("", toSource("url(\")\")")));
	}

	@Test
	public void tokenizesAtSymbol() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.AT_WORD, "@", 1, 1, 1, 1));
		assertEquals(tokens, tokenizer.tokenize("", toSource("@")));
	}

	@Test
	public void tokenizesComment() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.COMMENT, "/* a\nb */", 1, 1, 2, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("/* a\nb */")));
	}

	@Test
	public void changesLinesInComments() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "a", 1, 1, 1, 1));
		tokens.add(new PostCssToken(PostCssTokenType.COMMENT, "/* \n */", 1, 2, 2, 3));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "b", 2, 4, 2, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a/* \n */b")));
	}

	@Test
	public void supportsLineFeed() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "a", 1, 1, 1, 1));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\f", 1, 2, 1, 2));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "b", 1, 3, 1, 3));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a\fb")));
	}

	@Test
	public void supportsCarriageReturn() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "a", 1, 1, 1, 1));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\r", 1, 2, 1, 2));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "b", 2, 1, 2, 1));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\r\n", 2, 2, 2, 3));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "c", 3, 1, 3, 1));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a\rb\r\nc")));
	}

	@Test
	public void tokenizesCSS() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<PostCssToken> tokens = new ArrayList<>();
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "a", 1, 1, 1, 1));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, " ", 1, 2, 1, 2));
		tokens.add(new PostCssToken(PostCssTokenType.LBRACE, "{", 1, 3));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\n  ", 1, 4, 2, 2));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "content", 2, 3, 2, 9));
		tokens.add(new PostCssToken(PostCssTokenType.COLON, ":", 2, 10));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, " ", 2, 11));
		tokens.add(new PostCssToken(PostCssTokenType.STRING, "\"a\"", 2, 12, 2, 14));
		tokens.add(new PostCssToken(PostCssTokenType.SEMICOLON, ";", 2, 15));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\n  ", 2, 16, 3, 2));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "width", 3, 3, 3, 7));
		tokens.add(new PostCssToken(PostCssTokenType.COLON, ":", 3, 8));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, " ", 3, 9));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "calc", 3, 10, 3, 13));
        tokens.add(new PostCssToken(PostCssTokenType.LPAREN, "(", 3, 14, 3, 14));
        tokens.add(new PostCssToken(PostCssTokenType.WORD, "1px", 3, 15, 3, 17));
        tokens.add(new PostCssToken(PostCssTokenType.SEMICOLON, ";", 3, 18, 3, 18));
        tokens.add(new PostCssToken(PostCssTokenType.RPAREN, ")", 3, 19, 3, 19));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\n  ", 3, 20, 4, 2));
		tokens.add(new PostCssToken(PostCssTokenType.RBRACE, "}", 4, 3));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\n", 4, 4));
		tokens.add(new PostCssToken(PostCssTokenType.COMMENT, "/* small screen */", 5, 1, 5, 18));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, "\n", 5, 19));
		tokens.add(new PostCssToken(PostCssTokenType.AT_WORD, "@media", 6, 1, 6, 6));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, " ", 6, 7));
		tokens.add(new PostCssToken(PostCssTokenType.WORD, "screen", 6, 8, 6, 13));
		tokens.add(new PostCssToken(PostCssTokenType.SPACE, " ", 6, 14));
		tokens.add(new PostCssToken(PostCssTokenType.LBRACE, "{", 6, 15));
		tokens.add(new PostCssToken(PostCssTokenType.RBRACE, "}", 6, 16));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a {\n"
						+ "  content: \"a\";\n"
						+ "  width: calc(1px;)\n"
						+ "  }\n"
						+ "/* small screen */\n"
						+ "@media screen {}")));
	}

	@Test
	public void throwsErrorOnUnclosedString() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		try {
			tokenizer.tokenize("", toSource(" \""));
			fail();
		} catch (SyntaxException e) {
			assertEquals(":1:2: Unclosed string", e.getMessage());
		}
	}

	@Test
	public void throwsErrorOnUnclosedComment() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		try {
			tokenizer.tokenize("", toSource(" /*"));
			fail();
		} catch (SyntaxException e) {
			assertEquals(":1:2: Unclosed comment", e.getMessage());
		}
	}

	@Test
	public void throwsErrorOnUnclosedUrl() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		try {
			tokenizer.tokenize("", toSource("url("));
			fail();
		} catch (SyntaxException e) {
			assertEquals(":1:4: Unclosed bracket", e.getMessage());
		}
	}

	private Reader toSource(String text) {
		return new StringReader(text);
	}
}