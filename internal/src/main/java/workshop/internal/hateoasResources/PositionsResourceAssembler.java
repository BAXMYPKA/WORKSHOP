package workshop.internal.hateoasResources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import workshop.internal.controllers.rest.DepartmentsController;
import workshop.internal.controllers.rest.InternalAuthoritiesController;
import workshop.internal.controllers.rest.PositionsController;
import workshop.internal.entities.Position;

@Slf4j
@Component
public class PositionsResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Position> {
	
	public PositionsResourceAssembler() {
		super(PositionsController.class, Position.class);
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
		Link link;
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		
		if (DepartmentsController.GET_POSITIONS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(DepartmentsController.class).getPositions(
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
		} else if (InternalAuthoritiesController.GET_INTERNAL_AUTHORITY_POSITIONS.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(InternalAuthoritiesController.class).getInternalAuthorityPositions(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withMedia(media)
				.withHreflang(hrefLang)
				.withTitle(title);
			return link;
		} else {
			log.error("No matching 'controllerMethodName' found for the given parameter {} in the {} for the Link to be constructed!",
				controllerMethodName, getWorkshopControllerAbstractClass());
			return new Link("/no_link_found/");
		}
	}
}
