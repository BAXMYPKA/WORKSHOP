package internal.controllers;

import internal.entities.Task;
import internal.hateoasResources.TasksResourceAssembler;
import internal.services.TasksService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "/internal/tasks", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RestController
@ExposesResourceFor(Task.class)
public class TasksController extends WorkshopControllerAbstract<Task> {
	
	/**
	 * @param tasksService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                               and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                               to operate with.
	 * @param tasksResourceAssembler
	 */
	public TasksController(TasksService tasksService, TasksResourceAssembler tasksResourceAssembler) {
		super(tasksService, tasksResourceAssembler);
	}
}
