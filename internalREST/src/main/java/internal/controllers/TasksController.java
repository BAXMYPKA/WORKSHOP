package internal.controllers;

import internal.entities.Classifier;
import internal.entities.Employee;
import internal.entities.Order;
import internal.entities.Task;
import internal.exceptions.EntityNotFoundException;
import internal.hateoasResources.ClassifiersResourceAssembler;
import internal.hateoasResources.EmployeesResourceAssembler;
import internal.hateoasResources.OrdersResourceAssembler;
import internal.hateoasResources.TasksResourceAssembler;
import internal.services.ClassifiersService;
import internal.services.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/internal/tasks", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RestController
@ExposesResourceFor(Task.class)
public class TasksController extends WorkshopControllerAbstract<Task> {
	
	public static final String GET_TASK_CLASSIFIERS_METHOD_NAME = "taskClassifiers";
	@Autowired
	private EmployeesResourceAssembler employeesResourceAssembler;
	@Autowired
	private OrdersResourceAssembler ordersResourceAssembler;
	@Autowired
	private ClassifiersResourceAssembler classifiersResourceAssembler;
	@Autowired
	private ClassifiersService classifiersService;
	
	/**
	 * @param tasksService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                               and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                               to operate with.
	 * @param tasksResourceAssembler
	 */
	public TasksController(TasksService tasksService, TasksResourceAssembler tasksResourceAssembler) {
		super(tasksService, tasksResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/appointed_to")
	public ResponseEntity<String> taskEmployeeAppointedTo(@PathVariable("id") Long id) {
		
		Task taskById = getWorkshopEntitiesService().findById(id);
		if (taskById.getAppointedTo() != null) {
			Employee employeeAppointedTo = taskById.getAppointedTo();
			Resource<Employee> employeeResource = employeesResourceAssembler.toResource(employeeAppointedTo);
			String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(employeeResource);
			return ResponseEntity.ok(jsonEmployeeResource);
		} else {
			throw new EntityNotFoundException("No appointed Employee for Task.id=" + id, HttpStatus.NOT_FOUND,
				getMessageSource().getMessage(
					"httpStatus.notFound(2)",
					new Object[]{"Employee", getWorkshopEntityClassName() + " ID=" + id},
					LocaleContextHolder.getLocale()));
		}
	}
	
	@GetMapping(path = "/{id}/order")
	public ResponseEntity<String> taskOrder(@PathVariable("id") Long id) {
		
		Task taskById = getWorkshopEntitiesService().findById(id);
		Order taskOrder = taskById.getOrder();
		Resource<Order> taskOrderResource = ordersResourceAssembler.toResource(taskOrder);
		String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskOrderResource);
		return ResponseEntity.ok(jsonEmployeeResource);
	}
	
	@GetMapping(path = "/{id}/classifiers")
	public ResponseEntity<String> taskClassifiers(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = getPageable(pageSize, pageNum, orderBy, order);
		Page<Classifier> allClassifiersByTaskPage = classifiersService.findAllClassifiersByTask(pageable, id);
		Resources<Resource<Classifier>> classifiersByTaskPagedResources =
			classifiersResourceAssembler.toPagedSubResources(allClassifiersByTaskPage, id, GET_TASK_CLASSIFIERS_METHOD_NAME);
		String jsonPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(classifiersByTaskPagedResources);
		return ResponseEntity.ok(jsonPagedResources);
	}
}
