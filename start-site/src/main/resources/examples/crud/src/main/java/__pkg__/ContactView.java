package {{package}};

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.validation.constraints.NotBlank;

@Route("")
@PageTitle("Contacts")
public class ContactView extends VerticalLayout {

	private final ContactRepository repository;

	private final Grid<Contact> grid = new Grid<>(Contact.class);

	private final TextField firstName = new TextField("First name");

	private final TextField lastName = new TextField("Last name");

	private final TextField email = new TextField("Email");

	private final BeanValidationBinder<Contact> binder = new BeanValidationBinder<>(Contact.class);

	private final Button delete = new Button("Delete", event -> delete());

	private Contact editing = new Contact();

	public ContactView(ContactRepository repository) {
		this.repository = repository;
		setSizeFull();

		HorizontalLayout header = new HorizontalLayout(Alignment.CENTER, new H1("Contacts"));
		header.setWidthFull();
		header.addToEnd(new ColorSchemeSelect());

		this.grid.setColumns("firstName", "lastName", "email");
		this.grid.asSingleSelect().addValueChangeListener(event -> edit(event.getValue()));

		// @NotBlank is not among the constraints that mark a field as required by default
		this.binder.setRequiredConfigurator(
				RequiredFieldConfigurator.DEFAULT.chain((constraint, binding) -> constraint instanceof NotBlank));
		this.binder.bindInstanceFields(this);

		FormLayout form = new FormLayout();
		form.setAutoResponsive(true);
		form.setExpandColumns(true);
		form.setExpandFields(true);
		form.addFormRow(this.firstName, this.lastName);
		form.addFormRow(this.email);

		Button save = new Button("Save", event -> save());
		save.addThemeVariants(ButtonVariant.PRIMARY);

		add(header, this.grid, form, new HorizontalLayout(save, this.delete));
		expand(this.grid);
		refresh();
		edit(new Contact());
	}

	private void edit(Contact contact) {
		this.editing = (contact != null) ? contact : new Contact();
		this.binder.readBean(this.editing);
		this.delete.setEnabled(this.editing.getId() != null);
	}

	private void save() {
		if (this.binder.writeBeanIfValid(this.editing)) {
			this.repository.save(this.editing);
			Notification.show("Contact saved");
			refresh();
			edit(new Contact());
		}
	}

	private void delete() {
		this.repository.delete(this.editing);
		Notification.show("Contact deleted");
		refresh();
		edit(new Contact());
	}

	private void refresh() {
		this.grid.setItems(this.repository.findAll());
	}

}
