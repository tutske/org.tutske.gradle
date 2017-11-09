package org.tutske.gradle

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class Config {

	private static final String PREFIX = "TG_"
	private static final String filename = System.getenv ().containsKey (PREFIX + "C") ?
		System.getenv (PREFIX + "C") :
		System.getProperty ("user.home") + "/.tg.properties"

	private static final Properties properties = new Properties ()

	static {
		Path path = Paths.get (filename)
		if ( ! Files.exists (path) || ! Files.isRegularFile (path) ) { return }
		Files.newInputStream (path).withCloseable { s -> properties.load (s) }
	}

	private static GString findProperty (String name, GString otherwise) {
		def val = System.getenv (PREFIX + name)
		if ( val == null ) { val = properties.getProperty (name) }
		if ( val == null ) { val = otherwise }
		return val instanceof  GString ? val : GString.EMPTY + val;
	}

	public static class Urls {
		def String base = findProperty ("MAVEN_BASE_URL", GString.EMPTY + "http://localhost:8081/repository")
		def GString repo = findProperty ("MAVEN_PUBLIC_URL", "${-> base}/public")
		def GString release = findProperty ("MAVEN_RELEASE_URL", "${-> base}/maven-releases")
		def GString dirties = findProperty ("MAVEN_DIRTIES_URLt ", "${-> base}/dirties")
	}

	public static class Credentials {
		def String username = findProperty ("MAVEN_USER", GString.EMPTY + "gradle")
		def String password = findProperty ("MAVEN_PASS", GString.EMPTY + "gradle")
	}

	def Urls urls = new Urls ()
	def Credentials credentials = new Credentials ()
	def String vendor = findProperty ("PROJECT_VENDOR", GString.EMPTY + "Tutske Inc.")

}
