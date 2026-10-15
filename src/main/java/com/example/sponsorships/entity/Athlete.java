package com.example.sponsorships.entity;

import com.example.sponsorships.enums.AthleteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "athlete")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String discipline;

    @Column(nullable = false, length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AthleteStatus status;

    @OneToMany(mappedBy = "athlete")
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();
}
