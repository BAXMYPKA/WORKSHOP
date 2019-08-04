package internal.entities.hateoasResources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import internal.controllers.EmployeesController;
import internal.controllers.WorkshopController;
import internal.entities.Trackable;
import internal.entities.WorkshopEntity;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Spring HATEOAS Resource which has to be presented as a ResourceSupportDTO with the WorkshopEntity and Links to be
 * exposed as the JSON with the ResponseEntity<String> to the end Users.
 * That is it will be shown as a plain WorkshopEntity Json string
 * Every ResourceDTO at least has to have:
 * 1) Self-relation link with its id. By its id it can be obtained through a concrete WorkshopController instance for
 * the same WorkshopEntity instances.
 * 2) All-relation link. Also obtainable through a concrete instance of the WorkshopController.
 *
 * @param <T> the WorkshopEntity type for this WorkshopEntity and its controller.
 */
@JsonIgnoreProperties(value = {"workshopController", "simpleClassName"})
public abstract class WorkshopEntityResource<T extends WorkshopEntity> extends ResourceSupport {
	
	@JsonUnwrapped
	private final WorkshopEntity workshopEntity;
	private final WorkshopController<T> workshopController;
	private String simpleClassName;
	
	/**
	 * @param workshopEntity     The concrete instance of the WorkshopEntity to be the HATEOAS Resource.
	 * @param workshopController The concrete instance of the WorkshopController to get Links for this WorkshopEntity
	 *                           from.
	 */
	public WorkshopEntityResource(WorkshopEntity workshopEntity, WorkshopController workshopController) throws Throwable {
		this.workshopEntity = workshopEntity;
		this.workshopController = workshopController;
		simpleClassName = workshopEntity.getClass().getSimpleName();
		Link selfLink = linkTo(methodOn(workshopController.getClass()).getOne(workshopEntity.getId())).withSelfRel();
		Link allLink = linkTo(methodOn(workshopController.getClass()).getAll()).withRel("all");
		if (Trackable.class.isAssignableFrom(workshopEntity.getClass())){
			workshopEntity = (Trackable)workshopEntity;
			if (((Trackable) workshopEntity).getCreatedBy() != null){
				Link createdByLink =
					linkTo(
						methodOn(EmployeesController.class).getOne(((Trackable) workshopEntity).getCreatedBy().getId()))
						.withRel("created_by");
				add(createdByLink);
			}
			if (((Trackable) workshopEntity).getModifiedBy() != null) {
				Link modifiedByLink =
					ControllerLinkBuilder.linkTo(
						methodOn(EmployeesController.class).getOne(((Trackable) workshopEntity).getModifiedBy().getId()))
						.withRel("modified_by");
			}
		}
		add(selfLink, allLink);
	}
}
