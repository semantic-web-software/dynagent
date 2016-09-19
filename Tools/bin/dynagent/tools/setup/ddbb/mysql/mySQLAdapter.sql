-- Creamos la tabla de clases
DROP TABLE IF EXISTS clase;
CREATE TABLE  clase (
  `tableId` int not null auto_increment,
  `rdn` varchar(100) not null,
  `id` int not null,
  `abstracta` boolean not null,
  `r_destination` varchar(100),
  PRIMARY KEY(`tableId`)
) ENGINE=InnoDB;

INSERT INTO clase (id, rdn, abstracta)
	SELECT DISTINCT c.idto, c.name, CASE WHEN max(a.accesstype)=512 THEN true ELSE false END AS abstract
		FROM clases c LEFT JOIN access a ON c.idto=a.idto
		WHERE c.idto > 0 AND c.idto<10000 AND NOT c.name like 'Task_%' AND NOT c.name like 'Axiom_%'
		GROUP BY c.idto;

-- Creamos la tabla de DataProperties
DROP TABLE IF EXISTS propiedad_dato;
CREATE TABLE propiedad_dato(
	`tableId` int not null auto_increment,
	`rdn` varchar(100) not null,
	`id` int not null,
	`cat` int not null,
	`valuecls` int,
	`r_destination` varchar(100),
	PRIMARY KEY(`tableId`)
) ENGINE=InnoDB;

INSERT INTO propiedad_dato (id, rdn, cat, valuecls)
	SELECT prop, name, cat, valuecls 
		FROM properties p 
		WHERE cat%2=0 AND prop>0;
	
-- Creamos la tabla de ObjectProperties
DROP TABLE IF EXISTS propiedad_objeto;
CREATE TABLE propiedad_objeto(
	`tableId` int not null auto_increment,
	`rdn` varchar(100) not null,
	`id` int not null,
	`cat` int not null,
	`id_inversa` int,
	`r_destination` varchar(100),
	PRIMARY KEY(`tableId`)
) ENGINE=InnoDB;

INSERT INTO propiedad_objeto (id, rdn, cat, id_inversa)
	SELECT prop, name, cat, inv
		FROM properties p WHERE cat%3=0 AND prop>0;
		
-- Creamos la vista de propiedades
CREATE OR REPLACE VIEW v_propiedad AS 
         SELECT propiedad_objeto.r_destination, propiedad_objeto.`tableId`, propiedad_objeto.rdn, propiedad_objeto.id, propiedad_objeto.cat, NULL AS valuecls, propiedad_objeto.id_inversa, 29 AS idto
           FROM propiedad_objeto
UNION 
         SELECT pd.r_destination, pd.`tableId`, pd.rdn, pd.id, pd.cat, pd.valuecls, NULL AS id_inversa, 28 AS idto
           FROM propiedad_dato pd;
           
-- Adaptación de datos: Eliminación de individuos de modelo obsoletos.
DELETE FROM o_datos_atrib WHERE val_texto IN ('Alta', 'Media', 'Baja') AND id_to IN (
  SELECT idto FROM clases WHERE name='PRIORIDAD');