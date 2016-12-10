package net.arnx.altocss;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Environment {
	public InputStream newInputStream(String file) throws IOException;
	public OutputStream newOutputStream(String file) throws IOException;
	public void info(String message);
	public void warn(String message);
	public void error(String message);
}
