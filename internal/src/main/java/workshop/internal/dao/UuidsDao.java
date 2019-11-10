package workshop.internal.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import workshop.internal.entities.Uuid;

@Slf4j
@Setter
@Getter
@Repository
public class UuidsDao extends WorkshopEntitiesDaoAbstract<Uuid, Long> {
	
	public UuidsDao() {
		this.setEntityClass(Uuid.class);
		this.setKeyClass(Long.class);
		
	}
}
