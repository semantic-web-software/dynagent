DELETE FROM [dbo].[OrderProperties];

--Resumen: Base, recargo, retencio, total iva e importe
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(1,222,1,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(1,219,2,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(1,220,3,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(1,221,4,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(1,224,5,NULL,NULL)

--Datos de contacto: Nombre, apellidos....
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,108,1,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,168,2,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,165,3,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,137,4,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,135,5,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,136,6,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,257,7,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(2,256,8,NULL,NULL)

--Fecha min entrega, fecha estimada entrega, fecha max entrega
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(3,191,1,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(3,198,2,NULL,NULL)
INSERT INTO [dbo].[OrderProperties]([SEC],[PROP],[ORDER],[GROUP],[IDTO]) VALUES(3,192,3,NULL,NULL)

/*Consumo y salida, para trabajos y producciones*/
INSERT INTO OrderProperties(SEC,PROP,`ORDER`,`GROUP`,IDTO) VALUES(4,236,1,NULL,NULL);
INSERT INTO OrderProperties(SEC,PROP,`ORDER`,`GROUP`,IDTO) VALUES(4,234,2,NULL,NULL);

INSERT INTO OrderProperties(SEC,PROP,`ORDER`,`GROUP`,IDTO) VALUES(5,159,1,NULL,NULL);
INSERT INTO OrderProperties(SEC,PROP,`ORDER`,`GROUP`,IDTO) VALUES(5,161,2,NULL,NULL);