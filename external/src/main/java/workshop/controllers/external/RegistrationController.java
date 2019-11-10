package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.controllers.utils.UserMessagesCreator;
import workshop.external.dto.UserDto;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.exceptions.IllegalArgumentsException;
import workshop.internal.services.UsersService;
import workshop.internal.services.UuidsService;

import javax.servlet.http.HttpServletRequest;
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
	
	@GetMapping(params = {"success", "uuid"})
	public String getRegistrationSuccess(@RequestParam(name = "success") Boolean success, Model model, Locale locale,
		HttpServletRequest request) {
		model.addAttribute("userDto", userDto);
		
		if (success) { //The following code for the demo purposes only. The real one will be a bit different.
			if (request.getParameter("uuid") == null || request.getParameter("uuid").isEmpty()) {
				getUserMessagesCreator()
					.setMessageForUser(model, "Request header 'UUID' cannot be null or empty!");
				return "registration";
			}
			long uuidId = Long.parseLong(request.getParameter("uuid"));
			Uuid uuid = uuidsService.findById(uuidId);
			String registrationConfirmationUrl = request.getRequestURL().append("?uuid=").append(uuid.getUuid()).toString();
			String confirmRegistrationLink = "<a href=\"" + registrationConfirmationUrl + "\">Подтвердить регистрацию</a>.";
			String userMessageRegistrationConfirmation2 =
				getMessageSource().getMessage("message.confirmRegistration(3)",
					new Object[]{uuid.getUser().getEmail(), uuid.getUuid(), confirmRegistrationLink}, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageRegistrationConfirmation2);
		} else {
			//TODO: to complete after the RequestParameter 'success=false' will be implemented
			String userMessageRegistrationUnsuccessful = "";
		}
		return "registration";
	}
	
	@GetMapping(params = "uuid")
	public String getRegistrationConfirmation(@RequestParam(name = "uuid") String uuid, Model model, Locale locale) {
		model.addAttribute("userDto", userDto);
		if (uuid == null || uuid.isEmpty()) {
			String userMessageNullUuid = getMessageSource().getMessage("message.uuidNull", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageNullUuid);
			return "registration";
		}
		try {
			User confirmedUser = usersService.confirmNewUserByUuid(uuid);
		} catch (EntityNotFoundException e) { //UUID is not valid or outdated
			log.debug("UUID={} not found in the DataBase!", uuid);
			String userMessageUuidNotValid = getMessageSource().getMessage("message.uuidNotValid", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageUuidNotValid);
			return "registration";
		}
		return "registration";
	}
	
	@PostMapping
	public String postRegistration(@Validated({Persist.class}) @ModelAttribute(name = "userDto") UserDto userDto,
		BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "registration";
		}
		User newUser = modelMapper.map(userDto, User.class);
		usersService.createNewUser(newUser);
		Uuid uuid = uuidsService.findByProperty("uuid", newUser.getUuid().getUuid()).get(0);
		redirectAttributes.addAttribute("uuid", uuid.getIdentifier().toString());
		return "redirect:/registration?success=true";
	}
}
