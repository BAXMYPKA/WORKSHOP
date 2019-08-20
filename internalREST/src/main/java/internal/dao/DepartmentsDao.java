package internal.dao;

import internal.entities.Department;
import internal.entities.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Setter
@Getter
@Repository
public class DepartmentsDao extends WorkshopEntitiesDaoAbstract<Department, Long> {
	
	public DepartmentsDao() {
		this.setEntityClass(Department.class);
		this.setKeyClass(Long.class);
	}
	
	public Optional<Department> findDepartmentByPosition(Long positionId) {
		if (positionId == null) {
			throw new IllegalArgumentException("PositionId cannot be null!");
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Department> cq = cb.createQuery(Department.class);
		
		Root<Department> departmentRoot = cq.from(Department.class);
		Root<Position> positionRoot = cq.from(Position.class);
		
		Join<Department, Position> positionsJoin = departmentRoot.join("positions");
		
		
		Predicate positionIdPredicate = cb.equal(positionRoot.get("identifier"), positionId);
		
		positionsJoin.on(positionIdPredicate);
		
		TypedQuery<Department> typedQuery = entityManager.createQuery(cq);
		
		Department department = typedQuery.getSingleResult();
		
		return Optional.of(department);
	}
	
}
