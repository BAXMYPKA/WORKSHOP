package internal.entities;

/**
 * Interface for the Entities which must contain TrackingInformation implementation as @Embedded
 */
public interface Trackable {
	
	void setTrackingInformation(TrackingInformation trackingInformation);
	
	TrackingInformation getTrackingInformation();
}
