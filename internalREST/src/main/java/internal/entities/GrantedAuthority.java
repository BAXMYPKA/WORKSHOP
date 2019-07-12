package internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;

/**
 * GrantedAuthorities for the Users for fine tuning their permissions
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "authority"})
@JsonIgnoreProperties(value = {"users"}, allowSetters = true)
@Entity(name = "Granted_Authority")
@Table(name = "Granted_Authorities", schema = "EXTERNAL")
public class GrantedAuthority implements WorkshopEntity, org.springframework.security.core.GrantedAuthority {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorities_sequence")
	@SequenceGenerator(name = "authorities_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
	@PositiveOrZero(message = "{validation.positiveOrZero}")
	private long id;
	
	@Column(unique = true, nullable = false)
	@NotBlank(message = "{validation.notBlank}")
	private String authority;
	
	@Column
	private String description;
	
	/**
	 * Not serializable from GrantedAuthority object to JSON (to prevent overwhelming amount of Users).
	 * But it is possible to deserialize from JSON to Object with the Users' set within particular GrantedAuthority.
	 */
	//	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	private Set<User> users;
	
	@Override
	public String getAuthority() {
		return authority;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public void setId(long id) {
		this.id = id;
	}
}
