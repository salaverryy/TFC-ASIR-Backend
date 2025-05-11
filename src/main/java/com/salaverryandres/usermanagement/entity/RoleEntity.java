package com.salaverryandres.usermanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // Ej: ADMIN, USER

    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users;

    // Opcional: método toString si necesitas debug, evitando referencias cíclicas
}
