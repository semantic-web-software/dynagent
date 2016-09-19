-- Licencia DEMO

DELETE FROM License;
INSERT INTO License (Users,FechaMax,"type") VALUES (encrypt('20', '1332284400','aes'), encrypt('2467198781562', '1332284400','aes'), encrypt('0', '1332284400','aes'));

-- Licencia STANDARD(sin fecha fin) descargable. POner como clave de encriptaicon la fecha de aplicacion

DELETE FROM License;
INSERT INTO License (Users,FechaMax,"type") VALUES (encrypt('2', '1332284400','aes'), encrypt('2467198781562', '1332284400','aes'), encrypt('2', '1332284400','aes'));


-- Licencia CUSTOM (sin fecha fin)

DELETE FROM License;
INSERT INTO License (Users,FechaMax,"type") VALUES (encrypt('2', '1332284400','aes'), encrypt('2467198781562', '1332284400','aes'), encrypt('4', '1332284400','aes'));


-- 2467198781562 es el año 2048

-- impago custom licencia 25 dias
DELETE FROM License;
INSERT INTO License (Users,FechaMax,"type") VALUES (encrypt('4', '1332284400','aes'), encrypt((EXTRACT(EPOCH FROM now()+ interval '25 day')*1000)::bigint::varchar::bytea, '1332284400','aes'), encrypt('4', '1332284400','aes'));