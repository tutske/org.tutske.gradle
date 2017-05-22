package org.tutske.gradle


class Config {

	public static class Urls {
		def String base = fromEnv ("G_MAVEN_BASE_URL", GString.EMPTY + "http://localhost:8081/repository")
		def GString repo = fromEnv ("G_MAVEN_PUBLIC_URL", "${->base}/public")
		def GString release = fromEnv ("G_MAVEN_RELEASE_URL", "${->base}/maven-releases")
		def GString dirties = fromEnv ("G_MAVEN_DIRTIES_URLt ", "${->base}/dirties")

		private static GString fromEnv (String name, GString otherwise) {
			return otherwise;
		}
	}

	public static class Credentials {
		def String username = "gradle"
		def String password = "gradle"
	}

	def Urls urls = new Urls ()
	def Credentials credentials = new Credentials ()
	def String vendor = "Tutske Inc."

}
