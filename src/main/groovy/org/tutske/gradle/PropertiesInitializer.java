package org.tutske.gradle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


public class PropertiesInitializer {

	public static final String PREFIX = "TG_";
	public static final String filename = System.getenv ().getOrDefault (PREFIX + "CONFIG_FILE", ".tg.properties");
	public static final Path [] locations = {
		Paths.get ("."),
		Paths.get (System.getProperty ("user.home")),
		Paths.get (System.getProperty ("user.home")).resolve (".rcs")
	};

	public static Properties collectProperties () {
		Logger logger = LoggerFactory.getLogger (Config.class);

		Properties properties = new Properties ();
		List<Path> paths = new LinkedList<> ();

		if ( System.getenv ().containsKey (PREFIX + "CONFIG_FILE") ) {
			Path p = Paths.get (System.getenv (PREFIX + "CONFIG_FILE"));
			if ( Files.exists (p) && Files.isRegularFile (p) ) { paths.add (p); }
			else { logger.warn ("Configuration file {} does not exist, skipping", p); }
		}

		Arrays.stream (locations)
			.map (location -> location.resolve (filename))
			.filter (Files::exists)
			.filter (Files::isRegularFile)
			.forEachOrdered (paths::add);

		Collections.reverse (paths);
		paths.forEach (path -> {
			try ( InputStream stream = Files.newInputStream (path) ) { properties.load (stream); }
			catch ( IOException e ) { throw new RuntimeException (e); }
		});

		return properties;
	}

}
