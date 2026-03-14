package ru.lessons.my.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "Driver.detail",
        attributeNodes = {
                @NamedAttributeNode("enterprise"),
                @NamedAttributeNode("vehicles")
        }
)
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "salary")
    private Double salaryRub;

    @ManyToOne
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;

    @OneToOne(mappedBy = "activeDriver")
    private Vehicle currentVehicle;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name="driver_vehicle",
            joinColumns=  @JoinColumn(name="driver_id", referencedColumnName="id"),
            inverseJoinColumns= @JoinColumn(name="vehicle_id", referencedColumnName="id") )
    private Set<Vehicle> vehicles = new HashSet<>();
}
