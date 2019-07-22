package internal.dao;

import internal.entities.Classifier;
import org.springframework.stereotype.Repository;

@Repository
public class ClassifiersDao extends EntitiesDaoAbstract<Classifier, Long> {
	
	public ClassifiersDao() {
		setEntityClass(Classifier.class);
		setKeyClass(Long.class);
	}
}
