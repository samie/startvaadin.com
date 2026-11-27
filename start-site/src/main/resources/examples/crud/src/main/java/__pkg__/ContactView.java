package {{package}};

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Sort;

@Route("")
@PageTitle("Contacts")
public class ContactView extends VerticalLayout {

	private final ContactRepository repository;

	private final Grid<Contact> grid = new Grid<>();

	private final MasterDetailLayout layout = new MasterDetailLayout();

	private final ContactEditor editor = new ContactEditor(this::save, this::delete, this::close);

	public ContactView(ContactRepository repository) {
		this.repository = repository;
		setSizeFull();

		Button add = new Button("New contact", VaadinIcon.PLUS.create(), event -> {
			this.grid.asSingleSelect().clear();
			edit(new Contact());
		});
		HorizontalLayout header = new HorizontalLayout(Alignment.CENTER, new H1("Contacts"));
		header.setWidthFull();
		header.addToEnd(new ColorSchemeSelect());

		this.grid.addColumn(contact -> contact.getFirstName() + " " + contact.getLastName())
			.setHeader("Name")
			.setComparator(Contact::getFirstName);
		this.grid.addColumn(Contact::getEmail).setHeader("Email").setComparator(Contact::getEmail);
		this.grid.setHeightFull();
		this.grid.setEmptyStateText("No contacts yet. Add one with New contact.");
		this.grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				edit(event.getValue());
			}
			else {
				this.layout.setDetail(null);
			}
		});

		Paragraph placeholder = new Paragraph("Select a contact to edit it, or add a new one.");
		placeholder.addClassName("contact-placeholder");

		this.layout.setMaster(this.grid);
		this.layout.setWidthFull();
		this.layout.setExpandMaster(true);
		this.layout.setDetailSize("26rem");
		this.layout.setOverlaySize("100%");
		this.layout.setDetailPlaceholder(placeholder);
		this.layout.addBackdropClickListener(event -> close());
		this.layout.addDetailEscapePressListener(event -> close());

		add(header, add, this.layout);
		expand(this.layout);
		refresh();
	}

	private void edit(Contact contact) {
		this.editor.edit(contact);
		this.layout.setDetail(this.editor);
	}

	private void close() {
		this.grid.asSingleSelect().clear();
		this.layout.setDetail(null);
	}

	private void save(Contact contact) {
		this.repository.save(contact);
		Notification.show("Contact saved");
		refresh();
		close();
	}

	private void delete(Contact contact) {
		this.repository.delete(contact);
		Notification.show("Contact deleted");
		refresh();
		close();
	}

	private void refresh() {
		this.grid.setItems(this.repository.findAll(Sort.by("firstName", "lastName")));
	}

}
