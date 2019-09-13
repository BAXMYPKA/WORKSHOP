package internal.controllers;

import internal.entities.InternalAuthority;
import internal.hateoasResources.InternalAuthoritiesResourceAssembler;
import internal.services.InternalAuthoritiesService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ExposesResourceFor(InternalAuthority.class)
@RequestMapping(path = "/internal/authorities", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class InternalAuthoritiesController extends WorkshopControllerAbstract<InternalAuthority> {
	
	/**
	 * @param internalAuthoritiesService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                             and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                             to operate with.
	 * @param internalAuthoritiesResourceAssembler Will be using the '@ExposeResourceFor' definition.
	 */
	public InternalAuthoritiesController(InternalAuthoritiesService internalAuthoritiesService,
										 InternalAuthoritiesResourceAssembler internalAuthoritiesResourceAssembler) {
		super(internalAuthoritiesService, internalAuthoritiesResourceAssembler);
	}
}
