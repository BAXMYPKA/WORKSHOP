package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.dao.ClassifierTypesDao;
import workshop.internal.dao.ClassifiersDao;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.ClassifierType;
import workshop.internal.entities.Department;
import workshop.internal.entities.Position;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.exceptions.IllegalArgumentsException;

import java.util.Optional;

@Slf4j
@Service
public class ClassifierTypesService extends WorkshopEntitiesServiceAbstract<ClassifierType> {
	
	@Autowired
	private ClassifiersDao classifiersDao;
	
	@Autowired
	private ClassifierTypesDao classifierTypesDao;
	
	public ClassifierTypesService(ClassifierTypesDao classifierTypesDao) {
		super(classifierTypesDao);
	}
	
	/**
	 * @param classifierId {@link Classifier} ID its {@link ClassifierType} to be found from.
	 * @return The ClassifierType for that Classifier
	 * @throws EntityNotFoundException   If no Classifier found for that positionId.
	 * @throws IllegalArgumentsException If a given classifierId is null, zero or below.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public ClassifierType findClassifierTypeByClassifier(Long classifierId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(classifierId);
		
		Optional<Classifier> classifierById = classifiersDao.findById(classifierId);
		ClassifierType classifierTypeByClassifier = classifierById.orElseThrow(() -> new EntityNotFoundException(
			"No ClassifierType with such an ID", "httpStatus.notFound", HttpStatus.NOT_FOUND))
			.getClassifierType();
		return classifierTypeByClassifier;
	}
	
}
