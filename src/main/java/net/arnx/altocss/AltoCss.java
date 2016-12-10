package net.arnx.altocss;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.arnx.altocss.nodes.RootNode;
import net.arnx.altocss.plugins.postcss.PostCssParser;
import net.arnx.altocss.plugins.postcss.PostCssStringifier;
import net.arnx.altocss.util.DefaultEnvironment;
import net.arnx.altocss.util.SourceMapBuilder;

public class AltoCss {
	private static final byte[] AT_CHARSET_BYTES = "@charset \"".getBytes(StandardCharsets.US_ASCII);

	private Environment env;
	private List<Class<? extends Plugin>> pluginClasses = new ArrayList<>();
	private Map<Option<?>, Object> options = new HashMap<>();

	public AltoCss() {
		this(new DefaultEnvironment(Paths.get(".").toUri(), Paths.get(".")));
	}

	public AltoCss(File src, File dest) {
		this(new DefaultEnvironment(src.toURI(), dest.toPath()));
	}

	public AltoCss(Path src, Path dest) {
		this(new DefaultEnvironment(src.toUri(), dest));
	}

	public AltoCss(URI src, Path dest) {
		this(new DefaultEnvironment(src, dest));
	}

	public AltoCss(URL src, Path dest) {
		this(new DefaultEnvironment(src, dest));
	}

	public AltoCss(Environment resolver) {
		this.env = resolver;
	}

	public AltoCss use(Class<? extends Plugin> plugin) {
		synchronized (pluginClasses) {
			pluginClasses.add(plugin);
		}
		return this;
	}

	public <T> AltoCss option(Option<T> name, T value) {
		if (value != null) {
			options.put(name, name.type().cast(value));
		} else {
			options.remove(name);
		}
		return this;
	}

    public <T> T option(Option<T> name) {
        Object value = options.get(name);
        if (value != null) {
            return name.type().cast(value);
        }
        return null;
	}

	public void process(String infile, String outfile) throws IOException {
		process(infile, outfile, null);
	}

	public void process(String infile, String outfile, String mapfile) throws IOException {
		infile = infile.replace('\\', '/');
		outfile = outfile.replace('\\', '/');
		if (mapfile != null) {
			mapfile = mapfile.replace('\\', '/');
		}
		Map<Option<?>, Object> options = new HashMap<>(this.options);

		List<Class<? extends Plugin>> pluginClasses = new ArrayList<>(this.pluginClasses);
		pluginClasses = resolveDependency(pluginClasses);

		List<Plugin> plugins = new ArrayList<>();
		for (Class<? extends Plugin> pluginClass : pluginClasses) {
			try {
				Plugin plugin = pluginClass.newInstance();
				plugin.init(env, options);
				plugins.add(plugin);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

		Parser parser = null;
		for (int i = plugins.size()-1; i >= 0; i--) {
			parser = plugins.get(i).parser(infile);
			if (parser != null) {
				break;
			}
		}
		if (parser == null) {
			parser = new PostCssParser();
		}


		String content;
		try (BufferedInputStream in = new BufferedInputStream(env.newInputStream(infile))) {
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

		RootNode root = parser.parse(infile, content);

		for (Plugin plugin : plugins) {
			plugin.process(root);
		}

		for (Plugin plugin : plugins) {
			plugin.validate(root);
		}

		for (Plugin plugin : plugins) {
			plugin.minify(root);
		}

		Stringifier stringifier = null;
		for (int i = plugins.size()-1; i >= 0; i--) {
			stringifier = plugins.get(i).stringifier(outfile);
			if (stringifier != null) {
				break;
			}
		}
		if (stringifier == null) {
			stringifier = new PostCssStringifier();
		}

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

	private List<Class<? extends Plugin>> resolveDependency(List<Class<? extends Plugin>> root) {
		List<Class<? extends Plugin>> result = new ArrayList<>();
		Map<Class<? extends Plugin>, Boolean> states = new IdentityHashMap<>();
		List<Class<? extends Plugin>> visiting = new ArrayList<>();

		for (Class<? extends Plugin> plugin : root) {
			Boolean visit = states.get(plugin);
			if (visit == null) {
				topologicalSort(plugin, states, visiting, result);
			} else if (visit == Boolean.FALSE) {
				throw new IllegalStateException("Unexpected node in visiting state: " + plugin);
			}
		}

		return result;
	}

	private void topologicalSort(Class<? extends Plugin> root,
			Map<Class<? extends Plugin>, Boolean> states,
			List<Class<? extends Plugin>> visiting,
			List<Class<? extends Plugin>> result) {

		states.put(root, Boolean.FALSE);
		visiting.add(root);

		PluginConfiguration depends = root.getAnnotation(PluginConfiguration.class);
		if (depends != null) {
			for (Class<? extends Plugin> plugin : depends.depends()) {
				Boolean visit = states.get(plugin);
				if (visit == null) {
					topologicalSort(plugin, states, visiting, result);
				} else if (visit == Boolean.FALSE) {
					StringBuilder sb = new StringBuilder();
					sb.append("Circular dependency: ");
					sb.append(plugin);
					Class<? extends Plugin> cls;
					do {
						cls = visiting.remove(visiting.size() - 1);
						sb.append(" <- ");
						sb.append(cls);
					} while (!cls.equals(plugin));
					throw new IllegalStateException(sb.toString());
				}
			}
		}
		Class<? extends Plugin> poped = visiting.remove(visiting.size() - 1);
		if (root != poped) {
			throw new IllegalStateException("Unexpected internal error: expected to "
					+ "pop " + root + " but got " + poped);
		}
		states.put(root, Boolean.TRUE);
		result.add(root);
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
