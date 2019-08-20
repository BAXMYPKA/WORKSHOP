package internal.entities.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.entities.Department;
import internal.entities.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class DepartmentResourceAssembler extends WorkshopEntityResourceAssemblerAbstract<Department> {
	
	@Autowired
	private PositionResourceAssembler positionResourceAssembler;
	
	public DepartmentResourceAssembler() {
		setWorkshopControllerAbstractClass(DepartmentsController.class);
		setWorkshopEntityClass(Department.class);
	}
	
	/**
	 * @param workshopEntity An WorkshopEntity instance to extract 'identifier' from.
	 * @return Resource<Department> with added self-rel Link and 'departments/{id}/positions' relation Link.
	 */
	@Override
	public Resource<Department> toResource(Department workshopEntity) {
		Resource<Department> selfLinkedDepartmentResource = super.toResource(workshopEntity);
		return addPositionsFromDepartmentLink(selfLinkedDepartmentResource);
	}
	
	private Resource<Department> addPositionsFromDepartmentLink(Resource<Department> departmentResource) {
		Link departmentPositionsLink =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(DepartmentsController.class)
					.getDepartmentPositions(
						departmentResource.getContent().getIdentifier(),
						getDEFAULT_PAGE_SIZE(),
						1, getDEFAULT_ORDER_BY(),
						getDEFAULT_ORDER()))
				.withRel("Positions");
		
		departmentResource.add(departmentPositionsLink);
		return departmentResource;
	}
	
	public Resources<Resource<Position>> positionsFromDepartmentToPagedResources(
		Page<Position> positionPage, Long departmentId) {
		
		//Get every Position as a Resource<Position> and collect
		Collection<Resource<Position>> positionsResources = positionPage
			.stream()
			.map(position -> positionResourceAssembler.toResource(position))
			.collect(Collectors.toList());
		//Collect them into Resources
		Resources<Resource<Position>> positionResources = new Resources<>(positionsResources);
		//Get paged Links for this collection
		Collection<Link> pagedLinks = super.getPagedLinks(positionPage, departmentId);
		//Add fully paged navigation Links
		positionResources.add(pagedLinks);
		return positionResources;
	}
	
	/**
	 * @see WorkshopEntityResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, String, String, Long)
	 */
	@Override
	protected Link getPagedLink(Pageable pageable, int pageSize, String orderBy, String order, String relation, String hrefLang,
								String media, @Nullable String title, Long departmentId) {
		if (departmentId == null) { //It is a standard 'getAll' request
			return super.getPagedLink(pageable, pageSize, orderBy, order, relation, hrefLang, media, title, departmentId);
		} else { //It is the special request
			title = title == null ? "Page " + (pageable.getPageNumber() + 1) : title;
			
			Link link =
				ControllerLinkBuilder.linkTo(
					ControllerLinkBuilder.methodOn(DepartmentsController.class)
						.getDepartmentPositions(
							departmentId, pageSize, pageable.getPageNumber() + 1, orderBy, order))
					.withRel(relation)
					.withHreflang(hrefLang)
					.withMedia(media)
					.withTitle(title);
			
			return link;
		}
	}
}
