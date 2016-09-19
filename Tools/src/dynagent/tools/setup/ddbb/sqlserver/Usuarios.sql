INSERT INTO [dbo].[Usuarios] ([Login],[Pwd])
     VALUES ('poner_login',ENCRYPTBYPASSPHRASE ('dynamicIntelligent', 'poner_pwd'))