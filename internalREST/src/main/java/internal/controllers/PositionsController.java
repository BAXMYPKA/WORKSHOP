package internal.controllers;

import internal.entities.Position;
import internal.services.DepartmentsService;
import internal.services.PositionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/positions", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Position.class)
public class PositionsController extends WorkshopControllerAbstract<Position> {
	
	@Autowired
	private DepartmentsService departmentsService;

	/**
	 * @param positionsService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                         and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                         to operate with.
	 */
	public PositionsController(PositionsService positionsService) {
		super(positionsService);
	}
	
	@GetMapping(path = "/{id}/department")
	public ResponseEntity<String> getDepartment(@PathVariable("id") Long id) {
//		departmentsService.
		return null;
	}
}
