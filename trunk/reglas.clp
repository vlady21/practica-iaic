; Plantilla con los datos que utilizaremos para nuestras reglas
; aquellos que tienen un valor default, no son necesario asignarles
; valores, todos aquellos slots que no tienen default, obligatoriamente
; hay que asignarle uno de los valores posibles para el slot
(deftemplate datos
    (slot estado_actual)
    (slot tipo_estudios)    
    (slot conocimiento)   
    (slot apto_investigacion (default "no"))   
    (slot tipo_contrato (default "no_def"))
    (slot buscar_inem (default "no"))   
    (slot buscar_prensa (default "no"))
    (slot desea_informacion)         
    (slot lee_prensa)
    (slot estudia)   
    (slot tiempo_libre)     
    (slot experiencia)   
    (slot edad)   
    (slot destacar_formacion (default "no"))   
    (slot destacar_experiencia (default "no"))  
    (slot numero_paginas_CV)   
    (slot calificacion_estudios)
    (slot ruta_fichero_salida (default "log_grupoB09.txt"))        
    (slot fichero_salida (default "ficheroGuardar"))    
)




;-------------------------------
; ¿Donde buscar informacion?
;-------------------------------

(defrule donde_buscar_info1 "Comprobamos si es apto para la investigacion"
    (initial-fact)
    ; SI (estado_actual=busqueda & tipo_estudio=universitarios & apto_investigacion=no)
    ;	ENTONCES apto_investigacion=si
	?dat <- (datos (estado_actual "busqueda") (tipo_estudios "universitarios")(apto_investigacion "no"))
	=> 
    
    ;cumple los requisitos para ser apto para la investigacion (apto_investigacion=si)
    (modify ?dat (apto_investigacion "si"))
)

(defrule donde_buscar_info2 "Comprobamos donde podemos buscar"
    ; SI (conocimiento=investigacion & apto_investigacion=si & tipo_contrato=beca & buscar_prensa=no & buscar_inem=no) 
    ; 	ENTONCES buscar_prensa=si & buscar_inem=si
	?dat <- (datos (conocimiento "investigacion") (apto_investigacion "si") (tipo_contrato "beca")(buscar_prensa "no")(buscar_inem "no")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;si cumple los requisitos puede buscar en la prensa o en el inem (buscar_prensa=si & buscar_inem=si)
    (modify ?dat (buscar_prensa "si")(buscar_inem "si"))
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Si lo que desea es dedicarse a la investigación puede ponerse en contacto con 
        cualquier universidad y que le informe de los grupos de investigación que tienen 
        actualmente, de todas formas al desear una beca, puede mirar en la página web del 
        ministerio de ciencia e innovación (http://web.micinn.es/contenido.asp)
     	" crlf)
    (close ?fichero)    
)




