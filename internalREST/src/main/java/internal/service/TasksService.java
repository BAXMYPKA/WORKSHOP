package internal.service;

import internal.dao.TasksDao;
import internal.entities.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@Service
public class TasksService {
	
	@Autowired
	TasksDao tasksDao;
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Task persistTask(Task task) throws IllegalArgumentException {
		if (task == null){
			throw new IllegalArgumentException("The Task cannot be null!");
		}
		Task persistedTask = tasksDao.persistEntity(task);
		return persistedTask;
	}
	
}
