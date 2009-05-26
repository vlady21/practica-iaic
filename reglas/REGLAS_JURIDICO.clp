; PRACTICA 2 IAIC
; GRUPO B09
;
;	AUTORES:
;		- José Miguel Guerrero Hernández
;		- Víctor Adail Ferrer
;
; Se ha definido una plantilla con los datos que utilizaremos para nuestras reglas.
; Aquellos que tienen un valor default, no son necesario asignarles valores, todos 
; aquellos slots que no tienen default, segun la pregunta hay que asignarle uno de 
; los valores posibles para el slot.
;
; Para la modificacion de los slot, se ha utilizado el mismo hecho (fact). Se ha
; evitado hacer asertos (assert) para evitar la repeticion innecesaria de las reglas.
; Por eso se tiene un unico hecho, el cual en funcion de los valores de los slots, los
; cuales pueden ser modificados en diferentes reglas, iran activando las reglas necesarias
; y guardando los consejos en el fichero deseado ("log_grupoB09.txt" por defecto).
;
; REGLAS DE USO, DESDE JAVA:
;	- Ejecutar (reset)
;	- Cargar el fichero (batch reglasB09.clp)
;	- Crear el Fact con el template "datos"
;	- Asignar los valores a los slots en funcion de la pregunta a contestar:
;		- Delante de cada pregunta se indica en la cabecera a que slots hay
;		  que darles valor para que funcione. Los valores entre corchetes [,]
;		  indica el valor string que puede tener (pej: sexo = ["hombre", "mujer"]
;		  indica que al slot sexo hay que darle un valor, o bien mujer o bien hombre).
;		- Aquellos valores que pone "(asignacion por defecto)" significa que
;		  hay que darle ese valor al slot para poder contestar a la pregunta deseada.
;		  (pej: estado_actual = "busqueda" (asignacion por defecto) indica que para
;		  contestar a la pregunta hay que darle a estado_actual el valor "busqueda").
;		- Para los valores entre <,> se le asignara un valor entero pedido al usuario
;		  (pej: tiempo_libre = <numero entero positivo> indica que a tiempo_libre se
;		  le asignara un valor del tipo entero y positivo)
;	- Realizar el aserto del fact
;	- Ejecutar la aplicacion (run)
;	- Utilizar el fichero generado ("log_grupoB09.txt" por defecto) con los consejos
;	  de nuestra aplicacion, el cual puede ser tratado desde cualquier lenguaje de 
;	  programacion. A este fichero generado se le agregaran todos los consejos que 
;	  salgan de esta base de reglas.
;
; EJEMPLO USO CON JAVA:
;	Rete rete=new Rete();
;	rete.executeCommand("(reset)");
;	rete.executeCommand("(batch reglasB09.clp)");
;	Fact f = new Fact("datos", rete);
;	f.setSlotValue("estado_actual", new Value("busqueda_empleo", RU.STRING));
;	f.setSlotValue("numero_paginas_CV", new Value(2, RU.INTEGER));
;	... [insertar valores de los slots necesarios para la pregunta] ...
;	rete.assertFact(f);
;	rete.executeCommand("(run)");
;
;	Para cualquier duda o consulta contactar con:
;		- jomy.mc@gmail.com (José Miguel)
;		- lolken@gmail.com 	(Víctor)
;


(deftemplate datos
	
	; variable general para la seleccion de la pregunta
    (slot estado_actual)
    (slot ruta_fichero_salida (default "log_grupoB09.txt"))        
    (slot fichero_salida (default "ficheroGuardar"))  
	
	;variables del aspecto juridico
	(slot categoria_profesional)
	(slot tipo_trabajador)
	(slot baja)
	(slot grupo (default "0"))
	(slot contrato)
	(slot despedido)
	(slot anios)
    
)



; ****************************************
; **		REGLAS ASPECTO JURIDICO		**
; ****************************************

;-------------------------------------------------------------------------------------------------------------------
; 	PREGUNTA A LA QUE RESPONDE: ¿Cual es mi base de cotizacion?
;
;	DATOS ENTRADA:
;		- estado_actual = "cotizacion" (asignacion por defecto)
;
;		PREGUNTAS A REALIZAR, POSIBLES CONTESTACIONES PARA ASIGNAR A LA VARIABLE INDICADA:
;			¿Cual es su categoria profesional?
;				- categoria_profesional = ["licenciado", "diplomado", "jefe_administrativo", "ayudante_no_titulado", 
;						"oficial_administrativo", "subalterno", "auxiliar_administrativo", "oficial_de_primera_o_segunda", 
;						"oficial_de_tercera_o_especialista", "peon", "trabajador_menor_de_18"]
;-------------------------------------------------------------------------------------------------------------------

(defrule cotizacion1 "Comprobamos la categoria para dar una cotizacion"
    (initial-fact)
    ; SI (estado_actual=cotizacion & categoria_profesional=licenciado)
    ;	ENTONCES grupo=1
	?dat <- (datos (estado_actual "cotizacion") (categoria_profesional "licenciado") (grupo "0"))
	=> 
    
    (modify ?dat (grupo "1"))
)

(defrule cotizacion2 "Comprobamos la categoria para dar una cotizacion"
    ; SI (estado_actual=cotizacion & categoria_profesional=diplomado)
    ;	ENTONCES grupo=2
	?dat <- (datos (estado_actual "cotizacion") (categoria_profesional "diplomado")(grupo "0"))
	=> 
    
    (modify ?dat (grupo "2"))
)

