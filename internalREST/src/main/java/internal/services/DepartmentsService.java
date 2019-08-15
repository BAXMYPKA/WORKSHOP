package internal.services;

import internal.dao.DepartmentsDao;
import internal.entities.Department;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DepartmentsService extends WorkshopEntitiesServiceAbstract<Department> {
	
	@Setter
	@Getter
	@Autowired
	private DepartmentsDao departmentsDao;
	
	/**
	 * @param departmentsDao A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                            implementation of this EntitiesServiceAbstract<T>.
	 *                            To be injected to all the superclasses.
	 *                            For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public DepartmentsService(DepartmentsDao departmentsDao) {
		super(departmentsDao);
	}
}
