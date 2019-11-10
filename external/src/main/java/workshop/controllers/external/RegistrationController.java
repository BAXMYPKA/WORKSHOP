package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.controllers.utils.UserMessagesCreator;
import workshop.external.dto.UserDto;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.services.UsersService;
import workshop.internal.services.UuidsService;

import javax.servlet.http.HttpServletResponse;
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
	
	@Autowired
	private UuidsService uuidsService;
	
	@Autowired
	private UserMessagesCreator userMessagesCreator;
	
	@GetMapping
	public String getRegistration(Model model) {
		model.addAttribute("userDto", userDto);
		return "registration";
	}
	
	@GetMapping(params = "success")
	public String getSuccessRegistration(@RequestParam(name = "success") Boolean success, Model model, Locale locale) {
		
		model.addAttribute("userDto", userDto);
		if (success) {
			String userMessageRegistrationConfirmation = getMessageSource().getMessage("message.confirmRegistration(2)",
				new Object[]{"", ""}, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageRegistrationConfirmation);
		} else {
			String userMessageRegistrationUnsuccessful = "";
		}
		
		return "registration";
	}
	
	@PostMapping
	public String postRegistration(@Validated({Persist.class})	@ModelAttribute(name = "userDto") UserDto userDto,
		BindingResult bindingResult, HttpServletResponse response) {
		if (bindingResult.hasErrors()) {
			return "registration";
		}
		User newUser = modelMapper.map(userDto, User.class);
		usersService.createNewUser(newUser);
		Uuid uuid = uuidsService.findByProperty("uuid", newUser.getUuid().getUuid()).get(0);
		response.addHeader("UUID", uuid.getUuid());
		return "redirect:/registration?success=true";
	}
}
