package internal.services;

import internal.dao.TasksDao;
import internal.entities.Task;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class TasksService extends WorkshopEntitiesServiceAbstract<Task> {
	
	@Autowired
	private TasksDao tasksDao;
	
	/**
	 * @param tasksDao A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                 implementation of this EntitiesServiceAbstract<T>.
	 *                 To be injected to all the superclasses.
	 *                 For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public TasksService(TasksDao tasksDao) {
		super(tasksDao);
	}
	
	/**
	 * @param orderId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, isolation = Isolation.SERIALIZABLE)
	public Page<Task> findAllTasksByOrder(Pageable pageable, Long orderId)
		throws EntityNotFoundException, IllegalArgumentsException {
		
		super.verifyIdForNullZeroBelowZero(orderId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		
		Optional<List<Task>> allTasksByOrder = tasksDao.findAllTasksByOrder(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			verifiedPageable.getSort().getOrderFor(orderBy).getDirection(),
			orderId);
		
		Page<Task> tasksPage = super.getVerifiedEntitiesPage(verifiedPageable, allTasksByOrder);
		return tasksPage;
	}
	
	/**
	 * @param employeeId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<Task> findAllTasksAppointedToEmployee(Pageable pageable, Long employeeId) {
		
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		super.verifyIdForNullZeroBelowZero(employeeId);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		
		Optional<List<Task>> allPagedTasksAppointedToEmployee = tasksDao.findAllTasksAppointedToEmployee(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			verifiedPageable.getSort().getOrderFor(orderBy).getDirection(),
			employeeId);
		
		Page<Task> tasksPage = super.getVerifiedEntitiesPage(verifiedPageable, allPagedTasksAppointedToEmployee);
		return tasksPage;
	}
	
	/**
	 * @param employeeId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<Task> findAllTasksModifiedByEmployee(Pageable pageable, Long employeeId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Task>> allTasksModifiedByEmployee = tasksDao.findAllTasksModifiedByEmployee(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			employeeId);
		
		Page<Task> verifiedEntitiesPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allTasksModifiedByEmployee);
		
		return verifiedEntitiesPageFromDao;
	}
	
	/**
	 * @param employeeId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	
	public Page<Task> findAllTasksCreatedByEmployee(Pageable pageable, Long employeeId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Task>> allTasksCreatedByEmployee = tasksDao.findAllTasksCreatedByEmployee(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			employeeId);
		
		Page<Task> verifiedEntitiesPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allTasksCreatedByEmployee);
		
		return verifiedEntitiesPageFromDao;
	}
	
	/**
	 * @param classifierId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<Task> findAllTasksByClassifier(Pageable pageable, Long classifierId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(classifierId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Task>> allTasksModifiedByEmployee = tasksDao.findAllTasksByClassifier(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			classifierId);
		
		Page<Task> verifiedEntitiesPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allTasksModifiedByEmployee);
		
		return verifiedEntitiesPageFromDao;
	}
	
}
