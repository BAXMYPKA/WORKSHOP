package internal.dao;

import internal.entities.Department;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Setter
@Getter
@Repository
public class DepartmentsDao extends EntitiesDaoAbstract<Department, Long> {
	
	public DepartmentsDao() {
		this.setEntityClass(Department.class);
		this.setKeyClass(Long.class);
	}
}
