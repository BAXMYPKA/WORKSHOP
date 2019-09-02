package internal.controllers;

import internal.entities.Order;
import internal.entities.User;
import internal.hateoasResources.OrdersResourceAssembler;
import internal.hateoasResources.UsersResourceAssembler;
import internal.services.OrdersService;
import internal.services.UsersService;
import internal.services.WorkshopEntitiesServiceAbstract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RequestMapping(path = "/internal/users", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RestController
@ExposesResourceFor(User.class)
public class UsersController extends WorkshopControllerAbstract<User> {
	
	public static final String GET_USER_ORDERS_METHOD_NAME = "userOrders";
	@Autowired
	public OrdersService ordersService;
	@Autowired
	public OrdersResourceAssembler ordersResourceAssembler;
	
	/**
	 * @param usersService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                     and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                     to operate with.
	 */
	public UsersController(UsersService usersService, UsersResourceAssembler usersResourceAssembler) {
		super(usersService, usersResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/orders")
	public ResponseEntity<String> userOrders(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order){
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Order> allOrdersCreatedForUserPage = ordersService.findAllOrdersCreatedForUser(pageable, id);
		Resources<Resource<Order>> userOrdersPagedResources =
			ordersResourceAssembler.toPagedSubResources(allOrdersCreatedForUserPage, id, GET_USER_ORDERS_METHOD_NAME);
		String jsonUserOrdersPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(userOrdersPagedResources);
		return ResponseEntity.ok(jsonUserOrdersPagedResources);
	}
	
	public void grantedAuthorities() {
		//??????????????????????????????????????
	}
}
