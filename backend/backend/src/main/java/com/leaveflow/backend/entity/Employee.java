package com.leaveflow.backend.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.leaveflow.backend.enums.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 150)
   private String name;

   @Column(nullable = false, unique = true, length = 150)
   private String email;

   @Column(name = "password_hash", nullable = false)
   private String passwordHash;

   @Column(nullable = false, length = 100)
   private String department;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false, length = 20)
   private Role role;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "manager_id")
   private Employee manager;

   @CreationTimestamp
   @Column(name = "created_at", updatable = false)
   private LocalDateTime createdAt;

   @UpdateTimestamp
   @Column(name = "updated_at")
   private LocalDateTime updatedAt;
}
