DROP DATABASE IF EXISTS pokeplushies;

CREATE DATABASE pokeplushies;

USE pokeplushies;

CREATE TABLE clientes (
	cNIF char(9) PRIMARY KEY,
	cNombre varchar(25) NOT NULL,
    cApellido1 varchar(30) NOT NULL,
    cApellido2 varchar(30),
    cDireccion varchar(50) NOT NULL,
    iEstadoVIP tinyint NOT NULL
);

CREATE TABLE productos (
	iCodigoProducto int PRIMARY KEY AUTO_INCREMENT,
    cEspeciePokemon varchar(30) NOT NULL,
	cTipo varchar(40) NOT NULL,
    dAltura double NOT NULL,
    dPrecio decimal(4,2) NOT NULL,
    iStock int NOT NULL
);

CREATE TABLE carrito_compra (
	cNIF char(9) NOT NULL,
    iCodigoProducto int NOT NULL,
    iCantidad int NOT NULL,
    PRIMARY KEY (cNIF, iCodigoProducto),
    FOREIGN KEY (cNIF) REFERENCES clientes (cNIF) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (iCodigoProducto) REFERENCES productos (iCodigoProducto) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO clientes (cNIF, cNombre, cApellido1, cApellido2, cDireccion, iEstadoVIP) VALUES
	("43129313H", "Ángela", "Mena", "Rodriguez", "Calle One Piece", 1),
    ("31358431R", "Manuel", "Ramos", "Barbudo", "Perú", 0),
    ("31016040H", "Sergio", "Castillo", "Molina", "Campo de la mentira", 1),
    ("40130120U", "Yunaiber Josué", "Quintero", "Contreras", "Caracas, Venezuela", 0),
    ("94391203Y", "Julio", "Martinez", "Arjona", "Nose dime tu", 0),
    ("83913123U", "Raúl", "Enríquez", "Espejo", "Casa de raul", 1),
    ("95494134E", "Fernando", "Aparicio", "Salvador", "Apariciocasa", 0),
    ("85719843T", "Juan Carlos", "Sanchez", "Trapero", "juca calle", 1),
    ("57128312A", "Jose Enrique", "Ortega", "Ortega", "Calle mejorprofesor", 1),
    ("18313132F", "Manolo", "Mi", "Peluquero", "C/ Peluqueria", 0);

INSERT INTO productos (cEspeciePokemon, cTipo, dAltura, dPrecio, iStock) VALUES
	("Charmander", "Fuego", 0.20, 20.53, 35),
    ("Umbreon", "Siniestro", 0.23, 36.70, 284),
    ("Gengar", "Fantasma/Veneno", 0.15, 15, 537),
    ("Espeon", "Psiquico", 0.19, 42.69, 314),
    ("Charizard", "Fuego", 0.45, 69.10, 204),
    ("Eevee", "Normal", 0.09, 30.03, 104),
    ("Vaporeon", "Agua", 0.20, 53.03, 10),
    ("Tauros", "Normal", 0.30, 43.15, 400),
    ("Pikachu", "Electrico", 0.69, 70.30, 100),
    ("Squirtle", "Agua", 0.34, 40.87, 165);
    
INSERT INTO carrito_compra (cNIF, iCodigoProducto, iCantidad) VALUES
	("43129313H", 9, 1),
    ("83913123U", 3, 1),
    ("31016040H", 1, 1),
    ("43129313H", 6, 3),
    ("43129313H", 2, 1),
    ("40130120U", 7, 2),
    ("31358431R", 4, 1),
    ("18313132F", 10, 1),
    ("57128312A", 8, 2),
    ("94391203Y", 6, 1);
    
DELIMITER //
CREATE PROCEDURE insertarNuevoCliente(
	var_cNIF char(9),
	var_cNombre varchar(25),
    var_cApellido1 varchar(30),
    var_cApellido2 varchar(30),
    var_cDireccion varchar(50),
    var_iEstadoVIP tinyint
)
BEGIN
	INSERT INTO clientes (cNIF, cNombre, cApellido1, cApellido2, cDireccion, iEstadoVIP) VALUES 
                (var_cNIF, var_cNombre, var_cApellido1, var_cApellido2, var_cDireccion, var_iEstadoVIP);
END//

CREATE PROCEDURE darVIP(
	var_cNIF char(9)
)
BEGIN
	UPDATE clientes SET iEstadoVip = 1 WHERE cNIF = var_cNIF;
END//
