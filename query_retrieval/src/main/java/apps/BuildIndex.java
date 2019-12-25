package apps;

import index.IndexBuilder;

public class BuildIndex {
	public static void main(String[] args) {
		// args = [source_file, compressed]

		// usage inputFile true|false
		String sourcefile = args[0];
		boolean compress = Boolean.parseBoolean(args[1]);
		IndexBuilder builder = new IndexBuilder();
		builder.buildIndex(sourcefile, compress);
	}
}
