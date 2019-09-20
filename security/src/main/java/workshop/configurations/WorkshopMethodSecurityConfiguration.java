package workshop.configurations;

import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

//@Configuration
public class WorkshopMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

//	@Autowired
//	private WorkshopPermissionEvaluator workshopPermissionEvaluator;
	
/*
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(new WorkshopPermissionEvaluator());
		return expressionHandler;
	}
*/

//	@Override
//	public void setMethodSecurityExpressionHandler(List<MethodSecurityExpressionHandler> handlers) {
//		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
//		expressionHandler.setPermissionEvaluator(new WorkshopPermissionEvaluator());
//		super.setMethodSecurityExpressionHandler(handlers);
//	}
}
