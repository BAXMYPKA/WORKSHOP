package internal.controllers;

import internal.entities.ExternalAuthority;
import internal.hateoasResources.ExternalAuthoritiesResourceAssembler;
import internal.services.ExternalAuthoritiesService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/authorities")
@ExposesResourceFor(ExternalAuthority.class)
public class ExternalAuthoritiesController extends WorkshopControllerAbstract<ExternalAuthority> {
	
	/**
	 * @param externalAuthoritiesService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                                    and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                                    to operate with.
	 * @param externalAuthoritiesResourceAssembler
	 */
	public ExternalAuthoritiesController(
		ExternalAuthoritiesService externalAuthoritiesService,
		ExternalAuthoritiesResourceAssembler externalAuthoritiesResourceAssembler) {
		super(externalAuthoritiesService, externalAuthoritiesResourceAssembler);
	}
}
