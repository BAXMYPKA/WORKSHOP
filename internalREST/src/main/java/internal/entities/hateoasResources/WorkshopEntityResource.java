package internal.entities.hateoasResources;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import internal.entities.WorkshopEntity;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

/**
 * This is Spring HATEOAS Resource which has to be presented as a ResourceSupportDTO instead of plain WorkshopEntity.
 * This Resource is intended to contain the Links which have to be exposed as the JSON with the ResponseEntity<String> to the end Users.
 * This class will be shown as a plain WorkshopEntity Json string with the http 'links' ('self', 'all' etc)
 *
 * @param <T> the WorkshopEntity type for this WorkshopEntity and its controller.
 */
public abstract class WorkshopEntityResource<T extends WorkshopEntity> extends ResourceSupport {
	
//	@JsonUnwrapped
//	private final T workshopEntity;
//	private final String entityName;
	
	/**
	 * @param workshopEntity     The concrete instance of the WorkshopEntity to be the HATEOAS Resource.
	 */
	public WorkshopEntityResource(T workshopEntity) {
//		this.workshopEntity = workshopEntity;
//		entityName = workshopEntity.getClass().getSimpleName();
	}
	
	public WorkshopEntityResource() {
		super();
	}
	
}
