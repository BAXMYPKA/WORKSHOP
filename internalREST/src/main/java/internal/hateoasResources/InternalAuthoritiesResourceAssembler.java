package internal.hateoasResources;

import internal.controllers.InternalAuthoritiesController;
import internal.controllers.PositionsController;
import internal.entities.InternalAuthority;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class InternalAuthoritiesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<InternalAuthority> {
	
	/**
	 * Obligatory constructor.
	 * Delete the method arguments and only leave:
	 * super(WorkshopControllerInstance.class, WorkshopEntityInstance.class);
	 */
	public InternalAuthoritiesResourceAssembler() {
		super(InternalAuthoritiesController.class, InternalAuthority.class);
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media,
								String title, Long ownerId, String controllerMethodName) {
		Link link;
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		
		if (PositionsController.GET_POSITION_INTERNAL_AUTHORITIES_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(PositionsController.class)
					.getPositionInternalAuthorities(
						ownerId,
						pageable.getPageSize(),
						pageNum,
						orderBy,
						order))
				.withRel(relation)
				.withTitle(title)
				.withHreflang(hrefLang)
				.withMedia(media);
		} else {
			return getPagedLink(pageable, pageNum, relation, hrefLang, media, title);
		}
		
		return link;
	}
}
