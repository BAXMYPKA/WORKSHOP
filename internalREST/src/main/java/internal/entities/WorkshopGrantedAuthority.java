package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.MergingCheck;
import internal.entities.hibernateValidation.PersistenceCheck;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * GrantedAuthorities for the EXTERNAL.Users for fine tuning their permissions
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"identifier", "authority"})
@JsonIgnoreProperties(value = {"users"}, allowSetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity(name = "Granted_Authority")
@Table(name = "Granted_Authorities", schema = "EXTERNAL")
public class WorkshopGrantedAuthority extends WorkshopEntityAbstract implements GrantedAuthority {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorities_sequence")
	@SequenceGenerator(name = "authorities_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
	@NotNull(groups = {MergingCheck.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {MergingCheck.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {PersistenceCheck.class}, message = "{validation.null}")
	private Long identifier;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceCheck.class, MergingCheck.class}, message = "{validation.notBlank}")
	private String authority;
	
	@Column
	@Length(groups = {Default.class, PersistenceCheck.class, MergingCheck.class}, max = 254,
		message = "{validation.length}")
	private String description;
	
	@Column(updatable = false)
	@PastOrPresent(groups = {PersistenceCheck.class}, message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
	
	/**
	 * Not serializable to JSON (to prevent huge amount of Users).
	 * But it is possible to deserialize from JSON to Object with the Users' set within particular WorkshopGrantedAuthority.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "grantedAuthorities", targetEntity = User.class, cascade = {
		CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<User> users;
	
	@Override
	public String getAuthority() {
		return authority;
	}
	
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = ZonedDateTime.now();
		}
	}
}