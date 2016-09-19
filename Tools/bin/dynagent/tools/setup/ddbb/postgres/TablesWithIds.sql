-- Creamos la tabla de clases
DROP TABLE IF EXISTS clase_origin;
CREATE TABLE clase_origin
(
  r_destination character varying(100),
  "tableId" serial NOT NULL,
  rdn character varying(100) NOT NULL,
  abstracta boolean NOT NULL,
  id integer,
  CONSTRAINT clase_origin_pkey PRIMARY KEY ("tableId"),
  CONSTRAINT "U_clase_origin_rdn" UNIQUE (rdn)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE clase_origin OWNER TO dynagent;
;

-- Creamos la tabla de DataProperties
DROP TABLE IF EXISTS propiedad_dato_origin;
CREATE TABLE propiedad_dato_origin
(
  cat integer,
  r_destination character varying(100),
  "tableId" serial NOT NULL,
  rdn character varying(100) NOT NULL,
  valuecls integer,
  id integer,
  CONSTRAINT propiedad_dato_origin_pkey PRIMARY KEY ("tableId"),
  CONSTRAINT "U_prop_dato_origin_rdn" UNIQUE (rdn)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE propiedad_dato_origin OWNER TO dynagent;

-- Creamos la tabla de ObjectProperties
DROP TABLE IF EXISTS propiedad_objeto_origin;
CREATE TABLE propiedad_objeto_origin
(
  cat integer,
  r_destination character varying(100),
  id_inversa integer,
  "tableId" serial NOT NULL,
  rdn character varying(100) NOT NULL,
  id integer,
  CONSTRAINT propiedad_objeto_origin_pkey PRIMARY KEY ("tableId"),
  CONSTRAINT "U_prop_obj_origin_rdn" UNIQUE (rdn)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE propiedad_objeto_origin OWNER TO dynagent;

-- Creamos la vista de propiedades
CREATE OR REPLACE VIEW v_propiedad_origin AS 
         SELECT propiedad_objeto_origin.r_destination, propiedad_objeto_origin."tableId", propiedad_objeto_origin.rdn, propiedad_objeto_origin.id, propiedad_objeto_origin.cat, NULL AS valuecls, propiedad_objeto_origin.id_inversa, 29 AS idto
           FROM propiedad_objeto_origin
UNION 
         SELECT pd.r_destination, pd."tableId", pd.rdn, pd.id, pd.cat, pd.valuecls, NULL AS id_inversa, 28 AS idto
           FROM propiedad_dato_origin pd;

-- Creamos la tabla de instances
DROP TABLE IF EXISTS instances_origin;
CREATE TABLE instances_origin
(
  idto numeric(18,0),
  ido numeric(18,0),
  property numeric(18,0),
  "value" character varying(50),
  valuecls numeric(18,0),
  qmin numeric(18,0),
  qmax numeric(18,0),
  "name" character varying(100),
  op character varying(50),
  virtual boolean NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE instances_origin OWNER TO dynagent;
