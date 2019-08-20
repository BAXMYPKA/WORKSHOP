package internal.entities.hateoasResources;

import internal.controllers.WorkshopControllerAbstract;
import internal.entities.Employee;
import internal.entities.WorkshopEntity;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@Component
public abstract class WorkshopEntityResourceAssemblerAbstract <T extends WorkshopEntity>
	implements ResourceAssembler<T, Resource<T>> {
	
	private Class<? extends WorkshopControllerAbstract> workshopControllerAbstractClass;
	@Getter
	private Class<T> workshopEntityClass;
	@Autowired
	private EntityLinks entityLinks;
	private final String MEDIA = "application/json; charset=utf-8";
	private final String CURRENT_PAGE_REL = "currentPage";
	private final String PREV_PAGE_REL = "previousPage";
	private final String NEXT_PAGE_REL = "nextPage";
	private final String FIRST_PAGE_REL = "firstPage";
	private final String LAST_PAGE_REL = "lastPage";
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
	//	String lastPageTitle = "Page " + (page.getTotalPages());
//	String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total " +
//		"with " + page.getNumberOfElements() + " elements of " + page.getTotalElements() + " elements total.";
	
	public WorkshopEntityResourceAssemblerAbstract(
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
			.withMedia("application/hal+json; charset=utf-8")
			.withTitle("title");
		return new Resource<>(workshopEntity, selfGetLink);
	}
	
	/**
	 * Default method for obtaining the overall quantity of WorkshopEntities.
	 *
	 * @param workshopEntitiesPage Page<T> workshopEntitiesPage with current pageNum, pageSize, orderBy, order.
	 * @return 'Resources<Resource <T>>' - a collection WorkshopEntities as a resources with self-links
	 * and pagination Links (nextPage, prevPage etc).
	 */
	public Resources<Resource<T>> toPagedResources(Page<T> workshopEntitiesPage) {
		List<Resource<T>> resourcesCollection = workshopEntitiesPage.get()
			.map(this::toResource)
			.collect(Collectors.toList());
		Collection<Link> pagedLinks = getPagedLinks(workshopEntitiesPage, null);
		return new Resources<>(resourcesCollection, pagedLinks);
	}
	
	/**
	 * Special method for obtaining paged WorkshopEntities collections with their owner's ids.
	 *
	 * @param workshopEntitiesPage Page<T> workshopEntitiesPage with current pageNum, pageSize, orderBy, order.
	 * @param workshopEntityId     Can by used in some custom cases for deriving paged collections by owner's id.
	 *                             The given parameter passes down to
	 *                             {@link #getPagedLink(Pageable, int, String, String, String, String, String, String, Long)}
	 *                             method but not used by default. If you pass it, you have to override the method above
	 *                             to use this id.
	 * @return 'Resources<Resource <T>>' - a collection WorkshopEntities as a resources with self-links
	 * and pagination Links (nextPage, prevPage etc).
	 */
	public Resources<Resource<T>> toPagedResources(Page<T> workshopEntitiesPage, Long workshopEntityId) {
		List<Resource<T>> resourcesCollection = workshopEntitiesPage.get()
			.map(this::toResource)
			.collect(Collectors.toList());
		Collection<Link> pagedLinks = getPagedLinks(workshopEntitiesPage, workshopEntityId);
		return new Resources<>(resourcesCollection, pagedLinks);
	}
	
	/**
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 *
	 * @param page             The container with 'total pages', 'orderBy', 'order' and other data to prepare 'nextPage',
	 *                         'prevPage' and other Links to be included into the Resources<T> paged collection.
	 * @param workshopEntityId Not used and nullable by default.
	 *                         Can be passed with a value to
	 *                         {@link #getPagedLink(Pageable, int, String, String, String, String, String, String, Long)}
	 *                         method which has to be overriding to use this id as the collection's 'ownerId', for
	 *                         instance.
	 * @return Collection of Links as 'nextPage', 'prevPage' ect to be added into "Resources<Resource<T>>" Links
	 */
	Collection<Link> getPagedLinks(Page page, @Nullable Long workshopEntityId) {
		Collection<Link> pagedLinks = new ArrayList<>(7);
		
		String orderBy = page.getSort().iterator().next().getProperty();
		String order = page.getSort().iterator().next().getDirection().name();
		
		String hrefLang = LocaleContextHolder.getLocale().toLanguageTag();
		String lastPageTitle = "Page " + (page.getTotalPages());
		String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total. " +
			"Elements " + page.getNumberOfElements() + " of " + page.getTotalElements() + " elements total.";
		
		Link currentPageLink = getPagedLink(page.getPageable(), page.getSize(), orderBy, order, CURRENT_PAGE_REL,
			hrefLang, MEDIA, currentPageTitle, workshopEntityId);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(page.previousPageable(), page.getSize(), orderBy, order, PREV_PAGE_REL,
				hrefLang, MEDIA, null, workshopEntityId));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(page.nextPageable(), page.getSize(), orderBy, order, NEXT_PAGE_REL, hrefLang, MEDIA,
				null, workshopEntityId));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(page.getPageable().first(), page.getSize(), orderBy, order, FIRST_PAGE_REL,
				hrefLang, MEDIA, null, workshopEntityId));
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
	 * @param workshopEntityId Default = null and not used by this method.
	 *                         But can by used for special cased when you may want to override the method to
	 *                         invoke the given parameter.
	 */
	Link getPagedLink(Pageable pageable, int pageSize, String orderBy, String order, String relation,
								String hrefLang, String media, @Nullable String title, @Nullable Long workshopEntityId) {
		title = title == null ? "Page " + (pageable.getPageNumber() + 1) : title;
		
		Link link =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(workshopControllerAbstractClass)
					.getAll(pageSize, pageable.getPageNumber() + 1, orderBy, order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		return link;
	}
}
