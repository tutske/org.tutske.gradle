package org.tutske.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar


class TgPluginImpl implements Plugin<Project> {

	private Project project

	public TgPluginImpl (Project project) {
		this.project = project
	}

	void apply (Project project) {
		this.project = project
		this.apply ()
	}

	void apply () {
		project.apply (plugin: 'java')
		project.apply (plugin: 'maven-publish')

		project.extensions.create ('tg', Config, project)

		project.version = 'git describe --dirty'.execute ().text.trim ()

		project.dependencyLocking { lockAllConfigurations () }
		project.repositories { maven { url "${->project.tg.urls.repo}" } }

		addCopyDepsTask ()
		setupArtifacts ()
	}

	void addCopyDepsTask () {
		project.task ('copyDeps', type: Copy) {
			from project.configurations.runtime
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
				sources (MavenPublication) {
					artifact project.jar
					artifact project.sourcesJar
					artifact project.documentationJar
				}
			}

			repositories {
				def isDirty = project.version.endsWith ('-dirty')
				maven {
					url isDirty ? "${->project.tg.urls.dirties}" : "${->project.tg.urls.release}"
					credentials {
						username "${->project.tg.credentials.username}"
						password "${->project.tg.credentials.password}"
					}
				}
			}
		}

	}

}
