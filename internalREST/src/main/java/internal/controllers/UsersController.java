package internal.controllers;

import internal.entities.Order;
import internal.entities.Phone;
import internal.entities.User;
import internal.entities.WorkshopGrantedAuthority;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import internal.hateoasResources.OrdersResourceAssembler;
import internal.hateoasResources.PhonesResourceAssembler;
import internal.hateoasResources.UsersResourceAssembler;
import internal.hateoasResources.WorkshopGrantedAuthoritiesResourceAssembler;
import internal.services.OrdersService;
import internal.services.PhonesService;
import internal.services.UsersService;
import internal.services.WorkshopGrantedAuthoritiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Component
@RequestMapping(path = "/internal/users", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RestController
@ExposesResourceFor(User.class)
public class UsersController extends WorkshopControllerAbstract<User> {
	
	public static final String GET_USER_ORDERS_METHOD_NAME = "getUserOrders";
	public static final String GET_USER_PHONES_METHOD_NAME = "getUserPhones";
	public static final String GET_USER_AUTHORITIES_METHOD_NAME = "getUserGrantedAuthorities";
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
	public ResponseEntity<String> getUserOrders(
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
	
	/**
	 * Receives a new Order, sets it 'createdFor' a given User and persists.
	 *
	 * @param id    User.ID
	 * @param order A new Order to be persisted.
	 * @return The persisted Order with this User set.
	 */
	@PostMapping(path = "{id}/orders")
	public ResponseEntity<String> postUserOrder(@PathVariable(name = "id") Long id,
												@Validated(PersistenceValidation.class) @RequestBody Order order,
												BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		User user = getWorkshopEntitiesService().findById(id);
		order.setCreatedFor(user);
		order = ordersService.persistEntity(order);
		Resource<Order> orderResource = ordersResourceAssembler.toResource(order);
		String jsonOrderResource = getJsonServiceUtils().workshopEntityObjectsToJson(orderResource);
		return ResponseEntity.ok(jsonOrderResource);
	}
	
	/**
	 * Receives an existing Order, sets it 'createdFor' a given User and updates it in the DataBase.
	 *
	 * @param id    User.ID
	 * @param order An existing Order to be set 'createdFor' this User.
	 * @return The updated Order with this User set.
	 */
	@PutMapping(path = "{id}/orders")
	public ResponseEntity<String> putUserOrder(@PathVariable(name = "id") Long id,
											   @Validated(UpdateValidation.class) @RequestBody Order order,
											   BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		User user = getWorkshopEntitiesService().findById(id);
		order.setCreatedFor(user);
		order = ordersService.mergeEntity(order);
		Resource<Order> orderResource = ordersResourceAssembler.toResource(order);
		String jsonOrderResource = getJsonServiceUtils().workshopEntityObjectsToJson(orderResource);
		return ResponseEntity.ok(jsonOrderResource);
	}
	
	/**
	 * Just removes an Order from a given User.
	 *
	 * @param id      User.ID
	 * @param orderId Order.ID to be removed from a User.
	 * @return HttpStatus.NO_CONTENT
	 */
	@DeleteMapping(path = "{id}/orders/{orderId}")
	public ResponseEntity<String> deleteUserOrder(@PathVariable(name = "id") Long id,
												  @PathVariable(name = "orderId") Long orderId) {
		User user = getWorkshopEntitiesService().findById(id);
		Order order = ordersService.findById(orderId);
		order.setCreatedFor(null);
		ordersService.mergeEntity(order);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	@GetMapping(path = "/{id}/phones")
	public ResponseEntity<String> getUserPhones(
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
	
	@PostMapping(path = "{id}/phones")
	public ResponseEntity<String> postUserPhone(@PathVariable(name = "id") Long id,
												@Validated(PersistenceValidation.class) @RequestBody Phone phone,
												BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		User user = getWorkshopEntitiesService().findById(id);
		phone.setUser(user);
		phone = phonesService.persistEntity(phone);
		Resource<Phone> phoneResource = phonesResourceAssembler.toResource(phone);
		String jsonPhoneResource = getJsonServiceUtils().workshopEntityObjectsToJson(phoneResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonPhoneResource);
	}
	
	@PutMapping(path = "{id}/phones")
	public ResponseEntity<String> putUserPhone(@PathVariable(name = "id") Long id,
											   @Validated(UpdateValidation.class) @RequestBody Phone phone,
											   BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		User user = getWorkshopEntitiesService().findById(id);
		phone.setUser(user);
		phone = phonesService.mergeEntity(phone);
		Resource<Phone> phoneResource = phonesResourceAssembler.toResource(phone);
		String jsonPhoneResource = getJsonServiceUtils().workshopEntityObjectsToJson(phoneResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonPhoneResource);
	}
	
	/**
	 * Just removes a Phone from a given User.
	 *
	 * @param id      User.ID
	 * @param phoneId Phone.ID to be removed from a User.
	 * @return HttpStatus.NO_CONTENT
	 */
	@DeleteMapping(path = "{id}/phones/{phoneId}")
	public ResponseEntity<String> deleteUserPhone(@PathVariable(name = "id") Long id,
												  @PathVariable(name = "phoneId") Long phoneId) {
		User user = getWorkshopEntitiesService().findById(id);
		if (user.getPhones() != null && user.getPhones().stream().anyMatch(phone -> phone.getIdentifier().equals(phoneId))) {
			Phone userPhone = user.getPhones().stream()
				.filter(phone -> phone.getIdentifier().equals(phoneId))
				.findAny()
				.get();
			userPhone.setUser(null);
			phonesService.mergeEntity(userPhone);
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	
	@GetMapping(path = "/{id}/authorities")
	public ResponseEntity<String> getUserGrantedAuthorities(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable phonesPage = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<WorkshopGrantedAuthority> authoritiesByUserPage =
			workshopGrantedAuthoritiesService.findAllGrantedAuthoritiesByUser(phonesPage, id);
		Resources<Resource<WorkshopGrantedAuthority>> userAuthoritiesPagedResources =
			workshopGrantedAuthoritiesResourceAssembler.toPagedSubResources(
				authoritiesByUserPage, id, GET_USER_AUTHORITIES_METHOD_NAME);
		String jsonUserAuthoritiesPagedResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(userAuthoritiesPagedResources);
		return ResponseEntity.ok(jsonUserAuthoritiesPagedResources);
	}
}
