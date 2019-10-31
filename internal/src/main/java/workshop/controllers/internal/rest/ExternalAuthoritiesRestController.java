package workshop.controllers.internal.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import workshop.internal.entities.ExternalAuthority;
import workshop.internal.entities.User;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.entities.hibernateValidation.Merge;
import workshop.internal.hateoasResources.ExternalAuthoritiesResourceAssembler;
import workshop.internal.hateoasResources.UsersResourceAssembler;
import workshop.internal.services.ExternalAuthoritiesService;
import workshop.internal.services.UsersService;

@RestController
@RequestMapping(path = "/internal/external-authorities", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(ExternalAuthority.class)
public class ExternalAuthoritiesRestController extends WorkshopRestControllerAbstract<ExternalAuthority> {
	
	public static final String GET_EXTERNAL_AUTHORITY_USERS = "getExternalAuthorityUsers";
	@Autowired
	private UsersService usersService;
	@Autowired
	private UsersResourceAssembler usersResourceAssembler;
	
	/**
	 * @param externalAuthoritiesService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                             and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                             to operate with.
	 * @param externalAuthoritiesResourceAssembler
	 */
	public ExternalAuthoritiesRestController(
		ExternalAuthoritiesService externalAuthoritiesService,
		ExternalAuthoritiesResourceAssembler externalAuthoritiesResourceAssembler) {
		super(externalAuthoritiesService, externalAuthoritiesResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/users")
	@PreAuthorize("hasPermission('User', 'get')")
	public ResponseEntity<String> getExternalAuthorityUsers(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = getPageable(pageSize, pageNum, orderBy, order);
		Page<User> usersByExternalAuthorityPage = usersService.findUsersByExternalAuthority(pageable, id);
		Resources<Resource<User>> usersByAuthorityPagedResources =
			usersResourceAssembler.toPagedSubResources(usersByExternalAuthorityPage, id, GET_EXTERNAL_AUTHORITY_USERS);
		String jsonUsersByAuthorityResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(usersByAuthorityPagedResources);
		return ResponseEntity.ok(jsonUsersByAuthorityResources);
	}
	
	/**
	 * Receives a new User, sets this ExternalAuthority and persist the User.
	 *
	 * @param id   ExternalAuthority.ID to be set to the new User.
	 * @param user New User to be persisted with this ExternalAuthority added.
	 * @return The User with this ExternalAuthority.
	 */
	@PostMapping(path = "/{id}/users",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('User', 'post')")
	public ResponseEntity<String> postExternalAuthorityUser(
		@PathVariable(name = "id") Long id,
		@Validated(Persist.class) @RequestBody User user,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		ExternalAuthority externalAuthority = getWorkshopEntitiesService().findById(id);
		if (user.getExternalAuthorities() != null) {
			user.getExternalAuthorities().add(externalAuthority);
		} else {
			user.addGrantedAuthority(externalAuthority);
		}
		user = usersService.persistEntity(user);
		Resource<User> userResource = usersResourceAssembler.toResource(user);
		String jsonUserResource = getJsonServiceUtils().workshopEntityObjectsToJson(userResource);
		return ResponseEntity.ok(jsonUserResource);
	}
	
	/**
	 * Receives an existing User, sets this ExternalAuthority and updates one.
	 *
	 * @param id   ExternalAuthority.ID to be set to the User.
	 * @param user The existing User to be updated with this ExternalAuthority added.
	 * @return Updated User with this ExternalAuthority.
	 */
	@PutMapping(path = "/{id}/users",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('User', 'put')")
	public ResponseEntity<String> putExternalAuthorityUser(
		@PathVariable(name = "id") Long id,
		@Validated(Merge.class) @RequestBody User user,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		ExternalAuthority externalAuthority = getWorkshopEntitiesService().findById(id);
		if (user.getExternalAuthorities() != null) {
			user.getExternalAuthorities().add(externalAuthority);
		} else {
			user.addGrantedAuthority(externalAuthority);
		}
		user = usersService.mergeEntity(user);
		Resource<User> userResource = usersResourceAssembler.toResource(user);
		String jsonUserResource = getJsonServiceUtils().workshopEntityObjectsToJson(userResource);
		return ResponseEntity.ok(jsonUserResource);
	}
	
	/**
	 * Just removes this ExternalAuthority from a given User
	 *
	 * @param id     ExternalAuthority.ID to be removed from a User.
	 * @param userId User.ID
	 * @return HttpStatus.NO_CONTENT in case of success.
	 */
	@DeleteMapping(path = "{id}/users/{userId}")
	@PreAuthorize("hasPermission('User', 'put')")
	public ResponseEntity<String> deleteExternalAuthorityUser(@PathVariable(name = "id") Long id,
															  @PathVariable(name = "userId") Long userId) {
		User user = usersService.findById(userId);
		if (user.getExternalAuthorities() != null) {
			if (user.getExternalAuthorities().removeIf(auth -> auth.getIdentifier().equals(id))) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(getDeleteMessageSuccessLocalized(getWorkshopEntityClassName() + ".ID=" + id));
			}
		}
		String notFoundMessage = getMessageSource().getMessage(
			"httpStatus.notFound(2)",
			new Object[]{getWorkshopEntityClassName() + ".ID=" + id, " User.ID=" + userId},
			LocaleContextHolder.getLocale());
		return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, notFoundMessage);
	}
}
