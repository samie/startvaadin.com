package {{package}};

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleData {

	@Bean
	CommandLineRunner seedContacts(ContactRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.saveAll(List.of(contact("Aino", "Virtanen", "aino.virtanen@example.com"),
						contact("Marcus", "Johnson", "marcus.johnson@example.com"),
						contact("Sofia", "Rossi", "sofia.rossi@example.com"),
						contact("Liam", "O'Brien", "liam.obrien@example.com"),
						contact("Priya", "Sharma", "priya.sharma@example.com"),
						contact("Lukas", "Schmidt", "lukas.schmidt@example.com"),
						contact("Emma", "Lindqvist", "emma.lindqvist@example.com"),
						contact("Kenji", "Tanaka", "kenji.tanaka@example.com"),
						contact("Olivia", "Martin", "olivia.martin@example.com"),
						contact("Mateo", "García", "mateo.garcia@example.com")));
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
