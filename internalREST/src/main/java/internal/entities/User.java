package internal.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Users of 2 types can be persisted from online and created by manager.
 * All Users must provide either their email or phone as a login and the way for the further communications.
 * Other fields are optional.
 * Online Users must create a password during registration.
 * Offline created Users will have a possibility to register themselves by their previously provided email or phone
 * as a login.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "created"})
@ToString(of = {"id", "email", "firstName"})
@Entity
@Table(name = "Users", schema = "EXTERNAL")
public class User implements WorkshopEntity, Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_sequence")
	@SequenceGenerator(name = "users_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
	
	@Column
	private String firstName;
	
	@Column
	private String lastName;
	
	/**
	 * Can be written from JSON to the User.class as a raw password.
	 * But cannot be serialized as an encoded password from DB while serializing
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column
	private String password;
	
	@Column
	private String email;
	
	@Column(nullable = false)
	//TODO: possible to exclude similar fields from serialization as useless for Users
	private LocalDateTime created;
	
	@Column
	//TODO: possible to exclude similar fields from serialization as useless for Users
	private LocalDateTime modified;
	
	@Column
	private LocalDate birthday;
	
	@OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Phone> phones;
	
	@OneToMany(mappedBy = "createdFor", orphanRemoval = false, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<Order> orders;
}
