package workshop.internal.dao;

import org.springframework.stereotype.Repository;
import workshop.internal.entities.ClassifiersGroup;

@Repository
public class ClassifiersGroupsDao extends WorkshopEntitiesDaoAbstract<ClassifiersGroup, Long> {
	
	public ClassifiersGroupsDao() {
		setEntityClass(ClassifiersGroup.class);
		setKeyClass(Long.class);
	}
}
