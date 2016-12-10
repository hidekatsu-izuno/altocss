package net.arnx.altocss.plugins.postcss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

import net.arnx.altocss.Root;

@RunWith(Theories.class)
public class PostCssParserTest {

	@DataPoints
	public static String[] TESTCASES = new String[] {
			"atrule-decls",
			"atrule-empty",
			"atrule-no-params",
			"atrule-no-semicolon",
			"atrule-no-space",
			"atrule-params",
			"atrule-rules",
			"between",
			"colon-selector",
			"comments",
			"decls",
			"empty",
			"escape",
			"extends",
			"function",
			"ie-progid",
			"important",
			"inside",
			"no-selector",
			"prop",
			"quotes",
			"raw-decl",
			"rule-at",
			"rule-no-semicolon",
			"selector",
			"semicolons",
			"tab"
	};

	@Theory
	public void testParse(String testcase) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		String file1 = "../../cases/" + testcase + ".json";
		JsonNode json1 = mapper.readTree(readFile(file1));

		String file2 = "../../cases/" + testcase + ".css";
		PostCssParser parser = new PostCssParser();
		Root root = parser.parse(testcase + ".css", readFile(file2));
		String json2text = root.toJSON();
		JsonNode json2 =  mapper.readTree(json2text);

		try {
			Assert.assertEquals(json1, json2);
		} catch (Error e) {
			System.out.println("[diff]");
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(JsonDiff.asJson(json1, json2)));
			System.out.println("[result]");
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json2));
			throw e;
		}
	}

	private String readFile(String file) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file), StandardCharsets.UTF_8))) {
			int n;
			while ((n = reader.read()) != -1) {
				sb.append((char)n);
			}
		}
		return sb.toString().trim();
	}
}
