package org.tutske.gradle


class Config {

	public static class Urls {
		def String base = "http://localhost:8081/repository"
		def GString repo = "${->base}/public"
		def GString release = "${->base}/maven-releases"
		def GString dirties = "${->base}/dirties"
	}

	public static class Credentials {
		def String username = "gradle"
		def String password = "gradle"
	}

	def Urls urls = new Urls ()
	def Credentials credentials = new Credentials ()
	def String vendor = "Tutske Inc."

}
