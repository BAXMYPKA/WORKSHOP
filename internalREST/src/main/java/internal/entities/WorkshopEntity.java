package internal.entities;

import java.io.Serializable;

/**
 * Interface as the marker for implementing classes that they are from Workshop Entities factory
 */
public interface WorkshopEntity extends Serializable {
	
	long getId();
	
	void setId(long id);
}
