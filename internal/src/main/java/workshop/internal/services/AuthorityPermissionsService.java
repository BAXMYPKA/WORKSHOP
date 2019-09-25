package workshop.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import workshop.internal.dao.AuthorityPermissionsDao;
import workshop.internal.entities.AuthorityPermission;

@Service
public class AuthorityPermissionsService extends WorkshopEntitiesServiceAbstract<AuthorityPermission> {
	
	@Autowired
	private AuthorityPermissionsDao authorityPermissionsDao;
	
	public AuthorityPermissionsService(AuthorityPermissionsDao authorityPermissionsDao) {
		super(authorityPermissionsDao);
	}
}
