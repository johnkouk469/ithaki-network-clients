import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

	private final Properties properties = new Properties();
	private final String path;

	public Config(String path) throws IOException {
		this.path = path;
		try (FileInputStream in = new FileInputStream(path)) {
			properties.load(in);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(
					"Cannot find " + path + " (copy ithaki.properties.example to ithaki.properties and fill in your session codes).");
		}
	}

	public String optional(String key, String defaultValue) {
		String value = properties.getProperty(key);
		return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
	}

	public String get(String key) {
		String value = properties.getProperty(key);
		if (value == null || value.trim().isEmpty() || value.trim().endsWith("XXXX")) {
			throw new IllegalStateException(
					"Set '" + key + "' in " + path + " (copy ithaki.properties.example and fill in your session codes).");
		}
		return value.trim();
	}

}
