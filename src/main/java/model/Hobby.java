package model;

public class Hobby {

    //<editor-fold desc="Fields">
    private Integer id;
    private Integer employeeId;
    private String name;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public Hobby() {
    }

    public Hobby(Integer id, Integer employeeId, String name) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
    }

    public Hobby(String name) {
        this.name = name;
    }
    //</editor-fold>

    //<editor-fold desc="Getters and Setters">
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    //</editor-fold>
}