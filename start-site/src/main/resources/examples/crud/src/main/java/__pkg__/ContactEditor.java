package {{package}};

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.RequiredFieldConfigurator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import jakarta.validation.constraints.NotBlank;

/**
 * The form for one contact. It validates with the Bean Validation constraints on
 * {@link Contact} and leaves saving and deleting to the view.
 */
public class ContactEditor extends VerticalLayout {

	private final H2 title = new H2();

	private final TextField firstName = new TextField("First name");

	private final TextField lastName = new TextField("Last name");

	private final TextField email = new TextField("Email");

	private final BeanValidationBinder<Contact> binder = new BeanValidationBinder<>(Contact.class);

	private final Button delete = new Button("Delete");

	private Contact contact = new Contact();

	public ContactEditor(SerializableConsumer<Contact> onSave, SerializableConsumer<Contact> onDelete,
			SerializableRunnable onCancel) {
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
		form.setColspan(this.email, 2);

		Button save = new Button("Save", event -> {
			if (this.binder.writeBeanIfValid(this.contact)) {
				onSave.accept(this.contact);
			}
		});
		save.addThemeVariants(ButtonVariant.PRIMARY);
		Button cancel = new Button("Cancel", event -> onCancel.run());
		this.delete.addThemeVariants(ButtonVariant.ERROR);
		this.delete.addClickListener(event -> onDelete.accept(this.contact));

		HorizontalLayout buttons = new HorizontalLayout(save, cancel);
		buttons.setWidthFull();
		buttons.addToEnd(this.delete);

		add(this.title, form, buttons);
	}

	public void edit(Contact contact) {
		this.contact = contact;
		this.binder.readBean(contact);
		boolean saved = contact.getId() != null;
		this.title.setText(saved ? contact.getFirstName() + " " + contact.getLastName() : "New contact");
		this.delete.setVisible(saved);
	}

}
