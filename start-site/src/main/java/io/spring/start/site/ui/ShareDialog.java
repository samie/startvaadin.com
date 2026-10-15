/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Read-only display of the current form state as a shareable URL plus a one-click
 * clipboard copy. Mirrors the React Share popover.
 *
 * @author Vaadin UI Migration
 */
public class ShareDialog extends Dialog {

	private final TextField urlField = new TextField();

	public ShareDialog(InitializrFormModel model) {
		setHeaderTitle("Share");
		setWidth("32rem");
		String fragment = ShareUrlCodec.encode(model);
		this.urlField.setWidthFull();
		this.urlField.setReadOnly(true);
		this.urlField.setValue(fragment);
		Paragraph hint = new Paragraph("Share this URL — anyone opening it will land on this form pre-populated.");
		hint.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
		VerticalLayout body = new VerticalLayout(hint, this.urlField);
		body.setPadding(false);
		body.setSpacing(true);
		add(body);
		Button copy = new Button("Copy", (event) -> copyToClipboard());
		copy.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		Button close = new Button("Close", (event) -> close());
		getFooter().add(close, copy);
		addOpenedChangeListener((event) -> {
			if (event.isOpened()) {
				resolveFullUrl(fragment);
			}
		});
	}

	private void resolveFullUrl(String fragment) {
		UI ui = UI.getCurrent();
		// Share URLs point at the site root (origin + "/"); the recipient lands on the
		// generator with the form populated from the "#!" fragment.
		ui.getPage()
			.executeJs("return window.location.origin + '/';")
			.then(String.class, (origin) -> this.urlField.setValue(origin + fragment));
	}

	private void copyToClipboard() {
		UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", this.urlField.getValue());
		Notification.show("Copied!");
	}

}
