/*
 * Copyright 2012 - present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
