package internal.services;

import internal.dao.EmployeesDao;
import internal.entities.Employee;
import internal.entities.Phone;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.InternalServerErrorException;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EmployeesService extends WorkshopEntitiesServiceAbstract<Employee> {
	
	@Autowired
	private EmployeesDao employeesDao;
	@Autowired
	private PositionsService positionsService;
	@Autowired
	private PhonesService phonesService;
	
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
		Optional<Employee> employee = employeesDao.findEmployeeByEmail(email);
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
		
		List<Employee> employeesByPosition = employeesDao.findAllEmployeesByPosition(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			order,
			verifiedPageable.getSort().iterator().next().getDirection(),
			positionId)
			.orElseThrow(() -> new EntityNotFoundException("No Employees found!", HttpStatus.NOT_FOUND,
				getMessageSource().getMessage(
					"httpStatus.notFound(1)", new Object[]{"Employees"}, LocaleContextHolder.getLocale())));
		
		long totalEmployees = employeesDao.countAllEntities();
		
		Page<Employee> employeesPage = new PageImpl<>(employeesByPosition, verifiedPageable, totalEmployees);
		
		return employeesPage;
	}
	
	/**
	 * Sets a new or changed Phone to an existing Employee.
	 *
	 * @param employeeId     Existing Employee.ID
	 * @param phone Phone entity to be persisted or merged (if ID is set).
	 * @return The Employee with the new Phone set.
	 * @throws EntityNotFoundException      If no Employee with the given ID found.
	 * @throws InternalServerErrorException When update fails on underlying DAO layer.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public Employee addPhoneToEmployee(Long employeeId, Phone phone)
		throws EntityNotFoundException, InternalServerErrorException {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		
		if (!getWorkshopEntitiesDaoAbstract().isExist(employeeId)) {
			throw getEntityNotFoundException(getEntityClassSimpleName() + ".ID=" + employeeId);
		}
		Phone phonePersisted = phonesService.persistOrMergeEntity(phone);
		Optional<Employee> employeeWithNewPhone =
			employeesDao.addPhoneToEmployee(employeeId, phonePersisted.getIdentifier());
		return employeeWithNewPhone.orElseThrow(() ->
			new InternalServerErrorException("An error occurred on DAO layer during setting a new Phone to Employee!"
				, "httpStatus.internalServerError", HttpStatus.INTERNAL_SERVER_ERROR));
	}
	
	/**
	 * Sets an existing Phone to an existing Employee.
	 *
	 * @param employeeId Existing Employee.ID
	 * @param phoneId    Existing Phone.ID
	 * @return An Employee with the new Phone set
	 * @throws EntityNotFoundException If 'employeeId' or 'phoneId' is wrong.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public Employee addPhoneToEmployee(Long employeeId, Long phoneId) throws EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(employeeId, phoneId);
		Optional<Employee> employee = employeesDao.addPhoneToEmployee(employeeId, phoneId);
		return employee.orElseThrow(() -> getEntityNotFoundException(getEntityClassSimpleName()));
	}
	
}
