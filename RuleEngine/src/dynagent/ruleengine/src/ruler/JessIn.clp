(import dynagent.ruleengine.Constants)
(import dynagent.ruleengine.src.ruler.*)
(defclass instance dynagent.ruleengine.src.ruler.Fact)
(defclass property dynagent.ruleengine.src.ruler.FactProp)
(defclass hierarchy dynagent.ruleengine.src.ruler.FactHierarchy)
(defclass access dynagent.ruleengine.src.ruler.FactAccess)

(defrule regla_1
	(hierarchy (IDTO ?idtoP) (IDTOSUP %Pedido))
	(hierarchy (IDTO ?idtoG) (IDTOSUP %Genero))
	(instance (IDTO ?idtoP) (IDO ?idP) (PROP %level) (QMIN ?lev&:(>= ?lev (get-member Constants LEVEL_PROTOTYPE))))
	(instance (IDO ?idP) (PROP %importe_Pedido) (VALUE ?cant_ant))
	(instance (IDTO ?idtoG) (IDO ?idG) (PROP %level) (QMIN ?lev2&:(>= ?lev2 (get-member Constants LEVEL_PROTOTYPE))))
	(instance (IDO ?idP) (PROP %tiene_Producto) (VALUE ?idG))
	
	(instance (IDO ?idG) (PROP %precio) (VALUE ?cant))
	(instance (IDO ?idG) (PROP %descuento) (VALUE ?idDS))
	(instance (IDO ?idDS) (PROP %descuento_Simple) (VALUE ?cantD))		
	=>	
	(bind ?cantidad (+ ?cant ?cantD))
	(?engine modifyValuePropertyInstance ?idP %importe_Pedido (+ ?cant_ant ?cantidad) %int)	
)

(defrule regla_2
	(instance (PROP = (get-member Constants IdPROP_TARGETCLASS)) (VALUECLS %Confirmar_Oferta))
	(instance (IDTO %Confirmar_Oferta) (IDO ?idCO) (PROP %estado) (VALUE realizado))
	(instance (IDO ?idCO) (PROP %targetClass) (VALUE ?idOPV&:(neq ?idOPV nil)))
	
	(instance (IDTO %Venta) (IDO ?idV) (PROP %estado) (VALUE ?idEstado&:(neq ?idEstado nil)) (VALUECLS %Estado_Pedido))
	(instance (IDO ?idEstado) (PROP %nombre) (VALUE solicitado) (VALUECLS %string) (OBJECT ?obj))
	(instance (IDO ?idV) (PROP %oferta_Asociada) (VALUE ?idO))
	(instance (IDO ?idO) (PROP %fecha_Fin) (VALUECLS %date) (QMIN ?fechaFin&:(neq ?fechaFin nil)))
	=>
	(bind ?fechaHoy (?engine getFechaHoy))
	(if (> ?fechaHoy ?fechaFin) then		
		(throw IncompatibleValueException)
	else
		(?engine modifyValuePropertyInstance ?idEstado %nombre ofertaConfirmada/Pedido %string)		
	)
)

(defrule regla_3
	(instance (IDTO %Seguimiento_Pedido) (IDO ?utask) (PROP %targetClass) (VALUE ?pedFilter&:(neq ?pedFilter nil)) (VALUECLS ?idto))
	(instance (IDO ?utask) (PROP %level) (QMIN =(get-member Constants LEVEL_INDIVIDUAL)))
	(instance (IDO ?utask) (PROP %owner) (VALUE =(get-member Constants USER_SYSTEM)))	
	=>
	(?engine modifyQPropertyInstance ?pedFilter %fecha ?fechaHoy nil)
	(?engine modifyQPropertyInstanceBoolean ?pedFilter %recibido true true)
	
	(bind $?selList(create$ %fecha ?idto %recibido ?idto))
	(bind ?fechaHoy (?engine getFechaHoy))	
	 
	(bind $?resultList (?engine runQuery %Seguimiento_Pedido ?pedFilter $?selList))
	(foreach ?ido $?resultList			
		(?engine createTask %Seguimiento_Pedido ?ido ?idto)
	)	
)

(defrule regla_4
	(hierarchy (IDTO ?idto) (IDTOSUP %Pedido))
	(instance (IDTO ?idto) (IDO ?idPP) (PROP %level) (QMIN =(get-member Constants LEVEL_PROTOTYPE)))
	(instance (IDO ?idPP) (PROP %recibido) (VALUECLS %boolean) (QMIN true) (QMAX true))
	(instance (IDTO %Seguimiento_Pedido) (IDO ?idSP) (VALUE ?idPP&:(neq ?idPP nil)) (VALUECLS ?idto))
	=>
	(?engine removeTask ?idSP)
)
