package internal.service;

import internal.dao.EntitiesDaoAbstract;
import internal.dao.OrdersDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@DisplayName("To be redesigned in the EntitiesServiceAbstractImplementationsTest")
@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {
	
	@Mock
	OrdersDao ordersDao;
	
	@Mock
	EntitiesDaoAbstract entitiesDao;
	
	@InjectMocks
	OrdersService ordersService;
	
	@BeforeEach
	public void init() {
		ordersDao = new OrdersDao();
		ordersService = new OrdersService(ordersDao);
	}
	
	@ParameterizedTest
	@ValueSource(ints = {-150, -100 - 2, -1, 0, 1, 2, 49, 50, 51, 99, 100, 101, 1200})
	public void regardless_Of_Input_Size_And_Page_All_Of_Them_Should_Be_Set_Between_Their_Min_Max(int sizeAndPage) {
		
		//GIVEN
		int maxPageSize = 10;
		int maxPageNum = 20;
		
		ordersService.setDEFAULT_PAGE_SIZE(maxPageSize);
		ordersService.setMAX_PAGE_NUM(maxPageNum);
		entitiesDao.setDEFAULT_PAGE_SIZE(maxPageSize);
		entitiesDao.setMAX_PAGE_NUM(maxPageNum);
		ordersDao.setDEFAULT_PAGE_SIZE(maxPageSize);
		ordersDao.setMAX_PAGE_NUM(maxPageNum);
		
		ArgumentCaptor<Integer> sizeCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> pageCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> sortByCaptured = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Sort.Direction> sortCaptured = ArgumentCaptor.forClass(Sort.Direction.class);
		
		//WHEN
		Mockito.lenient().when(entitiesDao.findAll(sizeAndPage, sizeAndPage, "", Sort.Direction.DESC))
			.thenReturn(Optional.empty());
		
		Mockito.lenient().when(entitiesDao.findAll(sizeAndPage, sizeAndPage, "", Sort.Direction.DESC))
			.thenReturn(Optional.empty());
		
		ordersService.findAllEntities(sizeAndPage, sizeAndPage, "", Sort.Direction.DESC);
		
		//THEN
		
		Mockito.verify(ordersDao, Mockito.atLeastOnce()).findAll(
			sizeCaptured.capture(), pageCaptured.capture(), sortByCaptured.capture(), sortCaptured.capture());
		
		System.out.println("Size=" + sizeCaptured.getValue() + " Page=" + pageCaptured.getValue());
		
		assertTrue(sizeCaptured.getValue() > 0 && sizeCaptured.getValue() <= maxPageSize);
		assertTrue(pageCaptured.getValue() >= 1 && pageCaptured.getValue() <= maxPageNum);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"other", "AsC", "null", "dEsc"})
	@DisplayName("SortBy can only by empty or any value, AscDesc can only by 'asc' or 'desc' in the lowerCase")
	public void sortBy_and_AscDesc_Values_Should_Be_Corrected(String variable) {
		
		//GIVEN
		
		ArgumentCaptor<Integer> sizeCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> pageCaptured = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> sortByCaptured = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Sort.Direction> directionCaptured = ArgumentCaptor.forClass(Sort.Direction.class);
		
		//WHEN
		
		Mockito.lenient()
			.when(ordersDao.findAll(
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.any(Sort.Direction.class)))
			.thenReturn(Optional.empty());
		
		ordersService.findAllEntities(1, 1, variable, Sort.Direction.DESC);
		
		//THEN
		
		Mockito.verify(ordersDao, Mockito.atLeastOnce()).findAll(
			sizeCaptured.capture(), pageCaptured.capture(), sortByCaptured.capture(), directionCaptured.capture());
		
		System.out.println("SortBy=" + sortByCaptured.getValue() + " || AscDesc=" + directionCaptured.getValue());
		
		//'SortBy' should be either empty or equals to input value
		assertTrue(sortByCaptured.getValue() != null &&
			(sortByCaptured.getValue().isEmpty() || sortByCaptured.getValue().equals(variable)));
		
		assertTrue(directionCaptured.getValue() != null &&
			("asc".equalsIgnoreCase(directionCaptured.getValue().name()) ||
				"desc".equalsIgnoreCase(directionCaptured.getValue().name())));
	}
}