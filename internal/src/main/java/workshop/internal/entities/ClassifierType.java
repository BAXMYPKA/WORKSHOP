package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * The grouping class for {@link Classifier}
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "Classifier_Types", schema = "INTERNAL")
public class ClassifierType extends WorkshopAudibleEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(nullable = false, unique = true)
	@NotBlank(groups = {MergingValidation.class, PersistenceValidation.class, Default.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String name;
	
	@Column
	private String description;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(mappedBy = "classifierType", orphanRemoval = false, fetch = FetchType.EAGER,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Classifier> classifiers;
}
