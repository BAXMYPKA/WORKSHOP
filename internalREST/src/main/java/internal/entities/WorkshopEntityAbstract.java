package internal.entities;

import org.springframework.hateoas.ResourceSupport;

import javax.persistence.Transient;
import java.io.Serializable;

public abstract class WorkshopEntityAbstract extends ResourceSupport implements WorkshopEntity, Serializable {
	
	@Transient
	private long serialVersionUID = 5L;
	
	@Override
	public Long getIdentifier() {
		return null;
	}
	
	@Override
	public void setIdentifier(Long id) {
	
	}
}
