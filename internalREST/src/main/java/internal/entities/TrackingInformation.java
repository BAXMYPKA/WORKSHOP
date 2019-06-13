package internal.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Interface for the @Embeddable TrackingInfo implementations for the Trackable Entities
 */
public interface TrackingInformation extends Serializable {
	
	LocalDateTime getCreated();
	
	void setCreated(LocalDateTime created);
	
	Employee getCreatedBy();
	
	void setCreatedBy(Employee createdBy);
	
	LocalDateTime getModified();
	
	void setModified(LocalDateTime modified);
	
	Employee getModifiedBy();
	
	void getModifiedBy(Employee modifiedBy);
}
