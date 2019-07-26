package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.Order;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import internal.exceptions.PersistenceFailure;
import internal.service.EmployeesService;
import internal.service.serviceUtils.JsonServiceUtils;
import internal.service.OrdersService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.groups.Default;

@Slf4j
@Getter
@Setter
@DependsOn("ordersService")
@RestController
@RequestMapping(path = "/internal/orders", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class OrdersController {
	
	@Autowired
	private OrdersService ordersService;
	@Autowired
	private EmployeesService employeesService;
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	private ObjectMapper objectMapper;
	@Value("${default.page.size}")
	private int PAGE_SIZE;
	@Value("${default.page.max_num}")
	private int MAX_PAGE_NUM;
	
	/**
	 * @param size    Non-required amount of Orders on one page. Default is OrdersService.DEFAULT_PAGE_SIZE
	 * @param page    Number of page with the list of Orders. One page contains 'size' amount of Orders.
	 * @param orderBy The property of Order all the Orders have to be ordered by.
	 * @param order   Ascending or descending order.
	 * @return
	 * @throws JsonProcessingException
	 */
	@GetMapping(path = "/all")
	public ResponseEntity<String> getOrders(@RequestParam(value = "size", required = false) Integer size,
											@RequestParam(value = "page", required = false) Integer page,
											@RequestParam(name = "order-by", required = false) String orderBy,
											@RequestParam(name = "order", required = false) String order)
		throws JsonProcessingException {
		
		Pageable pageable = getPageable(size, page, orderBy, order);
		Page<Order> ordersPage = ordersService.findAllEntities(pageable, orderBy);
		
		if (ordersPage != null && !ordersPage.getContent().isEmpty()) {
			String jsonOrders = jsonServiceUtils.convertEntitiesToJson(ordersPage.getContent());
			return ResponseEntity.ok(jsonOrders);
		} else {
			throw new PersistenceFailure("No Orders found!", HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping(path = "/{id}")
	public ResponseEntity<String> getOrder(@PathVariable("id") long id) throws JsonProcessingException {
		if (id <= 0) {
			return new ResponseEntity<>("The 'id' parameter has to be above zero!", HttpStatus.BAD_REQUEST);
		}
		Order order = ordersService.findById(id);
		String jsonOrder = jsonServiceUtils.convertEntityToJson(order);
		return ResponseEntity.ok(jsonOrder);
	}
	
	/**
	 * Order may contain a new single Task and a new single User object without 'id' - they will be treated as new ones
	 * and persisted in the DataBase.
	 * If Order has to contain a few new Tasks - they all have to be persisted BEFORE the persistence the Order.
	 * If any of them will throw an Exception during a persistence process - the whole Order won't be saved!
	 *
	 * @param order Order object as JSON
	 * @return Either persisted Order or Http error with a description
	 * @throws JsonProcessingException
	 * @throws HttpMessageNotReadableException
	 */
	@PostMapping(consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<String> postOrder(@Validated(value = {PersistenceCheck.class, Default.class})
											@RequestBody Order order,
											BindingResult bindingResult)
		throws JsonProcessingException, HttpMessageNotReadableException, MethodArgumentNotValidException {
		
		if (bindingResult.hasErrors()) { //To be processed by ExceptionHandlerController.validationFailure()
			throw new MethodArgumentNotValidException(null, bindingResult);
		} else if (order.getId() > 0) {
			bindingResult.addError(
				new FieldError("Order.id", "id", "'id' field for the new Order has to be zero!"));
			throw new MethodArgumentNotValidException(null, bindingResult);
		}
		Order persistedOrder = ordersService.persistEntity(order);
		String jsonPersistedOrder = jsonServiceUtils.convertEntityToJson(persistedOrder);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonPersistedOrder);
	}
	
	@PutMapping(consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<String> putOrder(@Validated({UpdationCheck.class, Default.class})
										   @RequestBody Order order,
										   BindingResult bindingResult) throws MethodArgumentNotValidException, JsonProcessingException {
		if (bindingResult.hasErrors()) { //To be processed by ExceptionHandlerController.validationFailure()
			throw new MethodArgumentNotValidException(null, bindingResult);
		}
		Order mergedOrder = ordersService.mergeEntity(order);
		String serializedOrder = jsonServiceUtils.convertEntityToJson(mergedOrder);
		return ResponseEntity.ok(serializedOrder);
	}
	
	/**
	 * @param id Long.class
	 * @return HttpStatus 200 with a successful message.
	 * If no Entity for such an id will be found the HttpStatus 404 'NotFound' will be returned.
	 */
	@DeleteMapping(path = "/{id}")
	public ResponseEntity<String> deleteOrder(@PathVariable(name = "id") Long id) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Order id has to be above zero!");
		}
		ordersService.removeEntity(id);
		return ResponseEntity.status(HttpStatus.OK).body("Order " + id + " has been successfully deleted!");
	}
	
	@PostConstruct
	private void afterPropsSet() {
		objectMapper = jsonServiceUtils.getObjectMapper();
	}
	
	private Pageable getPageable(int size, int page, String orderBy, String order) {
		Sort.Direction direction = null;
		if (order != null && !order.isEmpty()) { //'Order' param is set in the Request
			try {
				direction = Sort.Direction.fromString(order);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("'Order' parameter must be equals 'asc' or 'desc' value!");
			}
		} else { //DESC is the default value if 'order' param is not presented in the Request
			direction = Sort.Direction.DESC;
		}
		//PageRequest doesn't allow empty parameters strings, so "created" as the default is used
		orderBy = orderBy == null || orderBy.isEmpty() ? "created" : orderBy;
		int pageSize = size <= 0 || size > PAGE_SIZE ? PAGE_SIZE : size;
		int pageNum = page <= 0 || page > MAX_PAGE_NUM ? 1 : page;
		
		return PageRequest.of(
			pageNum,
			pageSize,
			new Sort(direction, orderBy));
	}
}
