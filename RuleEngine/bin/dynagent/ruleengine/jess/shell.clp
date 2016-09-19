;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; shell.clp
; Author: Ildefonso Montero Perez - monteroperez@us.es
; Description: JESS Ruler SHELL
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;***************************************************************************
; APARIENCIA DEL SHELL
;***************************************************************************
;***************************************************************************
; (1) Presentacion del Shell
;***************************************************************************

(defrule inicio
  =>
  (printout t
	    crlf "======================================================================== " crlf crlf)
  (printout t "                     DYNAGENT RULER SHELL       " crlf crlf)
  (printout t " - Autor 1: Ildefonso Montero Perez - monteroperez(arroba)us.es " crlf crlf)
  (printout t " - Autor 2: Hassan Ali Sleiman      - hassansleiman(arroba)gmail.com " crlf crlf)
  (printout t "======================================================================== " crlf)
 
  (printout t "... iniciando shell \"help\" = ayuda."
	    crlf crlf)
	     (assert (simulacion 1)))
  

;***************************************************************************
; (2) Prompt del Shell: dos casos, uno en el cual eliminamos los hechos
; residuales de consulta que hayan quedado en la agenda y otro en el cual
; se refleja el modelo idoneo de ejecucion
;***************************************************************************



(defrule simulacion
  ?h <- (simulacion 1)
  (not (mostrar ?x))
  =>
  (retract ?h)  
  (printout t "[SHELL-DYNAGENT-RULER] > ")
  (bind ?cadena (readline))  
  (assert-string (str-cat "(orden " ?cadena ")"))
  (assert (incremento (mod (random) 60)))
)

(defrule simulacion-eliminacion-residuos-mostrar
  (declare (salience -10))
  ?h <- (simulacion 1)
  ?m <- (mostrar ?x)
  =>
  (retract ?h ?m)  
  (printout t "[SHELL-DYNAGENT-RULER] > ")
  (bind ?cadena (readline))  
  (assert-string (str-cat "(orden " ?cadena ")"))
  (assert (incremento (mod (random) 60)))
)

;***************************************************************************
; (3) Ayudas asociadas a cada comando: Conjunto de mensajes que se muestran
; por pantalla de manera que el usuario se familiarice rápidamente con
; el manejo del shell
;***************************************************************************
(defrule ayuda
  ?h <- (orden help)
  =>
  (retract ?h)
  (printout t " " crlf)
  (printout t "        *** AYUDA DEL DYNAGENT RULER SHELL *** " crlf crlf)
  (printout t " * instances            * quit 	 		 * help" crlf crlf)
  (printout t " * list-instances       * list-properties        * list-hierarchy" crlf crlf)
  (printout t " * list-model       	* list-utask" crlf crlf)
  (printout t " * consulta       	* Query mode" crlf crlf)
  (printout t " Puede ver la descripcion especifica de un comando introduciendo" crlf)
  (printout t " \"help\" <nombre-de-comando>. p.e: help lista-habitantes." crlf)
  (printout t " NOTA: [...] requieren argumento(s) adicional(es)." crlf crlf )
  (assert (simulacion 1)))

(defrule ayuda-instances
  ?h <- (orden help instances)
  =>
  (retract ?h)
  (printout t " " crlf)
  (printout t " - formato: instances" crlf)
  (printout t " - catalogo: consulta" crlf)
  (printout t " - descripcion: muestra las instancias que se han cargado en la agenda en formato JESS." crlf)
  (printout t " - ejemplo de uso: instances" crlf)
  (printout t " - ver: list-instances, list-hierarchy, list-properties" crlf crlf)
  (assert (simulacion 1)))

(defrule ayuda-quit
  ?h <- (orden help quit)
  =>
  (retract ?h)
  (printout t " " crlf)
  (printout t " - formato: quit" crlf)
  (printout t " - catalogo: otros comandos" crlf)
  (printout t " - descripcion: salida del shell." crlf)
  (printout t " - ejemplo de uso: quit" crlf)
  (printout t " - ver: (no existen comandos relacionados)" crlf crlf)
  (assert (simulacion 1)))

(defrule ayuda-help
  ?h <- (orden help help)
  =>
  (retract ?h)
  (printout t " " crlf)
  (printout t " - formato: help" crlf)
  (printout t " - catalogo: otros comandos" crlf)
  (printout t " - descripcion: ayuda del shell." crlf)
  (printout t " - ejemplo de uso: help" crlf)
  (printout t " - ver: (no existen comandos relacionados)" crlf crlf)
  (assert (simulacion 1)))
  
