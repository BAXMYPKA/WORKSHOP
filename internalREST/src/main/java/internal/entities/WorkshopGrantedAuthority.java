package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
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
	
//	@Transient
//	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorities_sequence")
	@SequenceGenerator(name = "authorities_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
	@PositiveOrZero(message = "{validation.positiveOrZero}")
	private Long identifier;
	
	@Column(unique = true, nullable = false)
	@NotBlank(message = "{validation.notBlank}")
	private String authority;
	
	@Column
	@Length(max = 254, message = "{validation.length}")
	private String description;
	
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
	
/*
	@Override
	public Long getIdentifier() {
		return identifier;
	}
	
	@Override
	public void setIdentifier(Long identifier) {
		this.identifier = identifier;
	}
*/
}