package workshop.internal.controllers;

import workshop.internal.entities.Phone;
import workshop.internal.hateoasResources.PhonesResourceAssembler;
import workshop.internal.services.PhonesService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/phones", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(Phone.class)
public class PhonesController extends WorkshopControllerAbstract<Phone> {
	
	/**
	 * @param phonesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                      and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                      to operate with.
	 */
	public PhonesController(PhonesService phonesService, PhonesResourceAssembler phonesResourceAssembler) {
		super(phonesService, phonesResourceAssembler);
	}
}
