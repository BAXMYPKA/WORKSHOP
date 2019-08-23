package internal.services;

import internal.dao.TasksDao;
import internal.entities.Task;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	 *                            implementation of this EntitiesServiceAbstract<T>.
	 *                            To be injected to all the superclasses.
	 *                            For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public TasksService(TasksDao tasksDao) {
		super(tasksDao);
	}
	
	/**
	 * @param orderId Self-description.
	 * @return A Set of Tasks from the given Order or throws EntityNotFoundException with appropriate HttpStatus.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, isolation = Isolation.SERIALIZABLE)
	public Page<Task> findAllTasksByOrder(Pageable pageable, Long orderId)
		throws EntityNotFoundException, IllegalArgumentsException {
		
		super.verifyIdForNullZeroBelowZero(orderId);
		Pageable verifiedPageable = super.verifyAndCorrectPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		
		Optional<List<Task>> allTasksByOrder = tasksDao.findAllTasksByOrder(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			verifiedPageable.getSort().getOrderFor(orderBy).getDirection(),
			orderId);
		
		Page<Task> tasksPage = super.getEntitiesPage(pageable, allTasksByOrder);
		return tasksPage;
	}
	
}
