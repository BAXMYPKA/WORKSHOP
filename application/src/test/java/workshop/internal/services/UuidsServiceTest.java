package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import workshop.internal.dao.UsersDao;
import workshop.internal.dao.UuidsDao;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = {"cronOutdatedUuids=* * 10 * * *"})
@Slf4j
class UuidsServiceTest {
	
	@Autowired
	private UsersService usersService;
	
	@Autowired
	private UsersDao usersDao;
	
	@Autowired
	private UuidsDao uuidsDao;
	
	@Autowired
	private UuidsService uuidsService;
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	void outdated_New_Users_With_Uuids_More_Than_24hrs_Ago_Should_Be_Deleted() {
		//GIVEN
		User user = new User("uuidNewUserToDelete@email.pro");
		user.setIsEnabled(false);
		Uuid uuid = new Uuid(user);
		uuid.setCreated(ZonedDateTime.now().minusHours(25));
		
		usersService.persistEntity(user);
		
		//Just the check that they have been persisted
		user = usersService.findById(user.getIdentifier());
		uuid = uuidsService.findById(user.getUuid().getIdentifier());
		
		//WHEN test "cronOutdatedUuids" property consider outdated Uuid with more than 24hrs ago "created" property
		uuidsService.clearOutdatedUuids();
		
		//THEN
		assertFalse(uuidsDao.isExist(uuid.getIdentifier()));
		assertFalse(usersDao.isExist(user.getIdentifier()));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	void not_Outdated_New_Users_With_Uuids_Less_Than_24hrs_Ago_Should_Not_Be_Deleted() {
		//GIVEN
		User user = new User("uuidNewUserNotToDelete@email.pro");
		user.setIsEnabled(false);
		
		Uuid uuid = new Uuid(user);
		uuid.setCreated(ZonedDateTime.now().minusHours(23));
		
		usersService.persistEntity(user);
		
		//Just the check that they have been persisted
		user = usersService.findById(user.getIdentifier());
		uuid = uuidsService.findById(user.getUuid().getIdentifier());
		
		//WHEN test "cronOutdatedUuids" property consider outdated Uuid for more than 24hrs ago. This should not be
		// the case
		uuidsService.clearOutdatedUuids();
		
		//THEN
		assertTrue(uuidsDao.isExist(uuid.getIdentifier()));
		assertTrue(usersDao.isExist(user.getIdentifier()));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	void outdated_PasswordResetUuids_More_Than_24hrs_Ago_Should_Be_Deleted() {
		//GIVEN
		User user = new User("uuidPassResUser@email.pro");
		user.setIsEnabled(true);
		user.setFirstName("FName");
		usersService.persistEntity(user);
		
		Uuid uuid = new Uuid();
		uuid.setPasswordResetUser(user);
		uuid.setCreated(ZonedDateTime.now().minusHours(25));
		uuidsService.persistEntity(uuid);
		
		user.setPasswordResetUuid(uuid);
		usersService.mergeEntity(user);
		
		//Just the check they both are set and same
		user = usersDao.findById(user.getIdentifier()).get();
		assertEquals(uuid, user.getPasswordResetUuid());
		
		//WHEN test "cronOutdatedUuids" property consider outdated Uuid for more than 24hrs ago
		uuidsService.clearOutdatedUuids();
		
		//THEN
		assertFalse(uuidsDao.isExist(uuid.getIdentifier()));
		assertNull(usersDao.findById(user.getIdentifier()).get().getPasswordResetUuid());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	void not_Outdated_PasswordResetUuids_Less_Than_24hrs_Ago_Should_Not_Be_Deleted() {
		//GIVEN
		User user = new User("uuidPassResUserNotForDeleting@email.pro");
		user.setIsEnabled(true);
		user.setFirstName("FName");
		usersService.persistEntity(user);
		
		Uuid uuid = new Uuid();
		uuid.setPasswordResetUser(user);
		uuid.setCreated(ZonedDateTime.now().minusHours(23));
		uuidsService.persistEntity(uuid);
		
		user.setPasswordResetUuid(uuid);
		usersService.mergeEntity(user);
		
		//Just the check they both are set and same
		user = usersDao.findById(user.getIdentifier()).get();
		assertEquals(uuid, user.getPasswordResetUuid());
		
		//WHEN test "cronOutdatedUuids" property consider outdated Uuid just a two seconds ago. But not for this case.
		uuidsService.clearOutdatedUuids();
		
		//THEN
		assertTrue(uuidsDao.isExist(uuid.getIdentifier()));
		assertNotNull(usersDao.findById(user.getIdentifier()).get().getPasswordResetUuid());
	}
	
}