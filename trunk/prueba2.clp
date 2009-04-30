(deftemplate datos
    (slot nombre)
    (slot edad)    
)


(defrule puede-fumar "Miramos si es mayor de edad y fuma para no prohibirselo"
	(datos (nombre ?nombre)(edad ?edad &:(< ?edad 18)))
	=> 
	(printout t ?nombre " tiene " ?edad " por lo que es menor." crlf)	
)

(defrule prueba
	(initial-fact)
	(datos (nombre ?nombre))
	=>
	(printout t " Bienvenido " ?nombre crlf)	
)



;(reset)
;(run)