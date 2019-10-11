package workshop.internal.hateoasResources;

import workshop.controllers.internal.rest.ExternalAuthoritiesRestController;
import workshop.controllers.internal.rest.UsersRestController;
import workshop.internal.entities.Phone;
import workshop.internal.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UsersResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<User> {
	
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	
	public UsersResourceAssembler() {
		super(UsersRestController.class, User.class);
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
	
	
	/**
	 * @see WorkshopEntitiesResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, Long, String)
	 */
	@Override
	protected Link getPagedLink(Pageable pageable,
								int pageNum,
								String relation,
								String hrefLang,
								String media,
								String title,
								Long ownerId,
								String controllerMethodName) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		Link link;
		if (ExternalAuthoritiesRestController.GET_EXTERNAL_AUTHORITY_USERS.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(ExternalAuthoritiesRestController.class).getExternalAuthorityUsers(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
			return link;
		} else {
			log.error("No matching 'controllerMethodName' found for the given parameter {} in the {} for the Link to be constructed!",
				controllerMethodName, getWorkshopControllerAbstractClass());
			return new Link("/no_link_found/");
		}
	}
}
