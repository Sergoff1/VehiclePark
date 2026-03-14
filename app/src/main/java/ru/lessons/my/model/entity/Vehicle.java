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
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "Vehicle.detail",
        attributeNodes = {
                @NamedAttributeNode("enterprise"),
                @NamedAttributeNode("drivers"),
                @NamedAttributeNode("model")
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_model_id", nullable = false)
    private VehicleModel model;

    @Column(name = "license_plate_number", unique = true, nullable = false)
    private String licensePlateNumber;

    @Column(name = "production_year", nullable = false)
    @Min(value = 1885, message = "Первый автомобиль выпустили в 1885")
    private int productionYear;

    @Column(name = "mileage", nullable = false)
    @Min(value = 0, message = "Пробег не может быть отрицательным")
    private int mileageKm;

    @Column(nullable = false)
    private String color;

    @Column(name = "purchase_price", nullable = false)
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private int purchasePriceRub;

    @Column(name = "purchase_date_time", nullable = false)
    private LocalDateTime purchaseDateTime;

    @ManyToOne
    @JoinColumn(name = "enterprise_id", nullable = false)
    private Enterprise enterprise;

    @OneToOne
    @JoinColumn(name = "active_driver_id", unique = true)
    private Driver activeDriver;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name="driver_vehicle",
            joinColumns=  @JoinColumn(name="vehicle_id", referencedColumnName="id"),
            inverseJoinColumns= @JoinColumn(name="driver_id", referencedColumnName="id") )
    private Set<Driver> drivers = new HashSet<>();

}
