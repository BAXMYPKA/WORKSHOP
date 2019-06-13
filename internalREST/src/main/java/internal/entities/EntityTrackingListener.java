package internal.entities;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

/**
 * Derives @Embeddable TrackingInfo from Entities implements Trackable with @Embedded one and sets info for tracking
 */
public class EntityTrackingListener {
	
	@PrePersist
	void prePersist(Trackable trackable) {
		TrackingInformation trackingInfo = trackable.getTrackingInformation();
		
		trackingInfo.setCreated(LocalDateTime.now());
		trackingInfo.setCreatedBy(trackingInfo.getCreatedBy());
	}
	
	@PreUpdate
	void preUpdate(Trackable trackable) {
		TrackingInformation trackingInfo = trackable.getTrackingInformation();
		
		trackingInfo.setModified(LocalDateTime.now());
	}
}
