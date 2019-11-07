package workshop.controllers.external;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.external.dto.UserDto;
import workshop.internal.entities.User;
import workshop.internal.entities.hibernateValidation.Merge;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.services.UsersService;

import javax.validation.groups.Default;
import java.awt.*;
import java.io.IOException;
import java.util.Locale;

@Controller
@RequestMapping(path = "/profile")
public class UserProfileController extends WorkshopControllerAbstract {
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private UserDto userDto;
	
	@Autowired
	private UsersService usersService;
	
	/**
	 * By every request adds the "user" attribute as the {@link User} obtained from the given {@link Authentication}
	 * if that User is present and not 'Anonymous'.
	 *
	 * @param authentication A {@link User#getEmail()} to be obtained from (if present).
	 * @return "profile.html"
	 * @throws EntityNotFoundException If a {@link User} cannot be obtained from the given {@link Authentication}
	 */
	@GetMapping
	public String getProfile(Model model, Authentication authentication, Locale locale) throws EntityNotFoundException {
		if (authentication == null || authentication.getPrincipal().toString().equals("Anonymous")) {
			return "profile";
		} else {
			User userByLogin = usersService.findByLogin(authentication.getName());
			userDto.setUser(userByLogin);
			model.addAttribute("userDto", userDto);
		}
		return "profile";
	}
	
	/**
	 * Receives an existing form data with {@link UserDto} and map its properties to existing {@link User} into
	 * DateBase.
	 * Simple properties (as String, Long etc) are being mapped as it. Inner objects
	 * (as {@link workshop.internal.entities.Phone}) are being mapped with special methods.
	 *
	 * @param userDto
	 * @param bindingResult
	 * @param authentication
	 * @return
	 */
	@PostMapping
	public String putProfile(@Validated({Merge.class, Default.class})
							 @ModelAttribute(name = "userDto") UserDto userDto, BindingResult bindingResult,
							 Authentication authentication) {
		if (bindingResult.hasErrors()) {
			return "profile";
		}
		User user = usersService.findByLogin(authentication.getName());
		mapDtoToUserPhones(userDto, user);
		modelMapper.map(userDto, user);
		usersService.mergeEntity(user);
		return "redirect:/profile";
	}
	
	/**
	 * If a give image exceeds the server size limit the
	 * {@link org.springframework.web.multipart.MaxUploadSizeExceededException} will be intercepted and treated in the
	 *  ExceptionHandlerController.maxUploadFileSizeExceeded() method.
	 * @param photo {@link MultipartFile} with a photo
	 * @return redirect to the User profile page if the photo is either null or empty or not an image.
	 */
	@PostMapping(path = "/photo",
		consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String postImage(@RequestParam(name = "photo") MultipartFile photo, Authentication authentication) {
		if (photo.isEmpty() || photo.getContentType() == null ||!photo.getContentType().startsWith("image/")) {
			return "redirect:/profile";
		}
		User user = usersService.findByLogin(authentication.getName());
		try {
			user.setPhoto(photo.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/profile";
	}
	
	private void mapDtoToUserPhones(UserDto userDto, User user) {
		user.getPhones().forEach(userPhone -> {
			userDto.getPhones().stream()
				.filter(userDtoPhone -> userDtoPhone.getIdentifier().equals(userPhone.getIdentifier()))
				.findFirst()
				.ifPresent(userDtoPhone -> {
					userPhone.setName(userDtoPhone.getName());
					userPhone.setPhone(userDtoPhone.getPhone());
				});
		});
	}
}

