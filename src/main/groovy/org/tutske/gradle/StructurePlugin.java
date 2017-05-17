package org.tutske.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


class StructurePlugin implements Plugin<Project> {

	@Override
	public void apply (Project project) {
		new StructurePluginImpl (project).apply ();
	}

}