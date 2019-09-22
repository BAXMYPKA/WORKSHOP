package workshop.internal.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Interface as the marker for implementing classes that they are from Workshop Entities factory.
 * If any implementation is intended to be exposed as the Resource for HATEOAS is also have extend Spring ResourceSupport
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
	 * @param created The obligatory property for being used for ordering by default.
	 */
	void setCreated(ZonedDateTime created);
	
	/**
	 * Not null.
	 *
	 * @return The obligatory property for being used for ordering by default.
	 */
	ZonedDateTime getCreated();
	
	/**
	 * Creates in the instance constructor for being accessed across domain.
	 */
	String getWorkshopEntityName();
}
