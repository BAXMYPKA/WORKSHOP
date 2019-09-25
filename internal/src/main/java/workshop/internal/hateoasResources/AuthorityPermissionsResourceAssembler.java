package workshop.internal.hateoasResources;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import workshop.internal.controllers.AuthorityPermissionsController;
import workshop.internal.entities.AuthorityPermission;

@Component
public class AuthorityPermissionsResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<AuthorityPermission> {
	/**
	 * Obligatory constructor.
	 * Delete the method arguments and only leave:
	 * super(WorkshopControllerInstance.class, WorkshopEntityInstance.class);
	 */
	public AuthorityPermissionsResourceAssembler() {
		super(AuthorityPermissionsController.class, AuthorityPermission.class);
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title, Long ownerId, String controllerMethodName) {
		return null;
	}
}
