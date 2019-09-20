package workshop.internal.hateoasResources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import workshop.internal.controllers.ClassifiersController;
import workshop.internal.controllers.EmployeesController;
import workshop.internal.controllers.OrdersController;
import workshop.internal.controllers.TasksController;
import workshop.internal.entities.Task;

@Slf4j
@Component
public class TasksResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Task> {
	
	public TasksResourceAssembler() {
		super(TasksController.class, Task.class);
	}
	
	/**
	 * @see WorkshopEntitiesResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, Long, String)
	 */
	@Override
	protected Link getPagedLink(Pageable pageable,
		int pageNum,
		String relation,
		String hrefLang,
		String media,
		String title,
		Long ownerId,
		String controllerMethodName) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		Link link;
		if (EmployeesController.APPOINTED_TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(EmployeesController.class).getAppointedTasks(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (EmployeesController.TASKS_MODIFIED_BY_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(EmployeesController.class).getTasksModifiedBy(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (EmployeesController.TASKS_CREATED_BY_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(EmployeesController.class).getTasksCreatedBy(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (OrdersController.GET_ORDER_TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(OrdersController.class).orderTasks(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (ClassifiersController.GET_TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(ClassifiersController.class).getTasks(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else {
			log.error(
				"No matching 'controllerMethodName' found for the given parameter {} in the {} for the Link to be constructed!",
				controllerMethodName, getWorkshopControllerAbstractClass());
			return new Link("/no_link_found/");
		}
		return link;
	}
}
