package {{package}};

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.aura.Aura;

// Vaadin generates all icon sizes from the one 512 x 512 image in META-INF/resources
@PWA(name = "{{name}}", shortName = "{{name}}", description = "{{description}}", iconPath = "icons/icon.png")
@ColorScheme(ColorScheme.Value.LIGHT_DARK)
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
public class AppShell implements AppShellConfigurator {

}
