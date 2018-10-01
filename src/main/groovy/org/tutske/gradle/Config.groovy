package org.tutske.gradle


class Config {

	private static final Properties properties = PropertiesInitializer.collectProperties ();

	private static GString findProperty (String name, GString otherwise) {
		def val = System.getenv (PropertiesInitializer.PREFIX + name)
		if ( val == null ) { val = properties.getProperty (name) }
		if ( val == null ) { val = otherwise }
		return val instanceof  GString ? val : GString.EMPTY + val;
	}

	public static class Urls {
		def String base = findProperty ("MAVEN_BASE_URL", GString.EMPTY + "http://localhost:8081/repository")
		def GString repo = findProperty ("MAVEN_PUBLIC_URL", "${-> base}/maven-public")
		def GString release = findProperty ("MAVEN_RELEASE_URL", "${-> base}/maven-releases")
		def GString dirties = findProperty ("MAVEN_DIRTIES_URLt ", "${-> base}/maven-snapshots")
	}

	public static class Credentials {
		def String username = findProperty ("MAVEN_USER", GString.EMPTY + "gradle")
		def String password = findProperty ("MAVEN_PASS", GString.EMPTY + "gradle")
	}

	def Urls urls = new Urls ()
	def Credentials credentials = new Credentials ()
	def String vendor = findProperty ("PROJECT_VENDOR", GString.EMPTY + "Tutske Inc.")

}
