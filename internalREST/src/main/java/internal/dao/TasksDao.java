package internal.dao;

import internal.entities.Task;
import org.springframework.stereotype.Repository;

@Repository
public class TasksDao extends WorkshopEntitiesDaoAbstract<Task, Long> {
	
	public TasksDao(){
		setEntityClass(Task.class);
		setKeyClass(Long.class);
	}
}
