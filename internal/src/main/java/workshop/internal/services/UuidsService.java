package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import workshop.internal.dao.UuidsDao;
import workshop.internal.entities.Uuid;

@Slf4j
@Service
public class UuidsService extends WorkshopEntitiesServiceAbstract<Uuid> {
	
	public UuidsService(UuidsDao uuidsDao) {
		super(uuidsDao);
	}
}
