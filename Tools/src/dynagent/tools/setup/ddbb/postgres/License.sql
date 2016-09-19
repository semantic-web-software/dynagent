DELETE FROM License;
INSERT INTO License (Users,FechaMax,"type") VALUES (encrypt('20', '1332284400','aes'), encrypt('2467198781562', '1332284400','aes'), encrypt('-1', '1332284400','aes'));

-- 2467198781562 es el año 2048