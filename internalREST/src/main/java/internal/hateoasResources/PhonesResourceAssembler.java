package internal.hateoasResources;

import internal.controllers.PhonesController;
import internal.entities.Phone;
import org.springframework.stereotype.Component;

@Component
public class PhonesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Phone> {
	
	public PhonesResourceAssembler() {
		super(PhonesController.class, Phone.class);
		setDEFAULT_TITLE("Phone");
	}
}
