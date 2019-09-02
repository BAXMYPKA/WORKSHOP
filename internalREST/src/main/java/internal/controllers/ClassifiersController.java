package internal.controllers;

import internal.entities.Classifier;
import internal.hateoasResources.ClassifiersResourceAssembler;
import internal.services.ClassifiersService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/classifiers")
@ExposesResourceFor(Classifier.class)
public class ClassifiersController extends WorkshopControllerAbstract<Classifier> {
	/**
	 * @param classifiersService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                     and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                     to operate with.
	 * @param classifiersResourceAssembler
	 */
	public ClassifiersController(ClassifiersService classifiersService, ClassifiersResourceAssembler classifiersResourceAssembler) {
		super(classifiersService, classifiersResourceAssembler);
	}
}
