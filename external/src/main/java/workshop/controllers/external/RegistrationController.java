package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.external.dto.UserDto;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.services.UsersService;
import workshop.internal.services.UuidsService;

import javax.servlet.http.HttpServletRequest;
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
	 * Receives {@link #postRegistration(UserDto, BindingResult, RedirectAttributes)} redirect with params
	 * "success=true" and "uuid=uuidString".
	 * Validates the given UUID and returns the message states that email with a confirmation link has been sent to the
	 * {@link User}.
	 *
	 * @param success Obligatory
	 * @param uuid    Obligatory
	 * @param request Just now only for demo purposes to construct a link from.
	 * @return '/registration' page with the message states that email with a confirmation link has been sent to the
	 * {@link User}.
	 */
	@GetMapping(params = {"success", "uuid"})
	public String getRegistrationConfirmationNeeded(@RequestParam(name = "success") Boolean success,
													@RequestParam(name = "uuid") String uuid,
													Model model, Locale locale,
													RedirectAttributes redirectAttributes) {
		model.addAttribute("userDto", userDto);
		
		if (success) { //The following code for the demo purposes only. The real one will be a bit different.
			if (uuid.isEmpty()) {
				String userMessageNullUuid = getMessageSource().getMessage("message.uuidNull", null, locale);
				getUserMessagesCreator().setMessageForUser(model, userMessageNullUuid);
				return "registration";
			}
			Uuid uuidEntity = uuidsService.findByProperty("uuid", uuid).get(0);
			String registrationConfirmationUrl = workshopUrl + "login?uuid="+uuid;
			String confirmRegistrationLink = "<a href=\"" + registrationConfirmationUrl + "\">Подтвердить регистрацию</a>.";
			String userMessageRegistrationConfirmation = getMessageSource().getMessage(
				"message.confirmRegistration(3)",
				new Object[]{uuidEntity.getUser().getEmail(), uuidEntity.getUuid(), confirmRegistrationLink},
				locale);
			getUserMessagesCreator().setMessageForUser(rmodel, userMessageRegistrationConfirmation);
		} else {
			//TODO: to complete after the RequestParameter 'success=false' will be implemented
			String userMessageRegistrationUnsuccessful = "";
		}
		return "registration";
	}
	
	/**
	 * Receives the final {@link Uuid} from which this method identifies the previously registered {@link User}
	 * ,grands access to the '/profile' page and deletes the unnecessary temporary UUID, connected with this User.
	 *
	 * @param uuid               Previously saved {@link Uuid} with the newly registered {@link User}
	 * @param redirectAttributes Are used after successful confirmation to pass the special 'userMessage' as a request
	 *                           parameter to '/login' page.
	 * @return '/registration' page if the confirmation was unsuccessful or '/login' page with the special 'userMessage'
	 * in both cases.
	 */
	@GetMapping(params = "uuid")
	public String getRegistrationConfirmation(
		@RequestParam(name = "uuid") String uuid, Model model, Locale locale, RedirectAttributes redirectAttributes) {
		model.addAttribute("userDto", userDto);
		
		if (uuid.isEmpty()) {
			String userMessageNullUuid = getMessageSource().getMessage("message.uuidNull", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageNullUuid);
			return "registration";
		}
		try {
			//TODO: users can confirm themselves just entered with the UUID without logging in!!!
			User user = usersService.confirmNewUserByUuid(uuid);
			String username = user.getFirstName() != null ? user.getFirstName() : "" + " " + user.getLastName();
			String userMessageConfirmed =
				getMessageSource().getMessage("message.confirmRegistrationSuccess(1)", new Object[]{username}, locale);
			String userMessageConfirmationNeeded = getMessageSource().getMessage("message.confirmRegistrationNeeded(1)")
			getUserMessagesCreator().setMessageForUser(redirectAttributes, userMessageConfirmed);
			return "redirect:/login";
		} catch (EntityNotFoundException e) { //UUID is not valid or outdated
			log.debug("UUID={} not found in the DataBase!", uuid);
			String userMessageUuidNotValid = getMessageSource().getMessage("message.uuidNotValid", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageUuidNotValid);
			return "registration";
		}
	}
	
	/**
	 * Receives {@link UserDto} with new properties, evaluates them and either redirects to "/registration" with
	 * "success=true" or returns same link with errors.
	 *
	 * @param redirectAttributes Is used to send a newly created UUID for the User.
	 */
	@PostMapping
	public String postRegistration(@Validated({Persist.class}) @ModelAttribute(name = "userDto") UserDto userDto,
								   BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "registration";
		}
		User newUser = modelMapper.map(userDto, User.class);
		usersService.createNewUser(newUser);
		Uuid uuid = uuidsService.findByProperty("uuid", newUser.getUuid().getUuid()).get(0);
		redirectAttributes.addAttribute("uuid", uuid.getUuid());
		return "redirect:/login";
	}
}
