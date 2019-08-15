package internal.services;

import internal.dao.TasksDao;
import internal.entities.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
