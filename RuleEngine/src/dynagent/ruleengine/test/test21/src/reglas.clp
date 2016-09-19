(defrule capacidad_almacen
	(instance (IDTO 4))
	=>
	;;(?engine modifyQPropertyInstance 10008 2 5 4)
	;;(bind(?engine runQuery 10008 (create$ 2 10008))
	;;(bind $?resultList (?engine runQuery 10008 (create$ 2 10008)))
	;;(printout t "LISTA: " $?resultList crlf)
	;;(foreach ?ido $?resultList	
	;;	(printout t "Funciona" crlf)		
	;;	(?engine createTask 105 ?ido 50)
	;;)
	(bind ?fecha (?engine getFechaHoy))
	(printout t "FECHA HOY" ?fecha crlf)			
)
