package internal.controllers;

import internal.entities.Order;
import internal.entities.Phone;
import internal.entities.User;
import internal.entities.WorkshopGrantedAuthority;
import internal.hateoasResources.OrdersResourceAssembler;
import internal.hateoasResources.PhonesResourceAssembler;
import internal.hateoasResources.UsersResourceAssembler;
import internal.hateoasResources.WorkshopGrantedAuthoritiesResourceAssembler;
import internal.services.*;
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

import java.util.Collection;

@Component
@RequestMapping(path = "/internal/users", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RestController
@ExposesResourceFor(User.class)
public class UsersController extends WorkshopControllerAbstract<User> {
	
	public static final String GET_USER_ORDERS_METHOD_NAME = "userOrders";
	public static final String GET_USER_PHONES_METHOD_NAME = "userPhones";
	public static final String GET_USER_AUTHORITIES_METHOD_NAME = "userGrantedAuthorities";
	@Autowired
	private OrdersService ordersService;
	@Autowired
	private PhonesService phonesService;
	@Autowired
	private WorkshopGrantedAuthoritiesService workshopGrantedAuthoritiesService;
	@Autowired
	private OrdersResourceAssembler ordersResourceAssembler;
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	@Autowired
	private WorkshopGrantedAuthoritiesResourceAssembler workshopGrantedAuthoritiesResourceAssembler;
	
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
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Order> allOrdersCreatedForUserPage = ordersService.findAllOrdersCreatedForUser(pageable, id);
		Resources<Resource<Order>> userOrdersPagedResources =
			ordersResourceAssembler.toPagedSubResources(allOrdersCreatedForUserPage, id, GET_USER_ORDERS_METHOD_NAME);
		String jsonUserOrdersPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(userOrdersPagedResources);
		return ResponseEntity.ok(jsonUserOrdersPagedResources);
	}
	
	@GetMapping(path = "/{id}/phones")
	public ResponseEntity<String> userPhones(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable phonesPage = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Phone> allPhonesByUserPage = phonesService.findAllPhonesByUser(phonesPage, id);
		Resources<Resource<Phone>> userPhonesPagedResources =
			phonesResourceAssembler.toPagedSubResources(allPhonesByUserPage, id, GET_USER_PHONES_METHOD_NAME);
		String jsonUserPhonesPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(userPhonesPagedResources);
		return ResponseEntity.ok(jsonUserPhonesPagedResources);
	}
	
	@GetMapping(path = "/{id}/authorities")
	public ResponseEntity<String> userGrantedAuthorities(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable phonesPage = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<WorkshopGrantedAuthority> authoritiesByUserPage = workshopGrantedAuthoritiesService.findAllGrantedAuthoritiesByUser(phonesPage, id);
		Resources<Resource<WorkshopGrantedAuthority>> userAuthoritiesPagedResources =
			workshopGrantedAuthoritiesResourceAssembler.toPagedSubResources(
				authoritiesByUserPage, id, GET_USER_AUTHORITIES_METHOD_NAME);
		String jsonUserAuthoritiesPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(userAuthoritiesPagedResources);
		return ResponseEntity.ok(jsonUserAuthoritiesPagedResources);
	}
}
