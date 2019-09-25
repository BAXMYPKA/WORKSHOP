package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import workshop.internal.dao.WorkshopEntityTypesDao;
import workshop.internal.entities.WorkshopEntityType;

@Slf4j
@Service
public class WorkshopEntityTypesService extends WorkshopEntitiesServiceAbstract<WorkshopEntityType> {
	
	@Autowired
	private WorkshopEntityTypesDao workshopEntityTypesDao;
	
	public WorkshopEntityTypesService(WorkshopEntityTypesDao workshopEntityTypesDao) {
		super(workshopEntityTypesDao);
	}
}
