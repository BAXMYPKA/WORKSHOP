package internal.entities;

import java.io.Serializable;

/**
 * Interface as the marker for implementing classes that they are from Workshop Entities factory
 */
public interface WorkshopEntity extends Serializable {
	
	public long getId();
	
	public void setId(long id);
}
