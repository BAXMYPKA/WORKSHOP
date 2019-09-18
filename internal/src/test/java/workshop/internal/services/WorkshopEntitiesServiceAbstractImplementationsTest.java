package workshop.internal.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import workshop.internal.dao.DepartmentsDao;
import workshop.internal.entities.Department;
import workshop.internal.services.DepartmentsService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkshopEntitiesServiceAbstractImplementationsTest {
	
	@Mock
	private DepartmentsDao departmentsDao;
	@Mock
	private MessageSource messageSource;
	private DepartmentsService departmentsService;
	
	
	@BeforeEach
	public void init() {
		Mockito.when(departmentsDao.getEntityClass()).thenReturn(Department.class);
		departmentsService = new DepartmentsService(departmentsDao);

//		Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(Object[].class), Mockito.any(Locale.class)))
//			.thenReturn("MESSAGE");
		departmentsService.setMessageSource(messageSource);
	}
	
	@Test
	public void init_Test() {
		assertNotNull(departmentsService);
		assertNotNull(departmentsService.getWorkshopEntitiesDaoAbstract());
		assertEquals(Department.class, departmentsDao.getEntityClass());
	}
	
	@ParameterizedTest
	@ValueSource(ints = {1, 3, 45, 149, 5100})
	public void pagination_With_Any_pageSize_Should_Return_Page_Within_Max_Limits(int pageSize) {
		//GIVEN limits and arguments
		int defaultPageSize = 2;
		int maxPageSize = 2;
		int pageNum = 1;
		String orderBy = "Property";
		Sort.Direction order = Sort.Direction.DESC;

		departmentsService.setDEFAULT_PAGE_SIZE(defaultPageSize);
		departmentsService.setMAX_PAGE_SIZE(maxPageSize);
		Pageable pageable = PageRequest.of(pageNum, pageSize, order, orderBy);
		
		ArgumentCaptor<Integer> capturedPageSize = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> capturedPageNum = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> capturedOrderBy = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Sort.Direction> capturedDirection = ArgumentCaptor.forClass(Sort.Direction.class);
		
		Mockito.when(departmentsDao.findAllEntities(
			capturedPageSize.capture(), capturedPageNum.capture(), capturedOrderBy.capture(), capturedDirection.capture()))
			.thenReturn(Optional.of(Collections.singletonList(new Department("Department"))));
		
		//WHEN
		Page<Department> emptyAllEntities = departmentsService.findAllEntities(pageable);
		
		//THEN
		Mockito.verify(departmentsDao, Mockito.times(1))
			.findAllEntities(capturedPageSize.getValue(), capturedPageNum.getValue(), orderBy, Sort.Direction.DESC);
		
		assertTrue(capturedPageSize.getValue() <= maxPageSize);
		assertTrue(capturedPageNum.getValue() <= defaultPageSize);
	}
}