package org.tutske.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test


class TgPlugin implements Plugin<Project> {

	private Project project

	void apply (Project project) {
		this.project = project
		this.apply ()
	}

	void apply () {
		project.apply (plugin: 'java-library')
		project.apply (plugin: 'maven-publish')
		project.apply (plugin: 'jacoco')
		project.apply (plugin: 'idea')

		project.extensions.create ('tg', Config, project)

		project.version = 'git describe --dirty'.execute ().text.trim ()

		project.dependencyLocking {
			lockFile = project.file("${project.projectDir}/gradle/dependencies.lockfile")
			lockAllConfigurations ()
		}

		addCopyDepsTask ()
		addSettingsTask ()
		setupRepository ()
		setupArtifacts ()
		setupJacoco ()
		setupItTests ()
		useJUnitplatorm ()
	}

	void setupRepository () {
		project.repositories {
			maven {
				url "${->project.tg.nexus.base.url}"
				if ( ! project.tg.nexus.base.password.isEmpty () ) {
					credentials {
						username "${-> project.tg.nexus.base.username}"
						password "${-> project.tg.nexus.base.password}"
					}
				}
			}
		}
	}

	void setupArtifacts () {
		project.task ('sourcesJar', type: Jar) {
			from project.sourceSets.main.java.srcDirs
			archiveClassifier = 'sources'
		}

		project.task ('documentationJar', type: Jar) {
			from "${project.projectDir}${->project.tg.dirs.docs}"
			archiveClassifier = 'documentation'
		}

		[ project.jar, project.sourcesJar, project.documentationJar ].each {
			it.doFirst {
				manifest.attributes (
					'Implementation-Title': project.name,
					'Implementation-Version': project.version,
					'Implementation-Vendor': "${->project.tg.vendor}"
				)
			}
		}

		project.publishing {
			publications {
				mavenJava (MavenPublication) {
					from project.components.java
					artifact project.sourcesJar
					artifact project.documentationJar
				}
			}

			repositories {
				def repo = (
					project.version =~ /.*-dirty$/ ? project.tg.nexus.snapshots :
					project.version =~ /.*(-pre)?-g[A-Fa-f0-9]{7,}/ ? project.tg.nexus.betas :
					project.tg.nexus.deploy
				);
				maven {
					url "${-> repo.url}"
					if ( ! repo.password.isEmpty () ) {
						credentials {
							username "${-> repo.username}"
							password "${-> repo.password}"
						}
					}
				}
			}
		}
	}

	void setupJacoco () {
		if ( ! project.tg.tools.jacocoVersion.isEmpty () ) {
			project.jacoco {
				toolVersion = project.tg.tools.jacocoVersion
			}
		}
		project.jacocoTestReport {
			reports {
				csv.required = false
				html.outputLocation = project.file ("${project.buildDir}/${project.tg.dirs.coverage}")
			}
		}
	}

	void setupItTests () {
		project.sourceSets {
			testIt
		}

		project.tasks.register ('it', Test) {
			description = 'Runs integration tests.'
			group = 'verification'

			testClassesDirs = project.sourceSets.testIt.output.classesDirs
			classpath = project.sourceSets.testIt.runtimeClasspath
			shouldRunAfter project.test
			dependsOn project.assemble

			useJUnitPlatform ()

			systemProperty ('junit.jupiter.extensions.autodetection.enabled', true)
		}

		project.check.dependsOn 'it'

		project.dependencies {
			testItRuntimeOnly ([ group: 'org.junit.platform', name: 'junit-platform-launcher', version: '[1,)' ])
			testItImplementation (
				[ group: 'org.hamcrest', name: 'hamcrest', version: '[2,)' ],
				[ group: 'org.junit.jupiter', name: 'junit-jupiter', version: '[5,)' ]
			)
		}

		project.idea {
			module {
				testSourceDirs += project.sourceSets.testIt.java.srcDirs
				testResourceDirs += project.sourceSets.testIt.resources.srcDirs
			}
		}
	}

	void useJUnitplatorm () {
		project.dependencies {
			testRuntimeOnly ([ group: 'org.junit.platform', name: 'junit-platform-launcher', version: '[1,)' ])
			testImplementation (
				[ group: 'org.hamcrest', name: 'hamcrest', version: '[2,)' ],
				[ group: 'org.junit.jupiter', name: 'junit-jupiter', version: '[5,)' ],
				[ group: 'org.mockito', name: 'mockito-core', version: '[2,)' ]
			)
		}

		project.test {
			useJUnitPlatform ()
		}
	}

	void addCopyDepsTask () {
		project.task ('copyDeps', type: Copy) {
			from project.configurations["${-> project.tg.depsConfiguration}"]
			into "${project.projectDir}${->project.tg.dirs.deps}"
		}
	}

	void addSettingsTask () {
		project.task ('settings') {
			doLast {
				def password = project.getProperties ().get ("showPasswords")
				project.tg.display ('yes' == password || 'true' == password);
			}
		}
	}

}
