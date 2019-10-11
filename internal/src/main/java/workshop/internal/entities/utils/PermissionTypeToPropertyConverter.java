package workshop.internal.entities.utils;

import org.springframework.http.HttpStatus;
import workshop.internal.exceptions.IllegalArgumentsException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter
public class PermissionTypeToPropertyConverter implements AttributeConverter<PermissionType, String> {
	
	@Override
	public String convertToDatabaseColumn(PermissionType permissionType) {
		if (permissionType == null) return null;
		return permissionType.name();
	}
	
	@Override
	public PermissionType convertToEntityAttribute(String s) {
		if (s == null) return null;
		return Stream.of(PermissionType.values())
			.filter(permType -> permType.name().equalsIgnoreCase(s))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentsException(
				"No PermissionType from a given String=" + s + " was found!",
				"httpStatus.notAcceptable.permissionType",
				HttpStatus.NOT_ACCEPTABLE));
	}
}
