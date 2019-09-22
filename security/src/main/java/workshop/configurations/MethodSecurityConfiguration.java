package workshop.configurations;

import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

//@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

//	@Autowired
//	private WorkshopPermissionEvaluator workshopPermissionEvaluator;

//	@Override
//	protected MethodSecurityExpressionHandler createExpressionHandler() {
//		WorkshopMethodSecurityExpressionHandler workshopExpressionHandler =
//			new WorkshopMethodSecurityExpressionHandler();
//		workshopExpressionHandler.setPermissionEvaluator(workshopPermissionEvaluator);
//		return workshopExpressionHandler;
//	}


	
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
