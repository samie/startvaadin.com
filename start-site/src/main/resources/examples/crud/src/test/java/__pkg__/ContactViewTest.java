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
class ContactViewTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ContactRepository repository;

	private BrowserlessApplicationContext app;

	private BrowserlessUIContext window;

	@BeforeEach
	void openWindow() {
		this.repository.deleteAll();
		givenContact("John", "Doe", "john@example.com");
		this.app = SpringBrowserlessApplicationContext.create(this.applicationContext, ContactView.class);
		this.window = this.app.newUser().newWindow();
	}

	@AfterEach
	void close() {
		this.app.close();
	}

	@Test
	void userAddsANewContact() {
		this.window.findTextField().withLabel("First name").setValue("Jane");
		this.window.findTextField().withLabel("Last name").setValue("Roe");
		this.window.findTextField().withLabel("Email").setValue("jane@example.com");
		this.window.findButton().withText("Save").click();

		assertEquals(2, this.window.findGrid(Contact.class).size());
		assertEquals(2, this.repository.count());
	}

	@Test
	void userChangesTheEmailOfAContact() {
		this.window.findGrid(Contact.class).select(0);
		this.window.findTextField().withLabel("Email").setValue("john.doe@example.com");
		this.window.findButton().withText("Save").click();

		assertEquals("john.doe@example.com", this.repository.findAll().get(0).getEmail());
	}

	@Test
	void userDeletesAContact() {
		this.window.findGrid(Contact.class).select(0);
		this.window.findButton().withText("Delete").click();

		assertEquals(0, this.window.findGrid(Contact.class).size());
		assertEquals(0, this.repository.count());
	}

	private void givenContact(String firstName, String lastName, String email) {
		Contact contact = new Contact();
		contact.setFirstName(firstName);
		contact.setLastName(lastName);
		contact.setEmail(email);
		this.repository.save(contact);
	}

}
