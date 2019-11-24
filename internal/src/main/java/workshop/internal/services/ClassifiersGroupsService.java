package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.dao.ClassifiersGroupsDao;
import workshop.internal.dao.ClassifiersDao;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.ClassifiersGroup;
import workshop.exceptions.EntityNotFoundException;
import workshop.exceptions.IllegalArgumentsException;

import java.util.Optional;

@Slf4j
@Service
public class ClassifiersGroupsService extends WorkshopEntitiesServiceAbstract<ClassifiersGroup> {
	
	@Autowired
	private ClassifiersDao classifiersDao;
	
	@Autowired
	private ClassifiersGroupsDao classifiersGroupsDao;
	
	public ClassifiersGroupsService(ClassifiersGroupsDao classifiersGroupsDao) {
		super(classifiersGroupsDao);
	}
	
	/**
	 * @param classifierId {@link Classifier} ID its {@link ClassifiersGroup} to be found from.
	 * @return The ClassifierType for that Classifier
	 * @throws EntityNotFoundException   If no Classifier found for that positionId.
	 * @throws IllegalArgumentsException If a given classifierId is null, zero or below.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public ClassifiersGroup findClassifiersGroupByClassifier(Long classifierId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(classifierId);
		
		Optional<Classifier> classifierById = classifiersDao.findById(classifierId);
		ClassifiersGroup classifiersGroupByClassifiers = classifierById.orElseThrow(() -> new EntityNotFoundException(
			"No ClassifiersGroup with such an ID", "httpStatus.notFound", HttpStatus.NOT_FOUND))
			.getClassifiersGroup();
		return classifiersGroupByClassifiers;
	}
	
}
