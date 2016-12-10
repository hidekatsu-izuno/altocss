package net.arnx.altocss;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import net.arnx.altocss.node.RootNode;
import net.arnx.altocss.plugins.postcss.PostCssParser;
import net.arnx.altocss.plugins.postcss.PostCssStringifier;
import net.arnx.altocss.util.SourceMapBuilder;

public class PluginContext implements Environment {
    private static final byte[] AT_CHARSET_BYTES = "@charset \"".getBytes(StandardCharsets.US_ASCII);

    private Environment env;
    private Map<Option<?>, Object> options;
    private List<Plugin> plugins;

    public PluginContext(Environment env, Map<Option<?>, Object> options, List<Plugin> plugins) {
        this.env = env;
        this.options = options;
        this.plugins = plugins;

        for (Plugin plugin : plugins) {
            plugin.init(this);
        }
    }

    public <T> T option(Option<T> name) {
        Object value = options.get(name);
        if (value != null) {
            return name.type().cast(value);
        }
        return null;
    }

    public RootNode parse(String file) throws IOException {
        String content;
        try (BufferedInputStream in = new BufferedInputStream(env.newInputStream(file))) {
            byte[] buf = new byte[4096];
            int pos = 0;
            int len;
            while ((len = in.read(buf, pos, buf.length - pos)) != -1) {
                pos += len;
                if (buf.length < pos + 4096) {
                    buf = Arrays.copyOf(buf, buf.length + 4096);
                }
            }
            Charset charset = StandardCharsets.UTF_8;
            if (buf.length > 3 && buf[0] == 0xEF && buf[1] == 0xBB && buf[2] == 0xBF) {
                charset = StandardCharsets.UTF_8;
            } else if (buf.length > 2 && buf[0] == 0xFE && buf[1] == 0xFF) {
                charset = StandardCharsets.UTF_16BE;
            } else if (buf.length > 2 && buf[0] == 0xFF && buf[1] == 0xFE) {
                charset = StandardCharsets.UTF_16LE;
            } else if (buf.length > 4 && buf[0] == 0x00 && buf[1] == 0x00 && buf[2] == 0xFE && buf[3] == 0xFF) {
                charset = Charset.forName("UTF-32BE");
            } else if (buf.length > 4 && buf[0] == 0xFF && buf[1] == 0xFE && buf[2] == 0x00 && buf[3] == 0x00) {
                charset = Charset.forName("UTF-32LE");
            } else if (buf.length > AT_CHARSET_BYTES.length + 2 && equalsBytes(buf, AT_CHARSET_BYTES, AT_CHARSET_BYTES.length)) {
                len = Math.min(1024, buf.length) - 1;
                for (int i = AT_CHARSET_BYTES.length; i < len; i++) {
                    if (buf[i] == 0x22 && buf[i + 1] == 0x3B) {
                        Charset detected = Charset.forName(new String(buf, 10, i-10));
                        if (equalsBytes("@charset \"".getBytes(detected), AT_CHARSET_BYTES, AT_CHARSET_BYTES.length)) {
                            charset = detected;
                        }
                        break;
                    }
                }
            }
            content = new String(buf, 0, pos, charset);
        }

        Parser parser = null;
        for (int i = plugins.size()-1; i >= 0; i--) {
            parser = plugins.get(i).parser(file);
            if (parser != null) {
                break;
            }
        }
        if (parser == null) {
            parser = new PostCssParser();
        }
        return parser.parse(file, content);
    }

    public void stringify(RootNode root, String outfile, String mapfile) throws IOException {
        Stringifier stringifier = null;
        for (int i = plugins.size()-1; i >= 0; i--) {
            stringifier = plugins.get(i).stringifier(outfile);
            if (stringifier != null) {
                break;
            }
        }
        stringifier = new PostCssStringifier();

        SourceMapBuilder builder = new SourceMapBuilder();
        builder.sourcesRoot((String)options.get(Option.SOURCE_MAP_SOURCES_ROOT));
        if (Boolean.TRUE.equals(options.get(Option.CONCAT_SOURCE_MAP))) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(env.newOutputStream(outfile), StandardCharsets.UTF_8))) {
                stringifier.stringify(root, writer, builder);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (Writer mwriter = new OutputStreamWriter(Base64.getEncoder().wrap(buffer), StandardCharsets.UTF_8)) {
                    builder.stringify(writer);
                }
                writer.append("/*# sourceMappingURL=data:application/json;charset=utf-8;base64,");
                writer.append(buffer.toString("US-ASCII"));
                writer.append(" */");
            }
        } else if (mapfile != null) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(env.newOutputStream(outfile), StandardCharsets.UTF_8))) {
                stringifier.stringify(root, writer, builder);
                writer.append("/*# sourceMappingURL=").append(mapfile).append(" */");
            }

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(env.newOutputStream(mapfile), StandardCharsets.UTF_8))) {
                builder.stringify(writer);
            }
        } else {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(env.newOutputStream(outfile), StandardCharsets.UTF_8))) {
                stringifier.stringify(root, writer, builder);
            }
        }
    }

    @Override
    public InputStream newInputStream(String file) throws IOException {
        return env.newInputStream(file);
    }

    @Override
    public OutputStream newOutputStream(String file) throws IOException {
        return env.newOutputStream(file);
    }

    @Override
    public void info(String message) {
        env.info(message);
    }

    @Override
    public void warn(String message) {
        env.warn(message);
    }

    @Override
    public void error(String message) {
        env.error(message);
    }

    private boolean equalsBytes(byte[] array1, byte[] array2, int size) {
        for (int i = 0; i < size; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }
}
