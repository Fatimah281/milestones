package DTO;
//<editor-fold desc="Imports">
import java.util.List;
//</editor-fold>

public class EmployeeDto {

    //<editor-fold desc="Fields">
    private final Long id;
    private final String name;
    private final String gender;
    private final String dateOfBirth;
    private final String phoneNumber;
    private final List<HobbyDto> hobbies;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public EmployeeDto(Long id, String name, String gender, String dateOfBirth, String phoneNumber, List<HobbyDto> hobbies) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.hobbies = hobbies;
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public List<HobbyDto> getHobbies() {
        return hobbies;
    }
    //</editor-fold>

    //<editor-fold desc="toString">
    @Override
    public String toString() {
        return "EmployeeDto{id=" + id + ", name='" + name + "', gender='" + gender + "', dateOfBirth='" + dateOfBirth + "', phoneNumber='" + phoneNumber + "', hobbies=" + hobbies + "}";
    }
    //</editor-fold>
}
