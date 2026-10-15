package {{package}};

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("")
public class ContactView extends VerticalLayout {

	private final ContactRepository repository;

	private final Grid<Contact> grid = new Grid<>(Contact.class);

	private final TextField firstName = new TextField("First name");

	private final TextField lastName = new TextField("Last name");

	private final TextField email = new TextField("Email");

	private final Binder<Contact> binder = new Binder<>(Contact.class);

	private Contact editing = new Contact();

	public ContactView(ContactRepository repository) {
		this.repository = repository;
		this.grid.setColumns("firstName", "lastName", "email");
		this.grid.asSingleSelect().addValueChangeListener(event -> edit(event.getValue()));
		this.binder.bindInstanceFields(this);
		Button save = new Button("Save", event -> save());
		Button delete = new Button("Delete", event -> delete());
		FormLayout form = new FormLayout(this.firstName, this.lastName, this.email);
		add(this.grid, form, new HorizontalLayout(save, delete));
		refresh();
		edit(new Contact());
	}

	private void edit(Contact contact) {
		this.editing = (contact != null) ? contact : new Contact();
		this.binder.readBean(this.editing);
	}

	private void save() {
		if (this.binder.writeBeanIfValid(this.editing)) {
			this.repository.save(this.editing);
			refresh();
			edit(new Contact());
		}
	}

	private void delete() {
		if (this.editing.getId() != null) {
			this.repository.delete(this.editing);
			refresh();
			edit(new Contact());
		}
	}

	private void refresh() {
		this.grid.setItems(this.repository.findAll());
	}

}
