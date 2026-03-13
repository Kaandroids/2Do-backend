package com.example._Do.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must be at most 100 characters")
    private String name;

    private String description;
}
