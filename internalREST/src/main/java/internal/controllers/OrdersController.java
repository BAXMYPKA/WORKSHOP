package internal.controllers;

import internal.entities.Order;
import internal.service.OrdersService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@Getter
@Setter
@RestController
@RequestMapping(path = "/internal/orders")
public class OrdersController {
	
	@Autowired
	public OrdersService ordersService;
	
	@GetMapping(path = "/all", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public Set<Order> getAll() {
		List<Order> allOrders = ordersService.findAllOrders();
		return null;
	}
}
