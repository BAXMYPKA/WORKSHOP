package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import workshop.internal.entities.AuthorityPermission;

@Slf4j
@Repository
public class AuthorityPermissionsDao extends WorkshopEntitiesDaoAbstract<AuthorityPermission, Long> {
	public AuthorityPermissionsDao() {
		setEntityClass(AuthorityPermission.class);
		setKeyClass(Long.class);
	}
}
