package net.arnx.altocss;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.arnx.altocss.node.RootNode;
import net.arnx.altocss.util.DefaultEnvironment;

public class AltoCss {
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
		List<Class<? extends Plugin>> pluginClasses = new ArrayList<>(this.pluginClasses);
		pluginClasses = resolveDependency(pluginClasses);

		List<Plugin> plugins = new ArrayList<>();
		for (Class<? extends Plugin> pluginClass : pluginClasses) {
			try {
				plugins.add(pluginClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

	    PluginContext context = new PluginContext(env, new HashMap<>(options), plugins);

	    RootNode root = context.parse(infile);

		for (Plugin plugin : plugins) {
			plugin.process(root);
		}

		for (Plugin plugin : plugins) {
			plugin.validate(root);
		}

		for (Plugin plugin : plugins) {
			plugin.minify(root);
		}

		context.stringify(root, outfile, mapfile);
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
}
