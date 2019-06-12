package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Embeddable
public class TrackingInfo {
	
	private LocalDateTime created;
	
	private LocalDateTime modified;
	
	private LocalDateTime deadline;
	
	private LocalDateTime finished;
	
	private Employee createdBy;
}
