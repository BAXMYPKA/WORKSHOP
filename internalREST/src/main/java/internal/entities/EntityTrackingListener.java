package internal.entities;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

/**
 * Derives @Embeddable Trackable from Entities implements Trackable with @Embedded one and sets info for tracking
 */
public class EntityTrackingListener {
	
	@PrePersist
	void prePersist(Trackable trackable) {
		
			}
	
	@PreUpdate
	void preUpdate(Trackable trackable) {
	}
}
