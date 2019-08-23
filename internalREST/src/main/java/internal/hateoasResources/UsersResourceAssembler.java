package internal.hateoasResources;

import internal.controllers.UsersController;
import internal.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UsersResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<User> {
	
	public UsersResourceAssembler() {
		super(UsersController.class, User.class);
	}
}
