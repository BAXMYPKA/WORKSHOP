package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.controllers.PositionsController;
import internal.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
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
	
	/**
	 * @return A 'Resource<Employee>' with Links to every Phone is contains.
	 */
	@Override
	public Resource<Employee> toResource(Employee employee) {
		Resource<Employee> employeeResource = super.toResource(employee);
		//Add Links to the every Phone of Employee
		if (employee.getPhones() != null) {
			List<Link> phonesSelfLinks = employee.getPhones()
				.stream()
				.map(phone -> phonesResourceAssembler.toResource(phone).getLink("self"))
				.collect(Collectors.toList());
			employeeResource.add(phonesSelfLinks);
		}
		return employeeResource;
	}
	
	/**
	 * @return "Resources<Resource<Employee>>" where every 'Resource<Employee>' has Link(s) to the every Phone it contains.
	 */
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
		
		if (PositionsController.GET_EMPLOYEES_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
			link = ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(PositionsController.class).positionEmployees(
					ownerId,
					pageable.getPageSize(),
					pageNum,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
			return link;
		} else {
			return super.getPagedLink(pageable, pageNum, relation, hrefLang, media, title, ownerId, controllerMethodName);
		}
	}
}
