package internal.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.entities.Department;
import internal.entities.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class DepartmentsResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Department> {
	
	public DepartmentsResourceAssembler() {
		setWorkshopControllerAbstractClass(DepartmentsController.class);
		setWorkshopEntityClass(Department.class);
		setDEFAULT_TITLE("Department");
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
	
	/**
	 * Adds navigation Links to the given "Resources<Resource<Position>>" extracted from the given "Page<Position>".
	 *
	 * @param positionsResources Unpaged "Resources<Resource<Position>>" without Links.
	 * @param positionPage       Pageable information (total pages, current page etc) to extract such an information.
	 * @param departmentId
	 * @return A fully navigable Resources with nextPage, prevPage etc Links.
	 */
	public Resources<Resource<Position>> positionsFromDepartmentToPagedResources(
		Resources<Resource<Position>> positionsResources, Page<Position> positionPage, Long departmentId) {
		//Get paged Links for this collection
		Collection<Link> pagedLinks = super.getPagedLinks(positionPage, departmentId);
		//Add fully paged navigation Links
		positionsResources.add(pagedLinks);
		return positionsResources;
	}
	
	/**
	 * @see WorkshopEntitiesResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, Long)
	 */
	@Override
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title,
								Long departmentId) {
		String orderBy = pageable.getSort().iterator().next().getProperty();
		String order = pageable.getSort().iterator().next().getDirection().name();
		Link link =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(DepartmentsController.class)
					.getDepartmentPositions(
						departmentId,
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
