INSERT INTO manager (password, username)
    VALUES('$2a$10$4NAMKtornsGOlov2ovTZguhfffWAnjk/DsEOs3y5yPJnHNEdmnUeq', 'manager1');
INSERT INTO manager (password, username)
    VALUES('$2a$10$4NAMKtornsGOlov2ovTZguhfffWAnjk/DsEOs3y5yPJnHNEdmnUeq', 'manager2');

INSERT INTO enterprise (city, name, time_zone)
    VALUES('Москва', 'Дорого и долго', 'UTC');
INSERT INTO enterprise (city, name, time_zone)
    VALUES('New York', 'Fast or slow', 'UTC');
INSERT INTO enterprise (city, name, time_zone)
    VALUES('Казань', 'Вихрь', 'UTC');
INSERT INTO enterprise (city, name, time_zone)
    VALUES('Moscow', 'Test', 'UTC');

INSERT INTO manager_enterprise (manager_id, enterprise_id)
    VALUES(1, 1);
INSERT INTO manager_enterprise (manager_id, enterprise_id)
    VALUES(1, 2);
INSERT INTO manager_enterprise (manager_id, enterprise_id)
    VALUES(2, 2);
INSERT INTO manager_enterprise (manager_id, enterprise_id)
    VALUES(2, 3);
INSERT INTO manager_enterprise (manager_id, enterprise_id)
    VALUES(1, 4);
INSERT INTO manager_enterprise (manager_id, enterprise_id)
    VALUES(2, 4);

INSERT INTO vehicle_model (brand_name, fuel_tank_capacity, load_capacity, model_name, seats_number, type)
    VALUES('Лада', 50, 1890, 'Веста', 5, 'PASSENGER');
INSERT INTO vehicle_model (brand_name, fuel_tank_capacity, load_capacity, model_name, seats_number, type)
    VALUES('Volvo', 150, 2890, 'B5LH', 28, 'BUS');
INSERT INTO vehicle_model (brand_name, fuel_tank_capacity, load_capacity, model_name, seats_number, type)
    VALUES('Kia', 48, 1895, 'Rio', 5, 'PASSENGER');

INSERT INTO driver (name, salary, enterprise_id)
    VALUES('Семён Горбунков', 50000.0, 1);
INSERT INTO driver (name, salary, enterprise_id)
    VALUES('Vin Diesel', 150000.0, 2);

INSERT INTO vehicle (license_plate_number, production_year, purchase_price, mileage, color, vehicle_model_id, active_driver_id, enterprise_id, purchase_date_time)
    VALUES('М123КА12', 2015, 1500000, 148756, 'Белый', 1, NULL, 1, '2024-05-19 17:34:00.000');
INSERT INTO vehicle (license_plate_number, production_year, purchase_price, mileage, color, vehicle_model_id, active_driver_id, enterprise_id, purchase_date_time)
    VALUES('К233КК12', 2009, 1000500, 389756, 'Чёрный', 2, NULL, 2, '2024-05-19 17:34:00.000');
INSERT INTO vehicle (license_plate_number, production_year, purchase_price, mileage, color, vehicle_model_id, active_driver_id, enterprise_id, purchase_date_time)
    VALUES('Р333АН47', 2022, 3000000, 22345, 'Серый', 3, 1, 1, '2024-05-19 17:34:00.000');
INSERT INTO vehicle (license_plate_number, production_year, purchase_price, mileage, color, vehicle_model_id, active_driver_id, enterprise_id, purchase_date_time)
    VALUES('М111АС777', 2019, 2780000, 45326, 'Синий', 3, 2, 2, '2024-05-19 17:34:00.000');

INSERT INTO driver_vehicle (vehicle_id, driver_id)
    VALUES(4, 2);
INSERT INTO driver_vehicle (vehicle_id, driver_id)
    VALUES(2, 2);
INSERT INTO driver_vehicle (vehicle_id, driver_id)
    VALUES(3, 1);