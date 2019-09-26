package workshop.internal.entities;

import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.entities.hibernateValidation.PersistEmployeeValidation;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackableBeanValidationTest {
	
	private static Validator validator;
	
	@BeforeAll
	public static void init() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(longs = {-10, -1, 0})
	public void default_Validation_Group_Should_Reject_Null_Zero_And_Negative_Identifier(Long incorrectIdentifier) {
		//GIVEN
		Order orderWithIncorrectId = new Order();
		orderWithIncorrectId.setIdentifier(incorrectIdentifier);
		
		//WHEN
		Set<ConstraintViolation<Order>> validatedOrder = validator.validate(orderWithIncorrectId);
		ConstraintViolation<Order> violation = validatedOrder.iterator().next();
		
		//THEN
		assertEquals(1, validatedOrder.size());
		assertEquals("identifier", violation.getPropertyPath().toString());
		
		if (incorrectIdentifier == null) {
			assertEquals("{validation.notNull}", violation.getMessageTemplate());
		} else if (incorrectIdentifier != null) {
			assertEquals("{validation.positive}", violation.getMessageTemplate());
		}
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(longs = {-101, -1, 0})
	public void merging_Validation_Group_Should_Reject_Null_Zero_And_Negative_Identifier(Long incorrectIdentifier) {
		//GIVEN
		Order orderWithIncorrectId = new Order();
		orderWithIncorrectId.setIdentifier(incorrectIdentifier);
		
		//WHEN
		Set<ConstraintViolation<Order>> validatedOrder = validator.validate(orderWithIncorrectId, MergingValidation.class);
		ConstraintViolation<Order> violation = validatedOrder.iterator().next();
		
		//THEN
		assertEquals(1, validatedOrder.size());
		assertEquals("identifier", violation.getPropertyPath().toString());
		
		if (incorrectIdentifier == null) {
			assertEquals("{validation.notNull}", violation.getMessageTemplate());
		} else if (incorrectIdentifier != null) {
			assertEquals("{validation.positive}", violation.getMessageTemplate());
		}
	}
	
	@ParameterizedTest
	@ValueSource(longs = {-1, 0, 1, 100})
	public void persistence_Validation_Group_Should_Reject_Any_NonNull_Identifier(Long incorrectIdentifier) {
		//GIVEN
		Order orderWithIncorrectId = new Order();
		orderWithIncorrectId.setIdentifier(incorrectIdentifier);
		
		//WHEN
		Set<ConstraintViolation<Order>> validatedOrder = validator.validate(orderWithIncorrectId, PersistenceValidation.class);
		ConstraintViolation<Order> violation = validatedOrder.iterator().next();
		
		//THEN
		assertEquals(1, validatedOrder.size());
		assertEquals("identifier", violation.getPropertyPath().toString());
		assertEquals("{validation.null}", violation.getMessageTemplate());
	}
	
	@ParameterizedTest
	@ValueSource(classes = {Default.class, PersistEmployeeValidation.class, MergingValidation.class})
	public void almost_Any_Validation_Group_Should_Accept_Only_PastOrPresent_Created(Class classValidation) {
		//GIVEN
		Order orderWithIncorrectCreated = new Order();
		orderWithIncorrectCreated.setIdentifier(1L);
		orderWithIncorrectCreated.setCreated(ZonedDateTime.now().plusMinutes(1));
		
		
		//WHEN
		Set<ConstraintViolation<Order>> validatedOrder = validator.validate(orderWithIncorrectCreated, classValidation);
		ConstraintViolation<Order> violation = validatedOrder.iterator().next();
		
		//THEN
		assertEquals(1, validatedOrder.size());
		assertEquals("created", violation.getPropertyPath().toString());
		assertEquals("{validation.pastOrPresent}", violation.getMessageTemplate());
	}
	
	@Test
	public void persistence_Validation_Group_Should_Reject_Filled_In_Created() {
		//GIVEN
		Order orderWithCorrectCreated = new Order();
		orderWithCorrectCreated.setCreated(ZonedDateTime.now().minusMinutes(1));
		
		//WHEN
		Set<ConstraintViolation<Order>> validatedOrder = validator.validate(orderWithCorrectCreated, PersistenceValidation.class);
		ConstraintViolation<Order> violation = validatedOrder.iterator().next();
		
		//THEN
		assertEquals(1, validatedOrder.size());
		assertEquals("created", violation.getPropertyPath().toString());
		assertEquals("{validation.null}", violation.getMessageTemplate());
	}

	@Test
	public void persistence_Validation_Group_Should_Accept_Only_Null_Modified() {
		//GIVEN
		Order orderWithCorrectModified = new Order();
		orderWithCorrectModified.setModified(ZonedDateTime.now().plusMinutes(1));
		
		//WHEN
		Set<ConstraintViolation<Order>> validatedOrder = validator.validate(orderWithCorrectModified, PersistenceValidation.class);
		ConstraintViolation<Order> violation = validatedOrder.iterator().next();
		
		//THEN
		assertEquals(1, validatedOrder.size());
		assertEquals("modified", violation.getPropertyPath().toString());
		assertEquals("{validation.null}", violation.getMessageTemplate());
	}
	
	@Test
	public void default_Validation_Group_Should_Accept_Only_PastOrPresent_Finished() {
		//GIVEN
		Order orderWithIncorrectFinished = new Order();
		orderWithIncorrectFinished.setFinished(ZonedDateTime.now().plusMinutes(1));
		orderWithIncorrectFinished.setIdentifier(1L);
		
		//WHEN
		Set<ConstraintViolation<Order>> validatedOrder = validator.validate(orderWithIncorrectFinished);
		ConstraintViolation<Order> violation = validatedOrder.iterator().next();
		
		//THEN
		assertEquals(1, validatedOrder.size());
		assertEquals("finished", violation.getPropertyPath().toString());
		assertEquals("{validation.pastOrPresent}", violation.getMessageTemplate());
	}
}