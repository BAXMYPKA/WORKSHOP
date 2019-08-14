package internal.entities;

import internal.entities.hibernateValidation.MergingValidation;
import internal.entities.hibernateValidation.PersistenceValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


class DepartmentBeanValidationTest {
	
	public static Validator validator;
	
	@BeforeAll
	public static void init() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	public void default_Validation_Group_Should_Reject_Null_And_Empty_Name(String nullOrEmptyString) {
		//GIVEN
		Department department = new Department(nullOrEmptyString);
		department.setIdentifier(1L);

		//WHEN
		Set<ConstraintViolation<Department>> validatedDepartment = validator.validate(department);
		
		//THEN
		ConstraintViolation<Department> violationIterator = validatedDepartment.iterator().next();
		
		assertEquals(1, validatedDepartment.size());
		assertEquals("name", violationIterator.getPropertyPath().toString());
		assertEquals("{validation.notBlank}", violationIterator.getMessageTemplate());
	}
	
	@ParameterizedTest
	@ValueSource(longs = {-12, -1, 0})
	public void default_Validation_Group_Should_Accept_Only_Positive_Identifier(long incorrectIdentifier) {
		//GIVEN
		Department department = new Department("Department");
		department.setIdentifier(incorrectIdentifier);
		
		//WHEN
		Set<ConstraintViolation<Department>> validatedDepartment = validator.validate(department);
		
		//THEN
		ConstraintViolation<Department> violationIterator = validatedDepartment.iterator().next();
		
		assertEquals(1, validatedDepartment.size());
		assertEquals("identifier", violationIterator.getPropertyPath().toString());
		assertEquals("{validation.positive}", violationIterator.getMessageTemplate());
	}
	
	@ParameterizedTest
	@ValueSource(longs = {-12, -1, 0})
	public void merging_Validation_Group_Should_Accept_Only_Positive_Identifier(long incorrectIdentifier) {
		//GIVEN
		Department department = new Department("Department");
		department.setIdentifier(incorrectIdentifier);
		
		//WHEN
		Set<ConstraintViolation<Department>> validatedDepartment = validator.validate(department, MergingValidation.class);
		
		//THEN
		ConstraintViolation<Department> violationIterator = validatedDepartment.iterator().next();
		
		assertEquals(1, validatedDepartment.size());
		assertEquals("identifier", violationIterator.getPropertyPath().toString());
		assertEquals("{validation.positive}", violationIterator.getMessageTemplate());
	}
	
	@ParameterizedTest
	@ValueSource(longs = {1, 12, 0})
	public void persistence_Validation_Group_Should_Accept_Only_Null_Identifier(long incorrectIdentifier) {
		//GIVEN
		Department department = new Department("Department");
		department.setIdentifier(incorrectIdentifier);
		
		//WHEN
		Set<ConstraintViolation<Department>> validatedDepartment = validator.validate(department, PersistenceValidation.class);
		
		//THEN
		ConstraintViolation<Department> violationIterator = validatedDepartment.iterator().next();
		
		assertEquals(1, validatedDepartment.size());
		assertEquals("identifier", violationIterator.getPropertyPath().toString());
		assertEquals("{validation.null}", violationIterator.getMessageTemplate());
	}
	
	@Test
	public void default_Validation_Group_Should_Accept_Only_PastOrPresent_Created() {
		//GIVEN
		Department department = new Department("Department");
		department.setIdentifier(1L);
		department.setCreated(ZonedDateTime.now().plusMinutes(1));
		
		//WHEN
		Set<ConstraintViolation<Department>> validatedDepartment = validator.validate(department, Default.class);
		
		//THEN
		ConstraintViolation<Department> violationIterator = validatedDepartment.iterator().next();
		
		assertEquals(1, validatedDepartment.size());
		assertEquals("created", violationIterator.getPropertyPath().toString());
		assertEquals("{validation.pastOrPresent}", violationIterator.getMessageTemplate());
	}
	
	@Test
	public void persistence_Validation_Group_Should_Accept_Only_PastOrPresent_Created() {
		//GIVEN
		Department department = new Department("Department");
		department.setCreated(ZonedDateTime.now().plusMinutes(1));
		
		//WHEN
		Set<ConstraintViolation<Department>> validatedDepartment = validator.validate(department,
			PersistenceValidation.class);
		
		//THEN
		ConstraintViolation<Department> violationIterator = validatedDepartment.iterator().next();
		
		assertEquals(1, validatedDepartment.size());
		assertEquals("created", violationIterator.getPropertyPath().toString());
		assertEquals("{validation.pastOrPresent}", violationIterator.getMessageTemplate());
	}
	
	@Test
	public void default_Validation_Group_Should_Validate_Included_Objects_Chain() {
		//GIVEN
		Department validDepartment = new Department("Department");
		validDepartment.setCreated(ZonedDateTime.now().minusMinutes(1));
		validDepartment.setIdentifier(1L);
		//Position has no name
		Position notValidPosition = new Position("", validDepartment);
		notValidPosition.setIdentifier(2L);
		
		//WHEN
		Set<ConstraintViolation<Department>> validatedDepartment = validator.validate(validDepartment, Default.class);
		Set<ConstraintViolation<Position>> validatedPosition = validator.validate(notValidPosition, Default.class);
		
		ConstraintViolation<Position> violationIterator = validatedPosition.iterator().next();
		
		//THEN
		assertEquals(0, validatedDepartment.size());
		
		assertEquals("name", violationIterator.getPropertyPath().toString());
		assertEquals("{validation.notBlank}", violationIterator.getMessageTemplate());
	}
}