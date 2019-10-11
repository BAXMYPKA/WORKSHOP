package workshop.internal.hateoasResources;

import workshop.controllers.internal.rest.ClassifiersRestController;
import workshop.controllers.internal.rest.TasksRestController;
import workshop.internal.entities.Classifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClassifiersResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Classifier> {
	
	/**
	 * @see WorkshopEntitiesResourceAssemblerAbstract#WorkshopEntitiesResourceAssemblerAbstract(Class, Class)
	 */
	public ClassifiersResourceAssembler() {
		super(ClassifiersRestController.class, Classifier.class);
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
		
		if (TasksRestController.GET_TASK_CLASSIFIERS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(TasksRestController.class).taskClassifiers(
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
			return new Link("/no_link_found/");
		}
	}
}
