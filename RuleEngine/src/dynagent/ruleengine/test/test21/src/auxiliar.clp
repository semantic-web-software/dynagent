(deffacts my-facts
	(instance (IDTO 15) (IDO 10) (PROP targetClass) (VALUE 1) (VALUECLS PedidoComercial))
	(instance (IDO 10) (PROP level) (QMIN =(get-member Constants LEVEL_PROTOTYPE)))
	(instance (IDO 1) (PROP level) (QMIN =(get-member Constants LEVEL_FILTER)))
)

;;(defrule capacidad_almacen
	;;(instance (IDO 10008))
	;;=>
	;;(?engine modify 1)
	;;(?engine modifyQPropertyInstance 10008 2 5 4)
	;;(?engine runQuery 10 (create$ a b c))
	;;(printout t "Funciona" crlf)	
;;)

(defrule regla_4
	;;(event (TYPE =(get-member Events EV_PEDIDO)))
	(hierarchy (IDTO ?idto) (IDTOSUP 108))
	;; USERTASK
	(instance (IDTO 15) (IDO ?utask) (PROP targetClass) (VALUE ?pedFilter&:(neq ?pedFilter nil)) (VALUECLS ?idto))
	(instance (IDO ?utask) (PROP level) (QMIN =(get-member Constants LEVEL_PROTOTYPE)))
	(instance (IDO ?pedFilter) (PROP level) (QMIN =(get-member Constants LEVEL_FILTER)))
	
	(bind $?selList(create$ 125 ?idto)
	(bind ?fechaHoy (?engine getFechaHoy))	
	=>
	(?engine modifyQPropertyInstanceBoolean ?pedFilter 125 true true)
	  
	(bind $?resultList (?engine runQuery ?pedFilter $?selList))
	(foreach ?ido $resultList			
		(?engine createTask 15 ?ido ?idto)
	)	
)