package workshop.internal.hateoasResources;

import workshop.controllers.internal.rest.InternalAuthoritiesRestController;
import workshop.controllers.internal.rest.PositionsRestController;
import workshop.internal.entities.InternalAuthority;
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
		super(InternalAuthoritiesRestController.class, InternalAuthority.class);
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media,
								String title, Long ownerId, String controllerMethodName) {
		Link link;
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		
		if (PositionsRestController.GET_POSITION_INTERNAL_AUTHORITIES_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(PositionsRestController.class)
					.getPositionInternalAuthorities(
						ownerId,
						pageable.getPageSize(),
						++pageNum,
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
