package internal.services;

import internal.dao.PhonesDao;
import internal.dao.WorkshopEntitiesDaoAbstract;
import internal.entities.Phone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhonesService extends WorkshopEntitiesServiceAbstract<Phone> {
	
	@Autowired
	private PhonesDao phonesDao;
	
	public PhonesService(PhonesDao phonesDao) {
		super(phonesDao);
	}
}
