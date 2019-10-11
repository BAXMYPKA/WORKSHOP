package workshop.internal.entities.utils;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.http.HttpMethod;

/**
 * Representation of {@link org.springframework.http.HttpMethod}s ENUM
 * as the permission types for 'GET' ('read'), 'POST' ('modify'), 'PUT' ('write'), 'DELETE' ('delete').
 */
public enum PermissionType {
	GET("For reading WorkshopEntities properties."),
	POST("For creating WorkshopEntities properties."),
	PUT("For modifying WorkshopEntities."),
	DELETE("For deleting WorkshopEntities.");
	
	@Getter(AccessLevel.PUBLIC)
	private String description;
	
	/**
	 * @param httpMethod Supports only GET, POST, PUT, DELETE {@link HttpMethod}
	 * @throws IllegalArgumentException If the given {@link HttpMethod} doesn't match any {@link PermissionType}
	 */
	public static PermissionType valueOf(HttpMethod httpMethod) throws IllegalArgumentException {
		for (PermissionType type : values()) {
			if (type.name().equalsIgnoreCase(httpMethod.name())) return type;
		}
		throw new IllegalArgumentException("The given 'HttpMethod' doesn't match available PermissionTypes!");
	}
	
	PermissionType(String description) {
		this.description = description;
	}
}
