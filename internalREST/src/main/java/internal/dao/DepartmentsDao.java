package internal.dao;

import internal.entities.Department;
import internal.entities.Position;
import internal.exceptions.InternalServerErrorException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
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
	
	/**
	 * @param positionId Position ID to get a Department from
	 * @return 'Optional.of(Department)' if found, otherwise Optional.empty().
	 * @throws IllegalArgumentException If position ID is null.
	 * @throws InternalServerErrorException For timeout, DateBase inconsistency errors, Lock errors etc.
	 */
	public Optional<Department> findDepartmentByPosition(Long positionId)
		throws IllegalArgumentException, InternalServerErrorException {
		
		if (positionId == null) {
			throw new IllegalArgumentException("PositionId cannot be null!");
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Department> cq = cb.createQuery(Department.class);
		
		Root<Department> departmentRoot = cq.from(Department.class);
		
		Join<Department, Position> departmentPositionJoin = departmentRoot.join("positions");
		
		departmentPositionJoin.on(cb.equal(departmentPositionJoin.get("identifier"), positionId));
		
		TypedQuery<Department> typedQuery = entityManager.createQuery(cq);
		
		try {
			Department department = typedQuery.getSingleResult();
			return Optional.of(department);
		} catch (NoResultException nre) {
			return Optional.empty();
		} catch (Exception e) {
			throw new InternalServerErrorException(
				e.getMessage(), "httpStatus.internalServerError", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
}