(defrule ayuda-list-instances
  ?h <- (orden help list-instances)
  =>
  (retract ?h)
  (printout t " " crlf)
  (printout t " - formato: list-instances" crlf)
  (printout t " - catalogo: consulta" crlf)
  (printout t " - descripcion: proporciona un listado de (instances)." crlf)
  (printout t " - ejemplo de uso: list-instances" crlf)
  (printout t " - ver: instances, list-properties, list-hierarchy" crlf crlf)
  (assert (simulacion 1)))
  
(defrule ayuda-list-properties
  ?h <- (orden help list-properties)
  =>
  (retract ?h)
  (printout t " " crlf)
  (printout t " - formato: list-properties" crlf)
  (printout t " - catalogo: consulta" crlf)
  (printout t " - descripcion: proporciona un listado de (property)." crlf)
  (printout t " - ejemplo de uso: list-properties" crlf)
  (printout t " - ver: instances, list-instances, list-hierarchy" crlf crlf)
  (assert (simulacion 1)))

(defrule ayuda-list-hierarchy
  ?h <- (orden help list-hierarchy)
  =>
  (retract ?h)
  (printout t " " crlf)
  (printout t " - formato: list-hierarchy" crlf)
  (printout t " - catalogo: consulta" crlf)
  (printout t " - descripcion: proporciona un listado de (hierarchy)." crlf)
  (printout t " - ejemplo de uso: list-hierarchy" crlf)
  (printout t " - ver: instances, list-properties, list-instances" crlf crlf)
  (assert (simulacion 1)))

;***************************************************************************
; COMANDOS DE EJECUCION DEL SHELL 
;***************************************************************************
; Conjunto de comandos que ejecutara nuestro shell, que son los siguientes:
;***************************************************************************
; INSTANCES (muestra el conjunto de instancias en formato Jess)
;***************************************************************************
(defrule instances
  ?l <- (orden instances)
  =>
  (retract ?l)
  (facts)
  (assert (simulacion 1)))	
  
 ;******************** 
 ;Consulta instancias
 ;******************** 


(defrule crea-consulta-instancias
 	?i <- (orden instance $?t)
 	=>
 ;	(printout t "Esta es tu consulta: (instance " $?t  ")" crlf crlf)
    (printout t "Resultados de la consulta:" crlf)
 	(retract ?i)
 	(undefrule consulta) 
    (undefrule consulta-rec)
  	(undefrule muestra-res-consulta)
    (bind ?v1 (str-cat "(defrule consulta ?c <- (orden consulta2 " $?t ") =>  (assert (mostrar-consulta " $?t ")))"))
    (bind ?v2 (str-cat "(defrule consulta-rec  ?c <- (orden consulta2 " $?t " ) ?m <- (mostrar-consulta " $?t ") =>  (retract ?c ?m) (assert (simulacion 1)) (assert (mostrar-consulta " $?t ")))"))
  	(bind ?v3 (str-cat "(defrule muestra-res-consulta ?m <- (mostrar-consulta " $?t ") ?i <- (instance (" $?t "))  =>  (assert (simulacion 1)) (assert (mostrar-consulta " $?t ")) (printout t \" instance: \" ?i  \"\(IDO \"(fact-slot-value ?i IDO) \"\)\(IDTO \"(fact-slot-value ?i IDTO) \"\)\(PROPERTY \"(fact-slot-value ?i PROPERTY) \"\)\(VALUE \"(fact-slot-value ?i VALUE)\"\)\(LEVEL \"(fact-slot-value ?i LEVEL)\"\)\" crlf))"))
 	(build ?v1)
 	(build ?v2)
 	(build ?v3)
 	(assert (orden consulta2 $?t))
)



;********************
; Modo Consulta
;
; Una regla que permite correlar por los slots de una istancia "instance"
; 
;********************


(defrule crea-consulta-prueba
	(declare (salience 11))
 	?i <- (orden consulta $?t)
 	=>
 	(retract  ?i)
 	(printout t "******************** Modo Consulta ******************" crlf)
 	(printout t "USO: (slot1 value1) (slot2 value2) (slot3 value3) ... " crlf)
	(printout t "*****************************************************" crlf)
	(printout t "Consulta> ")
	(bind ?cadena (readline))  
 	(undefrule consulta-prueba) 
    (bind ?v1  (str-cat "(defrule consulta-prueba (declare (salience 10))  ?i <- (instance "   ?cadena ") => (printout t \" instance: \" ?i  \"\(IDO \"(fact-slot-value ?i IDO) \"\)\(IDTO \"(fact-slot-value ?i IDTO) \"\)\(PROPERTY \"(fact-slot-value ?i PROPERTY) \"\)\(VALUE \"(fact-slot-value ?i VALUE)\"\)\(LEVEL \"(fact-slot-value ?i LEVEL)\"\)\" crlf)) (assert (consulta 1))"))
    (build ?v1)
    (assert (simulacion 1))
 	 	 	
)


