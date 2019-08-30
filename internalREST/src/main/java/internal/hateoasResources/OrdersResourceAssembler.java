package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.controllers.OrdersController;
import internal.entities.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrdersResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Order> {
	
	public OrdersResourceAssembler() {
		super(OrdersController.class, Order.class);
		setDEFAULT_TITLE("Order");
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
		Link link;
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		
		if (EmployeesController.GET_ORDERS_MODIFIED_BY_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				  ControllerLinkBuilder.methodOn(EmployeesController.class).getOrdersModifiedBy(
						ownerId,
						pageable.getPageSize(),
						pageNum,
						orderBy,
						order))
				  .withRel(relation)
				  .withHreflang(hrefLang)
				  .withMedia(media)
				  .withTitle(title);
			return link;
		} else {
			return super.getPagedLink(pageable, pageNum, relation, hrefLang, media, title, ownerId, controllerMethodName);
		}
	}
}