(defrule cotizacion3 "Comprobamos la categoria para dar una cotizacion"
    ; SI (estado_actual=cotizacion & categoria_profesional=jefe_administrativo & grupo=0)
    ;	ENTONCES grupo=3
	?dat <- (datos (estado_actual "cotizacion") (categoria_profesional "jefe_administrativo")(grupo "0"))
	=> 
    
    (modify ?dat (grupo "3"))
)

(defrule cotizacion4 "Comprobamos la categoria para dar una cotizacion"
    ; SI (estado_actual=cotizacion  & grupo=0 & ( categoria_profesional=ayudante_no_titulado OR categoria_profesional=oficial_administrativo
	;	OR categoria_profesional=subalterno OR categoria_profesional=auxiliar_administrativo OR categoria_profesional=oficial_de_primera_o_segunda 
	;	OR categoria_profesional=oficial_de_tercera_o_especialista OR categoria_profesional=peon OR categoria_profesional=trabajador_menor_de_18))
    ;	ENTONCES grupo=2
	?dat <- (datos (estado_actual "cotizacion") (grupo "0") (categoria_profesional "ayudante_no_titulado" | "oficial_administrativo" | "subalterno" 
	| "auxiliar_administrativo" | "oficial_de_primera_o_segunda"	| "oficial_de_tercera_o_especialista" | "peon" |  "trabajado_menor_de_18"))
	=> 
    
    (modify ?dat (grupo "4"))
)

(defrule cotizacion5 "Mostramos consejo para el grupo 1"
    ; SI (estado_actual=cotizacion & grupo=1) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "cotizacion") (grupo "1") (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Tu base de cotización debe ser  de 1.016’50 euros.
        
     	" crlf)
    (close ?fichero)    
)

(defrule cotizacion6 "Mostramos consejo para el grupo 2"
    ; SI (estado_actual=cotizacion & grupo=2) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "cotizacion") (grupo "2") (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Tu base de cotización debe ser  de 843’34 euros.
        
     	" crlf)
    (close ?fichero)    
)

(defrule cotizacion7 "Mostramos consejo para el grupo 3"
    ; SI (estado_actual=cotizacion & grupo=3) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "cotizacion") (grupo "3") (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Tu base de cotización debe ser  de 733’51 euros.
        
     	" crlf)
    (close ?fichero)    
)

(defrule cotizacion8 "Mostramos consejo para el grupo 4"
    ; SI (estado_actual=cotizacion & grupo=4) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "cotizacion") (grupo "4") (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Tu base de cotización debe ser  de 722’90 euros.
        
     	" crlf)
    (close ?fichero)    
)



;-------------------------------------------------------------------------------------------------------------------
; 	PREGUNTA A LA QUE RESPONDE: ¿Tengo derecho a cobrar la baja?
;
;	DATOS ENTRADA:
;		- estado_actual = "cobrar_baja" (asignacion por defecto)
;
;		PREGUNTAS A REALIZAR, POSIBLES CONTESTACIONES PARA ASIGNAR A LA VARIABLE INDICADA:
;			¿Que tipo de trabajador es?
;				- tipo_trabajador = ["autonomo", "cuenta_ajena"]
;			¿Esta de baja?
;				- baja = ["si", "no"]
;-------------------------------------------------------------------------------------------------------------------

(defrule baja1 "Es empleado por cuenta ajena"
    ; SI (estado_actual=baja & tipo_trabajador=cuenta_ajena & baja=si) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "baja") (tipo_trabajador "cuenta_ajena") (baja "si") (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Te corresponde cobrar la baja.
        
     	" crlf)
    (close ?fichero)    
)

(defrule baja2 "Es empleado autonomo"
    ; SI (estado_actual=baja & tipo_trabajador=autonomo & baja=si) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "baja")  (tipo_trabajador "autonomo") (baja "si") (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        No te corresponde cobrar la baja.
        
     	" crlf)
    (close ?fichero)    
)


;-------------------------------------------------------------------------------------------------------------------
; 	PREGUNTA A LA QUE RESPONDE: ¿Tengo derecho a cobrar el paro?
;
;	DATOS ENTRADA:
;		- estado_actual = "cobrar_paro" (asignacion por defecto)
;
;		PREGUNTAS A REALIZAR, POSIBLES CONTESTACIONES PARA ASIGNAR A LA VARIABLE INDICADA:
;			¿Que tipo de contrato tiene/tenia?
;				- contrato = ["indefinido", "temporal"]
;			¿Ha sido despedido?
;				- despedido = ["si", "no"]
;			¿Años trabajados?
;				- anios = <numero entero positivo>
;-------------------------------------------------------------------------------------------------------------------

(defrule cobrar_paro1 "Tiene 4 meses por anio"
    ; SI (estado_actual=cobrar_paro & contrato=indefinido & anios>0) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "cobrar_paro")(despedido "si") (contrato "indefinido") (anios ?anios &:(> ?anios 0)) (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
       Te corresponden 4 meses de paro por cada año trabajado.
        
     	" crlf)
    (close ?fichero)    
)

(defrule cobrar_paro2 "Finiquito indefinido"
    ; SI (estado_actual=cobrar_paro & contrato=indefinido & anios=0) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "cobrar_paro") (despedido "si") (contrato "indefinido") (anios ?anios &:(= ?anios 0)) (ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
       No te corresponde cobrar el paro. Solo te corresponde cobrar el finiquito
        
     	" crlf)
    (close ?fichero)    
)

(defrule cobrar_paro3 "Finiquito temporal"
    ; SI (estado_actual=cobrar_paro & contrato=temporal) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "cobrar_paro")(despedido "si") (contrato "temporal")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
       No te corresponde cobrar el paro. Solo te corresponde cobrar el finiquito.
        
     	" crlf)
    (close ?fichero)    
)