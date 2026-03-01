package DTO;

public class HobbyDto {

    //<editor-fold desc="Fields">
    private final Long id;
    private final String name;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public HobbyDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    //</editor-fold>

    //<editor-fold desc="toString">
    @Override
    public String toString() {
        return "HobbyDto{id=" + id + ", name='" + name + "'}";
    }
    //</editor-fold>
}
