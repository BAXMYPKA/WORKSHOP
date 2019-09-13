package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * GrantedAuthorities for the EXTERNAL.Users for fine tuning their permissions
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"employees", "position"}, allowSetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity(name = "Internal_Authority")
@Table(name = "Internal_Authorities", schema = "INTERNAL")
public class InternalAuthority extends Trackable implements GrantedAuthority {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
/*
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorities_sequence")
	@SequenceGenerator(name = "authorities_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
	@NotNull(groups = {UpdateValidation.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {UpdateValidation.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
*/
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String authority;
	
	@Column
	@Length(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class}, max = 254,
		message = "{validation.length}")
	private String description;
	
/*
	@Column(updatable = false)
	@PastOrPresent(groups = {PersistenceValidation.class}, message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
*/
	
	/**
	 * Not serializable to JSON (to prevent huge amount of Users).
	 * But it is possible to deserialize from JSON to Object with the Users' set within particular WorkshopGrantedAuthority.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(targetEntity = Position.class, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Positions_To_Internal_Authorities", schema = "INTERNAL",
		joinColumns = {@JoinColumn(name = "internal_authority_id", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "position_id", nullable = false)})
	private Set<@Valid Position> position;
	
	@Override
	public String getAuthority() {
		return authority;
	}
	
/*
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = ZonedDateTime.now();
		}
	}
*/
}