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
    
    ; variables del aspecto afectivo/couching
    (slot buscador (default "no_def"))
    (slot rango_edad)
    (slot rechazado)
    (slot situacion_laboral)
    (slot estudio)
    (slot sexo) 
    (slot seleccionado_entrevista) 
    (slot conoce_perfil) 
    (slot conoce_empresa) 
    (slot conoce_protocolo) 
    (slot entrevistado (default "no_def"))
    
)

; ********************************************
; **	REGLAS ASPECTO AFECTIVO/COUCHING	**
; ********************************************

;-------------------------------------------------------------------------------------------------------------------
; 	PREGUNTA A LA QUE RESPONDE: ¿Como debo orientar la busqueda de empleo?
;
;	DATOS ENTRADA:
;		- estado_actual = "busqueda_empleo" (asignacion por defecto)
;
;		PREGUNTAS A REALIZAR, POSIBLES CONTESTACIONES PARA ASIGNAR A LA VARIABLE INDICADA:
;			¿Edad?
;				- rango_edad = ["joven", "adulto", "jubilado"]
;			¿Ha sido rechazo en los ultimos procesos de seleccion?
;				- rechazado = ["si", "no"]
;			¿Cual es su situacion laboral?
;				- situacion_laboral = ["desempleado", "trabajando_media_jornada", "trabajando_jornada_completa", "trabajando_jornada_intensiva"]
;			¿Esta estudiando actualmente o tiene pensado estudiar en un futuro?
;				- estudio = ["si", "no"]
;-------------------------------------------------------------------------------------------------------------------

(defrule como_realizar_busque1 "Comprobamos si es un jubilado"
    ; SI (estado_actual=busqueda_empleo & rango_edad=jubilado & buscador=no_def)
    ;	ENTONCES buscador=jubilado
	?dat <- (datos (estado_actual "busqueda_empleo") (rango_edad "jubilado") (buscador "no_def"))
	=> 

    ;cumple los requisitos (buscador=jubilado)
    (modify ?dat (buscador "jubilado"))
)

(defrule como_realizar_busque2 "Comprobamos si ha sido rechazado"
    ; SI (estado_actual=busqueda_empleo & rechazado=si & buscador=no_def)
    ;	ENTONCES buscador=rechazado
	?dat <- (datos (estado_actual "busqueda_empleo") (rechazado "si") (buscador "no_def"))
	=> 

    ;cumple los requisitos (buscador=rechazado)
    (modify ?dat (buscador "rechazado"))
)

(defrule como_realizar_busque3 "Comprobamos si no ha sido rechazado"
    ; SI (estado_actual=busqueda_empleo & rechazado=no & buscador=no_def)
    ;	ENTONCES buscador=no_rechazado
	?dat <- (datos (estado_actual "busqueda_empleo") (rechazado "no") (buscador "no_def"))
	=> 

    ;cumple los requisitos (buscador=no_rechazado)
    (modify ?dat (buscador "no_rechazado"))
)

(defrule como_realizar_busque4 "Comprobamos si el buscador rechazado es un desempleado"
    ; SI (buscador=rechazado & situacion_laboral = desempleado)
    ;	ENTONCES buscador=desempleado_rechazado
	?dat <- (datos (buscador "rechazado") (situacion_laboral "desempleado"))
	=> 

    ;cumple los requisitos (buscador=desempleado_rechazado)
    (modify ?dat (buscador "desempleado_rechazado"))
)

