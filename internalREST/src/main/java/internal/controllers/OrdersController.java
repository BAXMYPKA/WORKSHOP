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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Slf4j
@Getter
@Setter
@DependsOn("ordersService")
@RestController
@RequestMapping(path = "/internal/orders")
public class OrdersController {
	
	@Autowired
	private OrdersService ordersService;
	@Autowired
	private JsonService jsonService;
	private ObjectMapper objectMapper;
	
	@GetMapping(path = "/all", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE}, params = {"size", "page", "sort"})
	public String getAll(@RequestParam("size") int size,
						 @RequestParam("page") int page,
						 @RequestParam(name = "sort", required = false) String sortBy) throws JsonProcessingException {
		List<Order> allOrders = ordersService.findAllOrders();
		String jsonOrders = jsonService.convertEntitiesToJson(allOrders);
		return jsonOrders;
	}
	
	@PostConstruct
	private void afterPropsSet() {
		objectMapper = jsonService.getObjectMapper();
	}
}
