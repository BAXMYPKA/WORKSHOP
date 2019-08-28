package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.controllers.PositionsController;
import internal.controllers.WorkshopControllerAbstract;
import internal.entities.Employee;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Employee> {
	
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	
	public EmployeesResourceAssembler() {
		super(EmployeesController.class, Employee.class);
		setDEFAULT_TITLE("Employee");
	}
	
	@Override
	public Resource<Employee> toResource(Employee employee) {
		Resource<Employee> employeeResource = super.toResource(employee);
		//Add Links to the every Phone of Employee
		if (employee.getPhones() != null) {
			List<Link> phonesSerfLinks = employee.getPhones()
				.stream()
				.map(phone -> phonesResourceAssembler.toResource(phone).getLink("self"))
				.collect(Collectors.toList());
			employeeResource.add(phonesSerfLinks);
		}
		return employeeResource;
	}
	
	@Override
	public Resources<Resource<Employee>> toPagedResources(Page<Employee> employeesPage) {
		Resources<Resource<Employee>> employeesResources = super.toPagedResources(employeesPage);
		//Add to every Resource<Employee> additional Links to its every Phone
		employeesResources.getContent().stream()
			.filter(employeeResource -> employeeResource.getContent().getPhones() != null)
			.forEach(employeeResource -> {
				List<Link> selfPhonesLinks = employeeResource.getContent().getPhones()
					.stream()
					.map(phone -> phonesResourceAssembler.toResource(phone).getLink("self"))
					.collect(Collectors.toList());
				employeeResource.add(selfPhonesLinks);
			});
		return employeesResources;
	}
	
	@Override
	public Resources<Resource<Employee>> toPagedSubResources(Page<Employee> workshopEntitiesPage, Long workshopEntityId) {
		return super.toPagedSubResources(workshopEntitiesPage, workshopEntityId);
	}
	
	/**
	 * @see WorkshopEntitiesResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, Long)
	 */
	@Override
	protected Link getPagedLink(Pageable pageable,
							 int pageSize,
							 String relation,
							 String hrefLang,
							 String media,
							 String title,
							 Long workshopEntityId) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().iterator().next().getDirection().name();
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
