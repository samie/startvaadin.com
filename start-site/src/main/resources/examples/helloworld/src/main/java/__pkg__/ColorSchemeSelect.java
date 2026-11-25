package {{package}};

import java.util.Map;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.select.Select;

/**
 * Lets the user pick light or dark, or follow the system. The choice applies to this
 * browser tab; a reload returns to the system setting that AppShell asks for.
 */
public class ColorSchemeSelect extends Select<ColorScheme.Value> {

	private static final Map<ColorScheme.Value, String> LABELS = Map.of(ColorScheme.Value.LIGHT_DARK, "System",
			ColorScheme.Value.LIGHT, "Light", ColorScheme.Value.DARK, "Dark");

	public ColorSchemeSelect() {
		setLabel("Appearance");
		setItems(ColorScheme.Value.LIGHT_DARK, ColorScheme.Value.LIGHT, ColorScheme.Value.DARK);
		setItemLabelGenerator(LABELS::get);
		addValueChangeListener((event) -> getUI().ifPresent((ui) -> ui.getPage().setColorScheme(event.getValue())));
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		ColorScheme.Value current = attachEvent.getUI().getPage().getColorScheme();
		setValue(LABELS.containsKey(current) ? current : ColorScheme.Value.LIGHT_DARK);
	}

}
