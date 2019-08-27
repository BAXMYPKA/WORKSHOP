package internal.dao;

import internal.entities.Phone;
import org.springframework.stereotype.Repository;

@Repository
public class PhonesDao extends WorkshopEntitiesDaoAbstract<Phone, Long> {
	
	public PhonesDao() {
		super.setEntityClass(Phone.class);
		super.setKeyClass(Long.class);
	}
}
