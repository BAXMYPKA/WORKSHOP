package workshop.internal.services;

import workshop.internal.dao.EmployeesDao;
import workshop.internal.dao.PhonesDao;
import workshop.internal.entities.Employee;
import workshop.internal.exceptions.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmployeesService extends WorkshopEntitiesServiceAbstract<Employee> {
	
	@Autowired
	private PositionsService positionsService;
	@Autowired
	private PhonesService phonesService;
	@Autowired
	private PhonesDao phonesDao;
	
	public EmployeesService(EmployeesDao employeesDao) {
		super(employeesDao);
	}
	
	/**
	 * @param email Employee's email
	 * @return Employee with such an email or throw EntityNotFoundException with HttpStatus and localized message
	 * @throws IllegalArgumentException If email is null.
	 * @throws EntityNotFoundException  With HttpStatus and localized message to be intercepted by
	 *                                  ExceptionHandlerController to be send for the end-users.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Employee findByEmail(String email) throws IllegalArgumentException, EntityNotFoundException {
		if (email == null) {
			throw new IllegalArgumentException("Email cannot be null!");
		}
		Optional<Employee> employee = ((EmployeesDao)getWorkshopEntitiesDaoAbstract()).findEmployeeByEmail(email);
		return employee.orElseThrow(() -> new EntityNotFoundException("No Employee found!", HttpStatus.NOT_FOUND,
			getMessageSource().getMessage("httpStatus.notFound(2)", new Object[]{"Employee", email},
				LocaleContextHolder.getLocale())));
	}
	
	/**
	 * @param pageable
	 * @param positionId
	 * @return
	 * @throws EntityNotFoundException If no Position or Employees found.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public Page<Employee> findEmployeesByPosition(Pageable pageable, Long positionId) throws EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(positionId);
		
		if (!positionsService.isExist(positionId)) {
			throw new EntityNotFoundException("No Position found!", HttpStatus.NOT_FOUND, getMessageSource()
				.getMessage("httpStatus.notFound(1)", new Object[]{"Position" + positionId}, LocaleContextHolder.getLocale()));
		}
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		String order = verifiedPageable.getSort().iterator().next().getProperty();
		
		List<Employee> employeesByPosition = ((EmployeesDao)getWorkshopEntitiesDaoAbstract()).findAllEmployeesByPosition(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			order,
			verifiedPageable.getSort().iterator().next().getDirection(),
			positionId)
			.orElseThrow(() -> new EntityNotFoundException("No Employees found!", HttpStatus.NOT_FOUND,
				getMessageSource().getMessage(
					"httpStatus.notFound(1)", new Object[]{"Employees"}, LocaleContextHolder.getLocale())));
		
		long totalEmployees = ((EmployeesDao)getWorkshopEntitiesDaoAbstract()).countAllEntities();
		
		Page<Employee> employeesPage = new PageImpl<>(employeesByPosition, verifiedPageable, totalEmployees);
		
		return employeesPage;
	}
	
}
