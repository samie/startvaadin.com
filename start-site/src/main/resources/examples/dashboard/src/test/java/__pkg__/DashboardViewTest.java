package {{package}};

import java.util.List;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.BrowserlessUIContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.NativeLabel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DashboardViewTest {

	@Autowired
	private ApplicationContext applicationContext;

	private BrowserlessApplicationContext app;

	private BrowserlessUIContext window;

	@BeforeEach
	void openWindow() {
		this.app = SpringBrowserlessApplicationContext.create(this.applicationContext, DashboardView.class);
		this.window = this.app.newUser().newWindow();
	}

	@AfterEach
	void close() {
		this.app.close();
	}

	@Test
	void userSeesTheKeyFigures() {
		List<String> figures = this.window.findCard().components().stream().map(Card::getTitleAsText).toList();

		assertEquals(List.of("1,204", "357", "$12.8k"), figures);
	}

	@Test
	void userSeesHowMuchOfEachResourceIsInUse() {
		assertEquals("42%", usageOf("CPU"));
		assertEquals("67%", usageOf("Memory"));
		assertEquals("23%", usageOf("Disk"));
	}

	private String usageOf(String resource) {
		NativeLabel label = this.window.findNativeLabel().withText(resource).component();
		return this.window.findSpan().inside(label.getParent().orElseThrow()).getText();
	}

}
