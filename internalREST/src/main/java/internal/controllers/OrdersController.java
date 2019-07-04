package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.dao.EmployeesDao;
import internal.entities.Employee;
import internal.entities.Order;
import internal.service.EmployeesService;
import internal.service.JsonService;
import internal.service.OrdersService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

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
	private JsonService jsonService;
	private ObjectMapper objectMapper;
	private final int PAGE_SIZE = OrdersService.PAGE_SIZE;
	private final int MAX_PAGE_NUM = OrdersService.MAX_PAGE_NUM;
	
	@GetMapping(path = "/all", params = {"size", "page"})
	public ResponseEntity<String> getOrders(@RequestParam(value = "size") Integer size,
											@RequestParam(value = "page") Integer page,
											@RequestParam(name = "order-by", required = false) String orderBy,
											@RequestParam(name = "order", required = false) String order)
		throws JsonProcessingException {
		
		Pageable pageable = getPageable(size, page, orderBy, order);
		Page<Order> ordersPage = ordersService.findAllOrders(pageable, orderBy);
		
		if (ordersPage != null && !ordersPage.getContent().isEmpty()) {
			String jsonOrders = jsonService.convertEntitiesToJson(ordersPage.getContent());
			return ResponseEntity.ok(jsonOrders);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No Orders found!");
		}
	}
	
	@GetMapping(path = "/{id}")
	public ResponseEntity<String> getOrder(@PathVariable("id") long id) throws JsonProcessingException {
		if (id <= 0) {
			return new ResponseEntity<>("The 'id' parameter has to be above zero!", HttpStatus.BAD_REQUEST);
		}
		Optional<Order> order = ordersService.findById(id);
		if (order.isPresent()) {
			String jsonOrder = jsonService.convertEntityToJson(order.get());
			return ResponseEntity.ok(jsonOrder);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The Order with id=" + id + " not found!");
		}
	}
	
	/**
	 * Order may contain a new single Task and a new single User object without 'id' - they will be treated as new ones
	 * and persisted in the DataBase.
	 * If Order has to contain a few new Tasks - they all have to be persisted BEFORE the persistence the Order.
	 * If any of them will throw an Exception during a persistence process - the whole Order won't be saved!
	 * @param order Order object as JSON
	 * @param authentication The Employee to save in Order.createdBy field
	 * @return Either persisted Order or Http error with a description
	 * @throws JsonProcessingException
	 * @throws HttpMessageNotReadableException
	 */
	@PostMapping(consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<String> postOrder(@Valid @RequestBody Order order, Authentication authentication)
		throws JsonProcessingException, HttpMessageNotReadableException {
		
		//TODO: jsr validation
		Optional<Order> persistedOrder = ordersService.persistOrder(order, authentication);
		if (persistedOrder.isPresent()) {
			String jsonPersistedOrder = jsonService.convertEntityToJson(persistedOrder.get());
			return ResponseEntity.status(HttpStatus.CREATED).body(jsonPersistedOrder);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The Order creation failure!");
		}
	}
	
	//TODO: exception handler
	
	@PostConstruct
	private void afterPropsSet() {
		objectMapper = jsonService.getObjectMapper();
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
