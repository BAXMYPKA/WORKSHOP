package internal.entities.hibernateValidation;

/**
 * The main purpose is to check 'created' (@AttributeOverrided as 'employed') field property for a valid manually set
 * ZonedDateTime (if presented) because for all other entities this field is set automatically.
 */
public interface PersistEmployeeValidation {
}
