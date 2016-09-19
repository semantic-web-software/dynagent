;;*****************************************************************************************************
;; reglas.clp
;; Autor: Ildefonso Montero Pérez - monteroperez@us.es
;; Descripcion: Fichero de reglas de negocio
;;*****************************************************************************************************
(import dynagent.ruleengine.src.ruler.*)
(import dynagent.ruleengine.src.data.dao.*)

(defclass instance dynagent.ruleengine.src.ruler.Fact)
(defclass property dynagent.ruleengine.src.data.dao.Properties)

;;*****************************************************************************************************
;; REGLA DE EUROCONVERSION
;; Descripcion: El precio en pesetas de un articulo es igual que el precio en euros del articulo
;;				multiplicado por 166.386.
;;*****************************************************************************************************
(defrule euros2ptas
	(instance (ROL ?a) (PROPERTY ?p) (VALUECLS ?ve) (QMIN ?euro) (QMAX ?euro) (LEVEL 1) (OBJECT ?obj))
	(instance (IDTO ?a) (NAME ARTICULO))
	(instance (IDTO ?ve) (NAME EURO))
	(instance (IDTO ?vp) (NAME PESETA))
	(property (PROP ?p) (NAME precio))
	=>
	(bind ?pta (* ?euro 166.386))
    (set ?obj QMIN ?pta)    
    (set ?obj QMAX ?pta)  
    (set ?obj VALUECLS ?vp)  
)

;;*****************************************************************************************************
;; REGLA DE POBLACION
;; Descripcion: La población de una zona de un inmueble es igual a la población de dicho inmueble
;;*****************************************************************************************************
;; (defrule poblacion

;;	(instance (ROL ?art) (PROPERTY ? p) (VALUE ?val) (LEVEL 1) (IDTO ?inmueble) (IDO ?inm))
;;	(instance (ROL ?art) (PROPERTY ?z) (VALUECLS ?vz) (LEVEL 1) (IDTO ?it) (IDO ?id))
;;	(instance (IDTO ?inmueble) (NAME INMUEBLE))
;;	(property (PROP ?p) (NAME poblacion))
;;	(property (PROP ?z) (NAME zona))
;;	(instance (IDTO ?vz) (NAME ZONA) (PROPERTY ?p) (OBJECT ?obj))
;;	=> 
;;	(set ?obj VALUE ?val)
;;  )
;;(defrule poblaZona_From_poblaInmueble_NEW
;;	(instance (IDTO ?inmueble) (NAME INMUEBLE))
;;	(instance (IDTO ?zona)(NAME ZONA))
;;	(property (PROP ?poblacion)(NAME poblacion))
;;	(property (PROP ?zona) (NAME zona))
;;	(instance (ido ?inm) (property poblacion) (value ?pob))
;;	(instance (ido ?inm) (property zona) (value ?zon)) 
;;	(not (exists (instance (idto ?zon) (property poblacion) (value ?pob))))
;;  =>
;;	(definstance ...)
;;	)
;;
;;(defrule poblaZona_From_poblaInmueble_UPDATE
;;	(instance (ido ?inm) (property inmueblePoblacion) (value ?pob&:(<> ?pob nil)))
;;	(instance (ido ?inm) (property inmuebleZona) (value ?zon)) 
;;	(instance (idto ?zon) (property zonaPoblacion) (value ?pobzona&:(<> ?pobzona ?pob)) (OBJECT ?zonaPoblacion))
;;  =>
;;	(set ?zonaPoblacion value ?pob)
;;	)
;;
;;(defrule poblaZona_From_poblaInmueble_CHECK
;;
;;	(instance (ido ?inm) (property inmueblePoblacion) (value nil))
;;	(instance (ido ?inm) (property inmuebleZona) (value ?zon&:(<> ?zon nil)) (commit FALSE)) 
;;	(instance (class ?zon) (property zonaPoblacion) (OBJECT ?zonaPoblacion))
;;  =>
;;	(printout t (call ?RULER exception “Primero debe pinchar la poblacion del inmueble”) crlf)
;;	)
;;
;;
;;
;;(defrule poblaZona_From_poblaInmueble
;; La poblacion de una zona de un inmueble coincide con la poblacion de dicho inmueble
;;	
;;	(instance (IDO ?inm) (IDTO inmueble) PROPERTY poblacion) (VALUE ?pob)))
;;	(instance (IDO ?inm) (IDTO inmueble)( PROPERTY zona) (value ?zon)) 
;;	(instance (ido ?zon) (idto zona)     (property zonaPoblacion)     (value ?zonpob))
;; =>
;;	(equal ?zonpob ?pob)
;;	)