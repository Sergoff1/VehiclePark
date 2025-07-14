package ru.lessons.my.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "Enterprise.detail",
        attributeNodes = {
                @NamedAttributeNode("drivers"),
                @NamedAttributeNode("vehicles")
        }
)
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String city;

    @OneToMany(mappedBy = "enterprise", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private Set<Vehicle> vehicles = new HashSet<>();

    @OneToMany(mappedBy = "enterprise", cascade = CascadeType.ALL)
    private Set<Driver> drivers = new HashSet<>();

}
