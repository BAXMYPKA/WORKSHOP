package workshop.internal.hateoasResources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import workshop.internal.controllers.AuthorityPermissionsController;
import workshop.internal.controllers.WorkshopEntityTypesController;
import workshop.internal.entities.WorkshopEntityType;

@Slf4j
@Component
public class WorkshopEntityTypesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<WorkshopEntityType> {
	
	/**
	 * Obligatory constructor.
	 * Delete the method arguments and only leave:
	 * super(WorkshopControllerInstance.class, WorkshopEntityInstance.class);
	 */
	public WorkshopEntityTypesResourceAssembler() {
		super(WorkshopEntityTypesController.class, WorkshopEntityType.class);
	}
	
	@Override
	protected Link getPagedLink(
		Pageable pageable,
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
		
		if (AuthorityPermissionsController.GET_AUTHORITY_PERMISSION_ENTITY_TYPES.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(AuthorityPermissionsController.class).getWorkshopEntitiesTypes(
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
			log.error(
				"No matching 'controllerMethodName' found for the given parameter {} in the {} for the Link to be constructed!",
				controllerMethodName, getWorkshopControllerAbstractClass());
			return getPagedLink(pageable, pageNum, relation, hrefLang, media, title);
		}
	}
}
