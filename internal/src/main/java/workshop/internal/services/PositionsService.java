package workshop.internal.services;

import workshop.internal.dao.DepartmentsDao;
import workshop.internal.dao.EmployeesDao;
import workshop.internal.dao.InternalAuthoritiesDao;
import workshop.internal.dao.PositionsDao;
import workshop.internal.entities.Department;
import workshop.internal.entities.Employee;
import workshop.internal.entities.Position;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.exceptions.IllegalArgumentsException;
import workshop.internal.exceptions.InternalServerErrorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Service
public class PositionsService extends WorkshopEntitiesServiceAbstract<Position> {
	
	@Autowired
	private DepartmentsDao departmentsDao;
	@Autowired
	private EmployeesDao employeesDao;
	@Autowired
	private InternalAuthoritiesDao internalAuthoritiesDao;
	
	/**
	 * @param positionsDao A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                     implementation of this EntitiesServiceAbstract<T>.
	 *                     To be injected to all the superclasses.
	 *                     For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public PositionsService(PositionsDao positionsDao) {
		super(positionsDao);
	}
	
	/**
	 * @param pageable
	 * @param departmentId
	 * @return Fully prepared Page with total elements, total Pages, hasNexPage etc set.
	 * @throws IllegalArgumentsException    If Pageable of departmentId == null
	 * @throws InternalServerErrorException If Pageable to be verified is null;
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public Page<Position> findPositionsByDepartment(Pageable pageable, Long departmentId)
		throws IllegalArgumentsException, InternalServerErrorException {
		
		super.verifyIdForNullZeroBelowZero(departmentId);
		
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		Optional<List<Position>> allPositionsByDepartment =
			((PositionsDao) getWorkshopEntitiesDaoAbstract()).findPositionsByDepartment(
				verifiedPageable.getPageSize(),
				verifiedPageable.getPageNumber(),
				verifiedPageable.getSort().iterator().next().getProperty(),
				verifiedPageable.getSort().iterator().next().getDirection(),
				departmentId);
		
		if (!allPositionsByDepartment.isPresent()) {
			throw new EntityNotFoundException("No Positions from a given Department found!", HttpStatus.NOT_FOUND,
				getMessageSource().getMessage(
					"httpStatus.notFound(2)",
					new Object[]{getEntityClass().getSimpleName(), "Department.id=" + departmentId},
					LocaleContextHolder.getLocale()));
		}
		Page<Position> entitiesPage = super.getVerifiedEntitiesPage(verifiedPageable, allPositionsByDepartment);
		return entitiesPage;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public Position updatePositionDepartment(Position position, Long departmentId) throws IllegalArgumentsException {
		super.verifyIdForNullZeroBelowZero(departmentId);
		Department department =
			(Department) super.getVerifiedWorkshopEntity(departmentsDao.findById(departmentId));
		Position mergedPosition = super.getVerifiedEntity(getWorkshopEntitiesDaoAbstract().mergeEntity(position));
		mergedPosition.setDepartment(department);
		return mergedPosition;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public Position addPositionToEmployee(long employeeId, Position position) {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		Employee employee = employeesDao.findById(employeeId).orElseThrow(() ->
			getEntityNotFoundException("Employee.ID=" + employeeId));
		Position mergedOrPersistedPosition = super.persistOrMergeEntity(position);
		employee.setPosition(mergedOrPersistedPosition);
		if (mergedOrPersistedPosition.getEmployees() == null) {
			mergedOrPersistedPosition.setEmployees(new HashSet<>(Collections.singletonList(employee)));
		} else {
			mergedOrPersistedPosition.getEmployees().add(employee);
		}
		return mergedOrPersistedPosition;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Page<Position> findPositionsByInternalAuthority(Pageable pageable, Long internalAuthorityId) {
		super.verifyIdForNullZeroBelowZero(internalAuthorityId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Position>> positionsByAuthority =
			((PositionsDao) getWorkshopEntitiesDaoAbstract()).findPositionsByInternalAuthority(
				verifiedPageable.getPageSize(),
				verifiedPageable.getPageNumber(),
				orderBy,
				order,
				internalAuthorityId);
		return super.getVerifiedEntitiesPage(verifiedPageable, positionsByAuthority);
	}
}
