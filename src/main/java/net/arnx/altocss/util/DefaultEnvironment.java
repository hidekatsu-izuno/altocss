package net.arnx.altocss.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import net.arnx.altocss.Environment;

public class DefaultEnvironment implements Environment {
	private URI src;
	private Path dest;

	public DefaultEnvironment(URI src, Path dest) {
		this.src = src;
		this.dest = dest;
	}

	public DefaultEnvironment(URL src, Path dest) {
		try {
			this.src = src.toURI();
			this.dest = dest;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("src is invalid.", e);
		}
	}

	@Override
	public InputStream newInputStream(String file) throws IOException {
		URI uri = src.resolve(file);
		if ("classpath".equalsIgnoreCase(uri.getScheme())) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl != null) {
				return cl.getResourceAsStream(uri.getPath());
			}
			return getClass().getResourceAsStream(uri.getPath());
		} else {
		    URLConnection con = uri.toURL().openConnection();
		    con.setUseCaches(false);
		    return con.getInputStream();
		}
	}

	@Override
	public OutputStream newOutputStream(String file) throws IOException {
		return Files.newOutputStream(dest.resolve(file));
	}

	@Override
	public void info(String message) {
	    System.out.println("[INFO] " + message);
	}

	@Override
	public void warn(String message) {
	    System.err.println("[WARN] " + message);
	}

	@Override
	public void error(String message) {
        System.err.println("[ERROR] " + message);
	}
}
