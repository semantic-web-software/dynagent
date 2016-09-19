DELETE FROM "user";
INSERT INTO "user" ("userRol","password",rdn) select "tableId", encrypt('admin', 'dynamicIntelligent','aes'),'admin' from userrol where rdn='administrador';