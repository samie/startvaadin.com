package {{package}};

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

	public MainLayout() {
		addToNavbar(new DrawerToggle(), new H1("Mobile App"));
		Tabs tabs = new Tabs(tab("Home", HomeView.class), tab("Profile", ProfileView.class));
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		addToDrawer(tabs);
	}

	private Tab tab(String label, Class<? extends Component> target) {
		return new Tab(new RouterLink(label, target));
	}

}
