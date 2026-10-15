package {{package}};

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleData {

	@Bean
	CommandLineRunner seedContacts(ContactRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.save(contact("John", "Doe", "john@example.com"));
				repository.save(contact("Jane", "Roe", "jane@example.com"));
			}
		};
	}

	private static Contact contact(String firstName, String lastName, String email) {
		Contact contact = new Contact();
		contact.setFirstName(firstName);
		contact.setLastName(lastName);
		contact.setEmail(email);
		return contact;
	}

}
