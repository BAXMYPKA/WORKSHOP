package internal.dao;

import internal.entities.Classifier;
import org.springframework.stereotype.Repository;

@Repository
public class ClassifiersDao extends DaoAbstract<Classifier, Long> {
	
	
	public ClassifiersDao() {
		setEntityClass(Classifier.class);
		setKeyClass(Long.class);
	}
}
