package workshop.internal.controllers;

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
import workshop.internal.entities.InternalAuthority;
import workshop.internal.entities.Position;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.hateoasResources.InternalAuthoritiesResourceAssembler;
import workshop.internal.hateoasResources.PositionsResourceAssembler;
import workshop.internal.services.InternalAuthoritiesService;
import workshop.internal.services.PositionsService;

import javax.servlet.http.HttpServletRequest;

@RestController
@ExposesResourceFor(InternalAuthority.class)
@RequestMapping(path = "/internal/internal-authorities", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class InternalAuthoritiesController extends WorkshopControllerAbstract<InternalAuthority> {
	
	public static final String GET_INTERNAL_AUTHORITY_POSITIONS = "getInternalAuthorityPositions";
	
	@Autowired
	private PositionsService positionsService;
	
	@Autowired
	private PositionsResourceAssembler positionsResourceAssembler;
	
	/**
	 * @param internalAuthoritiesService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                             and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                             to operate with.
	 * @param internalAuthoritiesResourceAssembler Will be using the '@ExposeResourceFor' definition.
	 */
	public InternalAuthoritiesController(InternalAuthoritiesService internalAuthoritiesService,
		InternalAuthoritiesResourceAssembler internalAuthoritiesResourceAssembler) {
		super(internalAuthoritiesService, internalAuthoritiesResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/positions")
	@PreAuthorize("hasPermission('Position', 'get')")
	public ResponseEntity<String> getInternalAuthorityPositions(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Position> positionsByAuthority = positionsService.findPositionsByInternalAuthority(pageable, id);
		Resources<Resource<Position>> authorityPositionsSubResources =
			positionsResourceAssembler.toPagedSubResources(positionsByAuthority, id, GET_INTERNAL_AUTHORITY_POSITIONS);
		String jsonAuthorityPositionsSubResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(authorityPositionsSubResources);
		return ResponseEntity.ok(jsonAuthorityPositionsSubResources);
	}
	
	@PostMapping(path = "/{id}/positions",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Position', 'post')")
	public ResponseEntity<String> postForbiddenMethodInternalAuthorityPosition(@PathVariable(name = "id") long id,
		@RequestBody Position position,
		HttpServletRequest request) {
		String errorMessage = getMessageSource().getMessage(
			"httpStatus.forbidden.withDescription(2)",
			new Object[]{request.getMethod(), " Use direct Positions link for such purposes!"},
			LocaleContextHolder.getLocale());
		return getResponseEntityWithErrorMessage(HttpStatus.FORBIDDEN, errorMessage);
	}
	
	/**
	 * Sets a Position into the InternalAuthority.
	 *
	 * @param id       InternalAuthority.ID to insert an existing Position to.
	 * @param position Position to be inserted into this InternalAuthority.
	 * @return The updated Position with this InternalAuthority set.
	 */
	@PutMapping(path = "/{id}/positions",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Position', 'put')")
	public ResponseEntity<String> putInternalAuthorityPosition(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody Position position,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		((InternalAuthoritiesService) getWorkshopEntitiesService())
			.addPositionToInternalAuthority(position.getIdentifier(), id);
		Position positionWithNewAuthority = positionsService.findById(position.getIdentifier());
		Resource<Position> positionResource = positionsResourceAssembler.toResource(positionWithNewAuthority);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.accepted().body(jsonPositionResource);
	}
	
	/**
	 * Removes a given Position from the InternalAuthority.
	 *
	 * @param id         InternalAuthority.ID to delete a Position from.
	 * @param positionId Position.ID to be deleted from this InternalAuthority.
	 */
	@DeleteMapping(path = "{id}/positions/{positionId}")
	@PreAuthorize("hasPermission('Position', 'put')")
	public ResponseEntity<String> deleteInternalAuthorityPosition(@PathVariable(name = "id") Long id,
		@PathVariable(name = "positionId") Long positionId) {
		((InternalAuthoritiesService) getWorkshopEntitiesService()).removePositionFromInternalAuthority(positionId, id);
		Position positionWithoutAuthority = positionsService.findById(positionId);
		Resource<Position> positionResource = positionsResourceAssembler.toResource(positionWithoutAuthority);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonPositionResource);
	}
}
