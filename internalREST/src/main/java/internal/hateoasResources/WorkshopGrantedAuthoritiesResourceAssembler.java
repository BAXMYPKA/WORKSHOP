package internal.hateoasResources;

import internal.controllers.UsersController;
import internal.controllers.WorkshopGrantedAuthoritiesController;
import internal.entities.WorkshopGrantedAuthority;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class WorkshopGrantedAuthoritiesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<WorkshopGrantedAuthority> {
	
	public WorkshopGrantedAuthoritiesResourceAssembler() {
		super(WorkshopGrantedAuthoritiesController.class, WorkshopGrantedAuthority.class);
		super.setDEFAULT_TITLE("WorkshopGrantedAuthority");
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable,
								int pageNum,
								String relation,
								String hrefLang,
								String media,
								String title,
								Long ownerId,
								String controllerMethodName) {
		Link link;
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		
		if (UsersController.GET_USER_AUTHORITIES_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(UsersController.class).userGrantedAuthorities(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
			return link;
		} else {
			return super.getPagedLink(pageable, pageNum, relation, hrefLang, media, title, ownerId, controllerMethodName);
		}
	}
}
