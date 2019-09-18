package workshop.internal.controllers;

import workshop.internal.entities.ExternalAuthority;
import workshop.internal.entities.Order;
import workshop.internal.entities.Phone;
import workshop.internal.entities.User;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.UpdateValidation;
import workshop.internal.hateoasResources.ExternalAuthoritiesResourceAssembler;
import workshop.internal.hateoasResources.OrdersResourceAssembler;
import workshop.internal.hateoasResources.PhonesResourceAssembler;
import workshop.internal.hateoasResources.UsersResourceAssembler;
import workshop.internal.services.ExternalAuthoritiesService;
import workshop.internal.services.OrdersService;
import workshop.internal.services.PhonesService;
import workshop.internal.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;

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
	private ExternalAuthoritiesService externalAuthoritiesService;
	@Autowired
	private OrdersResourceAssembler ordersResourceAssembler;
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	@Autowired
	private ExternalAuthoritiesResourceAssembler externalAuthoritiesResourceAssembler;
	
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
		Page<ExternalAuthority> authoritiesByUserPage =
			externalAuthoritiesService.findAllGrantedAuthoritiesByUser(phonesPage, id);
		Resources<Resource<ExternalAuthority>> userAuthoritiesPagedResources =
			externalAuthoritiesResourceAssembler.toPagedSubResources(
				authoritiesByUserPage, id, GET_USER_AUTHORITIES_METHOD_NAME);
		String jsonUserAuthoritiesPagedResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(userAuthoritiesPagedResources);
		return ResponseEntity.ok(jsonUserAuthoritiesPagedResources);
	}
	
	@PostMapping(path = "{id}/authorities")
	public ResponseEntity<String> postForbiddenMethodUserAuthority(@PathVariable(name = "id") Long id,
																   @RequestBody ExternalAuthority externalAuthority,
																   HttpServletRequest request) {
		String forbiddenMethodMessage = getMessageSource().getMessage(
			"httpStatus.forbidden.withDescription(2)",
			new Object[]{request.getMethod(), " Use dedicated ExternalAuthorities Link for this operation!"},
			LocaleContextHolder.getLocale());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(forbiddenMethodMessage);
	}
	
	/**
	 * Adds an ExternalAuthority to the User.
	 *
	 * @return HttpStatus.ACCEPTED in case of success.
	 */
	@PutMapping(path = "{id}/authorities")
	public ResponseEntity<String> putUserExternalAuthority(
		@PathVariable(name = "id") Long id,
		@Validated(UpdateValidation.class) @RequestBody ExternalAuthority externalAuthority,
		BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		User user = getWorkshopEntitiesService().findById(id);
		user.addGrantedAuthority(externalAuthority);
		user = getWorkshopEntitiesService().mergeEntity(user);
		if (externalAuthority.getUsers() != null) {
			externalAuthority.getUsers().add(user);
		} else {
			externalAuthority.setUsers(new HashSet<>(Collections.singletonList(user)));
		}
		Resource<ExternalAuthority> externalAuthorityResource =
			externalAuthoritiesResourceAssembler.toResource(externalAuthority);
		String jsonExternalAuthorityResource =
			getJsonServiceUtils().workshopEntityObjectsToJson(externalAuthorityResource);
		return ResponseEntity.accepted().body(jsonExternalAuthorityResource);
	}
	
	/**
	 * Removes an ExternalAuthority from a User.
	 *
	 * @param id          User.ID to remove ExternalAuthority from.
	 * @param authorityId ExternalAuthority.ID to be removed from the User.
	 * @return HttpStatus.NO_CONTENT in case of success.
	 */
	@DeleteMapping(path = "/{id}/authorities/{authorityId}")
	public ResponseEntity<String> deleteUserExternalAuthority(@PathVariable(name = "id") Long id,
															  @PathVariable(name = "authorityId") Long authorityId) {
		User user = getWorkshopEntitiesService().findById(id);
		boolean isRemoved = user.getExternalAuthorities()
			.removeIf(externalAuthority -> externalAuthority.getIdentifier().equals(authorityId));
		if (isRemoved) {
			getWorkshopEntitiesService().mergeEntity(user);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} else {
			String notFoundMessage = getMessageSource().getMessage(
				"httpStatus.notFound(2)",
				new Object[]{"ExternalAuthority.ID=" + authorityId, getWorkshopEntityClassName() + ".ID=" + id},
				LocaleContextHolder.getLocale());
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, notFoundMessage);
		}
	}
}
