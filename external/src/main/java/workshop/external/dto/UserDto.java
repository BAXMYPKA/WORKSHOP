package workshop.external.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import workshop.internal.entities.Phone;
import workshop.internal.entities.User;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class UserDto extends User {
	
//	private List<Phone> phones = new ArrayList<>(2);
	
/*
	public List<Phone> getPhones() {
		return phones;
	}
*/
	/*
	public void setPhones(List<Phone> phones) {
		List<Phone> phoneList = Objects.requireNonNull(phones, "Phones List cannot be null!");
		if (super.getPhones() == null) {
			super.setPhones(new HashSet<>(phones));
		} else {
			super.getPhones().addAll(phones);
		}
	}
*/

/*
	public Set<Phone> getPhones() {
		if (super.getPhones() == null) {
			super.setPhones(new HashSet<>(phones));
			return super.getPhones();
		} else {
			super.getPhones().addAll(phones);
			return super.getPhones();
		}
	}
*/
}
