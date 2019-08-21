package internal.services;

import internal.dao.DepartmentsDao;
import internal.dao.PositionsDao;
import internal.entities.Department;
import internal.entities.Position;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Service
public class DepartmentsService extends WorkshopEntitiesServiceAbstract<Department> {
	
	@Setter
	@Getter
	@Autowired
	private DepartmentsDao departmentsDao;
	@Autowired
	private PositionsDao positionsDao;
	
	/**
	 * @param departmentsDao A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                            implementation of this EntitiesServiceAbstract<T>.
	 *                            To be injected to all the superclasses.
	 *                            For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public DepartmentsService(DepartmentsDao departmentsDao) {
		super(departmentsDao);
	}
	
	/**
	 * @param positionId Position ID its Department to be found from.
	 * @return Department for that Position
	 * @throws EntityNotFoundException If no Position found for that positionId.
	 * @throws IllegalArgumentsException If a given positionId is null, zero or below.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public Department findDepartmentByPosition(Long positionId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(positionId);
		
		Optional<Position> positionById = positionsDao.findById(positionId);
		Department departmentByPosition = positionById.orElseThrow(() -> new EntityNotFoundException(
			"No Position with such an ID", "httpStatus.notFound", HttpStatus.NOT_FOUND))
			.getDepartment();
		return departmentByPosition;
	}
}
