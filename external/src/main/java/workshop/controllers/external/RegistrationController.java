package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.external.dto.UserDto;
import workshop.internal.entities.User;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.services.UsersService;

import javax.validation.groups.Default;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping(path = "/registration")
public class RegistrationController extends WorkshopControllerAbstract {
	
	@Autowired
	private UserDto userDto;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private UsersService usersService;
	
	
	@GetMapping
	public String getRegistration(Model model) {
		model.addAttribute("userDto", userDto);
		return "registration";
	}
	
	@PostMapping
	public String postRegistration(@Validated({Persist.class})
	@ModelAttribute(name = "userDto") UserDto userDto, BindingResult bindingResult, Locale locale) {
		if (bindingResult.hasErrors()) {
			return "registration";
		}
		System.out.println(userDto);
		User newUser = modelMapper.map(userDto, User.class);
		System.out.println(newUser);
		usersService.createNewUser(newUser);
		return "redirect:/registration?success=true";
	}
}
