package internal.dao;

import internal.entities.InternalAuthority;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public class InternalAuthoritiesDao extends WorkshopEntitiesDaoAbstract<InternalAuthority, Long> {
	
	public InternalAuthoritiesDao() {
		super.setEntityClass(InternalAuthority.class);
		super.setKeyClass(Long.class);
	}
	
	public Optional<Set<InternalAuthority>> findAllInternalAuthoritiesByPosition(Integer pageSize,
																				 Integer pageNum,
																				 String orderBy,
																				 Sort.Direction order,
																				 Long positionId) {
		
		//TODO: to complete
		return null;
	}
}
