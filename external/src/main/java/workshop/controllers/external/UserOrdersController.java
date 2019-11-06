package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.controllers.utils.ErrorMessagesJsonMapper;
import workshop.external.dto.UserDto;
import workshop.internal.entities.User;
import workshop.internal.services.UsersService;

@Controller
@RequestMapping(path = "/profile/orders")
public class UserOrdersController extends WorkshopControllerAbstract {
	
	@Autowired
	private ErrorMessagesJsonMapper errorMessagesJsonMapper;
	
	@Autowired
	private UsersService usersService;
	
	@Autowired
	private UserDto userDto;
	
	@GetMapping
	public String getUserOrders(Model model, Authentication authentication) {
		User user = usersService.findByLogin(authentication.getName());
		model.addAttribute("orders", user.getOrders());
		return "userOrders";
	}
	
	@GetMapping(path = "/{orderId}")
	public String getUserOrder(@PathVariable(name = "orderId") Long orderId, Authentication authentication, Model model) {
		User user = usersService.findByLogin(authentication.getName());
		if (user.getOrders() != null) {
			user.getOrders().stream()
				.filter(order -> order.getIdentifier().equals(orderId))
				.findFirst()
				.ifPresent(order -> model.addAttribute("order", order));
		} else {
		//
		}
		return "userOrder";
	}
}
