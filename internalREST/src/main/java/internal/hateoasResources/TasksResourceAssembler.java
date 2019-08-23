package internal.hateoasResources;

import internal.controllers.TasksController;
import internal.controllers.WorkshopControllerAbstract;
import internal.entities.Task;
import org.springframework.stereotype.Component;

@Component
public class TasksResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Task> {
	
	public TasksResourceAssembler() {
		super(TasksController.class, Task.class);
	}
}
