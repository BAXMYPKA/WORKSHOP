package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.dao.UsersDao;
import workshop.internal.dao.UuidsDao;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;

import java.time.ZonedDateTime;

@Slf4j
@Service
public class UuidsService extends WorkshopEntitiesServiceAbstract<Uuid> {
	
	@Autowired
	private UsersDao usersDao;
	
	public UuidsService(UuidsDao uuidsDao) {
		super(uuidsDao);
	}
	
	/**
	 * (cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
	 * <p>
	 * To clear the DataBase from outdated {@link Uuid}s created more than 24 hours ago.
	 * Clear interval is set from "workshop.properties" 'cronClearOutdatedUuids=' value.
	 */
	@Scheduled(cron = "${cronClearOutdatedUuids}", zone = "Europe/Moscow")
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public void clearOutdatedUuids() {
		getWorkshopEntitiesDaoAbstract().findAllEntities().ifPresent(uuids ->
			uuids.stream().filter(uuid -> uuid.getCreated().isBefore(ZonedDateTime.now().minusHours(24)))
				.forEach(uuid -> {
					//Removing outdated PasswordResetUuids
					if (uuid.getPasswordResetUser() != null) {
						User user = uuid.getPasswordResetUser();
						user.setPasswordResetUuid(null);
						//Removing Uuids for new Users and those Users either
					} else if (uuid.getUser() != null) {
						User user = uuid.getUser();
						user.setUuid(null);
						usersDao.removeEntity(user);
					}
				}));
	}
}
