/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.spring.initializr.metadata.Dependency;

/**
 * Right column header: "Add dependencies" button + chip list of selected dependencies.
 * The picker dialog is owned by this section.
 *
 * @author Vaadin UI Migration
 */
public class DependenciesSection extends VerticalLayout {

	private final InitializrUiService service;

	private final InitializrFormModel model;

	private final VerticalLayout chipList;

	private final Paragraph emptyState;

	private final DependencyPickerDialog dialog;

	private final Button addButton;

	public DependenciesSection(InitializrUiService service, InitializrFormModel model) {
		this.service = service;
		this.model = model;
		setPadding(false);
		setSpacing(true);
		setWidthFull();
		add(new H2("Project dependencies"));
		this.addButton = new Button("Add dependencies...", new Icon(VaadinIcon.PLUS), (event) -> openPicker());
		this.addButton.addThemeVariants(ButtonVariant.PRIMARY);
		add(this.addButton);
		this.emptyState = new Paragraph("No dependency selected.");
		this.emptyState.addClassName("muted-text");
		this.chipList = new VerticalLayout();
		this.chipList.setPadding(false);
		this.chipList.setSpacing(true);
		add(this.emptyState, this.chipList);
		this.dialog = new DependencyPickerDialog(service, model, this::toggle);
		refresh();
	}

	/** Open the picker programmatically (used by the Cmd/Ctrl+B shortcut in Step 7). */
	public void openPicker() {
		this.dialog.reload();
		this.dialog.open();
	}

	/**
	 * Recompute chip list + reload picker after external changes (e.g. share-URL load).
	 */
	public void refresh() {
		this.chipList.removeAll();
		boolean empty = this.model.getDependencies().isEmpty();
		this.emptyState.setVisible(empty);
		for (String id : this.model.getDependencies()) {
			this.service.getDependency(id).ifPresent((dependency) -> {
				String invalid = this.service.incompatibilityHint(dependency, this.model.getBootVersion());
				this.chipList.add(chip(dependency, invalid));
			});
		}
		this.dialog.reload();
	}

	private void toggle(String id) {
		if (this.model.getDependencies().contains(id)) {
			this.model.getDependencies().remove(id);
		}
		else {
			this.model.getDependencies().add(id);
		}
		refresh();
	}

	private HorizontalLayout chip(Dependency dependency, String invalidMessage) {
		String id = dependency.getId();
		String name = dependency.getName();
		String description = dependency.getDescription();
		boolean invalid = (invalidMessage != null);
		SvgIcon icon = VendorIcons.icon(dependency);
		Span title = new Span(name);
		title.getStyle().set("font-weight", "600");
		Span detail = new Span(invalid ? invalidMessage : ((description != null) ? description : ""));
		detail.addClassName("dependency-detail");
		detail.setClassName("invalid", invalid);
		HorizontalLayout header = new HorizontalLayout(title);
		header.setWidthFull();
		header.setAlignItems(FlexComponent.Alignment.CENTER);
		this.service.groupName(id).map(DependencyPickerDialog::groupBadge).ifPresent(header::add);
		VerticalLayout text = new VerticalLayout(header, detail);
		text.setPadding(false);
		text.setSpacing(false);
		Button remove = new Button(new Icon(VaadinIcon.CLOSE), (event) -> toggle(id));
		remove.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
		remove.setAriaLabel("Remove " + name);
		remove.setTooltipText("Remove " + name);
		HorizontalLayout chip = new HorizontalLayout(icon, text, remove);
		chip.setWidthFull();
		chip.setAlignItems(FlexComponent.Alignment.CENTER);
		chip.addClassName("dependency-chip");
		chip.setClassName("invalid", invalid);
		text.getStyle().set("flex-grow", "1");
		return chip;
	}

}
