/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.spring.start.site.ui.examples.Example;

/**
 * Gallery of selectable UI examples shown below the project form. Single-select: clicking
 * a card selects it, clicking the selected card clears the selection. Each example is a
 * Vaadin {@link Card}; selection and layout live in {@code styles/app.css} keyed off the
 * {@code example-card*} classes.
 *
 * @author Sami Ekblad
 */
public class ExamplesSection extends VerticalLayout {

	private final InitializrFormModel model;

	private final Consumer<Example> onSelect;

	private final Map<String, Card> cards = new LinkedHashMap<>();

	public ExamplesSection(InitializrUiService service, InitializrFormModel model, Consumer<Example> onSelect) {
		this.model = model;
		this.onSelect = onSelect;
		setPadding(false);
		setSpacing(true);
		setWidthFull();
		add(new H2("Starting point"));
		FlexLayout grid = new FlexLayout();
		grid.addClassName("example-cards");
		for (Example example : service.getExamples()) {
			Card card = card(example);
			this.cards.put(example.id(), card);
			grid.add(card);
		}
		add(grid);
		select(model.getExampleId());
	}

	public void select(String exampleId) {
		this.cards.forEach(
				(id, card) -> card.getElement().setAttribute("aria-pressed", String.valueOf(id.equals(exampleId))));
	}

	private Card card(Example example) {
		Image shot = new Image(example.screenshotUrl(), example.title());
		shot.addClassName("example-card-image");
		Card card = new Card();
		card.addClassName("example-card");
		card.setMedia(shot);
		card.setTitle(example.title());
		card.setSubtitle(example.description());
		card.setAriaRole("button");
		card.getElement().setAttribute("tabindex", "0");
		card.getElement().addEventListener("click", (event) -> choose(example));
		return card;
	}

	private void choose(Example example) {
		boolean selected = example.id().equals(this.model.getExampleId());
		this.onSelect.accept(selected ? null : example);
	}

}
