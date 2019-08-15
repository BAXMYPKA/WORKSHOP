package internal.entities.hateoasResources;

import internal.controllers.WorkshopControllerAbstract;
import internal.entities.WorkshopEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component
public class WorkshopEntityResourceAssembler<T extends WorkshopEntity>
	  implements ResourceAssembler<T, Resource<T>> {
	
	//	@Autowired
	private WorkshopControllerAbstract<T> workshopControllerAbstract;
	private Class<? extends WorkshopControllerAbstract> workshopControllerAbstractClass;
	@Getter
	private Class<T> workshopEntityClass;
	private String workshopEntitySimpleClassName;
	@Autowired
	private EntityLinks entityLinks;
	private final String MEDIA = "application/json; charset=utf-8";
	private final String CURRENT_PAGE_REL = "currentPage";
	private final String PREV_PAGE_REL = "previousPage";
	private final String NEXT_PAGE_REL = "nextPage";
	private final String FIRST_PAGE_REL = "firstPage";
	private final String LAST_PAGE_REL = "lastPage";
	private final String HREF_LANG = LocaleContextHolder.getLocale().toLanguageTag();
//	String lastPageTitle = "Page " + (page.getTotalPages());
//	String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total " +
//		"with " + page.getNumberOfElements() + " elements of " + page.getTotalElements() + " elements total.";
	
	/**
	 * Obligatory constructor to create a proper bean
	 *
	 * @param workshopControllerAbstract WorkshopController<T extends WorkshopEntity> has to be annotated with
	 *                                   '@ExposesResourceFor(WorkshopEntity.class)' for Spring's HATEOAS support,
	 *                                   has to be created according to String HATEOAS REST controllers convention
	 *                                   and contain the exact WorkshopEntity.class to be extracted
	 *                                   into {@link #workshopEntityClass}
	 */
	public WorkshopEntityResourceAssembler(WorkshopControllerAbstract<T> workshopControllerAbstract) {
		this.workshopControllerAbstract = workshopControllerAbstract;
		this.workshopControllerAbstractClass = workshopControllerAbstract.getClass();
		this.workshopEntityClass = workshopControllerAbstract.getWorkshopEntityClass();
		workshopEntitySimpleClassName = workshopEntityClass.getSimpleName();
		
	}
	
	/**
	 * Adds the self-Link to the given WorkshopEntities
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
			  .withMedia("application/json; charset=utf-8")
			  .withTitle("title");
		return new Resource<>(workshopEntity, selfGetLink);
	}
	
	public Resources<Resource<T>> toPagedResources(Page<T> workshopEntitiesPage) {
		List<Resource<T>> resourcesCollection = workshopEntitiesPage.get().map(this::toResource)
			.collect(Collectors.toList());
		Collection<Link> pagedLinks = getPagedLinks(workshopEntitiesPage);
		return new Resources<>(resourcesCollection, pagedLinks);
	}
	
	/**
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 */
	private Collection<Link> getPagedLinks(Page page) {
		Collection<Link> pagedLinks = new ArrayList<>(7);
		
		String orderBy = page.getSort().iterator().next().getProperty();
		String order = page.getSort().iterator().next().getDirection().name();
		
		String hrefLang = LocaleContextHolder.getLocale().toLanguageTag();
		String lastPageTitle = "Page " + (page.getTotalPages());
		String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total " +
			  "with " + page.getNumberOfElements() + " elements of " + page.getTotalElements() + " elements total.";
		
		Link currentPageLink = getPagedLink(page.getPageable(), page.getSize(), orderBy, order, CURRENT_PAGE_REL,
			  hrefLang, MEDIA, currentPageTitle);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(page.previousPageable(), page.getSize(), orderBy, order, PREV_PAGE_REL,
				  hrefLang, MEDIA, null));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(page.nextPageable(), page.getSize(), orderBy, order, NEXT_PAGE_REL, hrefLang, MEDIA,
				  null));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(page.getPageable().first(), page.getSize(), orderBy, order, FIRST_PAGE_REL,
				  hrefLang, MEDIA, null));
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
	 */
	private Link getPagedLink(Pageable pageable, int pageSize, String orderBy, String order, String relation,
							  String hrefLang, String media, @Nullable String title) {
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
