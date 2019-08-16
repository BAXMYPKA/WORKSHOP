package internal.entities.hateoasResources;

import internal.controllers.WorkshopControllerAbstract;
import internal.entities.Employee;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class EmployeeResourceAssembler extends WorkshopEntityResourceAssemblerAbstract<Employee> {
	
	public EmployeeResourceAssembler(Class<? extends WorkshopControllerAbstract<Employee>> workshopControllerAbstractClass,
									 Class<Employee> workshopEntityClass) {
		super(workshopControllerAbstractClass, workshopEntityClass);
	}
}
