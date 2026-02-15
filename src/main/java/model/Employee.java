package model;
//<editor-fold desc="Imports">
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, orphanRemoval = true)
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
        this.hobbies = hobbies != null ? hobbies : new ArrayList<>();
    }
    //</editor-fold>
}
