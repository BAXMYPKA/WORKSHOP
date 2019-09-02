package internal.services;

import internal.dao.ClassifiersDao;
import internal.dao.WorkshopEntitiesDaoAbstract;
import internal.entities.Classifier;
import internal.entities.Order;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClassifiersService extends WorkshopEntitiesServiceAbstract<Classifier> {
	
	@Autowired
	private ClassifiersDao classifiersDao;
	
	public ClassifiersService(ClassifiersDao classifiersDao) {
		super(classifiersDao);
	}
	
	/**
	 * @param taskId Self-description.
	 * @return 'Page<Classifier>' with included List of Classifiers and pageable info for constructing pages
	 * or throws EntityNotFoundException.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Task or Classifiers from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<Classifier> findAllClassifiersByTask(Pageable pageable, Long taskId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(taskId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Classifier>> allClassifiersByTask = classifiersDao.findAllClassifiersByTask(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			taskId);
		
		Page<Classifier> verifiedClassifiersPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allClassifiersByTask);
		
		return verifiedClassifiersPageFromDao;
	}
	
}
