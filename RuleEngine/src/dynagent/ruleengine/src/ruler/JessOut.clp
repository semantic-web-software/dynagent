(import dynagent.ruleengine.Constants)
(import dynagent.ruleengine.src.ruler.*)
(defclass instance dynagent.ruleengine.src.ruler.Fact)
(defclass property dynagent.ruleengine.src.ruler.FactProp)
(defclass hierarchy dynagent.ruleengine.src.ruler.FactHierarchy)
(defclass access dynagent.ruleengine.src.ruler.FactAccess)

(defrule regla_1
	(hierarchy (IDTO ?idtoP) (IDTOSUP 120))
	(hierarchy (IDTO ?idtoG) (IDTOSUP 119))
	(instance (IDTO ?idtoP) (IDO ?idP) (PROP 0) (QMIN ?lev&:(>= ?lev (get-member Constants LEVEL_PROTOTYPE))))
	(instance (IDO ?idP) (PROP 100) (VALUE ?cant_ant))
	(instance (IDTO ?idtoG) (IDO ?idG) (PROP 0) (QMIN ?lev2&:(>= ?lev2 (get-member Constants LEVEL_PROTOTYPE))))
	(instance (IDO ?idP) (PROP 120) (VALUE ?idG))
	
	(instance (IDO ?idG) (PROP 101) (VALUE ?cant))
	(instance (IDO ?idG) (PROP 151) (VALUE ?idDS))
	(instance (IDO ?idDS) (PROP 152) (VALUE ?cantD))		
	=>	
	(bind ?cantidad (+ ?cant ?cantD))
	(?engine modifyValuePropertyInstance ?idP 100 (+ ?cant_ant ?cantidad) 7920)	
)

(defrule regla_2
	(instance (PROP = (get-member Constants IdPROP_TARGETCLASS)) (VALUECLS 185))
	(instance (IDTO 185) (IDO ?idCO) (PROP 180) (VALUE realizado))
	(instance (IDO ?idCO) (PROP 8) (VALUE ?idOPV&:(neq ?idOPV nil)))
	
	(instance (IDTO 127) (IDO ?idV) (PROP 180) (VALUE ?idEstado&:(neq ?idEstado nil)) (VALUECLS 128))
	(instance (IDO ?idEstado) (PROP 136) (VALUE solicitado) (VALUECLS 7923) (OBJECT ?obj))
	(instance (IDO ?idV) (PROP 104) (VALUE ?idO))
	(instance (IDO ?idO) (PROP 182) (VALUECLS 7924) (QMIN ?fechaFin&:(neq ?fechaFin nil)))
	=>
	(bind ?fechaHoy (?engine getFechaHoy))
	(if (> ?fechaHoy ?fechaFin) then		
		(throw IncompatibleValueException)
	else
		(?engine modifyValuePropertyInstance ?idEstado 136 ofertaConfirmada/Pedido 7923)		
	)
)

(defrule regla_3
	(instance (IDTO 186) (IDO ?utask) (PROP 8) (VALUE ?pedFilter&:(neq ?pedFilter nil)) (VALUECLS ?idto))
	(instance (IDO ?utask) (PROP 0) (QMIN =(get-member Constants LEVEL_INDIVIDUAL)))
	(instance (IDO ?utask) (PROP 15) (VALUE =(get-member Constants USER_SYSTEM)))	
	=>
	(?engine modifyQPropertyInstance ?pedFilter 172 ?fechaHoy nil)
	(?engine modifyQPropertyInstanceBoolean ?pedFilter 173 true true)
	
	(bind $?selList(create$ 172 ?idto 173 ?idto))
	(bind ?fechaHoy (?engine getFechaHoy))	
	 
	(bind $?resultList (?engine runQuery 186 ?pedFilter $?selList))
	(foreach ?ido $?resultList			
		(?engine createTask 186 ?ido ?idto)
	)	
)

(defrule regla_4
	(hierarchy (IDTO ?idto) (IDTOSUP 120))
	(instance (IDTO ?idto) (IDO ?idPP) (PROP 0) (QMIN =(get-member Constants LEVEL_PROTOTYPE)))
	(instance (IDO ?idPP) (PROP 173) (VALUECLS 7929) (QMIN true) (QMAX true))
	(instance (IDTO 186) (IDO ?idSP) (VALUE ?idPP&:(neq ?idPP nil)) (VALUECLS ?idto))
	=>
	(?engine removeTask ?idSP)
)
