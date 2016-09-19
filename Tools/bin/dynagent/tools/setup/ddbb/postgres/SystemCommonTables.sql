DROP TABLE IF EXISTS helpclasses;
DROP TABLE IF EXISTS helpproperties;
DROP TABLE IF EXISTS helpindividuals;

-- Información de ayuda sobre una clase del modelo.
CREATE TABLE helpclasses
(
  description text NOT NULL,
  "language" character varying(50) NOT NULL,
  name character varying(50) NOT NULL
)
WITH (
  OIDS=FALSE
);

-- Información de ayuda sobre una propiedad del modelo.
CREATE TABLE helpproperties
(
  description text NOT NULL,
  "language" character varying(50) NOT NULL,
  name character varying(50) NOT NULL
)
WITH (
  OIDS=FALSE
);

-- Información de ayuda sobre un individuo del modelo.
CREATE TABLE helpindividuals
(
  description text NOT NULL,
  "language" character varying(50) NOT NULL,
  class character varying(50) NOT NULL,
  name character varying(50) NOT NULL
)
WITH (
  OIDS=FALSE
);

-- Cambiamos el dueño de las tablas a dynagent.
ALTER TABLE helpclasses OWNER TO dynagent;
ALTER TABLE helpproperties OWNER TO dynagent;
ALTER TABLE helpindividuals OWNER TO dynagent;