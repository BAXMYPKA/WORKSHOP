package internal.services;

import internal.dao.PositionsDao;
import internal.entities.Position;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import internal.exceptions.InternalServerErrorException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Service
public class PositionsService extends WorkshopEntitiesServiceAbstract<Position> {
	
	@Autowired
	private PositionsDao positionsDao;
	
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
		if (pageable == null) {
			throw new IllegalArgumentsException("Pageable cannot be null!", "httpStatus.internalServerError",
				HttpStatus.INTERNAL_SERVER_ERROR);
		} else if (departmentId == null) {
			throw new IllegalArgumentsException("DepartmentId cannot be null!", "httpStatus.internalServerError",
				HttpStatus.INTERNAL_SERVER_ERROR);
		}
		Pageable verifiedPageable = super.getVerifiedPageable(pageable);
		
		Optional<List<Position>> allPositionsByDepartment = positionsDao.findAllPositionsByDepartment(
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
		Page<Position> entitiesPage = super.getEntitiesPage(verifiedPageable, allPositionsByDepartment);
		return entitiesPage;
	}
}
