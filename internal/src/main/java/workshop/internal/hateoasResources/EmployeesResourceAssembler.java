package workshop.internal.hateoasResources;

import workshop.internal.controllers.rest.EmployeesController;
import workshop.internal.controllers.rest.PositionsController;
import workshop.internal.entities.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmployeesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Employee> {
	
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	
	public EmployeesResourceAssembler() {
		super(EmployeesController.class, Employee.class);
	}
	
	/**
	 * @see WorkshopEntitiesResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, Long, String)
	 */
	@Override
	protected Link getPagedLink(Pageable pageable,
								int pageNum,
								String relation,
								String hrefLang,
								String media,
								String title,
								Long ownerId,
								String controllerMethodName) {
		Link link;
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		
		if (PositionsController.GET_POSITION_EMPLOYEES_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(PositionsController.class).getPositionEmployees(
					ownerId,
					pageable.getPageSize(),
					++pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
			return link;
		} else {
			log.error("No matching 'controllerMethodName' found for the given parameter {} in the {} for the Link to be constructed!",
				controllerMethodName, getWorkshopControllerAbstractClass());
			return new Link("/no_link_found/");
		}
	}
}
