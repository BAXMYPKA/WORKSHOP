package internal.controllers;

import internal.entities.User;
import internal.hateoasResources.UsersResourceAssembler;
import internal.services.UsersService;
import internal.services.WorkshopEntitiesServiceAbstract;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RequestMapping(path = "/internal/users", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RestController
@ExposesResourceFor(User.class)
public class UsersController extends WorkshopControllerAbstract<User> {
	
	/**
	 * @param usersService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                to operate with.
	 */
	public UsersController(UsersService usersService, UsersResourceAssembler usersResourceAssembler) {
		super(usersService, usersResourceAssembler);
	}
}
