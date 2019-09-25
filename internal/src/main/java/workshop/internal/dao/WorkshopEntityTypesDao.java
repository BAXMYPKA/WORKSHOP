package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.WorkshopEntityType;

@Slf4j
@Repository
public class WorkshopEntityTypesDao extends WorkshopEntitiesDaoAbstract<WorkshopEntityType, Long> {
	
	public WorkshopEntityTypesDao() {
		setEntityClass(WorkshopEntityType.class);
		setKeyClass(Long.class);
		
	}
}
