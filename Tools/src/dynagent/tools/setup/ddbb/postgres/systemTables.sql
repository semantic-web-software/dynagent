-- Tiramos todas las tablas.
DROP TABLE IF EXISTS clase;
DROP TABLE IF EXISTS instances;
DROP TABLE IF EXISTS license;
DROP TABLE IF EXISTS locksid;
DROP TABLE IF EXISTS log_error;
DROP TABLE IF EXISTS nomodifydb;
DROP TABLE IF EXISTS propiedad_dato;
DROP TABLE IF EXISTS propiedad_objeto;
DROP TABLE IF EXISTS "propiedad_dato#clase";
DROP TABLE IF EXISTS "propiedad_objeto#clase";
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS t_herencias;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS operationsid;
DROP TABLE IF EXISTS deleted_objects;
DROP TABLE IF EXISTS replica_msg;
DROP TABLE IF EXISTS replica_send_get;
DROP TABLE IF EXISTS replica_send_post;
DROP TABLE IF EXISTS replica_destination_get;
DROP TABLE IF EXISTS replica_destination_post;
DROP TABLE IF EXISTS replica_source_get;
DROP TABLE IF EXISTS replica_source_post;

-- Tabla donde se almacenan las clases del modelo.
CREATE TABLE clase
(
  r_destination character varying(100),
  "tableId" serial NOT NULL,
  rdn character varying(100) NOT NULL,
  abstracta boolean NOT NULL,
  id integer,
  CONSTRAINT clase_pkey PRIMARY KEY ("tableId"),
  CONSTRAINT "U_clase_rdn" UNIQUE (rdn)
)
WITH (
  OIDS=FALSE
);




-- Tabla donde se almacena la configuración de cada clase del modelo.
CREATE TABLE instances
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

-- Tabla donde se almacena la licencia de uso para esta base de datos.
CREATE TABLE license
(
  users character varying(100),
  fechamax character varying(100),
  "type" character varying(100)
)
WITH (
  OIDS=FALSE
);

