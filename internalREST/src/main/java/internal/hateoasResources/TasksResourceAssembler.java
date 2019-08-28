package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.controllers.OrdersController;
import internal.controllers.TasksController;
import internal.entities.Task;
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

@Component
public class TasksResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Task> {
	
	@Autowired
	private EmployeesController employeesController;
	
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
		if (employeesController.getGET_APPOINTED_TASKS_METHOD_NAME().equalsIgnoreCase(controllerMethodName)) {
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
		} else { //If something would go wrong
			return super.getPagedLink(pageable, pageNum, relation, hrefLang, media, title, ownerId, controllerMethodName);
		}
		return link;
	}
}
