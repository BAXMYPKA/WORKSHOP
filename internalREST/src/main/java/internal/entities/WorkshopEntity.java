package internal.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Interface as the marker for implementing classes that they are from Workshop Entities factory.
 * If any implementation is intended to be exposed as the Resource for HATEOAS is also have extend Spring ResourceSupport
 */
public interface WorkshopEntity extends Serializable, Comparable<WorkshopEntity> {
	
	/**
	 * Obligatory to be set in every class as:
	 * '@Transient
	 * private static final long serialVersionUID = WorkshopEntity.serialVersionUID;'
	 */
	long serialVersionUID = 25L;
	
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
}
