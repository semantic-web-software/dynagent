delete from ColumnProperties;

--INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_FEEDING_CONCRETA_IN','producto',1);
--INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_FEEDING_CONCRETA_OUT','producto',1);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO_GENÉRICO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO_GENÉRICO','máxima_producción',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO_GENÉRICO','coste',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO_GENÉRICO','tipo_alimentación',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO_GENÉRICO','tipo_coste',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO_GENÉRICO','tipo_ejecución',6);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TIEMPO_OCUPACIÓN','fecha_ocupación',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TIEMPO_OCUPACIÓN','hora_comienzo_ocupación',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TIEMPO_OCUPACIÓN','hora_fin_ocupación',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'HORARIO','hora_comienzo_jornada',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'HORARIO','hora_fin_jornada',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'RECURSO_GENÉRICO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'RECURSO_GENÉRICO','necesita_preparación',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_DEMANDA','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_DEMANDA','cantidad',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_DEMANDA','fecha_aconsejada_recepción',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'RECURSO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'RECURSO','tipo_recurso',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GASTO_PRODUCCIÓN','producto',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GASTO_PRODUCCIÓN','cantidad',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GASTO_PRODUCCIÓN','tipo_trabajo',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GASTO_PRODUCCIÓN','entrada_salida',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'USO_RECURSO','tipo_trabajo',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'USO_RECURSO','línea_recurso',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'USO_RECURSO','adicional',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_RECURSO','tipo_recurso',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_RECURSO','número',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'COSTE_TIEMPO','tipo_trabajo',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'COSTE_TIEMPO','coste',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'COSTE_TIEMPO','máxima_producción',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO','tipo_trabajo',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO','comienzo',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO','fin',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRABAJO','cantidad_requerida',5);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALMACÉN','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALMACÉN','nombre',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALMACÉN','localidad',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALMACÉN','provincia',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'MOVIMIENTO_ALMACÉN','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'MOVIMIENTO_ALMACÉN','origen',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'MOVIMIENTO_ALMACÉN','destino',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'MOVIMIENTO_ALMACÉN','estado',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'MOVIMIENTO_ALMACÉN','fecha',5);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REGULARIZACIÓN','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REGULARIZACIÓN','almacén',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REGULARIZACIÓN','responsable',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REGULARIZACIÓN','fecha',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'STOCK','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'STOCK','producto',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'STOCK','cantidad',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'STOCK','lote',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE_POTENCIAL','nombre',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE_POTENCIAL','localidad',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE_POTENCIAL','provincia',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE_POTENCIAL','teléfono',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_PROVEEDOR','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_PROVEEDOR','proveedor',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_PROVEEDOR','transportista',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_PROVEEDOR','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_PROVEEDOR','facturado',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_PROVEEDOR','importe',6);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ARTÍCULO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ARTÍCULO','descripción',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','descripción',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','peso_bruto_gramos',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','peso_neto_gramos',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','marca',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','unidades_por_bulto',6);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','unidad',7);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'GÉNERO','punto_verde',8);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_PROVEEDOR','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_PROVEEDOR','proveedor',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_PROVEEDOR','transportista',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_PROVEEDOR','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_PROVEEDOR','importe',5);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_COMPRAS','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_COMPRAS','factura_que_rectifica',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_COMPRAS','es_rectificada_por',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_COMPRAS','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_COMPRAS','importe',5);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_A_PROVEEDOR','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_A_PROVEEDOR','proveedor',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_A_PROVEEDOR','estado',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_A_PROVEEDOR','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_A_PROVEEDOR','fecha_estimada_entrega',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_A_PROVEEDOR','importe',6);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_COMPRA','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_COMPRA','proveedor',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_COMPRA','fecha',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_COMPRA','importe',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PROVEEDOR','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PROVEEDOR','nombre',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PROVEEDOR','representante',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PROVEEDOR','localidad',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PROVEEDOR','provincia',5);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'EMPRESA','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'EMPRESA','nombre',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'EMPRESA','localidad',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'EMPRESA','provincia',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'COBRO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'COBRO','cliente',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'COBRO','fecha',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'COBRO','importe',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_COBRO','cliente',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_COBRO','fecha',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_COBRO','importe',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_COBRO','deuda',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_PAGO','proveedor',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_PAGO','fecha',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_PAGO','importe',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'VENCIMIENTO_DE_PAGO','deuda',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_CLIENTE','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_CLIENTE','cliente',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_CLIENTE','transportista',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_CLIENTE','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_CLIENTE','facturado',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'ALBARÁN_CLIENTE','importe',6);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_VENTAS','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_VENTAS','factura_que_rectifica',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_VENTAS','es_rectificada_por',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_VENTAS','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_RECTIFICATIVA_VENTAS','importe',5);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_A_CLIENTE','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_A_CLIENTE','cliente',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_A_CLIENTE','transportista',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_A_CLIENTE','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FACTURA_A_CLIENTE','importe',5);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_DE_CLIENTE','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_DE_CLIENTE','cliente',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_DE_CLIENTE','estado',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_DE_CLIENTE','fecha',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_DE_CLIENTE','fecha_estimada_entrega',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PEDIDO_DE_CLIENTE','importe',6);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_VENTA','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_VENTA','cliente',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_VENTA','fecha',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRESUPUESTO_VENTA','importe',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRANSPORTISTA','nombre',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRANSPORTISTA','localidad',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRANSPORTISTA','provincia',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'TRANSPORTISTA','teléfono',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE','nombre',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE','distribuidor',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE','localidad',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE','provincia',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CLIENTE','transportista',6);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍMITES_STOCK','producto',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍMITES_STOCK','stock_mínimo',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍMITES_STOCK','stock_máximo',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LOTE','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LOTE','producto_lote',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA','cantidad',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA','producto',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CONTROL_TRANSPORTE','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CONTROL_TRANSPORTE','matrícula',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CONTROL_TRANSPORTE','responsable_transporte',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO_POR_CANTIDAD','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO_POR_CANTIDAD','cantidad',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO_POR_CANTIDAD','precio',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO','tarifa_precio',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO','pvp',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO_PROVEEDOR','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO_PROVEEDOR','precio',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PRECIO_PROVEEDOR','proveedor',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PERSONA','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'PERSONA','nombre',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CUENTA_BANCARIA','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CUENTA_BANCARIA','banco',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CUENTA_BANCARIA','número_cuenta',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REPRESENTANTE','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REPRESENTANTE','nombre',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REPRESENTANTE','provincia',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'REPRESENTANTE','teléfono',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CARGO','tipo_cargo',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CARGO','descripción',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'CARGO','importe',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DIRECCIÓN','nombre',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DIRECCIÓN','dirección',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DIRECCIÓN','provincia',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'RECTIFICACIÓN','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'RECTIFICACIÓN','causa_rectificación',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','producto',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','producto',2);--Repetimos producto para que coja las dos primeras de producto
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','cantidad',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','facturada',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','servida',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','lote',6);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','unidades_por_bulto',7);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'LÍNEA_ARTÍCULOS','cajas',8);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DESTINATARIO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DESTINATARIO','nombre',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DESTINATARIO','localidad',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DESTINATARIO','provincia',4);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DISTRIBUIDOR','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DISTRIBUIDOR','nombre',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DISTRIBUIDOR','localidad',3);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DISTRIBUIDOR','provincia',4);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DISTRIBUIDOR','comisión_reparto',5);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DISTRIBUIDOR','comisión_venta',6);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DÍA_PAGO','rdn',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DÍA_PAGO','mes',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DÍA_PAGO','día',3);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DESCUENTO_GLOBAL','tipo_descuento_global',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'DESCUENTO_GLOBAL','porcentaje',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FORMA_PAGO','tipo_aplazamiento',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'FORMA_PAGO','medio_de_pago',2);

INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'INFORMACIÓN_CONTACTO','nombre',1);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'INFORMACIÓN_CONTACTO','teléfono',2);
INSERT INTO ColumnProperties(ClassParent,Class,Prop,Priority) VALUES(NULL,'INFORMACIÓN_CONTACTO','móvil',3);