-- Tabla donde se almacenan los bloqueos de individuos.
CREATE TABLE locksid
(
  id character varying(110) NOT NULL,
  "login" character varying(100),
  CONSTRAINT pk_locksid PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

-- Tabla donde se almacenan los errores graves que se dan durante el uso de la aplicación
CREATE TABLE log_error
(
  usuario character varying(50) NOT NULL,
  fecha character varying(50) NOT NULL,
  error text,
  debug text,
  descripcion text
)
WITH (
  OIDS=FALSE
);

-- Tabla donde se indican los identificadores de las clases que no se pueden modificar debido a la réplica.
CREATE TABLE nomodifydb
(
  id_to integer NOT NULL
)
WITH (
  OIDS=FALSE
);

-- Tabla donde se almacenan las DataProperties.
CREATE TABLE propiedad_dato
(
  cat integer,
  r_destination character varying(100),
  "tableId" serial NOT NULL,
  rdn character varying(100) NOT NULL,
  valuecls integer,
  id integer,
  
  CONSTRAINT propiedad_dato_pkey PRIMARY KEY ("tableId"),
  CONSTRAINT "U_prop_dato_rdn" UNIQUE (rdn)
)
WITH (
  OIDS=FALSE
);

-- Tabla donde se almacenan las ObjectProperties.
CREATE TABLE propiedad_objeto
(
  cat integer,
  r_destination character varying(100),
  id_inversa integer,
  "tableId" serial NOT NULL,
  rdn character varying(100) NOT NULL,
  id integer,
  CONSTRAINT propiedad_objeto_pkey PRIMARY KEY ("tableId"),
  CONSTRAINT "U_prop_obj_rdn" UNIQUE (rdn)
)
WITH (
  OIDS=FALSE
);

CREATE TABLE "propiedad_dato#clase"
(
  "tableId" serial NOT NULL,
  "claseId" integer NOT NULL,
  "propiedad_datoId" integer NOT NULL,
  CONSTRAINT "propiedad_dato#clase_pkey" PRIMARY KEY ("tableId")
)
WITH (
  OIDS=FALSE
);

CREATE TABLE "propiedad_objeto#clase"
(
  "propiedad_objetoId" integer NOT NULL,
  "tableId" serial NOT NULL,
  "claseId" integer NOT NULL,
  CONSTRAINT "propiedad_objeto#clase_pkey" PRIMARY KEY ("tableId")
)
WITH (
  OIDS=FALSE
);


-- Tabla que almacena la información sobre los usuarios identificados y usando la aplicación.
CREATE TABLE sessions
(
  id character varying(150) NOT NULL,
  idclient character varying(150) NOT NULL,
  CONSTRAINT pk_sessions PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

-- Tabla donde se almacenan las herencias entre clases.
CREATE TABLE t_herencias
(
  id_to integer,
  id_to_padre integer
)
WITH (
  OIDS=FALSE
);

CREATE TABLE reservation
(
  ido integer NOT NULL,
  prop integer NOT NULL,
  reservation double precision NOT NULL,
  "user" character varying NOT NULL,
  idsession integer NOT NULL
)
WITH (
  OIDS=FALSE
);

CREATE TABLE operationsid
(
  msguid character varying(100) NOT NULL,
  CONSTRAINT pk_operationsid PRIMARY KEY (msguid)
)
WITH (
  OIDS=FALSE
);

-- Cambiamos el dueño de las tablas a dynagent.
ALTER TABLE clase OWNER TO dynagent;
ALTER TABLE "propiedad_dato#clase" OWNER TO dynagent;
ALTER TABLE "propiedad_objeto#clase" OWNER TO dynagent;
ALTER TABLE instances OWNER TO dynagent;
ALTER TABLE license OWNER TO dynagent;
ALTER TABLE locksid OWNER TO dynagent;
ALTER TABLE log_error OWNER TO dynagent;
ALTER TABLE nomodifydb OWNER TO dynagent;
ALTER TABLE propiedad_dato OWNER TO dynagent;
ALTER TABLE propiedad_objeto OWNER TO dynagent;
ALTER TABLE sessions OWNER TO dynagent;
ALTER TABLE t_herencias OWNER TO dynagent;
ALTER TABLE reservation OWNER TO dynagent;
ALTER TABLE operationsid OWNER TO dynagent;

-- Creamos la vista que auna las propiedades.
DROP VIEW IF EXISTS v_propiedad;

CREATE OR REPLACE VIEW v_propiedad AS 
         SELECT propiedad_objeto.r_destination, propiedad_objeto."tableId", propiedad_objeto.rdn, propiedad_objeto.id, propiedad_objeto.cat, NULL::unknown AS valuecls, propiedad_objeto.id_inversa, 29 AS idto
           FROM propiedad_objeto
UNION 
         SELECT propiedad_dato.r_destination, propiedad_dato."tableId", propiedad_dato.rdn, propiedad_dato.id, propiedad_dato.cat, propiedad_dato.valuecls, NULL::unknown AS id_inversa, 28 AS idto
           FROM propiedad_dato;

ALTER TABLE v_propiedad OWNER TO dynagent;

-- Creamos la vista que auna las clases con sus propiedades.
DROP VIEW IF EXISTS "v_propiedad#clase";

CREATE OR REPLACE VIEW "v_propiedad#clase" AS 
         SELECT 29 AS "propiedadIdto", 26 AS "claseIdto", "propiedad_objeto#clase"."propiedad_objetoId" AS "propiedadId", "propiedad_objeto#clase"."claseId"
           FROM "propiedad_objeto#clase"
UNION 
         SELECT 28 AS "propiedadIdto", 26 AS "claseIdto", "propiedad_dato#clase"."propiedad_datoId" AS "propiedadId", "propiedad_dato#clase"."claseId"
           FROM "propiedad_dato#clase";

ALTER TABLE "v_propiedad#clase" OWNER TO dynagent;

-- Tabla que almacena la información sobre las queries predefinidas.
CREATE TABLE s_query
(
  "name" character varying(50) NOT NULL,
  query text NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE s_query OWNER TO dynagent;

-- Table: deleted_objects

CREATE TABLE deleted_objects
(
  "tableId" integer NOT NULL,
  idto integer NOT NULL,
  rdn character varying(100),
  date timestamp without time zone,
  autonum serial NOT NULL,
  CONSTRAINT deleted_objects_pkey PRIMARY KEY (autonum)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE deleted_objects OWNER TO dynagent;

-- Table: replica_msg

CREATE TABLE replica_msg
(
  autonum serial NOT NULL,
  msguid character varying(100) NOT NULL,
  "content" text NOT NULL,
  fecha date NOT NULL,
  source character varying(100),
  destination character varying(1000),
  debug text,
  CONSTRAINT replica_msg_pkey PRIMARY KEY (autonum)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE replica_msg OWNER TO dynagent;


-- Table: replica_send_get

CREATE TABLE replica_send_get
(
  msguid character varying(100) NOT NULL,
  autonum_replica_msg integer NOT NULL,
  fecha date NOT NULL,
  source character varying(100) NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE replica_send_get OWNER TO dynagent;

-- Table: replica_send_post

CREATE TABLE replica_send_post
(
  msguid character varying(100) NOT NULL,
  autonum_replica_msg integer NOT NULL,
  fecha date NOT NULL,
  source character varying(100) NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE replica_send_post OWNER TO dynagent;

-- Table: replica_node_get

CREATE TABLE replica_destination_get
(
  destination character varying(100) NOT NULL,
  ip character varying(100) NOT NULL,
  business integer NOT NULL,
  port integer NOT NULL,
  portjboss integer NOT NULL,
  ftpUser character varying(100),
  ftpPass character varying(100),
  ftpPort integer,
  ftpUrl character varying(100),
  ftpDestinationPath character varying(100),
  xslpath character varying(100),
  "full" boolean,
  CONSTRAINT replica_destination_get_pkey PRIMARY KEY (destination)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE replica_destination_get OWNER TO dynagent;

-- Table: replica_node_post

CREATE TABLE replica_destination_post
(
  destination character varying(100) NOT NULL,
  ip character varying(100) NOT NULL,
  business integer NOT NULL,
  port integer NOT NULL,
  portjboss integer NOT NULL,
  ftpUser character varying(100),
  ftpPass character varying(100),
  ftpPort integer,
  ftpUrl character varying(100),
  ftpDestinationPath character varying(100),
  "full" boolean,
  CONSTRAINT replica_destination_post_pkey PRIMARY KEY (destination)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE replica_destination_post OWNER TO dynagent;


-- Table: replica_node_get

CREATE TABLE replica_source_get
(
  source character varying(100) NOT NULL,
  ip character varying(100) NOT NULL,
  business integer NOT NULL,
  port integer NOT NULL,
  portjboss integer NOT NULL,
  "user" character varying(100) NOT NULL,
  password character varying(100) NOT NULL,
  ftpUser character varying(100),
  ftpPass character varying(100),
  ftpPort integer,
  ftpUrl character varying(100),
  ftpSourcePath character varying(100),
  xslpath character varying(100),
  CONSTRAINT replica_source_get_pkey PRIMARY KEY (source)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE replica_source_get OWNER TO dynagent;

-- Table: replica_node_post

CREATE TABLE replica_source_post
(
  source character varying(100) NOT NULL,
  ip character varying(100) NOT NULL,
  business integer NOT NULL,
  port integer NOT NULL,
  portjboss integer NOT NULL,
  "user" character varying(100) NOT NULL,
  password character varying(100) NOT NULL,
  ftpUser character varying(100),
  ftpPass character varying(100),
  ftpPort integer,
  ftpUrl character varying(100),
  ftpSourcePath character varying(100),
  xslpath character varying(100),
  CONSTRAINT replica_source_post_pkey PRIMARY KEY (source)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE replica_source_post OWNER TO dynagent;



-- pgcrypto.sql

/* $PostgreSQL: pgsql/contrib/pgcrypto/pgcrypto.sql.in,v 1.15 2007/11/13 04:24:28 momjian Exp $ */

-- Adjust this setting to control where the objects get created.
SET search_path = public;

CREATE OR REPLACE FUNCTION digest(text, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_digest'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION digest(bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_digest'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION hmac(text, text, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_hmac'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION hmac(bytea, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_hmac'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION crypt(text, text)
RETURNS text
AS '$libdir/pgcrypto', 'pg_crypt'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION gen_salt(text)
RETURNS text
AS '$libdir/pgcrypto', 'pg_gen_salt'
LANGUAGE c VOLATILE STRICT;

CREATE OR REPLACE FUNCTION gen_salt(text, int4)
RETURNS text
AS '$libdir/pgcrypto', 'pg_gen_salt_rounds'
LANGUAGE c VOLATILE STRICT;

CREATE OR REPLACE FUNCTION encrypt(bytea, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_encrypt'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION decrypt(bytea, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_decrypt'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION encrypt_iv(bytea, bytea, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_encrypt_iv'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION decrypt_iv(bytea, bytea, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_decrypt_iv'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION gen_random_bytes(int4)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_random_bytes'
LANGUAGE c VOLATILE STRICT;

--
-- pgp_sym_encrypt(data, key)
--
CREATE OR REPLACE FUNCTION pgp_sym_encrypt(text, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_sym_encrypt_text'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION pgp_sym_encrypt_bytea(bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_sym_encrypt_bytea'
LANGUAGE c STRICT;

--
-- pgp_sym_encrypt(data, key, args)
--
CREATE OR REPLACE FUNCTION pgp_sym_encrypt(text, text, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_sym_encrypt_text'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION pgp_sym_encrypt_bytea(bytea, text, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_sym_encrypt_bytea'
LANGUAGE c STRICT;

--
-- pgp_sym_decrypt(data, key)
--
CREATE OR REPLACE FUNCTION pgp_sym_decrypt(bytea, text)
RETURNS text
AS '$libdir/pgcrypto', 'pgp_sym_decrypt_text'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION pgp_sym_decrypt_bytea(bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_sym_decrypt_bytea'
LANGUAGE c IMMUTABLE STRICT;

--
-- pgp_sym_decrypt(data, key, args)
--
CREATE OR REPLACE FUNCTION pgp_sym_decrypt(bytea, text, text)
RETURNS text
AS '$libdir/pgcrypto', 'pgp_sym_decrypt_text'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION pgp_sym_decrypt_bytea(bytea, text, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_sym_decrypt_bytea'
LANGUAGE c IMMUTABLE STRICT;

--
-- pgp_pub_encrypt(data, key)
--
CREATE OR REPLACE FUNCTION pgp_pub_encrypt(text, bytea)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_pub_encrypt_text'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION pgp_pub_encrypt_bytea(bytea, bytea)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_pub_encrypt_bytea'
LANGUAGE c STRICT;

--
-- pgp_pub_encrypt(data, key, args)
--
CREATE OR REPLACE FUNCTION pgp_pub_encrypt(text, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_pub_encrypt_text'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION pgp_pub_encrypt_bytea(bytea, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_pub_encrypt_bytea'
LANGUAGE c STRICT;

--
-- pgp_pub_decrypt(data, key)
--
CREATE OR REPLACE FUNCTION pgp_pub_decrypt(bytea, bytea)
RETURNS text
AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION pgp_pub_decrypt_bytea(bytea, bytea)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
LANGUAGE c IMMUTABLE STRICT;

--
-- pgp_pub_decrypt(data, key, psw)
--
CREATE OR REPLACE FUNCTION pgp_pub_decrypt(bytea, bytea, text)
RETURNS text
AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
LANGUAGE c IMMUTABLE STRICT;

--
-- pgp_pub_decrypt(data, key, psw, arg)
--
CREATE OR REPLACE FUNCTION pgp_pub_decrypt(bytea, bytea, text, text)
RETURNS text
AS '$libdir/pgcrypto', 'pgp_pub_decrypt_text'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION pgp_pub_decrypt_bytea(bytea, bytea, text, text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pgp_pub_decrypt_bytea'
LANGUAGE c IMMUTABLE STRICT;

--
-- PGP key ID
--
CREATE OR REPLACE FUNCTION pgp_key_id(bytea)
RETURNS text
AS '$libdir/pgcrypto', 'pgp_key_id_w'
LANGUAGE c IMMUTABLE STRICT;

--
-- pgp armor
--
CREATE OR REPLACE FUNCTION armor(bytea)
RETURNS text
AS '$libdir/pgcrypto', 'pg_armor'
LANGUAGE c IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION dearmor(text)
RETURNS bytea
AS '$libdir/pgcrypto', 'pg_dearmor'
LANGUAGE c IMMUTABLE STRICT;


-- dblink.sql

/* $PostgreSQL: pgsql/contrib/dblink/dblink.sql.in,v 1.19 2009/08/05 16:11:07 joe Exp $ */

-- Adjust this setting to control where the objects get created.
SET search_path = public;

-- dblink_connect now restricts non-superusers to password
-- authenticated connections
CREATE OR REPLACE FUNCTION dblink_connect (text)
RETURNS text
AS '$libdir/dblink','dblink_connect'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_connect (text, text)
RETURNS text
AS '$libdir/dblink','dblink_connect'
LANGUAGE c STRICT;

-- dblink_connect_u allows non-superusers to use
-- non-password authenticated connections, but initially
-- privileges are revoked from public
CREATE OR REPLACE FUNCTION dblink_connect_u (text)
RETURNS text
AS '$libdir/dblink','dblink_connect'
LANGUAGE c STRICT SECURITY DEFINER;

CREATE OR REPLACE FUNCTION dblink_connect_u (text, text)
RETURNS text
AS '$libdir/dblink','dblink_connect'
LANGUAGE c STRICT SECURITY DEFINER;

REVOKE ALL ON FUNCTION dblink_connect_u (text) FROM public;
REVOKE ALL ON FUNCTION dblink_connect_u (text, text) FROM public;

CREATE OR REPLACE FUNCTION dblink_disconnect ()
RETURNS text
AS '$libdir/dblink','dblink_disconnect'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_disconnect (text)
RETURNS text
AS '$libdir/dblink','dblink_disconnect'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_open (text, text)
RETURNS text
AS '$libdir/dblink','dblink_open'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_open (text, text, boolean)
RETURNS text
AS '$libdir/dblink','dblink_open'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_open (text, text, text)
RETURNS text
AS '$libdir/dblink','dblink_open'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_open (text, text, text, boolean)
RETURNS text
AS '$libdir/dblink','dblink_open'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_fetch (text, int)
RETURNS setof record
AS '$libdir/dblink','dblink_fetch'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_fetch (text, int, boolean)
RETURNS setof record
AS '$libdir/dblink','dblink_fetch'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_fetch (text, text, int)
RETURNS setof record
AS '$libdir/dblink','dblink_fetch'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_fetch (text, text, int, boolean)
RETURNS setof record
AS '$libdir/dblink','dblink_fetch'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_close (text)
RETURNS text
AS '$libdir/dblink','dblink_close'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_close (text, boolean)
RETURNS text
AS '$libdir/dblink','dblink_close'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_close (text, text)
RETURNS text
AS '$libdir/dblink','dblink_close'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_close (text, text, boolean)
RETURNS text
AS '$libdir/dblink','dblink_close'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink (text, text)
RETURNS setof record
AS '$libdir/dblink','dblink_record'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink (text, text, boolean)
RETURNS setof record
AS '$libdir/dblink','dblink_record'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink (text)
RETURNS setof record
AS '$libdir/dblink','dblink_record'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink (text, boolean)
RETURNS setof record
AS '$libdir/dblink','dblink_record'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_exec (text, text)
RETURNS text
AS '$libdir/dblink','dblink_exec'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_exec (text, text, boolean)
RETURNS text
AS '$libdir/dblink','dblink_exec'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_exec (text)
RETURNS text
AS '$libdir/dblink','dblink_exec'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_exec (text,boolean)
RETURNS text
AS '$libdir/dblink','dblink_exec'
LANGUAGE c STRICT;

CREATE TYPE dblink_pkey_results AS (position int, colname text);

CREATE OR REPLACE FUNCTION dblink_get_pkey (text)
RETURNS setof dblink_pkey_results
AS '$libdir/dblink','dblink_get_pkey'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_build_sql_insert (text, int2vector, int, _text, _text)
RETURNS text
AS '$libdir/dblink','dblink_build_sql_insert'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_build_sql_delete (text, int2vector, int, _text)
RETURNS text
AS '$libdir/dblink','dblink_build_sql_delete'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_build_sql_update (text, int2vector, int, _text, _text)
RETURNS text
AS '$libdir/dblink','dblink_build_sql_update'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_current_query ()
RETURNS text
AS '$libdir/dblink','dblink_current_query'
LANGUAGE c;

CREATE OR REPLACE FUNCTION dblink_send_query(text, text)
RETURNS int4
AS '$libdir/dblink', 'dblink_send_query'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_is_busy(text)
RETURNS int4
AS '$libdir/dblink', 'dblink_is_busy'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_get_result(text)
RETURNS SETOF record
AS '$libdir/dblink', 'dblink_get_result'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_get_result(text, bool)
RETURNS SETOF record
AS '$libdir/dblink', 'dblink_get_result'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_get_connections()
RETURNS text[]
AS '$libdir/dblink', 'dblink_get_connections'
LANGUAGE c;

CREATE OR REPLACE FUNCTION dblink_cancel_query(text)
RETURNS text
AS '$libdir/dblink', 'dblink_cancel_query'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_error_message(text)
RETURNS text
AS '$libdir/dblink', 'dblink_error_message'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_get_notify(
    OUT notify_name TEXT,
    OUT be_pid INT4,
    OUT extra TEXT
) 
RETURNS setof record
AS '$libdir/dblink', 'dblink_get_notify'
LANGUAGE c STRICT;

CREATE OR REPLACE FUNCTION dblink_get_notify(
    conname TEXT,
    OUT notify_name TEXT,
    OUT be_pid INT4,
    OUT extra TEXT
) 
RETURNS setof record
AS '$libdir/dblink', 'dblink_get_notify'
LANGUAGE c STRICT;


CREATE OR REPLACE FUNCTION isnumeric(text) RETURNS boolean AS 
'SELECT $1 ~ ''^[0-9]+$''' LANGUAGE 'sql';




CREATE OR REPLACE FUNCTION restore_from(iprestore character varying,
										ipget character varying,
										ippost  character varying,
										dbportrestore integer,
										jbossporttienda integer, 
										jbossportcentral integer, 
										portftp integer,
										userftp character varying,
										passftp character varying,
										pathftp character varying,
										connectbusiness integer,
										businesscentral integer,
										userdb character varying, passdb character varying, 
										delegation character varying, warehouse character varying, installationNumber integer, franquicia boolean, solo_mi_destino boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	strColumns character varying;
	r RECORD;
	tables RECORD;
	sequences RECORD;
	sqlGetText character varying;
	sqlSequence character varying;
	firstTable character varying;
	secondTable character varying;
	firstTableRange character varying;
	secondTableRange character varying;
	delegationTableId integer;
	delegationCentralRdn character varying;
	warehouseTableId integer;
	miEmpresaTableId integer;
	sequenceValue bigint;
	businesslocal integer;
	maxautonumtmp integer;
	strDate character varying;
BEGIN

SET CONSTRAINTS ALL DEFERRED; 

	businesslocal=1;
	EXECUTE 'SELECT * FROM dblink_connect(''host='||iprestore||'
                                  port='||dbportrestore||'
                                  user='||userdb||'
                                  password='||passdb||'
                                  dbname=dyna'||connectbusiness||'
                                  connect_timeout=10'')';
    -- Abro transaccion remota para que no cambie datos durante la sesion, que podria provocar traer datos incompletos	
		
	PERFORM * FROM dblink('UPDATE mi_empresa set r_destination=null') as (rows varchar);
	PERFORM * FROM dblink('UPDATE "índice" set r_destination=null') as (rows varchar);
	
	PERFORM * FROM dblink('UPDATE "mensaje" as mm set r_destination=dd.rdn from "aplicación" as ap, mi_empresa as me,"delegación" as dd where (leido=true or mm.r_destination is null) and ap.mi_empresa=me."tableId" and dd."tableId"=me.delegacion_central') as (rows varchar);
	
	PERFORM * FROM dblink('BEGIN TRANSACTION; SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;') as (rows varchar);
	
	-- TODO existe alguna tabla asociacion con '~' por ser demasiado larga, habria que mapearlas a lo que significa, y tirar de esa semantica
	FOR tables IN SELECT table_name FROM information_schema.tables where table_schema='public' and table_type='BASE TABLE' and table_name not like '%~%'
	LOOP
		--RAISE NOTICE 'table %', tables.table_name;
		strColumns='';
		strDate='';

		FOR r IN EXECUTE 'SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ''' || tables.table_name || ''''
			LOOP
			--RAISE NOTICE 'i want to print % and %', r.column_name, r.data_type;
			IF strColumns<>'' THEN
				strColumns=strColumns || ', ';
			END IF;
			strColumns=strColumns || '"' || r.column_name || '" ' || r.data_type;
			
			IF r.column_name='fecha' AND tables.table_name<>'aplicación' THEN
				strDate='(fecha is null OR fecha>EXTRACT(EPOCH FROM now())-5184000)';--Le restamos dos mes a la fecha actual
			END IF;
	    END LOOP;
		
		sqlGetText='';
		IF position('#' in tables.table_name)<>0 THEN
			IF tables.table_name<>'módulo_negocio#propiedades' AND tables.table_name<>'módulo_negocio#clases' AND position('estadistica' in tables.table_name)<=0 THEN
				firstTable=split_part(tables.table_name,'#',1);
				secondTable=split_part(tables.table_name,'#',2);
				firstTableRange=firstTable||'Id';
				secondTableRange=secondTable||'Id';
				IF firstTable=secondTable THEN
					secondTableRange=secondTableRange||'Rango';
				END IF;
				sqlGetText='SELECT * FROM dblink(''SELECT "'|| tables.table_name ||'".* FROM "'|| tables.table_name ||'","'|| firstTable ||'" as F,"'|| secondTable ||'" as S';
				IF solo_mi_destino THEN
					sqlGetText=sqlGetText || ' WHERE ("'|| tables.table_name ||'"."'|| firstTableRange ||'"=F."tableId" AND (position(''''' || delegation || ''''' in F.r_destination)<>0)) AND ("'|| tables.table_name ||'"."'|| secondTableRange ||'"=S."tableId" AND (position(''''' || delegation || ''''' in S.r_destination)<>0))'')';	
				ELSE
					IF franquicia=false THEN
						sqlGetText=sqlGetText || ' WHERE ("'|| tables.table_name ||'"."'|| firstTableRange ||'"=F."tableId" AND (F.r_destination=''''*'''' OR F.r_destination=''''#'''' OR F.r_destination is NULL OR position(''''' || delegation || ''''' in F.r_destination)<>0)) AND ("'|| tables.table_name ||'"."'|| secondTableRange ||'"=S."tableId" AND (S.r_destination=''''*'''' OR S.r_destination=''''#'''' OR S.r_destination is NULL OR position(''''' || delegation || ''''' in S.r_destination)<>0))'')';
					ELSE
						sqlGetText=sqlGetText || ' WHERE ("'|| tables.table_name ||'"."'|| firstTableRange ||'"=F."tableId" AND (F.r_destination=''''*'''' OR F.r_destination is NULL OR position(''''' || delegation || ''''' in F.r_destination)<>0)) AND ("'|| tables.table_name ||'"."'|| secondTableRange ||'"=S."tableId" AND (S.r_destination=''''*'''' OR S.r_destination is NULL OR position(''''' || delegation || ''''' in S.r_destination)<>0))'')';
					END IF;
				END IF;
				-- RAISE NOTICE 'sqlGetText association table %', sqlGetText;
			END IF;	
		ELSIF tables.table_name<>'deleted_objects' AND tables.table_name<>'log_error' AND position('replica_' in tables.table_name)<>1 AND position('rotacion' in tables.table_name)<=0 THEN
			IF position('r_destination' in strColumns)<>0
			THEN
				IF strDate<>'' THEN
					strDate='AND '||strDate;	
				END IF;
				sqlGetText='SELECT * FROM dblink(''SELECT * FROM "'|| tables.table_name ||'"';
				IF solo_mi_destino THEN
					sqlGetText=sqlGetText || ' WHERE (position(''''' || delegation || ''''' in r_destination)<>0) '|| strDate ||''')';
				ELSE
					IF franquicia=false THEN
						sqlGetText=sqlGetText || ' WHERE (r_destination=''''*'''' OR r_destination=''''#'''' OR r_destination is NULL OR position(''''' || delegation || ''''' in r_destination)<>0) '|| strDate ||''')';
					ELSE
						sqlGetText=sqlGetText || ' WHERE (r_destination=''''*'''' OR r_destination is NULL OR position(''''' || delegation || ''''' in r_destination)<>0) '|| strDate ||''')';
					END IF;
				END IF;
			ELSE
				IF strDate<>'' THEN
					sqlGetText='SELECT * FROM dblink(''SELECT * FROM "'|| tables.table_name ||'"'' WHERE '|| strDate ||')';
				ELSE
					sqlGetText='SELECT * FROM dblink(''SELECT * FROM "'|| tables.table_name ||'"'')';
				END IF;
				
			END IF;
		END IF;
		
		IF sqlGetText<>'' THEN
			EXECUTE 'DELETE FROM "'|| tables.table_name ||'"';
			EXECUTE 'INSERT INTO "'|| tables.table_name ||'" '|| sqlGetText ||' AS remotetable (' || strColumns || ')';
		END IF;
		
	END LOOP;
	-- las tablas asociacion no saben si el rango cumple las fechas, aqui parcheamos al menos problema en ticket
	DELETE FROM "ticket_venta#ticket_venta" where "ticket_ventaId" not in (select "tableId" from ticket_venta);
	DELETE FROM "ticket_venta#ticket_venta" where "ticket_ventaIdRango" not in (select "tableId" from ticket_venta);
	DELETE FROM desglose_iva as di where "ticket_ventaId" is not null and "ticket_ventaId" not in(select "tableId" from ticket_venta);
	
	DELETE FROM cobro_anticipo as e where "ticket_ventaId" is not null and "ticket_ventaId" not in(select "tableId" from ticket_venta);
	DELETE FROM cobro_anticipo as e where "clienteCLIENTE_VARIOS" is not null and "clienteCLIENTE_VARIOS" not in(select "tableId" from cliente_varios);
	UPDATE distribuidor set "agente_comercialAGENTE_COMERCIAL_EXTERNO"=null where "agente_comercialAGENTE_COMERCIAL_EXTERNO" not in(select "tableId" from agente_comercial_externo);
	
	DELETE FROM "crédito_tarjeta" as c where "documento_activaciónTICKET_VENTA" is not null and "documento_activaciónTICKET_VENTA" not in(select "tableId" from ticket_venta);
	DELETE from "imagen" where "géneroId" is not null and "géneroId" not in (select "tableId" from "género");
	
	DELETE FROM "línea_artículos_materia" as e where "ticket_ventaId" is not null and "ticket_ventaId" not in(select "tableId" from ticket_venta);
	DELETE FROM "línea_artículos_financiera" as e where "ticket_ventaId" is not null and "ticket_ventaId" not in(select "tableId" from ticket_venta) or 
															tarjeta is not null and tarjeta not in(select "tableId" from "crédito_tarjeta");
	
	DELETE FROM "línea_artículos_materia" as e where "producto" is not null and "producto" not in(select "tableId" from "género");		
	UPDATE "línea_artículos_materia" as e set "albarán-factura_clienteId" = null where "albarán-factura_clienteId" not in(select "tableId" from "albarán-factura_cliente");
	UPDATE "línea_artículos_materia" as e set "albarán_clienteId" = null where "albarán_clienteId" not in(select "tableId" from "albarán_cliente");
	UPDATE "línea_artículos_materia" as e set "factura_a_clienteId" = null where "factura_a_clienteId" not in(select "tableId" from "factura_a_cliente");
	
	DELETE FROM "línea_materia" as e where "producto" is not null and "producto" not in(select "tableId" from "género");		
	DELETE FROM "línea_materia" as e where "traspaso_almacenesId" is not null and "traspaso_almacenesId" not in(select "tableId" from traspaso_almacenes);
	DELETE FROM "línea_materia" as e where "pedido_traspaso_almacenesId" is not null and "pedido_traspaso_almacenesId" not in(select "tableId" from pedido_traspaso_almacenes);
	UPDATE "línea_artículos_materia" set "comisiónCOMISIÓN_PORCENTUAL"=null where "comisiónCOMISIÓN_PORCENTUAL" is not null and "comisiónCOMISIÓN_PORCENTUAL" not in (select "tableId" from "comisión_porcentual");
	UPDATE distribuidor set "agente_comercialAGENTE_COMERCIAL_FIJO"=null;
	UPDATE distribuidor set "agente_comercialAGENTE_COMERCIAL_EXTERNO"=null;
	UPDATE distribuidor set "agente_comercialDISTRIBUIDOR"=null;
	UPDATE distribuidor set "cuenta_bancaria"=null;
	DELETE FROM "distribuidor#cuenta_contable";
	DELETE from sessions;
	
	DELETE FROM "a_la_entrega";
	DELETE FROM "día_pago";
	DELETE FROM "fecha_pago";
	DELETE FROM "pago_adelantado";
	
	FOR sequences IN SELECT sequence_name FROM information_schema.sequences where sequence_schema='public'
	LOOP
	    --RAISE NOTICE 'sequence %', sequences.sequence_name;
		sqlSequence='SELECT last_value FROM dblink(''SELECT last_value FROM "'|| sequences.sequence_name ||'"'')';
		EXECUTE sqlSequence || 'AS remotetable (last_value bigint)' INTO sequenceValue;
		EXECUTE 'ALTER SEQUENCE "'|| sequences.sequence_name ||'" RESTART '|| (sequenceValue+1);
	END LOOP;


	--RAISE NOTICE 'Actualizando replica_send';
	INSERT INTO replica_send_get(autonum_replica_msg,source,msguid,fecha) 
			SELECT maxautonum,central,msguid,current_date
			FROM dblink('SELECT autonum,del.rdn as central,msguid 
						FROM replica_msg,"aplicación" as ap inner join "delegación" as del on(ap."delegación"=del."tableId") 
						WHERE autonum=(select max(autonum) FROM replica_msg)')  AS remotetable (maxautonum integer,central character varying,msguid character varying);
	
	SELECT central FROM dblink('SELECT del.rdn as central FROM "aplicación" as ap inner join "delegación" as del on(ap."delegación"=del."tableId")')  AS remotetable (central character varying) INTO delegationCentralRdn;
	--RAISE NOTICE 'Actualizando replica_destination';
	
	EXECUTE 'INSERT INTO replica_destination_get(destination,ip,business,port,portjboss) VALUES('''||delegation||''',''localhost'','||businesslocal||',5432,'||jbossporttienda||')';
	EXECUTE 'INSERT INTO replica_destination_post(destination,ip,business,port,portjboss) VALUES('''||delegationCentralRdn||''','''||ippost||''','||businesscentral||',5432,'||jbossportcentral||')';
	insert into replica_source_get(source,ip,business,port,portjboss,"user",password,ftpUser,ftpPass,ftpPort,ftpUrl,ftpSourcePath) values(''||delegationCentralRdn||'',ipget,businesscentral,5432,jbossportcentral,userdb,passdb,userftp,passftp,portftp,ippost,pathftp);
	insert into replica_source_post(source,ip,business,port,portjboss,"user",password) values(delegation,'localhost',businesslocal,5432,jbossporttienda,userdb,passdb);
	
	--RAISE NOTICE 'Actualizando aplicacion';
	EXECUTE 'SELECT "tableId" FROM "delegación" WHERE rdn='''||delegation||'''' INTO delegationTableId;
	EXECUTE 'SELECT "empresaMI_EMPRESA" FROM "delegación" WHERE rdn='''||delegation||'''' INTO miEmpresaTableId;
	EXECUTE 'SELECT "tableId" FROM "almacén" WHERE rdn='''||warehouse||'''' INTO warehouseTableId;
	--RAISE NOTICE 'Delegation tableId %',delegationTableId;
	--RAISE NOTICE 'Delegation tableId %',warehouseTableId;
	EXECUTE 'UPDATE "aplicación" SET "mi_empresa"='||miEmpresaTableId||', "delegación"='||delegationTableId||', "almacén_entradas_por_defecto"='||warehouseTableId||', "almacén_por_defecto"='||warehouseTableId||', "almacén_salidas_por_defecto"='||warehouseTableId;
	
	--RAISE NOTICE 'Actualizando indices';
	UPDATE "índice" SET prefijo=installationNumber||prefijo, inicio_contador=1 WHERE prefijo is not null;
	UPDATE "índice" SET prefijo=installationNumber, inicio_contador=1 WHERE prefijo is null;
	UPDATE "índice" SET sufijo=0 WHERE valor_filtro='false';
	UPDATE "índice" SET sufijo=1 WHERE valor_filtro='true';
	-- necesario
	insert into "índice"(rdn,dominio,inicio_contador,"campo_en_prefijoPROPIEDAD_OBJETO","dígitos_mínimos",sufijo) select 'ticketventa',dom."tableId",100,p."tableId",5,0 
	from clase as dom,propiedad_objeto as p 
	where p.rdn='delegación' and dom.rdn='TICKET_VENTA' and not exists(select * from "índice"inner join clase on clase.rdn='TICKET_VENTA' where "índice".dominio=clase."tableId");
	update "índice" set "campo_en_prefijoPROPIEDAD_OBJETO"=p."tableId" from clase as dom,propiedad_objeto as p where dominio=dom."tableId" and p.rdn='delegación' and dom.rdn='TICKET_VENTA';

    --RAISE NOTICE 'Terminando';
    PERFORM * FROM dblink('END TRANSACTION;') as (rows varchar);
    
    -- Si estoy tirando de una copia distinta a la operativa, puede faltar ultimos ticket de mi tienda, y no se los va a traer por ser source=mi delegacion. Para que si se los traiga pongo destino nulo.
    -- Los cambios debo hacer en post porque get es solo lectura, y si hay alta disponibilidad se replica al get
    IF connectbusiness <> businesscentral THEN
    	EXECUTE 'SELECT * FROM dblink_connect(''host='||ippost||'
                                  port=5432
                                  user='||userdb||'
                                  password='||passdb||'
                                  dbname=dyna'||businesscentral||'
                                  connect_timeout=10'')';
		SELECT max(autonum_replica_msg) FROM replica_send_get INTO maxautonumtmp;	
		PERFORM * FROM dblink('UPDATE replica_msg set source=null where autonum>'||maxautonumtmp||' and source='''||delegation||'''') as (rows varchar);                                          
    END IF;
	
	RETURN true;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION restore_from(iprestore character varying,
							ipget character varying,
							ippost  character varying,
							dbportrestore integer,
							jbossporttienda integer, 
							jbossportcentral integer,							
							portftp integer,
							userftp character varying,
							passftp character varying,
							pathftp character varying,
							connectbusiness integer,
							businesscentral integer,
							userdb character varying, passdb character varying, 
							delegation character varying, warehouse character varying, installationNumber integer, franquicia boolean, solo_mi_destino boolean) OWNER TO dynagent;


--- DICCIONARIO
DROP TABLE IF EXISTS system_dictionary;
CREATE TABLE system_dictionary
(
  label_es character varying(1000),
  label character varying(1000),
  idiom character varying(100),
  type character varying(100)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE system_dictionary
  OWNER TO dynagent;
 
insert into system_dictionary(label_es,label,idiom) values
('FACTURA_A_CLIENTE','INVOICE','ingles'),
('ALBARÁN_CLIENTE','DELIVERY_NOTE','ingles'),
('PEDIDO_DE_CLIENTE','CUSTOMER_ORDER','ingles'),
('ALBARÁN-FACTURA_CLIENTE','DELIVERY_NOTE-INVOICE','ingles'),
('PRESUPUESTO_VENTA','QUOTE','ingles'),
('cliente','customer','ingles'),
('datos','data','ingles'),
('entrega','delivery','ingles'),
('horario','hours','ingles'),
('Bultos','groups','ingles'),
('Palets','pallets','ingles'),
('Peso','weight','ingles'),
('Neto','net','ingles'),
('Referencia','reference','ingles'),
('Fecha','date','ingles'),
('Transportista','transport','ingles'),
('Matrícula','licence','ingles'),
('Observaciones','comments','ingles'),
('Distribuidor','agent','ingles'),
('Proveedor','supplier','ingles'),
('Código','code','ingles'),
('Empresa','company','ingles'),
('pidiente','ask-for','ingles'),
('responsable','responsible','ingles'),
('DNI','ID number','ingles'),
('IVA','VAT','ingles'),
('Unidades','units','ingles'),
('Cubicaje','volume','ingles'),
('Comercial','agent','ingles'),
('Base','net','ingles'),
('recargo','surcharge','ingles'),
('departamento','departament','ingles'),
('Su Rfrª','your Ref.','ingles'),
('Total punto verde incluido en factura','Total green point included in invoice:','ingles'),
('retención','withholding','ingles'),
('descripción','description','ingles'),
('documentos','documents','ingles'),
('bruto','gross','ingles'),
('conforme','agreed','ingles'),
('firma','signature','ingles'),
('y','and','ingles'),
('portes','shipping','ingles'),
('pagados','free','ingles'),
('debidos','collect','ingles'),
('Forma de pago','Payment','ingles'),
('Importe','amount','ingles'),
('importante!','important!','ingles'),
('En caso de litigio, el comprador se someterá a los Tribunales y Juzgados de Estepa.','In case of dispute, the buyer will be submitted to the Tribunals and Courts of Estepa','ingles'),
('OPERACIÓN ASEGURADA POR CREDITO Y CAUCIÓN','OPERATION IS ASEGURATED BY CREDIT AND ON BOND','ingles'),
('NO EXISTE RECLAMACION SI LA INCIDENCIA NO ES ANOTADA EN EL MOMENTO DE LA DESCARGA EN EL ALBARAN DEL CONDUCTOR','incident must be noted on delivery time','ingles'),
('Conforme: Firma y sello','Agreed: Signature and stamp','ingles'), 
('sello','stamp','ingles');

