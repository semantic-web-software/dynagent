DELETE from [dbo].[License];
INSERT INTO [dbo].[License] ([Users],[FechaMax])
     VALUES (ENCRYPTBYPASSPHRASE ('dynamicIntelligent', '10'), ENCRYPTBYPASSPHRASE ('dynamicIntelligent', '2467198781562'))

-- 2467198781562 es el año 2048
           