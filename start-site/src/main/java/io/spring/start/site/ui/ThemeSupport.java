/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.component.Component;

/**
 * Shared Aura light/dark handling, used by every routed view so both screens toggle the
 * scheme identically. Aura keys its dark variant off the CSS {@code color-scheme} on the
 * document root (not a Lumo theme attribute), so applying it there also covers overlays
 * (dialogs, menus, tooltips) that render outside the current view.
 *
 * @author Sami Ekblad
 */
final class ThemeSupport {

	private ThemeSupport() {
	}

	// Dark-first: a developer tool defaults to dark. Only an explicit saved "light"
	// preference opts out; unset/blank/"dark" all resolve to dark.
	static boolean isDark(String savedTheme) {
		return !"light".equalsIgnoreCase(savedTheme);
	}

	static void applyColorScheme(Component component, boolean dark) {
		component.getElement().executeJs("document.documentElement.style.colorScheme = $0;", dark ? "dark" : "light");
	}

}
