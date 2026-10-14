/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Configures the Vaadin application shell and loads the Aura theme.
 *
 * @author Vaadin UI Migration
 */
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("context://styles/app.css")
public class AppShell implements AppShellConfigurator {

}
