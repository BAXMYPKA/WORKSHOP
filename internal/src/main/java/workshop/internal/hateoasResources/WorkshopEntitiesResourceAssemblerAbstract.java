package workshop.internal.hateoasResources;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import workshop.controllers.internal.rest.WorkshopRestControllerAbstract;
import workshop.internal.entities.WorkshopEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * To construct custom navigable Links for a given Resource and its subResources
 * (e.g. to get pageable collection of all the Orders modified by Employee through Employee.getOrdersModifiedBy
 * with the ability to iterate them page by page with 'nextPage', 'lastPage' Links etc)
 * you have to use {@link #toPagedSubResources(Page, Long, String)} method by passing the desired ControllerMethodName
 * and only override {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)} method
 * to implement your custom logic depending on given 'controllerMethodName'.
 */
@Getter
@Setter
@Slf4j
@Component
public abstract class WorkshopEntitiesResourceAssemblerAbstract<T extends WorkshopEntity>
	implements ResourceAssembler<T, Resource<T>> {
	
	private final String LINK_MEDIA = "application/json; charset=utf-8";
	private final String LINK_CURRENT_PAGE_REL = "currentPage";
	private final String LINK_PREV_PAGE_REL = "previousPage";
	private final String LINK_NEXT_PAGE_REL = "nextPage";
	private final String LINK_FIRST_PAGE_REL = "firstPage";
	private final String LINK_LAST_PAGE_REL = "lastPage";
	private Class<? extends WorkshopRestControllerAbstract> workshopControllerAbstractClass;
	private Class<T> workshopEntityClass;
	@Autowired
	private EntityLinks entityLinks;
	/**
	 * To be optionally set by every subclass as its own name, eg, "Order", "Task" etc.
	 */
	private String LINK_DEFAULT_TITLE = "";
	@Value("${page.size.default}")
	private int DEFAULT_PAGE_SIZE;
	@Value("${page.max_num}")
	private int MAX_PAGE_NUM;
	@Value("${page.size.max}")
	private int MAX_PAGE_SIZE;
	@Value("${default.orderBy}")
	private String DEFAULT_ORDER_BY;
	@Value("${default.order}")
	private String DEFAULT_ORDER;
	@Value("${Allow}")
	private String httpHeaderAllowValue;
	
	/**
	 * Obligatory constructor.
	 * Delete the method arguments and only leave:
	 * super(WorkshopControllerInstance.class, WorkshopEntityInstance.class);
	 *
	 * @param workshopControllerAbstractClass The concrete class-type of WorkshopController
	 * @param workshopEntityClass             The concrete class-type of WorkshopEntity
	 */
	@Autowired(required = false)
	public WorkshopEntitiesResourceAssemblerAbstract(
		Class<? extends WorkshopRestControllerAbstract<T>> workshopControllerAbstractClass,
		Class<T> workshopEntityClass) {
		
		this.workshopControllerAbstractClass = workshopControllerAbstractClass;
		this.workshopEntityClass = workshopEntityClass;
		this.LINK_DEFAULT_TITLE = workshopEntityClass.getSimpleName();
	}
	
	/**
	 * Transforms "WorkshopEntity<T>" into Resource<WorkshopEntity> and adds the self-Link to it.
	 * Scans "WorkshopController<T>" for "@GetMapping" annotated methods containing "path = '/{id}/...'", constructs
	 * and adds Links to its subResources according to the given workshopEntity.identifier.
	 * E.g. a given Employee.identifier=5 and EmployeesController contains "@GetMapping(path = '/{id}/position')"
	 * so "Resource<Employee>" will contain a Link with href="/5/position", title = "[/{id}/position],[GET, POST, PUT, DELETE]" etc.
	 * Any other method parameters will be null.
	 *
	 * @param workshopEntity An WorkshopEntity instance to extract 'identifier' from.
	 * @return Resource<T extends WorkshopEntity> entity resource with self-Link
	 * according its WorkshopController<T extends WorkshopEntity> class.
	 */
	@Override
	public Resource<T> toResource(T workshopEntity) {
		Link selfGetLink = entityLinks
			.linkForSingleResource(workshopEntityClass, workshopEntity.getIdentifier())
			.withSelfRel()
			.withHreflang(LocaleContextHolder.getLocale().toLanguageTag())
			.withMedia(LINK_MEDIA)
			.withTitle(LINK_DEFAULT_TITLE);
		
		Resource<T> workshopEntityResource = new Resource<>(workshopEntity, selfGetLink);
		
		List<Method> idMethods = Arrays.stream(workshopControllerAbstractClass.getDeclaredMethods())
			.filter(method -> method.isAnnotationPresent(GetMapping.class))
			.filter(method -> method.getAnnotation(GetMapping.class).path()[0].contains("/{id}/"))
			.collect(Collectors.toList());
		
		for (Method m : idMethods) {
			Class<?>[] parameterTypes = m.getParameterTypes();
			Object[] parameterValues = new Object[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				if (parameterTypes[i].equals(Long.class)) {
					parameterValues[i] = workshopEntity.getIdentifier();
				} else {
					parameterValues[i] = null;
				}
			}
			Link link = ControllerLinkBuilder.linkTo(workshopControllerAbstractClass, m, parameterValues)
				.withRel(m.getName().replaceFirst("get", ""))
				.withMedia(LINK_MEDIA)
				.withHreflang(LocaleContextHolder.getLocale().toLanguageTag())
				.withTitle(Arrays.toString(m.getAnnotation(GetMapping.class).path()) +
					",[" + httpHeaderAllowValue + "]");
			workshopEntityResource.add(link);
		}
		return workshopEntityResource;
	}
	
	/**
	 * Default method for obtaining the overall quantity of WorkshopEntities. This method:
	 * 1) Transforms the every given WorkshopEntity to "Resource<T>"
	 * 2) From a given Page information prepares pageable navigation Links through WorkshopEntities collection
	 * 3) Returns a ready to use pageable "Resources<Resource<T>>"
	 *
	 * @param workshopEntitiesPage Page<T> workshopEntitiesPage with current pageNum, pageSize, orderBy, order.
	 * @return 'Resources<Resource <T>>' - a collection WorkshopEntities as a resources with self-links
	 * and pagination Links (nextPage, prevPage etc).
	 */
	public Resources<Resource<T>> toPagedResources(Page<T> workshopEntitiesPage) {
		List<Resource<T>> resourcesCollection = workshopEntitiesPage.get()
			.map(this::toResource)
			.collect(Collectors.toList());
		Collection<Link> pagedLinks = getPagedLinks(workshopEntitiesPage);
		return new Resources<>(resourcesCollection, pagedLinks);
	}
	
	/**
	 * NOTICE: YOU HAVE TO IMPLEMENT {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)}
	 * abstract method FOR USING THIS!
	 * Special method for obtaining paged WorkshopEntities collections with their owner's ID
	 * by overriding {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)} method
	 * and constructing custom ControllerLinkBuilder.linkTo(methodOn()) Links for various sub collections from one
	 * WorkshopEntity (Employee.getAppointedTasks, Employee.getModifiedTasks etc).
	 * This method:
	 * 1) Transforms the every given WorkshopEntity from "Page<T>.get()" to "Resource<T>"
	 * 2) From a given Page information prepares pageable navigation Links through WorkshopEntities collection
	 * 3) Returns a ready to use pageable "Resources<Resource<T>>" with navigation Links included.
	 *
	 * @param workshopEntitiesPage  Page<T> workshopEntitiesPage with current pageNum, pageSize, orderBy, order.
	 * @param workshopEntityOwnerId Has to be used in some custom cases for deriving paged collections by owner's id.
	 *                              The given parameter passes down to
	 *                              {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)}
	 *                              method but not used by default. If you pass it, you have to override the method above
	 *                              to use this id.
	 * @param controllerMethodName  The discriminator name of the RestController method to being passed to the
	 *                              {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)} method
	 *                              to construct a custom ControllerLinkBuilder.linkTo(methodOn()) Links based on the given name
	 *                              for various sub collections of one WorkshopEntity (Employee.getAppointedTasks, Employee.getModifiedTasks etc).
	 * @return 'Resources<Resource <T>>' - a collection WorkshopEntities as a resources with self-links
	 * and pagination Links (nextPage, prevPage etc).
	 */
	public Resources<Resource<T>> toPagedSubResources(
		Page<T> workshopEntitiesPage,
		Long workshopEntityOwnerId,
		String controllerMethodName) {
		List<Resource<T>> resourcesCollection = workshopEntitiesPage.get()
			.map(this::toResource)
			.collect(Collectors.toList());
		Collection<Link> pagedLinks = getPagedLinks(workshopEntitiesPage, workshopEntityOwnerId, controllerMethodName);
		return new Resources<>(resourcesCollection, pagedLinks);
	}
	
	/**
	 * The constructor for pagination Links (nextPage, prevPage etc).
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 *
	 * @param page The container with 'total pages', 'orderBy', 'order' and other data to prepare 'nextPage',
	 *             'prevPage' and other Links to be included into the Resources<T> paged collection.
	 * @return Collection of Links as 'nextPage', 'prevPage' etc to be added into "Resources<Resource<T>>" paged Links
	 */
	Collection<Link> getPagedLinks(Page page) {
		Collection<Link> pagedLinks = new ArrayList<>(7);
		
		String orderBy = page.getSort().iterator().next().getProperty();
		String order = page.getSort().iterator().next().getDirection().name();
		
		String hrefLang = LocaleContextHolder.getLocale().toLanguageTag();
		String lastPageTitle = "Page " + (page.getTotalPages());
		String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total. " +
			"Elements " + page.getNumberOfElements() + " of " + page.getTotalElements() + " elements total.";
		
		Link currentPageLink = getPagedLink(
			page.getPageable(), page.getNumber(), LINK_CURRENT_PAGE_REL, hrefLang, LINK_MEDIA, currentPageTitle);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(
				page.previousPageable(), page.previousPageable().getPageNumber(), LINK_PREV_PAGE_REL, hrefLang, LINK_MEDIA, LINK_DEFAULT_TITLE));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(
				page.nextPageable(), page.nextPageable().getPageNumber(), LINK_NEXT_PAGE_REL, hrefLang, LINK_MEDIA, LINK_DEFAULT_TITLE));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(
				page.getPageable().first(), page.getPageable().first().getPageNumber(), LINK_FIRST_PAGE_REL, hrefLang, LINK_MEDIA, LINK_DEFAULT_TITLE));
		}
		if (!page.isLast()) { //Add LastPage
			Link lastPageLink =
				ControllerLinkBuilder.linkTo(
					ControllerLinkBuilder.methodOn(workshopControllerAbstractClass)
						.getAll(page.getSize(), page.getTotalPages(), orderBy, order, null))
					.withRel(LINK_LAST_PAGE_REL)
					.withHreflang(hrefLang)
					.withMedia(LINK_MEDIA)
					.withTitle(lastPageTitle);
			
			pagedLinks.add(lastPageLink);
		}
		return pagedLinks;
	}
	
	/**
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 *
	 * @param page                 The container with 'total pages', 'orderBy', 'order' and other data to prepare 'nextPage',
	 *                             'prevPage' and other Links to be included into the Resources<T> paged collection.
	 * @param ownerId              Not used and nullable by default.
	 *                             Can be passed with a value to
	 *                             {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)}
	 *                             method which has to be overriding to use this id as the collection's 'ownerId', for
	 *                             instance.
	 * @param controllerMethodName Custom discrimination string to be caught here for building a custom
	 *                             ControllerLinkBuilder.methodOn(Controller.class).getAllCustom(ownerId, ...pageableParams)
	 *                             logic depending on this discrimination String.
	 * @return Collection of Links as 'nextPage', 'prevPage' ect to be added into "Resources<Resource<T>>" Links
	 */
	Collection<Link> getPagedLinks(Page page, Long ownerId, String controllerMethodName) {
		Collection<Link> pagedLinks = new ArrayList<>(7);
		
		String orderBy = page.getSort().iterator().next().getProperty();
		String order = page.getSort().iterator().next().getDirection().name();
		
		String hrefLang = LocaleContextHolder.getLocale().toLanguageTag();
		String lastPageTitle = "Page " + (page.getTotalPages());
		String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total. " +
			"Elements " + page.getNumberOfElements() + " of " + page.getTotalElements() + " elements total.";
		
		Link currentPageLink = getPagedLink(page.getPageable(), page.getSize(), LINK_CURRENT_PAGE_REL,
			hrefLang, LINK_MEDIA, currentPageTitle, ownerId, controllerMethodName);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(page.previousPageable(), page.previousPageable().getPageNumber(), LINK_PREV_PAGE_REL,
				hrefLang, LINK_MEDIA, LINK_DEFAULT_TITLE, ownerId, controllerMethodName));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(page.nextPageable(), page.nextPageable().getPageNumber(), LINK_NEXT_PAGE_REL, hrefLang,
				LINK_MEDIA, LINK_DEFAULT_TITLE, ownerId, controllerMethodName));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(page.getPageable().first(), page.getPageable().first().getPageNumber(),
				LINK_FIRST_PAGE_REL, hrefLang, LINK_MEDIA, LINK_DEFAULT_TITLE, ownerId, controllerMethodName));
		}
		if (!page.isLast()) { //Add LastPage
			pagedLinks.add(getPagedLink(
				page.getPageable(), page.getTotalPages(), LINK_LAST_PAGE_REL, hrefLang, LINK_MEDIA, lastPageTitle,
				ownerId, controllerMethodName));
		}
		
		return pagedLinks;
	}
	
	/**
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 *
	 * @param pageable Pageable information from a Clients with the desired parameters for custom Page and info about their current Page.
	 * @param pageNum  Obligatory current page number
	 * @return A fully prepared Link based on client's Pageable info and @ExposedResourceFor(Class.class) from "WorkshopController<T>".
	 */
	protected Link getPagedLink(
		Pageable pageable,
		int pageNum,
		String relation,
		String hrefLang,
		String media,
		String title) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		Link link =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(workshopControllerAbstractClass).getAll(
					pageable.getPageSize(),
					pageNum + 1,
					orderBy,
					order,
					null))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		return link;
	}
	
	/**
	 * This method has to be overridden to be able to receive 'controllerMethodName' as a discriminator for method from
	 * any {@literal WorkshopController<T>} which should return a paged List of this {@literal WorkshopEntity<T>}.
	 * You have to build your own Link with {@link ControllerLinkBuilder#methodOn(Class, Object...)} as in the following example:
	 * <p>
	 * <p>
	 * String orderBy = pageable.getSort().iterator().next().getProperty();
	 * String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
	 * Link link;
	 * if (WorkshopControllerAbstract<T>.CONTROLLER_METHOD_NAME.equalsIgnoreCase(controllerMethodName)) {
	 * link = ControllerLinkBuilder.linkTo(
	 * ControllerLinkBuilder.methodOn(WorkshopControllerAbstract<T>.class).controllerMethod(
	 * ownerId, pageable.getPageSize(), pageNum + 1, orderBy, order))
	 * .withRel(relation).withHreflang(hrefLang).withMedia(media).withTitle(title);
	 * } else {
	 * log.error("No matching 'controllerMethodName' found for the given parameter {} in the {} for the Link to be constructed!",
	 * controllerMethodName, workshopControllerAbstractClass);
	 * return new Link("/no_link_found/");
	 * }
	 * return link;
	 *
	 * @param pageable             The main info about pageable state.
	 * @param pageNum              Has to be ++pageNum as the result for the end users has to start from 1 despite
	 *                             inner Spring and JPA pagination starts from 0!
	 *                             The  obligatory parameter to obtain the current number of page.
	 * @param relation             Link relation parameter.
	 * @param hrefLang             Link language.
	 * @param media                Media type of the Link.
	 * @param title                Title of the Link.
	 * @param ownerId              ID of the Owner of this collection. E.g., getUser(ownerId).getOrders()
	 * @param controllerMethodName Discrimination string method name in order to allow to pass a needed method name and
	 *                             construct a custom Link according to ControllerLinkBuilder.methodOn().
	 *                             As usual, it passes as static string from WorkshopController
	 *                             .ORDERS_CREATED_BY_METHOD_NAME.
	 * @return A single custom Link created according to 'controllerMethodName'.
	 */
	protected abstract Link getPagedLink(
		Pageable pageable,
		int pageNum,
		String relation,
		String hrefLang,
		String media,
		String title,
		Long ownerId,
		String controllerMethodName);
}
