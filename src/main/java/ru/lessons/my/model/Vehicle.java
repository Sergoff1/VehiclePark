package ru.lessons.my.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate_number", unique = true, nullable = false)
    private String licensePlateNumber;

    @Column(name = "production_year", nullable = false)
    @Min(value = 1885, message = "Первый автомобиль выпустили в 1885")
    private int productionYear;

    @Column(nullable = false)
    @Min(value = 0, message = "Пробег не может быть отрицательным")
    private int mileage;

    @Column(nullable = false)
    private String color;

    @Column(name = "purchase_price", nullable = false)
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private int purchasePrice;

}
