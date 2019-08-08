package internal.entities;

import org.springframework.hateoas.Link;

import java.io.Serializable;

/**
 * Interface as the marker for implementing classes that they are from Workshop Entities factory.
 * If any implementation is intended to be exposed as the Resource for HATEOAS is also have extend Spring ResourceSupport
 */
public interface WorkshopEntity extends Serializable {
	
	/**
	 * Has to have the annotation '@Column(name = 'id')' for being the Entity for JPA
	 *
	 * @return
	 */
	Long getIdentifier();
	
	/**
	 * Has to have the annotation '@Column(name = 'id')' for being the Entity for JPA
	 *
	 * @param id
	 */
	void setIdentifier(Long id);
}
