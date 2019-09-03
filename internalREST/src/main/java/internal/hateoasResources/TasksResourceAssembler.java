package internal.hateoasResources;

import internal.controllers.ClassifiersController;
import internal.controllers.EmployeesController;
import internal.controllers.OrdersController;
import internal.controllers.TasksController;
import internal.entities.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TasksResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Task> {
	
	public TasksResourceAssembler() {
		super(TasksController.class, Task.class);
		setDEFAULT_TITLE("Task");
	}
	
	/**
	 * @param pageable             The main info about pageable state.
	 * @param pageNum              The obligatory parameter to obtain the current number of page.
	 * @param relation             The relation ("self", "tasksAppointedTo" etc).
	 * @param hrefLang             Depending on given user's Locale.
	 * @param media                MediaType (json-hal, utf-8)
	 * @param title                The Title of the Link.
	 * @param ownerId              ID of the Owner of this collection. E.g., if an Employee is the owner, getEmployee
	 *                             (ownerId).getOrders()
	 * @param controllerMethodName Discriminator string method name in order to allow to pass custom parameters and
	 *                             construct a custom Link according to ControllerLinkBuilder.methodOn().
	 *                             As usual, it passes as static string from WorkshopController
	 *                             .GET_ORDERS_CREATED_BY_METHOD_NAME.
	 * @return A single custom Link created according to 'controllerMethodName'.
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
				ControllerLinkBuilder.methodOn(EmployeesController.class).appointedTasks(
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
				ControllerLinkBuilder.methodOn(EmployeesController.class).tasksModifiedBy(
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
				ControllerLinkBuilder.methodOn(EmployeesController.class).tasksCreatedBy(
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
		} else if (ClassifiersController.TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(ClassifiersController.class).tasks(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		} else { //If something would go wrong this is the default fallback
			return super.getPagedLink(pageable, pageNum, relation, hrefLang, media, title, ownerId, controllerMethodName);
		}
		return link;
	}
}
