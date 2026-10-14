/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.spring.initializr.metadata.DefaultMetadataElement;

/**
 * Left column: bound to {@link InitializrFormModel} via {@link Binder}. Mirrors the React
 * form: project / language / boot version / metadata fields.
 *
 * @author Vaadin UI Migration
 */
public class ProjectFormSection extends VerticalLayout {

	private final InitializrUiService service;

	private final InitializrFormModel model;

	private final Binder<InitializrFormModel> binder;

	private final RadioButtonGroup<String> bootVersion;

	private Runnable bootVersionChangeListener = () -> {
	};

	private boolean packageNameDerivedFromCoordinates = true;

	public ProjectFormSection(InitializrUiService service, InitializrFormModel model,
			Binder<InitializrFormModel> binder) {
		this.service = service;
		this.model = model;
		this.binder = binder;
		setPadding(false);
		setSpacing(true);
		setWidthFull();

		RadioButtonGroup<String> type = radioGroup("Project", this.service.getTypes());
		RadioButtonGroup<String> language = radioGroup("Language", this.service.getLanguages());
		this.bootVersion = radioGroup("Spring Boot", this.service.getBootVersions());
		// Technical/code-like values render monospace (a dev-tool cue; see the .mono
		// utility in styles/app.css). Version numbers qualify.
		this.bootVersion.addClassName("mono");
		add(responsiveForm(type, language, this.bootVersion));

		add(new H2("Project Metadata"));
		TextField group = new TextField("Group");
		TextField artifact = new TextField("Artifact");
		TextField name = new TextField("Name");
		TextField description = new TextField("Description");
		TextField packageName = new TextField("Package name");
		// Coordinates are code identifiers → monospace. Name/Description are prose.
		group.addClassName("mono");
		artifact.addClassName("mono");
		packageName.addClassName("mono");
		FormLayout metadata = new FormLayout(group, artifact, name, description, packageName);
		metadata.setWidthFull();
		add(metadata);

		RadioButtonGroup<String> packaging = radioGroup("Packaging", this.service.getPackagings());
		RadioButtonGroup<String> javaVersion = radioGroup("Java", this.service.getJavaVersions());
		javaVersion.addClassName("mono");
		List<DefaultMetadataElement> formats = this.service.getConfigurationFileFormats();
		FormLayout options = responsiveForm(packaging, javaVersion);
		RadioButtonGroup<String> configFile = null;
		if (!formats.isEmpty()) {
			configFile = radioGroup("Config file", formats);
			options.add(configFile);
		}
		add(options);

		this.binder.forField(type).bind(InitializrFormModel::getType, InitializrFormModel::setType);
		this.binder.forField(language).bind(InitializrFormModel::getLanguage, InitializrFormModel::setLanguage);
		this.binder.forField(this.bootVersion)
			.bind(InitializrFormModel::getBootVersion, InitializrFormModel::setBootVersion);
		this.binder.forField(group).bind(InitializrFormModel::getGroupId, InitializrFormModel::setGroupId);
		this.binder.forField(artifact).bind(InitializrFormModel::getArtifactId, InitializrFormModel::setArtifactId);
		this.binder.forField(name).bind(InitializrFormModel::getName, InitializrFormModel::setName);
		this.binder.forField(description)
			.bind(InitializrFormModel::getDescription, InitializrFormModel::setDescription);
		this.binder.forField(packageName)
			.bind(InitializrFormModel::getPackageName, InitializrFormModel::setPackageName);
		this.binder.forField(packaging).bind(InitializrFormModel::getPackaging, InitializrFormModel::setPackaging);
		this.binder.forField(javaVersion)
			.bind(InitializrFormModel::getJavaVersion, InitializrFormModel::setJavaVersion);
		if (configFile != null) {
			this.binder.forField(configFile)
				.bind(InitializrFormModel::getConfigurationFileFormat, InitializrFormModel::setConfigurationFileFormat);
		}

		// Only auto-derive on real user edits; programmatic loads (default form,
		// bookmark/history via readBean) carry their own package name and must not be
		// clobbered by derivation.
		group.addValueChangeListener((event) -> {
			if (event.isFromClient()) {
				derivePackageName(group.getValue(), artifact.getValue(), packageName);
			}
		});
		artifact.addValueChangeListener((event) -> {
			if (event.isFromClient()) {
				derivePackageName(group.getValue(), artifact.getValue(), packageName);
			}
		});
		packageName.addValueChangeListener((event) -> {
			if (event.isFromClient()) {
				this.packageNameDerivedFromCoordinates = false;
			}
		});
		this.bootVersion.addValueChangeListener((event) -> this.bootVersionChangeListener.run());
	}

	/**
	 * Allow surrounding view to react to boot version changes (e.g. re-filter deps).
	 * @param listener invoked after the boot version radio group changes value.
	 */
	public void onBootVersionChange(Runnable listener) {
		this.bootVersionChangeListener = (listener != null) ? listener : () -> {
		};
	}

	private void derivePackageName(String groupId, String artifactId, TextField packageNameField) {
		if (!this.packageNameDerivedFromCoordinates) {
			return;
		}
		if (groupId == null || groupId.isBlank()) {
			return;
		}
		String safeArtifact = (artifactId != null) ? artifactId : "";
		String derived = (groupId + "." + safeArtifact).toLowerCase(Locale.ROOT)
			.replace('-', '_')
			.replaceAll("[^a-z0-9._]", "")
			.replaceAll("\\.+", ".")
			.replaceAll("^\\.|\\.$", "");
		packageNameField.setValue(derived);
		this.model.setPackageName(derived);
	}

	/**
	 * Wraps the given radio groups in a {@link FormLayout} styled like the Project
	 * Metadata block, so the groups flow two-per-row on wide screens and collapse to a
	 * single column below the default {@code 32em} breakpoint.
	 * @param groups the radio groups to lay out
	 * @return the configured form layout
	 */
	private FormLayout responsiveForm(RadioButtonGroup<?>... groups) {
		FormLayout layout = new FormLayout(groups);
		layout.setWidthFull();
		layout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("32em", 2),
				new ResponsiveStep("48em", 3));
		return layout;
	}

	private RadioButtonGroup<String> radioGroup(String label, List<DefaultMetadataElement> elements) {
		RadioButtonGroup<String> group = new RadioButtonGroup<>();
		group.setLabel(label);
		group.setItems(elements.stream().map(DefaultMetadataElement::getId).toList());
		group.setItemLabelGenerator((id) -> elements.stream()
			.filter((e) -> e.getId().equals(id))
			.findFirst()
			.map(DefaultMetadataElement::getName)
			.orElse(id));
		return group;
	}

}
