package internal.services;

import internal.dao.ClassifiersDao;
import internal.dao.WorkshopEntitiesDaoAbstract;
import internal.entities.Classifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClassifiersService extends WorkshopEntitiesServiceAbstract<Classifier> {
	
	@Autowired
	private ClassifiersDao classifiersDao;
	
	public ClassifiersService(ClassifiersDao classifiersDao) {
		super(classifiersDao);
	}
}