(defrule como_realizar_busque5 "Comprobamos si el buscador rechazado es un trabajador"
    ; SI (buscador=rechazado & ((situacion_laboral = trabajando_media_jornada) OR (situacion_laboral = trabajando_jornada_completa) OR (situacion_laboral = trabajando_jornada_intensiva))
    ;	ENTONCES buscador=trabajador_rechazado
	?dat <- (datos (buscador "rechazado") (situacion_laboral "trabajando_media_jornada" | "trabajando_jornada_completa" | "trabajando_jornada_intensiva"))
	=> 

    ;cumple los requisitos (buscador=trabajador_rechazado)
    (modify ?dat (buscador "trabajador_rechazado"))
)

(defrule como_realizar_busque6 "Comprobamos si el buscador no rechazado es un desempleado"
    ; SI (buscador=no_rechazado & situacion_laboral = desempleado)
    ;	ENTONCES buscador=desempleado_no_rechazado
	?dat <- (datos (buscador "no_rechazado") (situacion_laboral "desempleado"))
	=> 

    ;cumple los requisitos (buscador=desempleado_no_rechazado)
    (modify ?dat (buscador "desempleado_no_rechazado"))
)

(defrule como_realizar_busque7 "Comprobamos si el buscador no rechazado es un trabajador"
    ; SI (buscador=no_rechazado & ((situacion_laboral = trabajando_media_jornada) OR (situacion_laboral = trabajando_jornada_completa) OR (situacion_laboral = trabajando_jornada_intensiva))
    ;	ENTONCES buscador=trabajador_no_rechazado
	?dat <- (datos (buscador "no_rechazado") (situacion_laboral "trabajando_media_jornada" | "trabajando_jornada_completa" | "trabajando_jornada_intensiva"))
	=> 

    ;cumple los requisitos (buscador=trabajador_no_rechazado)
    (modify ?dat (buscador "trabajador_no_rechazado"))
)

(defrule como_realizar_busque8 "Comprobamos si esta estudiando el buscador de empleo"
    ; SI (estado_actual=busqueda_empleo & estudio=si)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (estado_actual "busqueda_empleo") (estudio "si")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Debes de buscar ofertas de empleo que te permitan compatibilizar el trabajo con tus estudios.
     	
        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_busque9 "Consejos para el desempleado rechazado"
    ; SI (buscador=desempleado_rechazado)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (buscador "desempleado_rechazado")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Evita mandar tu CV a todas las ofertas que encuentres. Cuando intentas obtener trabajos para los que no estás calificado, 
        te rechazan más seguido. Tómate el tiempo para preparar tu búsqueda de trabajo, apunta a lo que quieras y ejerce tu profesión 
        con determinación de acero. No pierdas el tiempo enviando currículum para cargos que en realidad no quieres.
        Mientras más centrado y específico seas, más poderosa será tu búsqueda de trabajo. Utiliza la carta de presentación y el CV 
        como herramientas a la hora de realizar la búsqueda de trabajo. No utilices un CV en video si no lo requiere la oferta de 
        trabajo solicitada. Personaliza la carta de presentación y actualiza tu CV con los datos más acordes a la oferta demandada.
        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_busque10 "Consejos para el trabajador rechazado"
    ; SI (buscador=trabajador_rechazado)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (buscador "trabajador_rechazado")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero "
	  Si lo que quieres es combinar tu trabajo actual con otro, busca las ofertas que permitan compatibilizar ambos trabajos. 
	  Si por el contrario quieres cambiar de trabajo intenta mantener en la medida de lo posible el actual trabajo hasta que 
	  encuentres una oferta segura. Evita mandar tu CV a todas las ofertas que encuentres. Cuando intentas obtener trabajos 
	  para los que no estás calificado, te rechazan más seguido. Tómate el tiempo para preparar tu búsqueda de trabajo, 
	  apunta a lo que quieras y ejerce tu profesión con determinación de acero. No pierdas el tiempo enviando currículum para
	  cargos que en realidad no quieres. Mientras más centrado y específico seas, más poderosa será tu búsqueda de trabajo. 
	  Utiliza la carta de presentación y el CV como herramientas a la hora de realizar la búsqueda de trabajo. No utilices 
	  un CV en video si no lo requiere la oferta de trabajo solicitada. Personaliza la carta de presentación y actualiza tu CV
	  con los datos más acordes a la oferta demandada.

        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_busque11 "Consejos para el desempleado no rechazado"
    ; SI (buscador=desempleado_no_rechazado)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (buscador "desempleado_no_rechazado")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero "
	  Evita mandar tu CV a todas las ofertas que encuentres. Tómate el tiempo para preparar tu búsqueda de trabajo, apunta a lo
	  que quieras y ejerce tu profesión con determinación de acero.
	  No pierdas el tiempo enviando currículum para cargos que en realidad no quieres.

        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_busque12 "Consejos para el trabajador no rechazado"
    ; SI (buscador=trabajador_no_rechazado)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (buscador "trabajador_no_rechazado")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero "
	    Si lo que quieres es combinar tu trabajo actual con otro, busca las ofertas que permitan compatibilizar ambos trabajos. 
	    Si por el contrario quieres cambiar de trabajo intenta mantener en la medida de lo posible el actual trabajo hasta que 
        encuentres una oferta segura.

        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_busque13 "Consejos para el jubilado"
    ; SI (buscador=jubilado)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (buscador "jubilado")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero "
        Busca trabajos que requieran una alta experiencia o si lo prefieres busca trabajos de voluntariados en la web 
        http://www.voluntariado.net/ o en otras similares.
        
        " crlf)
    (close ?fichero) 
)



;-------------------------------------------------------------------------------------------------------------------
; 	PREGUNTA A LA QUE RESPONDE: ¿Como debo de realizar la entrevista de trabajo?
;
;	DATOS ENTRADA:
;		- estado_actual = "entrevista" (asignacion por defecto)
;
;		PREGUNTAS A REALIZAR, POSIBLES CONTESTACIONES PARA ASIGNAR A LA VARIABLE INDICADA:
;			¿Sexo?
;				- sexo = ["hombre", "mujer"]
;			¿Ha sido seleccionado para una entrevista de trabajo?
;				- seleccionado_entrevista = ["si", "no"]
;			¿Conoce el perfil del puesto al que te has inscrito?
;				- conoce_perfil = ["si", "no"]
;			¿Conoce la empresa empleadora?
;				- conoce_empresa = ["si", "no"]
;			¿Conoce la forma correcta de realizar una entrevista de trabajo?
;				- conoce_protocolo = ["si", "no"]
;-------------------------------------------------------------------------------------------------------------------

(defrule como_realizar_entre1 "Comprobamos si no tiene protocolo"
    ; SI (estado_actual=entrevista & seleccionado_entrevista=si & conoce_protocolo=no & entrevistado=no_def)
    ;	ENTONCES entrevistado=sin_protocolo
	?dat <- (datos (estado_actual "entrevista") (seleccionado_entrevista "si")(conoce_protocolo "no") (entrevistado "no_def")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;cumple los requisitos (entrevistado=sin_protocolo)
    (modify ?dat (entrevistado "sin_protocolo"))
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Nunca llames al entrevistador por su nombre a menos que te digan ‘Puedes llamarme Fred’ o una frase equivalente, 
        dirígete al entrevistador de una manera formal.
		No te vistas de manera demasiado sensual, informal, escandalosa ni uses demasiadas joyas. Vístete como lo harías 
        para el cargo que quieres obtener.
		Un entrevistador no busca sólo un sí o un no como respuesta a sus preguntas. Aun así asegúrate de no hablar demasiado, 
        ya que indica nerviosismo.
		No hacer preguntas abiertas es una manera de demostrar que no te preocupa la empresa para la que te entrevistan. 
        Haz preguntas como ‘¿Cuál cree usted que es el futuro de este cargo?’ o ‘¿Por qué hay una vacante para este cargo?’
        
     	" crlf)
    (close ?fichero) 
)

(defrule como_realizar_entre2 "Comprobamos si no tiene protocolo y es hombre"
    ; SI (estado_actual=entrevista & entrevistado=sin_protocolo & sexo=hombre)
    ;	ENTONCES entrevistado=hombre_sin_protocolo
	?dat <- (datos (estado_actual "entrevista") (entrevistado "sin_protocolo")(sexo "hombre"))
	=> 

    ;cumple los requisitos (entrevistado=hombre_sin_protocolo)
    (modify ?dat (entrevistado "hombre_sin_protocolo"))
)

(defrule como_realizar_entre3 "Comprobamos si no tiene protocolo y es mujer"
    ; SI (estado_actual=entrevista & entrevistado=sin_protocolo & sexo=mujer)
    ;	ENTONCES entrevistado=mujer_sin_protocolo
	?dat <- (datos (estado_actual "entrevista") (entrevistado "sin_protocolo")(sexo "mujer"))
	=> 

    ;cumple los requisitos (entrevistado=mujer_sin_protocolo)
    (modify ?dat (entrevistado "mujer_sin_protocolo"))
)

(defrule como_realizar_entre4 "Si es mujer sin protocolo mostramos el mensaje"
    ; SI (estado_actual=entrevista & entrevistado=mujer_sin_protocolo)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (estado_actual "entrevista") (entrevistado "mujer_sin_protocolo")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        No te vistas de manera demasiado sensual, informal, escandalosa ni uses demasiadas joyas. 
        Vístete como lo harías para el cargo que quieres obtener.
     	
        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_entre5 "Si es hombre sin protocolo mostramos el mensaje"
    ; SI (estado_actual=entrevista & entrevistado=hombre_sin_protocolo)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (estado_actual "entrevista") (entrevistado "hombre_sin_protocolo")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        No te vistas de manera informal o escandaloso. Vístete como lo harías para el cargo que quieres obtener.
     	
        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_entre6 "Si esta seleccionado y no conoce el perfil"
    ; SI (estado_actual=entrevista & seleccionado_entrevista=si & conoce_perfil=no)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (estado_actual "entrevista") (seleccionado_entrevista "si") (conoce_perfil "no")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Antes de acudir a la entrevista de trabajo deberías informarte sobre el perfil de la oferta seleccionada.
        
        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_entre7 "Si esta seleccionado y no conoce la empresa"
    ; SI (estado_actual=entrevista & seleccionado_entrevista=si & conoce_perfil=no)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (estado_actual "entrevista") (seleccionado_entrevista "si") (conoce_empresa "no")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Una de las mayores decepciones es que un candidato no investigue lo suficiente de la compañía a la que postula. 
        Investiga sobre la compañía en la que has empezado a formar parte del proceso de selección.
        
        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_entre8 "Si no esta seleccionado"
    ; SI (estado_actual=entrevista & seleccionado_entrevista=no)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (estado_actual "entrevista") (seleccionado_entrevista "no")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Al no haber sido seleccionado para la entrevista, si se presenta es muy probable que haya más candidatos. 
        No piense en que debe destacar por encima, muestre seguridad en sus contestaciones y demuestre su conocimiento sobre lo que 
        se pregunte. Evite mirar de mal modo a algún candidato e intente si hay algún trabajo en grupo, demostrar su capacidad de trabajo, 
        habla y manejo de la situación. Si debe simular dirigir a un equipo, intente demostrar que tiene dotes necesarios para ello sin ser 
        brusco. Demuestre cercanía y educación en todo momento.
        
        " crlf)
    (close ?fichero) 
)

(defrule como_realizar_entre9 "Si conoce todo sobre la empresa, perfil y protocolo"
    ; SI (estado_actual=entrevista & conoce_empresa=si & conoce_perfil=si & conoce_protocolo=si)
    ;	ENTONCES mostrar mensaje
	?dat <- (datos (estado_actual "entrevista") (conoce_empresa "si") (conoce_perfil "si") (conoce_protocolo "si")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Veo que conoce la empresa, el cargo al que aspira así como el protocolo. Procure no estar
        nervioso, demuestre seriedad, de muestras de acercamiento por su parte, sea amable, no titubee,
        procure mirar a la cara al entrevistador y no juegue con las manos (mover los dedos, coger y mover un bolígrafo, etc...).
        Muestre educación y saber estar. Cuanto más preparado se le vea para el puesto, más probabilidades tiene de ser admitido.
        Responda a lo que se le pregunte con respuestas directas y concisas, no de rodeos.
        
        " crlf)
    (close ?fichero) 
)



