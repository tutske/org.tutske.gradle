package org.tutske.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test

class StructurePluginImpl implements Plugin<Project> {

	private Project project

	public StructurePluginImpl (Project project) {
		this.project = project
	}

	void apply () {
		project.apply (plugin: 'java')
		project.apply (plugin: 'maven')
		project.extensions.create ('structure', Config)

		project.version = 'git describe --dirty'.execute ().text.trim ()
		project.sourceCompatibility = 1.8

		setRepositories ()
		setUploadConfig ()
		addJarInfo ()
		addCopyDepsTask ()
		addIntegration ()
	}

	void apply (Project project) {
		this.project = project
		this.apply ()
	}

	void setRepositories () {
		project.repositories {
			maven { url "${->project.structure.urls.repo}" }
			mavenLocal ()
		}
	}

	void setUploadConfig () {
		project.uploadArchives {
			doFirst {
				def isDirty = project.version.endsWith ('-dirty')
				def url = isDirty ? "${->project.structure.urls.dirties}" : "${->project.structure.urls.release}"
				repositories.mavenDeployer {
					repository (url: url) {
						authentication (
							userName: "${->project.structure.credentials.username}",
							password: "${->project.structure.credentials.password}"
						)
					}
				}
			}
		}
	}

	void addJarInfo () {
		project.jar {
			doFirst {
				manifest.attributes (
					'Implementation-Title': project.rootProject.name,
					'Implementation-Version': project.version,
					'Implementation-Vendor': "${->project.structure.vendor}"
				)
			}
		}
	}

	void addCopyDepsTask () {
		project.task ('copyDeps', type: Copy) {
			from project.configurations.runtime
			into "${project.buildDir}/libs"
		}
	}

	void addIntegration () {
		project.sourceSets {
			integration {
				compileClasspath += main.output + test.output
				runtimeClasspath += main.output + test.output
				java {
					srcDir (project.file ('src/it/java'))
				}
				resources {
					srcDir (project.file ('src/it/resources'))
				}
			}
		}

		project.configurations {
			integrationCompile.extendsFrom testCompile
			integrationRuntime.extendsFrom testRuntime
		}

		project.task ('itJar', type: Jar) {
			from project.sourceSets.integration.output
			archiveName ("it-${project.name}${project.version?'-':''}${project.version}.jar")
		}

		project.task ('copyItDeps', type: Copy) {
			from project.configurations.integrationRuntime
			into "${project.buildDir}/libs"
		}

		project.task ('it', type: Test, dependsOn: ['assemble', 'itJar', 'copyDeps', 'copyItDeps'] ) {
			testClassesDir = project.sourceSets.integration.output.classesDir
			classpath = project.sourceSets.integration.runtimeClasspath
			environment 'TEST_JAR_PATH', project.itJar.archivePath
			onOutput { descriptor, event -> print (event.message) }
			reports.html.destination = "${project.buildDir}/integration-report"
			outputs.upToDateWhen { false }
		}
	}

}
