package workshop.internal.dao;

import workshop.internal.entities.Department;
import workshop.internal.entities.Position;
import workshop.exceptions.InternalServerErrorException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
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
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Department> cq = cb.createQuery(Department.class);
		
		Root<Department> departmentRoot = cq.from(Department.class);
		
		Join<Department, Position> departmentPositionJoin = departmentRoot.join("positions");
		
		departmentPositionJoin.on(cb.equal(departmentPositionJoin.get("identifier"), positionId));
		
		TypedQuery<Department> typedQuery = getEntityManager().createQuery(cq);
		
		try {
			Department department = typedQuery.getSingleResult();
			return Optional.of(department);
		} catch (NoResultException nre) {
			return Optional.empty();
		} catch (Exception e) {
			throw new InternalServerErrorException(
				e.getMessage(), "httpStatus.internalServerError.common", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
}
