package model;
//<editor-fold desc="Imports">
import java.util.List;
//</editor-fold>

public class Employee {
    //<editor-fold desc="Fields">
    private Integer  id;
    private String name;
    private String gender;
    private String dateOfBirth;
    private String phoneNumber;
    private List<String> hobbies;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public Employee() {}

    public Employee(Integer id, String name, String gender, String dateOfBirth, String phoneNumber, List<String> hobbies) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.hobbies = hobbies;
    }
    //</editor-fold>

    //<editor-fold desc="Getters and Setters">

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }
    //</editor-fold>
}
