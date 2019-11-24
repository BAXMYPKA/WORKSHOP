package workshop.external.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import workshop.internal.entities.*;
import workshop.internal.entities.hibernateValidation.Merge;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.entities.hibernateValidation.PersistEmployee;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderDto implements Serializable {
	
	private Order order;
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@NotNull(groups = {Merge.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {Merge.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	@ToString.Include
	private Long identifier;
	
	@PastOrPresent(groups = {PersistEmployee.class},
		message = "{validation.pastOrPresent}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	private ZonedDateTime created;
	
	@Null(groups = {Persist.class}, message = "{validation.null}")
	private ZonedDateTime modified;
	
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private ZonedDateTime finished;
	
	/**
	 * Sets automatically in the DaoAbstract.persistEntity() if an Employee is presented in the SecurityContext.
	 * Also may be set manually.
	 */
	@Valid
	private Employee createdBy;
	
	@Valid
	private Employee modifiedBy;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Future(groups = {Persist.class}, message = "{validation.future}")
	@EqualsAndHashCode.Include
	private ZonedDateTime deadline;
	
	@EqualsAndHashCode.Include
	private String description;
	
	/**
	 * This will be sent to {@link #createdFor} when this Order is finished or cancelled.
	 */
	private String messageToUser;
	
	/**
	 * Enabled by @EnableJpaAudition
	 * If an Order is created by User himself - this field is filling in automatically in the DaoAbstract.persistEntity()
	 * (if an User is presented in the SecurityContext).
	 */
	@Valid
	private User createdFor;
	
	private Set<@Valid Task> tasks = new HashSet<>(5);
	
	/**
	 * Sets automatically as the sum of the all included Tasks.
	 * Also can be set or corrected manually.
	 */
	@PositiveOrZero(groups = {Default.class, Persist.class, Merge.class},
		message = "{validation.positiveOrZero}")
	@EqualsAndHashCode.Include
	private BigDecimal overallPrice = BigDecimal.ZERO;
}
