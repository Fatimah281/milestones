package Entity;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
//</editor-fold>

@Entity
@Table(name = "EMPLOYEE", schema = "EMP_HOB")
public class Employee {

    //<editor-fold desc="Fields">
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "GENDER", length = 50)
    private String gender;

    @Column(name = "DATE_OF_BIRTH", length = 20)
    private String dateOfBirth;

    @Column(name = "PHONE_NUMBER", length = 50)
    private String phoneNumber;

    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH })
    @JsonManagedReference
    private List<Hobby> hobbies = new ArrayList<>();
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public Employee() {
    }

    public Employee(Long id, String name, String gender, String dateOfBirth, String phoneNumber, List<Hobby> hobbies) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.hobbies = hobbies != null ? hobbies : new ArrayList<>();
    }
    //</editor-fold>

    //<editor-fold desc="Getters and Setters">
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Hobby> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<Hobby> hobbies) {
        if (this.hobbies != null) {
            this.hobbies.forEach(h -> h.setEmployee(null));
        }
        this.hobbies = hobbies != null ? hobbies : new ArrayList<>();
        this.hobbies.forEach(h -> h.setEmployee(this));
    }

    /**
     * Adds a hobby and maintains the bidirectional relationship.
     */
    public void addHobby(Hobby hobby) {
        if (hobby != null) {
            hobbies.add(hobby);
            hobby.setEmployee(this);
        }
    }

    /**
     * Removes a hobby and clears the bidirectional relationship.
     */
    public void removeHobby(Hobby hobby) {
        if (hobby != null) {
            hobbies.remove(hobby);
            hobby.setEmployee(null);
        }
    }
    //</editor-fold>

    //<editor-fold desc="toString">
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
    //</editor-fold>

}