;***************************************************************************
; LIST-INSTANCES (muestra nombre de las instancias cargadas)
;***************************************************************************
(defrule listar-instances
  ?l <- (orden list-instances)
  =>
  (retract ?l)
  (assert (simulacion 1))
  (assert (mostrar instances)))

(defrule listar-instances-rec
  ?l <- (orden list-instances)
  ?m <- (mostrar instances)
  =>
  (retract ?l ?m)
  (assert (simulacion 1))
  (assert (mostrar instances)))

(defrule muestra-instances
  (mostrar instances)
  (instance (NAME ?n))
  =>
  (printout t "* " ?n crlf))

;***************************************************************************
; LIST-PROPERTIES (muestra nombre de las properties cargadas)
;***************************************************************************
; Sin argumentos de busqueda
;***************************************************************************
(defrule listar-properties
  ?l <- (orden list-properties)
  =>
  (retract ?l)
  (assert (simulacion 1))
  (assert (mostrar properties)))

(defrule listar-properties-rec
  ?l <- (orden list-properties)
  ?m <- (mostrar properties)
  =>
  (retract ?l ?m)
  (assert (simulacion 1))
  (assert (mostrar properties)))

(defrule muestra-properties
  (mostrar properties)
  (property (NAME ?n))
  =>
  (printout t "* " ?n crlf))
  
;***************************************************************************
; LIST-HIERARCHY (muestra jerarquias directas)
;***************************************************************************
; Sin argumentos de busqueda
;***************************************************************************
(defrule listar-hierarchy
  ?l <- (orden list-hierarchy)
  =>
  (retract ?l)
  (assert (simulacion 1))
  (assert (mostrar hierarchy)))

(defrule listar-hierarchy-rec
  ?l <- (orden list-hierarchy)
  ?m <- (mostrar hierarchy)
  =>
  (retract ?l ?m)
  (assert (simulacion 1))
  (assert (mostrar hierarchy)))

(defrule muestra-hierarchy
  (mostrar hierarchy)
  (hierarchy (ID_TO ?h) (ID_TO_Padre ?p))
  (instance (IDTO ?h) (NAME ?nh))
  (instance (IDTO ?p) (NAME ?np))
  =>
  (printout t "* " ?np " es padre de " ?nh crlf))
  
  
;***************************************************************************
; LIST-MODEL (muestra las clases del modelo)
;***************************************************************************
(defrule listar-model
  ?l <- (orden list-model)
  =>
  (retract ?l)
  (assert (simulacion 1))
  (assert (mostrar model)))

(defrule listar-model-rec
  ?l <- (orden list-model)
  ?m <- (mostrar model)
  =>
  (retract ?l ?m)
  (assert (simulacion 1))
  (assert (mostrar model)))

(defrule muestra-model
  (mostrar model)
  (instance (NAME ?n))
  =>
  (printout t "* " ?n crlf))

;***************************************************************************
; LIST-UTASK (muestra las usertask del modelo)
;***************************************************************************
(defrule listar-utask
  ?l <- (orden list-utask)
  =>
  (retract ?l)
  (assert (simulacion 1))
  (assert (mostrar utask)))

(defrule listar-utask-rec
  ?l <- (orden list-utask)
  ?m <- (mostrar utask)
  =>
  (retract ?l ?m)
  (assert (simulacion 1))
  (assert (mostrar utask)))

(defrule muestra-utask
  (mostrar utask)
  (instance (NAME ?n) (LEVEL "1"))
  =>
  (printout t "* " ?n crlf))

;***************************************************************************
; CARGA REGLAS
;***************************************************************************
(defrule carga-reglas
  ?s <- (orden negocio ?reglas)
  =>
  (retract ?s)
  (batch ?reglas)  
  (reset)
  (assert (simulacion 1))
) 
;***************************************************************************
; quit: finalizacion del shell
;***************************************************************************
(defrule salir
  ?q <- (orden quit)
  =>
  (retract ?q)
  (printout t "Adios" crlf))

;*****************
; Fin de fichero *
;*****************