package DTO;

import java.util.List;

public record EmployeeDto(
    Long id,
    String name,
    String gender,
    String dateOfBirth,
    String phoneNumber,
    List<HobbyDto> hobbies
) {
}

