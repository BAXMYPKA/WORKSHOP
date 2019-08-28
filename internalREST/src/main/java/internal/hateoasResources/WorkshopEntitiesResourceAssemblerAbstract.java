package internal.hateoasResources;

import internal.controllers.WorkshopControllerAbstract;
import internal.entities.WorkshopEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@Component
public abstract class WorkshopEntitiesResourceAssemblerAbstract <T extends WorkshopEntity>
	implements ResourceAssembler<T, Resource<T>> {
	
	private Class<? extends WorkshopControllerAbstract> workshopControllerAbstractClass;
	private Class<T> workshopEntityClass;
	@Autowired
	private EntityLinks entityLinks;
	private final String MEDIA = "application/json; charset=utf-8";
	private final String CURRENT_PAGE_REL = "currentPage";
	private final String PREV_PAGE_REL = "previousPage";
	private final String NEXT_PAGE_REL = "nextPage";
	private final String FIRST_PAGE_REL = "firstPage";
	private final String LAST_PAGE_REL = "lastPage";
	protected String DEFAULT_TITLE = "";
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
	
	/**
	 * Obligatory constructor.
	 *
	 * @param workshopControllerAbstractClass The concrete class-type of WorkshopController
	 * @param workshopEntityClass             The concrete class-type of WorkshopEntity
	 */
	public WorkshopEntitiesResourceAssemblerAbstract(
		Class<? extends WorkshopControllerAbstract<T>> workshopControllerAbstractClass,
		Class<T> workshopEntityClass) {
		
		setWorkshopControllerAbstractClass(workshopControllerAbstractClass);
		setWorkshopEntityClass(workshopEntityClass);
	}
	
	/**
	 * Transforms WorkshopEntity into Resource<WorkshopEntity> and adds the self-Link to it.
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
			.withMedia(MEDIA)
			.withTitle(DEFAULT_TITLE);
		return new Resource<>(workshopEntity, selfGetLink);
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
	 * Special method for obtaining paged WorkshopEntities collections with their owner's ID
	 * if ////////////// TO COMPLETE ///////////////
	 * This method:
	 * 1) Transforms the every given WorkshopEntity to "Resource<T>"
	 * 2) From a given Page information prepares pageable navigation Links through WorkshopEntities collection
	 * 3) Returns a ready to use pageable "Resources<Resource<T>>"
	 * For using this method it is obligatory to override {@link #getPagedLinks(Page, Long)} method to use
	 * 'workshopEntityID' parameter for constructing your own paged navigation links with ControllerLinkBuilder
	 * .methodOn().
	 *
	 * @param workshopEntitiesPage  Page<T> workshopEntitiesPage with current pageNum, pageSize, orderBy, order.
	 * @param workshopEntityOwnerId Has to be used in some custom cases for deriving paged collections by owner's id.
	 *                              The given parameter passes down to
	 *                              {@link #getPagedLink(Pageable, int, String, String, String, String, Long)}
	 *                              method but not used by default. If you pass it, you have to override the method above
	 *                              to use this id.
	 * @return 'Resources<Resource <T>>' - a collection WorkshopEntities as a resources with self-links
	 * and pagination Links (nextPage, prevPage etc).////////////////// TO COMPLETE ///////////////////////////
	 */
	public Resources<Resource<T>> toPagedSubResources(Page<T> workshopEntitiesPage, Long workshopEntityOwnerId) {
		List<Resource<T>> resourcesCollection = workshopEntitiesPage.get()
			.map(this::toResource)
			.collect(Collectors.toList());
		Collection<Link> pagedLinks = getPagedLinks(workshopEntitiesPage, workshopEntityOwnerId);
		return new Resources<>(resourcesCollection, pagedLinks);
	}
	
	/**
	 * NOTICE: DON'T FORGET TO OVERRIDE {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)} method
	 * IN CASE OF USING THIS!
	 * Special method for obtaining paged WorkshopEntities collections with their owner's ID
	 * by overriding {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)} method
	 * and constructing custom ControllerLinkBuilder.linkTo(methodOn()) Links for various sub collections from one
	 * WorkshopEntity (Employee.getAppointedTasks, Employee.getModifiedTasks etc).
	 * This method:
	 * 1) Transforms the every given WorkshopEntity to "Resource<T>"
	 * 2) From a given Page information prepares pageable navigation Links through WorkshopEntities collection
	 * 3) Returns a ready to use pageable "Resources<Resource<T>>"
	 * For using this method it is obligatory to override {@link #getPagedLinks(Page, Long)} method to use
	 * 'workshopEntityID' parameter for constructing your own paged navigation links with ControllerLinkBuilder
	 * .methodOn().
	 *
	 * @param workshopEntitiesPage  Page<T> workshopEntitiesPage with current pageNum, pageSize, orderBy, order.
	 * @param workshopEntityOwnerId Has to be used in some custom cases for deriving paged collections by owner's id.
	 *                              The given parameter passes down to
	 *                              {@link #getPagedLink(Pageable, int, String, String, String, String, Long)}
	 *                              method but not used by default. If you pass it, you have to override the method above
	 *                              to use this id.
	 * @param controllerMethodName  The discriminator name of the RestController method to being passed to the
	 *                              {@link #getPagedLink(Pageable, int, String, String, String, String, Long, String)} method
	 *                              to construct a custom ControllerLinkBuilder.linkTo(methodOn()) Links based on the given name
	 *                              for various sub collections of one WorkshopEntity (Employee.getAppointedTasks, Employee.getModifiedTasks etc).
	 * @return 'Resources<Resource <T>>' - a collection WorkshopEntities as a resources with self-links
	 * and pagination Links (nextPage, prevPage etc).
	 */
	public Resources<Resource<T>> toPagedSubResources(Page<T> workshopEntitiesPage,
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
			page.getPageable(), page.getNumber(), CURRENT_PAGE_REL, hrefLang, MEDIA, currentPageTitle);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(
				page.previousPageable(), page.previousPageable().getPageNumber(), PREV_PAGE_REL, hrefLang, MEDIA, DEFAULT_TITLE));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(
				page.nextPageable(), page.nextPageable().getPageNumber(), NEXT_PAGE_REL, hrefLang, MEDIA, DEFAULT_TITLE));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(
				page.getPageable().first(), page.getPageable().first().getPageNumber(), FIRST_PAGE_REL, hrefLang, MEDIA, DEFAULT_TITLE));
		}
		if (!page.isLast()) { //Add LastPage
			Link lastPageLink =
				ControllerLinkBuilder.linkTo(
					ControllerLinkBuilder.methodOn(workshopControllerAbstractClass)
						.getAll(page.getSize(), page.getTotalPages(), orderBy, order))
					.withRel(LAST_PAGE_REL)
					.withHreflang(hrefLang)
					.withMedia(MEDIA)
					.withTitle(lastPageTitle);
			
			pagedLinks.add(lastPageLink);
		}
		return pagedLinks;
	}
	
	/**
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 *
	 * @param page    The container with 'total pages', 'orderBy', 'order' and other data to prepare 'nextPage',
	 *                'prevPage' and other Links to be included into the Resources<T> paged collection.
	 * @param ownerId Passes with a value to
	 *                {@link #getPagedLink(Pageable, int, String, String, String, String, Long)}
	 *                method which has to be overridden to use this id as the collection's 'ownerId' with a custom
	 *                ControllerLinkBuilder.methodOn(Controller.class).getAllCustom(ownerId, ...pageable) Link.
	 * @return Collection of Links as 'nextPage', 'prevPage' ect to be added into "Resources<Resource<T>>" Links
	 */
	Collection<Link> getPagedLinks(Page page, Long ownerId) {
		Collection<Link> pagedLinks = new ArrayList<>(7);
		
		String orderBy = page.getSort().iterator().next().getProperty();
		String order = page.getSort().iterator().next().getDirection().name();
		
		String hrefLang = LocaleContextHolder.getLocale().toLanguageTag();
		String lastPageTitle = "Page " + (page.getTotalPages());
		String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total. " +
			"Elements " + page.getNumberOfElements() + " of " + page.getTotalElements() + " elements total.";
		
		Link currentPageLink = getPagedLink(
			page.getPageable(), page.getNumber(), CURRENT_PAGE_REL, hrefLang, MEDIA, currentPageTitle, ownerId);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(
				page.previousPageable(), page.previousPageable().getPageNumber(), PREV_PAGE_REL, hrefLang, MEDIA, DEFAULT_TITLE, ownerId));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(
				page.nextPageable(), page.nextPageable().getPageNumber(), NEXT_PAGE_REL, hrefLang, MEDIA, DEFAULT_TITLE, ownerId));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(
				page.getPageable().first(), page.getPageable().first().getPageNumber(), FIRST_PAGE_REL, hrefLang, MEDIA, DEFAULT_TITLE, ownerId));
		}
		if (!page.isLast()) { //Add LastPage
			pagedLinks.add(getPagedLink(
				page.getPageable(), page.getTotalPages(), LAST_PAGE_REL, hrefLang, MEDIA, lastPageTitle, ownerId));
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
		
		Link currentPageLink = getPagedLink(page.getPageable(), page.getSize(), CURRENT_PAGE_REL,
			hrefLang, MEDIA, currentPageTitle, ownerId, controllerMethodName);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(page.previousPageable(), page.previousPageable().getPageNumber(), PREV_PAGE_REL,
				hrefLang, MEDIA, DEFAULT_TITLE, ownerId, controllerMethodName));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(page.nextPageable(), page.nextPageable().getPageNumber(), NEXT_PAGE_REL, hrefLang,
				MEDIA, DEFAULT_TITLE, ownerId, controllerMethodName));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(page.getPageable().first(), page.getPageable().first().getPageNumber(),
				FIRST_PAGE_REL, hrefLang, MEDIA, DEFAULT_TITLE, ownerId, controllerMethodName));
		}
		if (!page.isLast()) { //Add LastPage
			pagedLinks.add(getPagedLink(
				page.getPageable(), page.getTotalPages(), LAST_PAGE_REL, hrefLang, MEDIA, lastPageTitle,
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
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		Link link =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(workshopControllerAbstractClass).getAll(
					pageable.getPageSize(),
					pageNum + 1,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		return link;
	}
	
	/**
	 * The method obligatory to be overridden if you use Long 'ownerID' parameter for constructing your own
	 * Links.
	 * You have to use your own "ControllerLinkBuilder.methodOn(Controller.class).getAllCustom(ownerId, ...pageable)"
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 *
	 * @param pageable The main info about pageable state.
	 * @param pageNum  The obligatory parameter to obtain the current number of page.
	 * @param ownerId  ID of the Owner of this collection. E.g., getUser(ownerId).getOrders()
	 */
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title,
								Long ownerId) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		Link link;
		if (ownerId != null) {
			//Do custom logic
		}
		link =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(workshopControllerAbstractClass).getAll(
					pageable.getPageSize(),
					pageNum + 1,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		return link;
	}
	
	/**
	 * This method is strictly intended to be overridden.
	 * If not, default WorkshopController.getAll() method will be used for constructing a ControllerLinkBuilder.methodOn(Controller.class).getAll()
	 * Link.
	 * You have to use your own "ControllerLinkBuilder.methodOn(Controller.class).getAllCustom(ownerId, ...pageable)"
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 *
	 * @param pageable             The main info about pageable state.
	 * @param pageNum              The obligatory parameter to obtain the current number of page.
	 * @param ownerId              ID of the Owner of this collection. E.g., getUser(ownerId).getOrders()
	 * @param controllerMethodName Discriminator string name in order to allow to pass custom parameters to play with.
	 *                             Doesn't used by default.
	 */
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title,
								Long ownerId, String controllerMethodName) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().getOrderFor(orderBy).getDirection().name();
		Link link =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(workshopControllerAbstractClass).getAll(
					pageable.getPageSize(),
					pageNum + 1,
					orderBy,
					order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		return link;
	}
}
