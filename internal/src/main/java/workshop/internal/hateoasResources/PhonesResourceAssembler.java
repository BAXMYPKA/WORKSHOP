package workshop.internal.hateoasResources;

import workshop.internal.controllers.PhonesController;
import workshop.internal.entities.Phone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PhonesResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Phone> {
	
	public PhonesResourceAssembler() {
		super(PhonesController.class, Phone.class);
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable,
								int pageNum,
								String relation,
								String hrefLang,
								String media,
								String title,
								Long ownerId,
								String controllerMethodName) {
		return null;
	}
}
