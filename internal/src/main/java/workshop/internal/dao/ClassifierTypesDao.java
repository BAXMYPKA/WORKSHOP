package workshop.internal.dao;

import org.springframework.stereotype.Repository;
import workshop.internal.entities.ClassifierType;

@Repository
public class ClassifierTypesDao extends WorkshopEntitiesDaoAbstract<ClassifierType, Long> {
	
	public ClassifierTypesDao() {
		setEntityClass(ClassifierType.class);
		setKeyClass(Long.class);
	}
}
