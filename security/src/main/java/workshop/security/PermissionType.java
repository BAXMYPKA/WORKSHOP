package workshop.security;

import lombok.AccessLevel;
import lombok.Getter;

public enum PermissionType {
	READ("For reading WorkshopEntities properties."),
	WRITE("For modifying WorkshopEntities properties."),
	FULL("For creating and deleting WorkshopEntities.");
	
	@Getter(AccessLevel.PUBLIC)
	private String description;
	
	PermissionType(String description) {
		this.description = description;
	}
}
