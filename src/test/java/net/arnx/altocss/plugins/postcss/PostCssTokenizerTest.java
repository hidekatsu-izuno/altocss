package net.arnx.altocss.plugins.postcss;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.arnx.altocss.SyntaxException;
import net.arnx.altocss.token.AtWordToken;
import net.arnx.altocss.token.ColonToken;
import net.arnx.altocss.token.CommentToken;
import net.arnx.altocss.token.LBraceToken;
import net.arnx.altocss.token.LBracketToken;
import net.arnx.altocss.token.LParenToken;
import net.arnx.altocss.token.RBraceToken;
import net.arnx.altocss.token.RBracketToken;
import net.arnx.altocss.token.RParenToken;
import net.arnx.altocss.token.SemiColonToken;
import net.arnx.altocss.token.SpaceToken;
import net.arnx.altocss.token.StringToken;
import net.arnx.altocss.token.Token;
import net.arnx.altocss.token.WordToken;

public class PostCssTokenizerTest {

	@Test
	public void tokenizesEmptyFile() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		assertEquals(tokens, tokenizer.tokenize("", toSource("")));
	}

	@Test
	public void tokenizesSpace() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new SpaceToken("\r\n \f\t", 1, 1, 2, 3));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\r\n \f\t")));
	}

	@Test
	public void tokenizesWord() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("ab", 1, 1, 1, 2));
		assertEquals(tokens, tokenizer.tokenize("", toSource("ab")));
	}

	@Test
	public void splitsWordByEx() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("aa", 1, 1, 1, 2));
		tokens.add(new WordToken("!bb", 1, 3, 1, 5));
		assertEquals(tokens, tokenizer.tokenize("", toSource("aa!bb")));
	}

	@Test
	public void changesLinesInSpace() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("a", 1, 1, 1, 1));
		tokens.add(new SpaceToken(" \n ", 1, 2, 2, 1));
		tokens.add(new WordToken("b", 2, 2, 2, 2));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a \n b")));
	}

	@Test
	public void tokenizesControlChars() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new LBraceToken("{", 1, 1));
		tokens.add(new ColonToken(":", 1, 2));
		tokens.add(new SemiColonToken(";", 1, 3));
		tokens.add(new RBraceToken("}", 1, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("{:;}")));
	}

	@Test
	public void escapesControlSymbols() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("\\(", 1, 1, 1, 2));
		tokens.add(new WordToken("\\{", 1, 3, 1, 4));
		tokens.add(new WordToken("\\\"", 1, 5, 1, 6));
		tokens.add(new WordToken("\\@", 1, 7, 1, 8));
		tokens.add(new WordToken("\\\\", 1, 9, 1, 10));
		tokens.add(new StringToken("\"\"", 1, 11, 1, 12));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\\(\\{\\\"\\@\\\\\"\"")));
	}

	@Test
	public void escapesBackslash() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("\\\\\\\\", 1, 1, 1, 4));
		tokens.add(new LBraceToken("{", 1, 5));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\\\\\\\\{")));
	}

	@Test
	public void tokenizesSimpleBrackets() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
        tokens.add(new LParenToken("(", 1, 1));
        tokens.add(new WordToken("ab", 1, 2, 1, 3));
        tokens.add(new RParenToken(")", 1, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("(ab)")));
	}

	@Test
	public void tokenizesSquareBrackets() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("a", 1, 1, 1, 1));
		tokens.add(new LBracketToken("[", 1, 2));
		tokens.add(new WordToken("bc", 1, 3, 1, 4));
		tokens.add(new RBracketToken("]", 1, 5));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a[bc]")));
	}

	@Test
	public void tokenizesComlicatedBrackets() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new LParenToken("(", 1, 1));
        tokens.add(new LParenToken("(", 1, 2));
        tokens.add(new RParenToken(")", 1, 3));
		tokens.add(new RParenToken(")", 1, 4));
		tokens.add(new LParenToken("(", 1, 5));
		tokens.add(new StringToken("\"\"", 1, 6, 1, 7));
		tokens.add(new RParenToken(")", 1, 8));
		tokens.add(new LParenToken("(", 1, 9));
		tokens.add(new CommentToken("/**/", 1, 10, 1, 13));
		tokens.add(new RParenToken(")", 1, 14));
		tokens.add(new LParenToken("(", 1, 15));
		tokens.add(new WordToken("\\\\", 1, 16, 1, 17));
		tokens.add(new RParenToken(")", 1, 18));
		tokens.add(new LParenToken("(", 1, 19));
		tokens.add(new SpaceToken("\n", 1, 20, 1, 20));
		tokens.add(new RParenToken(")", 2, 1));
		tokens.add(new LParenToken("(", 2, 2));
		assertEquals(tokens, tokenizer.tokenize("", toSource("(())(\"\")(/**/)(\\\\)(\n)(")));
	}

	@Test
	public void tokenizesString() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new StringToken("'\"'", 1, 1, 1, 3));
		tokens.add(new StringToken("\"\\\"\"", 1, 4, 1, 7));
		assertEquals(tokens, tokenizer.tokenize("", toSource("'\"'\"\\\"\"")));
	}

	@Test
	public void tokenizesEscapedString() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new StringToken("\"\\\\\"", 1, 1, 1, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\"\\\\\"")));
	}

	@Test
	public void changesLineInStrings() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new StringToken("\"\n\n\"", 1, 1, 3, 1));
		tokens.add(new StringToken("\"\n\n\"", 3, 2, 5, 1));
		assertEquals(tokens, tokenizer.tokenize("", toSource("\"\n\n\"\"\n\n\"")));
	}

	@Test
	public void tokenizesAtWord() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new AtWordToken("@word", 1, 1, 1, 5));
		tokens.add(new SpaceToken(" ", 1, 6, 1, 6));
		assertEquals(tokens, tokenizer.tokenize("", toSource("@word ")));
	}

	@Test
	public void tokenizesAtWordEnd() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new AtWordToken("@one", 1, 1, 1, 4));
		tokens.add(new LBraceToken("{", 1, 5));
		tokens.add(new AtWordToken("@two", 1, 6, 1, 9));
        tokens.add(new LParenToken("(", 1, 10));
        tokens.add(new RParenToken(")", 1, 11));
		tokens.add(new AtWordToken("@three", 1, 12, 1, 17));
		tokens.add(new StringToken("\"\"", 1, 18, 1, 19));
		tokens.add(new AtWordToken("@four", 1, 20, 1, 24));
		tokens.add(new SemiColonToken(";", 1, 25));
		assertEquals(tokens, tokenizer.tokenize("", toSource("@one{@two()@three\"\"@four;")));
	}

	@Test
	public void tokenizesUrls() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("url", 1, 1, 1, 3));
        tokens.add(new LParenToken("(", 1, 4));
        tokens.add(new WordToken("/*\\)", 1, 5, 1, 8));
        tokens.add(new RParenToken(")", 1, 9));
		assertEquals(tokens, tokenizer.tokenize("", toSource("url(/*\\))")));
	}

	@Test
	public void tokenizesQuotedUrls() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("url", 1, 1, 1, 3));
		tokens.add(new LParenToken("(", 1, 4));
		tokens.add(new StringToken("\")\"", 1, 5, 1, 7));
		tokens.add(new RParenToken(")", 1, 8));
		assertEquals(tokens, tokenizer.tokenize("", toSource("url(\")\")")));
	}

	@Test
	public void tokenizesAtSymbol() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new AtWordToken("@", 1, 1, 1, 1));
		assertEquals(tokens, tokenizer.tokenize("", toSource("@")));
	}

	@Test
	public void tokenizesComment() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new CommentToken("/* a\nb */", 1, 1, 2, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("/* a\nb */")));
	}

	@Test
	public void changesLinesInComments() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("a", 1, 1, 1, 1));
		tokens.add(new CommentToken("/* \n */", 1, 2, 2, 3));
		tokens.add(new WordToken("b", 2, 4, 2, 4));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a/* \n */b")));
	}

	@Test
	public void supportsLineFeed() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("a", 1, 1, 1, 1));
		tokens.add(new SpaceToken("\f", 1, 2, 1, 2));
		tokens.add(new WordToken("b", 1, 3, 1, 3));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a\fb")));
	}

	@Test
	public void supportsCarriageReturn() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("a", 1, 1, 1, 1));
		tokens.add(new SpaceToken("\r", 1, 2, 1, 2));
		tokens.add(new WordToken("b", 2, 1, 2, 1));
		tokens.add(new SpaceToken("\r\n", 2, 2, 2, 3));
		tokens.add(new WordToken("c", 3, 1, 3, 1));
		assertEquals(tokens, tokenizer.tokenize("", toSource("a\rb\r\nc")));
	}

	@Test
	public void tokenizesCSS() throws IOException {
		PostCssTokenizer tokenizer = new PostCssTokenizer();
		List<Token> tokens = new ArrayList<>();
		tokens.add(new WordToken("a", 1, 1, 1, 1));
		tokens.add(new SpaceToken(" ", 1, 2, 1, 2));
		tokens.add(new LBraceToken("{", 1, 3));
		tokens.add(new SpaceToken("\n  ", 1, 4, 2, 2));
		tokens.add(new WordToken("content", 2, 3, 2, 9));
		tokens.add(new ColonToken(":", 2, 10));
		tokens.add(new SpaceToken(" ", 2, 11, 2, 11));
		tokens.add(new StringToken("\"a\"", 2, 12, 2, 14));
		tokens.add(new SemiColonToken(";", 2, 15));
		tokens.add(new SpaceToken("\n  ", 2, 16, 3, 2));
		tokens.add(new WordToken("width", 3, 3, 3, 7));
		tokens.add(new ColonToken(":", 3, 8));
		tokens.add(new SpaceToken(" ", 3, 9, 3, 9));
		tokens.add(new WordToken("calc", 3, 10, 3, 13));
        tokens.add(new LParenToken("(", 3, 14));
        tokens.add(new WordToken("1px", 3, 15, 3, 17));
        tokens.add(new SemiColonToken(";", 3, 18));
        tokens.add(new RParenToken(")", 3, 19));
		tokens.add(new SpaceToken("\n  ", 3, 20, 4, 2));
		tokens.add(new RBraceToken("}", 4, 3));
		tokens.add(new SpaceToken("\n", 4, 4, 4, 4));
		tokens.add(new CommentToken("/* small screen */", 5, 1, 5, 18));
		tokens.add(new SpaceToken("\n", 5, 19, 5, 19));
		tokens.add(new AtWordToken("@media", 6, 1, 6, 6));
		tokens.add(new SpaceToken(" ", 6, 7, 6, 7));
		tokens.add(new WordToken("screen", 6, 8, 6, 13));
		tokens.add(new SpaceToken(" ", 6, 14, 6, 14));
		tokens.add(new LBraceToken("{", 6, 15));
		tokens.add(new RBraceToken("}", 6, 16));
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
