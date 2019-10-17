package workshop.internal.hateoasResources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import workshop.controllers.internal.rest.ClassifierTypesRestController;
import workshop.controllers.internal.rest.WorkshopRestControllerAbstract;
import workshop.internal.entities.ClassifierType;

@Slf4j
@Component
public class ClassifierTypesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<ClassifierType> {
	/**
	 * Obligatory constructor.
	 * Delete the method arguments and only leave:
	 * super(WorkshopControllerInstance.class, WorkshopEntityInstance.class);
	 */
	public ClassifierTypesResourceAssembler() {
		super(ClassifierTypesRestController.class, ClassifierType.class);
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title, Long ownerId, String controllerMethodName) {
		
		//TODO: to complete
		
		return null;
	}
}
