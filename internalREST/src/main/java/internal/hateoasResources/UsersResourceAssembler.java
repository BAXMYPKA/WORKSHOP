package internal.hateoasResources;

import internal.controllers.UsersController;
import internal.entities.Phone;
import internal.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class UsersResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<User> {
	
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	
	public UsersResourceAssembler() {
		super(UsersController.class, User.class);
		setDEFAULT_TITLE("User");
	}
	
	/**
	 * @return "Resource<User>" with added Links to every Phone and GrantedAuthority as a Resource.
	 */
	@Override
	public Resource<User> toResource(User user) {
		if (user.getPhones() == null || user.getPhones().isEmpty()) {
			return super.toResource(user);
		}
		Resource<User> userResource = super.toResource(user);
		userResource.getContent().getPhones().stream()
			.map(phone -> phonesResourceAssembler.toResource(phone))
			.forEach(phoneResource -> userResource.add(phoneResource.getLink("self")));
		return userResource;
	}
	
	/**
	 * @return "Resources<Resource<User>>" with added Links to every Phone and GrantedAuthority as a Resource.
	 */
	@Override
	public Resources<Resource<User>> toPagedResources(Page<User> workshopEntitiesPage) {
		Resources<Resource<User>> pagedResources = super.toPagedResources(workshopEntitiesPage);
		pagedResources.getContent().stream()
			.filter(userResource -> userResource.getContent().getPhones() != null && !userResource.getContent().getPhones().isEmpty())
			.forEach(userResource -> {
				userResource.getContent().getPhones().forEach(phone -> {
					Resource<Phone> phoneResource = phonesResourceAssembler.toResource(phone);
					userResource.add(phoneResource.getLink("self"));
				});
			});
		return pagedResources;
	}
}
