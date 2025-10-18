package com.hackovation.authservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hackovation.authservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "roles")
public class Role{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    private UserRole roleName;

    @Column(nullable = false, unique = true)
    private Integer roleId; // As defined in UserRole Enum

    @OneToMany(mappedBy = "role")
    private Set<UserRoleMapping> userRoles = new HashSet<>();
}
