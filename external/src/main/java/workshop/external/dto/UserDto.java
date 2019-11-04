package workshop.external.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import workshop.internal.entities.*;
import workshop.internal.entities.hibernateValidation.Merge;
import workshop.internal.entities.hibernateValidation.Persist;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

@Getter
@Setter
@Slf4j
@ToString(of = {"identifier", "firstName", "email"})
@groovy.transform.EqualsAndHashCode
@NoArgsConstructor
@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserDto implements Serializable {
	
	private User user;
	
	private List<@Valid Phone> phones = new ArrayList<>(2);
	
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@NotNull(groups = {Merge.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {Merge.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	@EqualsAndHashCode.Include
	private String firstName;
	
	@EqualsAndHashCode.Include
	private String lastName;
	
	@Pattern(regexp = "^(\\w){5,36}$", message = "{validation.passwordStrength}")
	private String password;
	
	@Email(message = "{validation.email}")
	@EqualsAndHashCode.Include
	private String email;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@Past(message = "{validation.past}")
	@EqualsAndHashCode.Include
	private LocalDate birthday;
	
	private Boolean isEnabled = true;
	
	private String languageTag;
	
	private Collection<@Valid Order> orders;
	
	private Set<@Valid ExternalAuthority> externalAuthorities;
	
	public void setUser(User user) {
		BeanUtils.copyProperties(Objects.requireNonNull(user), this);
		if (user.getPhones() != null) {
			this.phones.addAll(user.getPhones());
		}
	}
	
	public User getUser() {
		if (this.phones != null && this.phones.size() > 0) {
			Objects.requireNonNull(this.user).getPhones().addAll(this.phones);
		}
		return this.user;
	}
	
	/*
	@PostConstruct
	void createUser() {
	}
*/
}
