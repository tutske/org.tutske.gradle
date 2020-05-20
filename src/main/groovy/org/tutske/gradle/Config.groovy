package org.tutske.gradle

import org.gradle.api.Project


class Config {

	public class Repo {
		def String location
		def String repo
		def String username
		def String password
		def String url
	}

	public class Nexus {
		def String location
		def String username
		def String password
		def Repo base = new Repo ()
		def Repo deploy = new Repo ()
		def Repo betas = new Repo ()
		def Repo snapshots = new Repo ()
	}

	public class Dirs {
		def String docs
		def String deps
		def String coverage
	}

	private final Project project
	private final Properties properties

	def Nexus nexus;
	def Dirs dirs;
	def String vendor
	def String depsConfiguration

	Config (Project project) {
		this.project = project

		properties = PropertiesInitializer.collectProperties ()

		nexus = new Nexus ()
		dirs = new Dirs ()

		vendor = findProperty ('vendor', GString.EMPTY + "UNKNOWN")
		depsConfiguration = findProperty ('depsConfiguration', 'runtimeClasspath')

		dirs.docs = "${-> stripTrailingSlash (findProperty ('dirs.docs', '/src/main/docs'))}"
		dirs.deps = "${-> stripTrailingSlash (findProperty ('dirs.deps', '/build/libs'))}"
		dirs.coverage = "${-> stripTrailingSlash (findProperty ('dirs.coverage', '/reports/coverage'))}"

		nexus.location = "${-> stripTrailingSlash (findProperty ('nexus.url', 'http://nexus.tutske.org:10080/repository'))}"
		nexus.username = "${-> findProperty ('nexus.username', 'builder')}"
		nexus.password = "${-> findProperty ('nexus.password', GString.EMPTY)}"

		nexus.base.location = "${-> stripTrailingSlash (findProperty ('nexus.base.location', nexus.location))}"
		nexus.base.repo = "${-> findProperty ('nexus.base.repo', 'maven-public')}"
		nexus.base.username = "${-> findProperty ('nexus.base.username', nexus.username)}"
		nexus.base.password = "${-> findProperty ('nexus.base.password', GString.EMPTY)}"
		nexus.base.url = "${-> findProperty ('nexus.base.url', nexus.base.location + '/' + nexus.base.repo)}"

		nexus.deploy.location = "${-> stripTrailingSlash (findProperty ('nexus.deploy.location', nexus.location))}"
		nexus.deploy.repo = "${-> findProperty ('nexus.deploy.repo', 'maven-releases')}"
		nexus.deploy.username = "${-> findProperty ('nexus.deploy.username', nexus.username)}"
		nexus.deploy.password = "${-> findProperty ('nexus.deploy.password', nexus.password)}"
		nexus.deploy.url = "${-> findProperty ('nexus.deploy.url', nexus.deploy.location + '/' + nexus.deploy.repo)}"

		nexus.betas.location = "${-> stripTrailingSlash (findProperty ('nexus.betas.location', nexus.location))}"
		nexus.betas.repo = "${-> findProperty ('nexus.betas.repo', 'maven-pre-releases')}"
		nexus.betas.username = "${-> findProperty ('nexus.betas.username', nexus.username)}"
		nexus.betas.password = "${-> findProperty ('nexus.betas.password', nexus.password)}"
		nexus.betas.url = "${-> findProperty ('nexus.betas.url', nexus.betas.location + '/' + nexus.betas.repo)}"

		nexus.snapshots.location = "${-> stripTrailingSlash (findProperty ('nexus.snapshots.location', nexus.location))}"
		nexus.snapshots.repo = "${-> findProperty ('nexus.snapshots.repo', 'maven-public')}"
		nexus.snapshots.username = "${-> findProperty ('nexus.snapshots.username', nexus.username)}"
		nexus.snapshots.password = "${-> findProperty ('nexus.snapshots.password', nexus.password)}"
		nexus.snapshots.url = "${-> findProperty ('nexus.snapshots.url', nexus.snapshots.location + '/' + nexus.snapshots.repo)}"
	}

	public GString findProperty (String name, String otherwise) {
		def val = System.getenv (PropertiesInitializer.PREFIX + name)
		if ( val == null ) { val = properties.getProperty (name) }
		if ( val == null ) { val = project.getProperties ().get (name) }
		if ( val == null ) { val = otherwise }
		return val instanceof GString ? val : GString.EMPTY + val
	}

	public static String stripTrailingSlash (String url) {
		return url.endsWith ("/") ? url.substring (0, url.length () - 1) : url
	}

}
