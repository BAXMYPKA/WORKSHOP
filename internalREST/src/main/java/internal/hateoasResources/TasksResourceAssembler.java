package internal.hateoasResources;

import internal.controllers.OrdersController;
import internal.controllers.TasksController;
import internal.entities.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class TasksResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Task> {
	
	public TasksResourceAssembler() {
		super(TasksController.class, Task.class);
	}
	
	@Override
	Link getPagedLink(Pageable pageable,
					  int pageSize,
					  String orderBy,
					  String order,
					  String relation,
					  String hrefLang,
					  String media,
					  String title,
					  Long workshopEntityId) {
		if (workshopEntityId == null) {
			return super.getPagedLink(pageable, pageSize, orderBy, order, relation, hrefLang, media, title, workshopEntityId);
		}
		
		Link tasksByOrderLink = ControllerLinkBuilder.linkTo(
			  ControllerLinkBuilder.methodOn(OrdersController.class).getTasks(workshopEntityId, pageSize, 0,
					orderBy, order))
			  .withRel(relation)
			  .withHreflang(hrefLang)
			  .withMedia(media)
			  .withTitle(title);
		return tasksByOrderLink;
	}
}
