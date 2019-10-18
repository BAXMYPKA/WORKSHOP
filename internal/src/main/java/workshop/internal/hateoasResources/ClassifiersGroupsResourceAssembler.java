package workshop.internal.hateoasResources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import workshop.controllers.internal.rest.ClassifierGroupsRestController;
import workshop.internal.entities.ClassifiersGroup;

@Slf4j
@Component
public class ClassifiersGroupsResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<ClassifiersGroup> {
	/**
	 * Obligatory constructor.
	 * Delete the method arguments and only leave:
	 * super(WorkshopControllerInstance.class, WorkshopEntityInstance.class);
	 */
	public ClassifiersGroupsResourceAssembler() {
		super(ClassifierGroupsRestController.class, ClassifiersGroup.class);
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title, Long ownerId, String controllerMethodName) {
		
		//TODO: to complete
		
		return null;
	}
}
