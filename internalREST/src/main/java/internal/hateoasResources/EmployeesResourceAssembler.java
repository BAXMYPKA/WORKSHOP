package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.controllers.PositionsController;
import internal.controllers.WorkshopControllerAbstract;
import internal.entities.Employee;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class EmployeesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Employee> {
	
	public EmployeesResourceAssembler(Class<? extends WorkshopControllerAbstract<Employee>> workshopControllerAbstractClass,
									  Class<Employee> workshopEntityClass) {
		super(workshopControllerAbstractClass, workshopEntityClass);
	}
	
	/**
	 * @see WorkshopEntitiesResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, String, String, Long)
	 */
	@Override
	Link getPagedLink(Pageable pageable,
					  int pageSize,
					  String orderBy,
					  String order,
					  String relation,
					  String hrefLang,
					  String media,
					  String title,
					  Long workshopEntityId) {
		
		Link pagedLink = ControllerLinkBuilder.linkTo(
			ControllerLinkBuilder.methodOn(PositionsController.class).getEmployees(
				workshopEntityId,
				pageSize,
				0,
				orderBy,
				order))
			.withRel(relation)
			.withHreflang(hrefLang)
			.withMedia(media)
			.withTitle(title);
		
		return pagedLink;
	}
}
