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

	public class Tools {
		def String jacocoVersion
	}

	private final Project project
	private final Properties properties

	def Nexus nexus;
	def Dirs dirs;
	def String vendor
	def String depsConfiguration
	def Tools tools

	Config (Project project) {
		this.project = project

		properties = PropertiesInitializer.collectProperties ()

		nexus = new Nexus ()
		dirs = new Dirs ()
		tools = new Tools ()

		vendor = findProperty ('vendor', GString.EMPTY + "UNKNOWN")
		depsConfiguration = findProperty ('depsConfiguration', 'runtimeClasspath')

		dirs.docs = "${-> stripTrailingSlash (findProperty ('dirs.docs', '/src/main/docs'))}"
		dirs.deps = "${-> stripTrailingSlash (findProperty ('dirs.deps', '/build/libs'))}"
		dirs.coverage = "${-> stripTrailingSlash (findProperty ('dirs.coverage', '/reports/coverage'))}"

		tools.jacocoVersion = "${-> findProperty ("tools.jacoco.version", "")}"

		nexus.location = "${-> stripTrailingSlash (findProperty ('nexus.url', 'https://nexus.tutske.org:10443/repository'))}"
		nexus.username = "${-> findProperty ('nexus.username', 'builder')}"
		nexus.password = "${-> findProperty ('nexus.password', GString.EMPTY)}"

		nexus.base.location = "${-> stripTrailingSlash (findProperty ('nexus.base.location', nexus.location))}"
		nexus.base.repo = "${-> findProperty ('nexus.base.repo', 'maven-develop')}"
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
		nexus.snapshots.repo = "${-> findProperty ('nexus.snapshots.repo', 'maven-dirties')}"
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

	public void display (boolean showPassword) {
		println "                  vendor: ${vendor}"
		println "       depsConfiguration: ${depsConfiguration}"
		println "               dirs.docs: ${dirs.docs}"
		println "               dirs.deps: ${dirs.deps}"
		println "           dirs.coverage: ${dirs.coverage}"
		println "    tools.jacoco.version: ${tools.jacocoVersion}"

		println ''

		println "          nexus.location: ${nexus.location}"
		println "          nexus.username: ${nexus.username}"
		println "          nexus.password: ${nexus.password.isEmpty () ? "" : showPassword ? nexus.password : '********'}"

		println ''

		println "     nexus.base.location: ${nexus.base.location}"
		println "         nexus.base.repo: ${nexus.base.repo}"
		println "     nexus.base.username: ${nexus.base.username}"
		println "     nexus.base.password: ${nexus.base.password.isEmpty () ? "" : showPassword ? nexus.base.password : '********'}"
		println "          nexus.base.url: ${nexus.base.url}"

		println ''

		println "   nexus.deploy.location: ${nexus.deploy.location}"
		println "       nexus.deploy.repo: ${nexus.deploy.repo}"
		println "   nexus.deploy.username: ${nexus.deploy.username}"
		println "   nexus.deploy.password: ${nexus.deploy.password.isEmpty () ? "" : showPassword ? nexus.deploy.password : '********'}"
		println "        nexus.deploy.url: ${nexus.deploy.url}"

		println ''

		println "    nexus.betas.location: ${nexus.betas.location}"
		println "        nexus.betas.repo: ${nexus.betas.repo}"
		println "    nexus.betas.username: ${nexus.betas.username}"
		println "    nexus.betas.password: ${nexus.betas.password.isEmpty () ? "" : showPassword ? nexus.betas.password : '********'}"
		println "         nexus.betas.url: ${nexus.betas.url}"

		println ''

		println "nexus.snapshots.location: ${nexus.snapshots.location}"
		println "    nexus.snapshots.repo: ${nexus.snapshots.repo}"
		println "nexus.snapshots.username: ${nexus.snapshots.username}"
		println "nexus.snapshots.password: ${nexus.snapshots.password.isEmpty () ? "" : showPassword ? nexus.snapshots.password : '********'}"
		println "     nexus.snapshots.url: ${nexus.snapshots.url}"
	}

}
