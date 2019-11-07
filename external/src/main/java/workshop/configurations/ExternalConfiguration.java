package workshop.configurations;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import workshop.external.dto.OrderDto;
import workshop.external.dto.UserDto;
import workshop.internal.entities.Order;
import workshop.internal.entities.User;

/**
 * Includes:
 * {@link ModelMapper} configuration
 */
@Configuration
public class ExternalConfiguration {
	
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
			.setFieldMatchingEnabled(true)
			.setSkipNullEnabled(true)
			.setPropertyCondition(Conditions.isNotNull())
			.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
		
		modelMapper.addMappings(new PropertyMap<UserDto, User>() {
			@Override
			protected void configure() {
				skip(destination.getPhones());
				skip(destination.getExternalAuthorities());
				skip(destination.getOrders());
				skip(destination.getIsEnabled());
//				skip(destination.getPassword());
			}
		});
		
		modelMapper.addMappings(new PropertyMap<Order, OrderDto>() {
			@Override
			protected void configure() {
				skip(destination.getOrder());
			}
		});
		
		return modelMapper;
	}
}
