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
@EqualsAndHashCode
@NoArgsConstructor
@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserDto implements Serializable {
	
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	private User user;
	
	private List<@Valid Phone> phones = new ArrayList<>(2);
	
	@NotNull(message = "{validation.notNull}")
	@Positive(message = "{validation.positive}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	@NotEmpty(message = "{validation.notNull}")
	@Pattern(groups = {Persist.class, Default.class}, message = "{validation.pattern.name}",
			 regexp = "^([\\p{LD}-]){3,50}\\s?([\\p{LD}-]){0,50}\\s?([\\p{LD}-]){0,50}")
	@EqualsAndHashCode.Include
	private String firstName;
	
	@Pattern(groups = {Persist.class, Default.class}, message = "{validation.pattern.name}",
			 regexp = "^([\\p{LD}-]){3,50}\\s?([\\p{LD}-]){0,50}\\s?([\\p{LD}-]){0,50}")
	@EqualsAndHashCode.Include
	private String lastName;
	
	/**
	 * The raw, non-encoded password
	 */
	@Pattern(groups = Persist.class, regexp = "^[\\p{LD}\\-._+=()*&%$#@!<>\\[{\\]}'\"^;:?/~`]{5,254}$",
			 message = "{validation.passwordStrength}")
	private String confirmPassword;
	
	@NotEmpty(groups = {Persist.class, Default.class}, message = "{validation.notNull}")
	@Email(groups = {Persist.class, Default.class}, message = "{validation.email}")
	@EqualsAndHashCode.Include
	private String email;
	
	@NotNull(message = "{validation.notNull}")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@Past(groups = {Persist.class, Default.class}, message = "{validation.past}")
	@EqualsAndHashCode.Include
	private LocalDate birthday;
	
	private Boolean isEnabled = true;
	
	@Pattern(groups = {Persist.class, Merge.class}, regexp = "^[a-zA-z]{2,3}$")
	private String languageTag;
	
	private Collection<@Valid Order> orders;
	
	private Set<@Valid ExternalAuthority> externalAuthorities;
	
	@Size(max = 5242880, message = "{validation.photoSize}")
	private byte[] photo;
	
	public User getUser() {
		if (this.phones != null && this.phones.size() > 0) {
			Objects.requireNonNull(this.user).getPhones().addAll(this.phones);
		}
		return this.user;
	}
	
	public void setUser(User user) {
		BeanUtils.copyProperties(Objects.requireNonNull(user), this);
		if (user.getPhones() != null) {
			this.phones.addAll(user.getPhones());
		}
	}
}
