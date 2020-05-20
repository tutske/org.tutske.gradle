package org.tutske.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar


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

		project.extensions.create ('tg', Config, project)

		project.version = 'git describe --dirty'.execute ().text.trim ()

		project.dependencyLocking { lockAllConfigurations () }
		project.repositories { maven { url "${->project.tg.nexus.base.url}" } }

		addCopyDepsTask ()
		setupArtifacts ()
		setupJacoco ()
	}

	void addCopyDepsTask () {
		project.task ('copyDeps', type: Copy) {
			from project.configurations["${-> project.tg.depsConfiguration}"]
			into "${project.projectDir}${->project.tg.dirs.deps}"
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
					credentials {
						username "${-> repo.username}"
						password "${-> repo.password}"
					}
				}
			}
		}
	}

	void setupJacoco () {
		project.jacocoTestReport {
			reports {
				xml.enabled false
				csv.enabled false
				html.destination project.file ("${project.buildDir}/${project.tg.dirs.coverage}")
			}
		}
	}

}
