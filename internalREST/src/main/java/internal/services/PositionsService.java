package internal.services;

import internal.dao.DepartmentsDao;
import internal.dao.PositionsDao;
import internal.entities.Department;
import internal.entities.Position;
import internal.entities.WorkshopEntity;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import internal.exceptions.InternalServerErrorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Service
public class PositionsService extends WorkshopEntitiesServiceAbstract<Position> {
	
	@Autowired
	private DepartmentsDao departmentsDao;
	
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
			((PositionsDao) getWorkshopEntitiesDaoAbstract()).findAllPositionsByDepartment(
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
			(Department)super.getVerifiedWorkshopEntity(departmentsDao.findById(departmentId));
		Position mergedPosition = super.getVerifiedEntity(getWorkshopEntitiesDaoAbstract().mergeEntity(position));
		mergedPosition.setDepartment(department);
		return mergedPosition;
	}
}
