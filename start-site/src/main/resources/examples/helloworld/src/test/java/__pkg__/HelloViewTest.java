package {{package}};

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.BrowserlessUIContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HelloViewTest {

	@Autowired
	private ApplicationContext applicationContext;

	private BrowserlessApplicationContext app;

	private BrowserlessUIContext window;

	@BeforeEach
	void openWindow() {
		this.app = SpringBrowserlessApplicationContext.create(this.applicationContext, HelloView.class);
		this.window = this.app.newUser().newWindow();
	}

	@AfterEach
	void close() {
		this.app.close();
	}

	@Test
	void userIsGreetedByName() {
		this.window.findTextField().withLabel("Your name").setValue("Ada");
		this.window.findButton().withText("Say hello").click();

		assertEquals("Hello, Ada", this.window.findNotification().getText());
	}

}
