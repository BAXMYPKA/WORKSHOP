package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.controllers.OrdersController;
import internal.controllers.TasksController;
import internal.entities.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TasksResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Task> {
	
	public TasksResourceAssembler() {
		super(TasksController.class, Task.class);
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable,
								int pageNum,
								String relation,
								String hrefLang,
								String media,
								String title,
								Long workshopEntityId) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().iterator().next().getDirection().name();
		
		Link tasksByOrderLink = ControllerLinkBuilder.linkTo(
			ControllerLinkBuilder.methodOn(OrdersController.class).getTasks(
				workshopEntityId,
				pageable.getPageSize(),
				pageNum,
				orderBy,
				order))
			.withRel(relation)
			.withHreflang(hrefLang)
			.withMedia(media)
			.withTitle(title);
		return tasksByOrderLink;
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
		if (EmployeesController.GET_APPOINTED_TASKS_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
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
		} else if (EmployeesController.GET_TASKS_MODIFIED_BY_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
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
		} else if (EmployeesController.GET_TASKS_CREATED_BY_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
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
		} else { //If something would go wrong this is the default fallback
			return super.getPagedLink(pageable, pageNum, relation, hrefLang, media, title, ownerId, controllerMethodName);
		}
		return link;
	}
}
