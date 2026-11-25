package {{package}};

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.theme.aura.Aura;

// The theme first, then the app's own styles in src/main/resources/META-INF/resources
@ColorScheme(ColorScheme.Value.LIGHT_DARK)
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
public class AppShell implements AppShellConfigurator {

}
