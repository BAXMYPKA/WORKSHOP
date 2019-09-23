package workshop.internal.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Every instance has to be named with a simple noun with a single word without changeable last part of it
 * by plural form, e.g. 'Word', 'Task' etc with 'words', 'tasks'.
 * Also the name doesn't have to include intermediate hyphens.
 * All above are for the sake of constructing simple URI paths '/domainPath/words/', '/domainPath/tasks/' etc.
 * In other cases you will need to correct {@link WorkshopPermissionEvaluator#evaluateServletWebRequest} method for
 * proper evaluating {@link WorkshopEntity#getWorkshopEntityName()} from a URI path.
 * <p>
 * Any implementations are intended to be exposed as the Resource for HATEOAS and also have extended Spring
 * ResourceSupport
 */
public interface WorkshopEntity extends Serializable, Comparable<WorkshopEntity> {
	
	/**
	 * The Set for all the available WorkshopEntities names for being accessed across the domain.
	 */
	public static final Set<String> workshopEntitiesNames = new HashSet<>();
	
	/**
	 * Obligatory to be set in every class as:
	 * '@Transient
	 * private static final long serialVersionUID = WorkshopEntity.serialVersionUID;'
	 */
	long serialVersionUID = 50L;
	
	/**
	 * Has to have the annotation '@Column(name = 'id')' for being the Entity for JPA
	 *
	 * @return '@Column(name = 'id')'
	 */
	Long getIdentifier();
	
	/**
	 * Has to have the annotation '@Column(name = 'id')' for being the Entity for JPA
	 *
	 * @param id '@Column(name = 'id')'
	 */
	void setIdentifier(Long id);
	
	/**
	 * Not null.
	 *
	 * @return The obligatory property for being used for ordering by default.
	 */
	ZonedDateTime getCreated();
	
	/**
	 * Not null.
	 *
	 * @param created The obligatory property for being used for ordering by default.
	 */
	void setCreated(ZonedDateTime created);
	
	/**
	 * Creates in the instance constructor for being accessed across domain.
	 */
	String getWorkshopEntityName();
}
