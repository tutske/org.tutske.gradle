package org.tutske.gradle

import org.gradle.api.Project


class Config {

	public class Urls {
		def String base = "${-> findProperty ('repo.base', 'http://localhost:8081/repository')}"
		def GString repo = "${-> findProperty ('repo.public', base + '/maven-public')}"
		def GString release = "${-> findProperty ('repo.releases', base + 'maven-releases')}"
		def GString dirties = "${-> findProperty ('repo.snapshots', base + '/maven-snapshots')}"
	}

	public class Credentials {
		def String username = "${-> findProperty ('repo.username', 'gradle')}"
		def String password = "${-> findProperty ('repo.password', 'gradle')}"
	}

	public class Dirs {
		def String docs = "${-> findProperty ('dirs.docs', '/src/main/docs')}"
		def String deps = "${-> findProperty ('dirs.deps', '/build/libs')}"
	}

	private final Project project
	private final Properties properties

	def Urls urls
	def Credentials credentials
	def Dirs dirs;
	def String vendor

	Config (Project project) {
		this.project = project

		properties = PropertiesInitializer.collectProperties ();

		urls = new Urls ()
		credentials = new Credentials ()
		dirs = new Dirs ()
		vendor = findProperty ("vendor", GString.EMPTY + "UNKNOWN")
	}

	private GString findProperty (String name, String otherwise) {
		def val = System.getenv (PropertiesInitializer.PREFIX + name)
		if ( val == null ) { val = properties.getProperty (name) }
		if ( val == null ) { val = project.getProperties ().get (name) }
		if ( val == null ) { val = otherwise }
		return val instanceof GString ? val : GString.EMPTY + val
	}


}
