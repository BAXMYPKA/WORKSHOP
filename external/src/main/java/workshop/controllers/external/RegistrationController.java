package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.external.dto.UserDto;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.exceptions.EntityNotFoundException;
import workshop.internal.services.UsersService;
import workshop.internal.services.UuidsService;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Objects;

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
	
	@Value("${url}")
	private String workshopUrl;
	
	/**
	 * @return Just returns "registration.html" view
	 */
	@GetMapping
	public String getRegistration(Model model) {
		model.addAttribute("userDto", userDto);
		return "registration";
	}
	
	/**
	 * After registration process in {@link #postRegistration(UserDto, BindingResult, Model, Locale)} receives the newly-created
	 *  {@link Uuid}.
	 *  If it is valid shows the 'userMessage' to follow the link from their email to activate their account.
	 *  If not just redirects to the '/registration' page.
	 * @param uuid
	 * @param redirectAttributes
	 * @param model
	 * @param locale
	 * @return
	 */
	@GetMapping(params = {"uuid"})
	public String getRegistrationConfirmationNeeded(
		@RequestParam(name = "uuid") String uuid, RedirectAttributes redirectAttributes, Model model, Locale locale) {
		model.addAttribute("userDto", userDto);
		
		if (uuid.isEmpty()) {
			String userMessageNullUuid = getMessageSource().getMessage("message.uuidNull", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageNullUuid);
			return "registration";
		}
		try {  //The following code for the demo purposes only. The real one will be a bit different.
			Uuid uuidEntity = uuidsService.findByProperty("uuid", uuid).get(0);
			String registrationConfirmationUrl = workshopUrl + "login?uuid=" + uuidEntity.getUuid();
			String confirmRegistrationLink = "<a href=\"" + registrationConfirmationUrl + "\">Подтвердить регистрацию</a>.";
			String userMessageRegistrationConfirmation = getMessageSource().getMessage(
				"message.confirmRegistrationRequiredDemo(3)",
				new Object[]{uuidEntity.getUser().getEmail(), uuidEntity.getUuid(), confirmRegistrationLink},
				locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageRegistrationConfirmation);
			return "registration";
		} catch (EntityNotFoundException e) {
			log.debug("UUID={} not found in the DataBase!", uuid);
			String userMessageUuidNotValid = getMessageSource().getMessage("message.uuidNotValid", null, locale);
			getUserMessagesCreator().setMessageForUser(redirectAttributes, userMessageUuidNotValid);
			return "redirect:/registration";
		}
	}
	
	/**
	 * Receives the {@link UserDto} to be registered.
	 * If valid persists new non-enabled {@link User} with the linked appropriate {@link Uuid} and returns the
	 * '/registration' page with the 'uuid' parameter.
	 *
	 * @param userDto       {@link UserDto} object to be evaluated for being persisted.
	 * @param bindingResult Will be returned if UserDto contains {@link org.springframework.validation.FieldError}s
	 */
	@PostMapping
	public String postRegistration(@Validated({Persist.class}) @ModelAttribute(name = "userDto") UserDto userDto,
								   BindingResult bindingResult, Model model, Locale locale) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("userDto", userDto);
			return "registration";
		} else if (checkUserExists(bindingResult, userDto, locale).hasErrors()) {
			model.addAttribute("userDto", userDto);
			return "registration";
		}
		User newUser = modelMapper.map(userDto, User.class);
		try {
			newUser = usersService.createNewUser(newUser);
			Uuid uuid = uuidsService.findByProperty("uuid", newUser.getUuid().getUuid()).get(0);
			model.addAttribute("uuid", uuid.getUuid());
			return "redirect:/registration";
		} catch (RuntimeException e) {
			System.out.println(e.getMessage());
			//TODO: sent internal server error?
			return "redirect:/registration";
		}
	}
	
	private BindingResult checkUserExists(BindingResult bindingResult, UserDto userDto, Locale locale) {
		try {
			usersService.findByLogin(Objects.requireNonNull(userDto.getEmail()));
			String emailExistFieldErrorMessage = getMessageSource().getMessage(
				"message.emailExist(1)", new Object[]{userDto.getEmail()}, locale);
			FieldError fieldError = new FieldError("userDto", "email", emailExistFieldErrorMessage);
			bindingResult.addError(fieldError);
			return bindingResult;
		} catch (EntityNotFoundException e) {
			return bindingResult;
		}
	}
}
