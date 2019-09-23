package workshop.security;

import lombok.AccessLevel;
import lombok.Getter;

public enum PermissionType {
	GET("For reading WorkshopEntities properties."),
	POST("For creating WorkshopEntities properties."),
	PUT("For modifying WorkshopEntities."),
	DELETE("For deleting WorkshopEntities.");
	
	@Getter(AccessLevel.PUBLIC)
	private String description;
	
	PermissionType(String description) {
		this.description = description;
	}
}
