package {{package}};

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLink;

// Makes a tapped menu item look current at once, before the server answers. A static
// file, so loading it needs no frontend build.
@JavaScript("context://nav-pending.js")
public class MainLayout extends AppLayout {

	public MainLayout() {
		// Wide screens: the app's icon and name above a side menu in the drawer
		SideNav menu = new SideNav();
		menu.addItem(new SideNavItem("Home", HomeView.class, VaadinIcon.HOME.create()),
				new SideNavItem("Profile", ProfileView.class, VaadinIcon.USER.create()));
		addToDrawer(brand(), new Scroller(menu));
		setPrimarySection(Section.DRAWER);

		// Phones: the same destinations as a tab bar at the bottom, within thumb reach
		RouterLink home = tab("Home", VaadinIcon.HOME, HomeView.class);
		home.setHighlightCondition((link, event) -> {
			String path = event.getLocation().getPath();
			return path.isEmpty() || path.startsWith("item/");
		});
		Nav tabBar = new Nav(home, tab("Profile", VaadinIcon.USER, ProfileView.class));
		tabBar.addClassName("tab-bar");
		addToNavbar(true, tabBar);
	}

	// The same icon as the installed app (AppShell's @PWA); the name says what it shows
	private Header brand() {
		Header brand = new Header(new Image("icons/icon.png", ""), new Span("{{name}}"));
		brand.addClassName("drawer-brand");
		return brand;
	}

	// Icon only; the label is for screen readers
	private RouterLink tab(String label, VaadinIcon icon, Class<? extends Component> target) {
		RouterLink link = new RouterLink(target);
		link.add(icon.create());
		link.getElement().setAttribute("aria-label", label);
		return link;
	}

}
