package {{package}};

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.BrowserlessUIContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
class MobileAppTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ItemService items;

	private BrowserlessApplicationContext app;

	private BrowserlessUIContext window;

	@BeforeEach
	void openWindow() {
		this.app = SpringBrowserlessApplicationContext.create(this.applicationContext, MainLayout.class);
		this.window = this.app.newUser().newWindow();
	}

	@AfterEach
	void close() {
		this.app.close();
	}

	@Test
	void userStartsOnHomeWithACardForEveryFeature() {
		assertInstanceOf(HomeView.class, this.window.getCurrentView());
		assertEquals(this.items.findAll().size(), this.window.findCard().components().size());
	}

	@Test
	void userSpotsFeaturesByTheirIcons() {
		assertEquals(iconName(VaadinIcon.BELL), iconOnCard("Push notifications"));
		assertEquals(iconName(VaadinIcon.ADJUST), iconOnCard("Light and dark"));
	}

	@Test
	void userOpensAFeatureToReadAboutIt() {
		this.window.navigate("item/drill-down", ItemView.class);

		assertEquals("Drill-down view", this.window.findH1().getText());
		assertEquals(iconName(VaadinIcon.LEVEL_DOWN), iconByTitle());
	}

	@Test
	void userSavesTheirProfile() {
		this.window.findSideNav().clickItem("Profile");
		this.window.findTextField().withLabel("Name").setValue("Ada");
		this.window.findButton().withText("Save").click();

		assertEquals("Profile saved", this.window.findNotification().getText());
	}

	private String iconOnCard(String title) {
		Icon icon = (Icon) this.window.findCard()
			.withCondition((card) -> title.equals(card.getTitleAsText()))
			.getHeaderPrefix();
		return icon.getIcon();
	}

	private String iconByTitle() {
		return this.window.find(Icon.class, this.window.findH1().component()).single().getIcon();
	}

	private static String iconName(VaadinIcon icon) {
		return icon.create().getIcon();
	}

}
