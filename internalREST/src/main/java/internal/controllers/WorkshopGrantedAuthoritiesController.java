package internal.controllers;

import internal.entities.WorkshopGrantedAuthority;
import internal.hateoasResources.WorkshopGrantedAuthoritiesResourceAssembler;
import internal.services.WorkshopGrantedAuthoritiesService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/authorities")
@ExposesResourceFor(WorkshopGrantedAuthority.class)
public class WorkshopGrantedAuthoritiesController extends WorkshopControllerAbstract<WorkshopGrantedAuthority> {
	
	/**
	 * @param workshopGrantedAuthoritiesService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                                    and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                                    to operate with.
	 * @param workshopGrantedAuthoritiesResourceAssembler
	 */
	public WorkshopGrantedAuthoritiesController(
		WorkshopGrantedAuthoritiesService workshopGrantedAuthoritiesService,
		WorkshopGrantedAuthoritiesResourceAssembler workshopGrantedAuthoritiesResourceAssembler) {
		super(workshopGrantedAuthoritiesService, workshopGrantedAuthoritiesResourceAssembler);
	}
}
