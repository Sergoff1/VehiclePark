package ru.lessons.my.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.exception.ConstraintViolationException;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "Enterprise.detail",
        attributeNodes = {
                @NamedAttributeNode("drivers"),
                @NamedAttributeNode("managers"),
                @NamedAttributeNode("vehicles")
        }
)
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String city;

    @ManyToMany(mappedBy = "enterprises", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Manager> managers = new HashSet<>();

    //todo Рассмотреть возможность использования коллекции чисел для простого хранения айдишников.
    // Кажется, что целые сущности тут ни к чему.
    @OneToMany(mappedBy = "enterprise", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<Vehicle> vehicles = new HashSet<>();

    @OneToMany(mappedBy = "enterprise", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private Set<Driver> drivers = new HashSet<>();

}
