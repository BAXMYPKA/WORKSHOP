package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.Order;
import internal.service.JsonService;
import internal.service.OrdersService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	private JsonService jsonService;
	private ObjectMapper objectMapper;
	
	@GetMapping(path = "/all", params = {"size", "page"})
	public ResponseEntity<String> getAll(@RequestParam(value = "size") Integer size,
										 @RequestParam(value = "page") Integer page,
										 @RequestParam(name = "sort", required = false) String orderBy,
										 @RequestParam(name = "asc-desc", required = false) String ascDesc)
		throws JsonProcessingException {
		
		Optional<List<Order>> allOrders = ordersService.findAllOrders(
			size,
			page,
			(orderBy != null && !orderBy.isEmpty()) ? orderBy : "",
			"asc".equalsIgnoreCase(ascDesc) ? ascDesc : "desc");
		if (allOrders.isPresent() && !allOrders.get().isEmpty()) {
			String jsonOrders = jsonService.convertEntitiesToJson(allOrders.get());
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
	
	//TODO: exception handler
	
	@PostConstruct
	private void afterPropsSet() {
		objectMapper = jsonService.getObjectMapper();
	}
}
