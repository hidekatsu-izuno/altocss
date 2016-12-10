package net.arnx.altocss;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class AltoCssTest {

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
    public void testProcess(String testcase) throws IOException {
        String input = readFile("cases/" + testcase + ".css");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        AltoCss altocss = new AltoCss(new Environment() {

            @Override
            public InputStream newInputStream(String file) throws IOException {
                return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public OutputStream newOutputStream(String file) throws IOException {
                return output;
            }

            @Override
            public void warn(String message) {
                throw new IllegalStateException(message);
            }

            @Override
            public void error(String message) {
                throw new IllegalStateException(message);
            }
        });
        altocss.process(testcase + ".css", testcase + ".css");
        try {
            assertEquals(input, output.toString("UTF-8"));
        } catch (Error e) {
            System.out.println(output.toString("UTF-8"));
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
