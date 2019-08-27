package internal.controllers;

import internal.entities.Phone;
import internal.hateoasResources.PhonesResourceAssembler;
import internal.services.PhonesService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/phones", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(Phone.class)
public class PhonesController extends WorkshopControllerAbstract<Phone> {
	
	/**
	 * @param phonesService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                to operate with.
	 * @param phonesResourceAssembler
	 */
	public PhonesController(PhonesService phonesService, PhonesResourceAssembler phonesResourceAssembler) {
		super(phonesService, phonesResourceAssembler);
	}
}
