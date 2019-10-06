package workshop.internal.hateoasResources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import workshop.controllers.internal.rest.ClassifiersRestController;
import workshop.controllers.internal.rest.EmployeesRestController;
import workshop.controllers.internal.rest.OrdersRestController;
import workshop.controllers.internal.rest.TasksRestController;
import workshop.internal.entities.Task;

@Slf4j
@Component
public class TasksResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Task> {
	
	public TasksResourceAssembler() {
		super(TasksRestController.class, Task.class);
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
		if (EmployeesRestController.GET_APPOINTED_TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(EmployeesRestController.class).getAppointedTasks(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (EmployeesRestController.GET_TASKS_MODIFIED_BY_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(EmployeesRestController.class).getTasksModifiedBy(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (EmployeesRestController.GET_TASKS_CREATED_BY_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(EmployeesRestController.class).getTasksCreatedBy(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (OrdersRestController.GET_ORDER_TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(OrdersRestController.class).orderTasks(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else if (ClassifiersRestController.GET_TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(ClassifiersRestController.class).getTasks(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
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
