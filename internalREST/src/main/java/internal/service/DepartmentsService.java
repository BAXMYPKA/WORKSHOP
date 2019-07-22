package internal.service;

import internal.dao.DepartmentsDao;
import internal.entities.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepartmentsService extends EntitiesServiceAbstract <Department> {
	
	@Autowired
	private DepartmentsDao departmentsDao;
	
	public DepartmentsService(DepartmentsDao departmentsDao) {
		super(departmentsDao);
	}
}
