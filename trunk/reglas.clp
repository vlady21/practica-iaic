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
    (slot desea_informacion (default "no"))         
    (slot lee_prensa)
    (slot estudia)   
    (slot tiempo_libre)     
    (slot experiencia)   
    (slot destacar_formacion (default "no"))   
    (slot destacar_experiencia (default "no"))  
    (slot numero_paginas_CV)   
    (slot ruta_fichero_salida (default "log_grupoB09.txt"))        
    (slot fichero_salida (default "ficheroGuardar"))    
)



;------------------------------------------------------------------------------
; ¿Donde buscar informacion?
;
;	DATOS ENTRADA:
;		- estado_actual = busqueda
;		- tipo_estudios = ["universitarios", "no_universitarios"]
;		- conocimiento = ["investigacion", "basico"]
;		- tipo_contrato = ["beca", "jornada"]
;		- desea_informacion = ["si", "no"]
;		- lee_prensa = ["si", "no"]
;------------------------------------------------------------------------------

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
    ; SI (estado_actual=busqueda & conocimiento=investigacion & apto_investigacion=si & tipo_contrato=beca & buscar_prensa=no & buscar_inem=no) 
    ; 	ENTONCES buscar_prensa=si & buscar_inem=si
	?dat <- (datos (estado_actual "busqueda") (conocimiento "investigacion") (apto_investigacion "si") (tipo_contrato "beca")(buscar_prensa "no")(buscar_inem "no")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;si cumple los requisitos puede buscar en la prensa o en el inem (buscar_prensa=si & buscar_inem=si)
    (modify ?dat (buscar_prensa "si")(buscar_inem "si"))
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Puede buscar empleo en el INEM o en la prensa.
        Si lo que desea es dedicarse a la investigación puede ponerse en contacto con 
        cualquier universidad y que le informe de los grupos de investigación que tienen 
        actualmente, de todas formas al desear una beca, puede mirar en la página web del 
        ministerio de ciencia e innovación (http://web.micinn.es/contenido.asp).
        
     	" crlf)
    (close ?fichero)    
)

(defrule donde_buscar_info3 "Comprobamos donde podemos buscar"
    ; SI (estado_actual=busqueda & tipo_contrato=jornada & buscar_prensa=no & buscar_inem=no) 
    ; 	ENTONCES buscar_prensa=si & buscar_inem=si
	?dat <- (datos (estado_actual "busqueda") (tipo_contrato "jornada")(buscar_prensa "no")(buscar_inem "no")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;si cumple los requisitos puede buscar en la prensa o en el inem (buscar_prensa=si & buscar_inem=si)
    (modify ?dat (buscar_prensa "si")(buscar_inem "si"))
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Puede buscar empleo en el INEM o en la prensa.
        
     	" crlf)
    (close ?fichero)    
)

(defrule donde_buscar_info4 "Miramos si puede buscar en la prensa"
    ; SI (estado_actual=busqueda & apto_investigacion=no & buscar_prensa=no) 
    ; 	ENTONCES buscar_prensa=si
	?dat <- (datos (estado_actual "busqueda") (apto_investigacion "no") (buscar_prensa "no")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
    
    ;si cumple los requisitos puede buscar en la prensa (buscar_prensa=si)
    (modify ?dat (buscar_prensa "si"))
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Puede buscar empleo en la prensa.
        
     	" crlf)
    (close ?fichero)    
)

(defrule donde_buscar_info5 "Miramos si quiere mas informacion y puede buscar en el INEM"
    ; SI (estado_actual=busqueda & buscar_inem=si & desea_informacion=si) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "busqueda") (buscar_inem "si") (desea_informacion "si")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
       
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Esta es una fuente de búsqueda de empleo de carácter público, el Instituto Nacional 
        de Empleo (I.N.E.M.), en cuyas oficinas puedes inscribirte como demandante activo en 
        búsqueda de empleo, debiéndolo de hacer en aquella que más cerca se encuentre de tu 
        domicilio o localidad. Este paso es imprescindible para acceder a las ofertas de trabajo 
        que se canalizan a través de este organismo.
        
		¿Por qué puede ser interesante estar inscrito en él? porque es un espacio de búsqueda 
        que te ofrece:
			•	ENTRAR EN PROCESOS DE SELECCIÓN. 
			•	LA POSIBILIDAD DE CONSEGUIR ORIENTACIÓN PROFESIONAL. 
			•	REALIZAR CURSOS DE FORMACIÓN OCUPACIONAL DEL I.N.E.M. U OTROS ORGANISMOS. 
			•	QUE TE INCLUYAN EN PROGRAMAS ESPECÍFICOS DE BÚSQUEDA DE EMPLEO
        
		Para registrarte como demandante debes llevar el carnet de identidad, y tu cartilla o 
        número de la Seguridad Social (lo tendrás en caso de haber trabajado con anterioridad), 
        así como las justificaciones académicas y profesionales. Como demandante rellenarás una 
        solicitud que se tramita mediante una entrevista personalizada. Deberías ir con una 
        orientación clara del sector profesional o de las actividades en las que te vayas a 
        inscribir. Si no la tuvieras, pídeles asesoramiento, están suficientemente preparados 
        y disponen de la información para orientarte.
        
		Tienes dos formas de inscribirte en este organismo:
			•	COMO CUALQUIER OTRO TRABAJADOR 
			•	EN EL REGISTRO DE MINUSVÁLIDOS
        
		Muchas personas con discapacidad de las que buscan trabajo se inscriben como demandantes 
        no discapacitados, porque piensan que si no, les puede restar posibilidades de encontrar 
        un empleo.
        
		O bien te puedes inscribir en el registro de minusválidos. Lo más importante de esto es 
        que hay empresas que solicitan expresamente trabajadores con algún tipo de discapacidad 
        a este organismo. Para inscribirse en este registro, hay que presentar el Certificado 
        Oficial de Minusvalía.
        
		Una vez inscrito como demandante de empleo en tu oficina más cercana (la que te corresponde), 
        debes presentarte periódicamente en la fecha que aparece en tu tarjeta de demanda. Debes 
        renovarla en los días exactos indicados y acudir a la oficina de empleo cuando previamente 
        seas requerido. Si no la renuevas en estas fechas o no te presentas, perderás la antigüedad, 
        que en algunos programas o puestos es en sí un criterio añadido de selección, así como todos 
        los derechos derivados de tu inscripción. Debes comunicarles todos los cambios que consideres 
        importantes a nivel formativo y profesional que te puedan ayuda a encontrar trabajo.
        
		Existen dos acciones y actitudes a la hora de estar inscrito en el I.N.E.M. para sacar el 
        máximo beneficio o provecho:
        
		ACCION ACTIVA Y ACTITUD POSITIVA
			•	Realizar cursos de formación ocupacional 
			•	Solicitar información y orientación laboral 
			•	Preguntar por las ofertas actuales 
			•	Pasarse periódicamente por la oficina
        
		ACCION O ACTITUD PASIVA  
			•	Presentarse cuando señala la tarjeta de demanda 
			•	Presentarse sólo cuando se es requerido 
			•	No solicitar información
        
		De ti depende que emprendas una acción u otra, nosotros te recomendamos que emprendas la 
        primera. Acércate siempre que puedas o quieras, hay una sección de demandas actuales, 
        solicítalas, no te dé vergüenza el solicitar cuanta información creas necesaria para ti. 
        Por último, recordarte que estar inscrito como demandante de empleo en la oficina del 
        I.N.E.M. es un requisito previo para poder ser contratado y si ya estás trabajando puedes 
        inscribirte como demandante de mejora de empleo.
        
     	" crlf)
    (close ?fichero)    
)

(defrule donde_buscar_info6 "Miramos si quiere mas informacion y puede buscar en la prensa si la lee"
    ; SI (estado_actual=busqueda & buscar_prensa=si & desea_informacion=si & lee_prensa=si) 
    ; 	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "busqueda") (buscar_prensa "si") (desea_informacion "si")(lee_prensa "si")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
    => 
       
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Los medios de comunicación (sobre todo la prensa escrita) constituyen otra fuente de información
        valiosa en la búsqueda de empleo. En ellos puedes encontrar información interesante acerca del
        mercado laboral, ofertas de puestos de trabajo, actividades empresariales de nueva creación, 
        nuevas ideas, etcétera. 
        
		En la prensa diaria aparecen anuncios de ofertas y demandas de trabajo, no sólo diariamente, 
        sino que algunos periódicos los concentran los fines de semana, sobre todo el domingo, como por 
        ejemplo: ABC, EL PAÍS (ambos de tirada nacional). Es otro espacio de búsqueda de empleo que 
        puedes y debes utilizar.
        
		Para muchas empresas la prensa es el canal más habitual para captar a sus trabajadores. Normalmente 
        son empresas especializadas en selección de personal las que ponen los anuncios. Los perfiles son 
        predefinidos y dan como resultado respuestas masivas.
        
		Debes acostumbrarte a saber cuando salen y en que periódicos se concentra el mayor numero de ofertas.
			
        	ANUNCIOS EN LA PRENSA (OFERTAS):  
				•	TIRADA O AMBITO LOCAL: 
					o	Empresas ubicadas en tu zona. 
        
				•	TIRADA O AMBITO NACIONAL: 
					o	Oferta más amplia y diversificada. 
					o	Mas información sobre el mercado laboral en general 
					o	Ofertas fuera de tu lugar de residencia.
        
				•	REVISTAS ESPECIALIZADAS: 
					o	Información específica y bolsa de empleo.
        
			Ante una oferta interesante:
				•	Responde rápidamente (en el plazo de cinco días). 
				•	Envía una carta de presentación y el curriculum vitae
        
		No te desanimes si crees no cumplir alguno de los requisitos exigidos. Piensa que la mayoría de las 
        demandas conlleva procesos de selección personalizados (entrevista personal). Puedes destacar por 
        otras cualidades de las que no eras consciente y que no están expuestas en el anuncio. Las personas 
        que logran superar la entrevista personal tienen que superar a su vez un periodo de prueba.
        
		Recuerda que también puedes acceder o informarte por este canal sobre las convocatorias de empleo 
        público-oposiciones, contrataciones laborales, en prácticas, etcétera.
        
		Puedes utilizar este medio para ofrecer tus servicios profesionales (puedes crear tu demanda). Es otra 
        vía que te ofrece el mercado para poder acceder a un puesto de trabajo. Los pasos que debes seguir si 
        optas por este caso serian:
			1.	Saber lo que quieres: cómo hacerlo, que deberías poner, y que puedes o deseas conseguir. 
			2.	Selecciona el medio o los medios de comunicación más acordes con lo que deseas para tu anuncio. 
        		Esto dependerá de tus intereses y objetivos, no descartes el hacerlo en los de gran difusión 
        		(aunque su coste sea superior que en los locales) o tal vez, en aquellos de difusión especializada, 
        		pero de menor divulgación. 
			3.	Crea un buen anuncio, en el que de manera atractiva y sencilla ofertes tus servicios. Intenta ser 
        		original, pero teniendo en cuenta la sencillez y la claridad en el mismo. 
			4.	Debes estar preparado por si te llaman para salir bien del proceso de selección.
        
     	" crlf)
    (close ?fichero)    
)



;------------------------------------------------------------------------------
; ¿Que tipo de contrato me interesa?
;
;	DATOS ENTRADA:
;		- estado_actual = "contrato"
;		- estudia = ["si", "no"]
;		- conocimiento = ["investigacion", "basico"]
;		- tiempo_libre = <numero entero positivo>;
;------------------------------------------------------------------------------

(defrule tipo_contrato1 "Comprobamos si le interesa una beca"
    ; SI (estado_actual=contrato & estudia=si & tipo_contrato<>beca)
    ;	ENTONCES tipo_contrato=beca
	?dat <- (datos (estado_actual "contrato") (estudia "si")(tipo_contrato ~"beca"))
	=> 

    ;cumple los requisitos para ser apto para la beca (tipo_contrato=beca)
    (modify ?dat (tipo_contrato "beca"))
)

(defrule tipo_contrato2 "Comprobamos si le interesa un trabajo normal a jornada"
    ; SI (estado_actual=contrato & estudia=no & tipo_contrato<>jornada)
    ;	ENTONCES tipo_contrato=jornada
	?dat <- (datos (estado_actual "contrato") (estudia "no")(tipo_contrato ~"jornada"))
	=> 

    ;cumple los requisitos para ser apto para la beca (tipo_contrato=jornada)
    (modify ?dat (tipo_contrato "jornada"))
)

(defrule tipo_contrato3 "Comprobamos si le interesa una beca de investigacion"
    ; SI (estado_actual=contrato & tipo_contrato=beca & conocimiento=investigacion & tiempo_libre<=6)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "contrato") (tipo_contrato "beca") (conocimiento "investigacion") (tiempo_libre ?tiempo_libre &:(<= ?tiempo_libre 6))(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Con los conocimientos de investigación y las " ?tiempo_libre " horas de las que dispone, le interesaría 
        buscar un contrato que sea una beca de investigación.
        
     	" crlf)
    (close ?fichero)  
)

(defrule tipo_contrato4 "Comprobamos si le interesa una beca normal"
    ; SI (estado_actual=contrato & tipo_contrato=beca & conocimiento<>investigacion & tiempo_libre<=6)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "contrato") (tipo_contrato "beca") (conocimiento ~"investigacion") (tiempo_libre ?tiempo_libre &:(<= ?tiempo_libre 6))(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Le interesa un contrato de beca, el cual sea flexible para poder asistir a clase.
        
     	" crlf)
    (close ?fichero)  
)

(defrule tipo_contrato5 "Comprobamos si le interesa trabajo a media jornada"
    ; SI (estado_actual=contrato & tipo_contrato=jornada & tiempo_libre<=6)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "contrato") (tipo_contrato "jornada") (tiempo_libre ?tiempo_libre &:(<= ?tiempo_libre 6))(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Le interesaría un contrato a media jornada de unas 4-6 horas diarias.
        
     	" crlf)
    (close ?fichero)  
)

(defrule tipo_contrato6 "Comprobamos si le interesa trabajo a jornada completa"
    ; SI (estado_actual=contrato & tipo_contrato=jornada & tiempo_libre>6)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "contrato") (tipo_contrato "jornada") (tiempo_libre ?tiempo_libre &:(> ?tiempo_libre 6))(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Le interesaría un contrato a jornada completa, normalmente unas 8 horas diarias.
        
     	" crlf)
    (close ?fichero)  
)

(defrule tipo_contrato7 "Comprobamos si le interesa trabajo de beca pero tambien puede ser jornada"
    ; SI (estado_actual=contrato & tipo_contrato=beca & tiempo_libre>6)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "contrato") (tipo_contrato "beca") (tiempo_libre ?tiempo_libre &:(> ?tiempo_libre 6))(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 
    
    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Aunque le interesaría una beca, con el tiempo libre que dispone, puede aceptar un trabajo a media jornada.
        
     	" crlf)
    (close ?fichero)  
)



;------------------------------------------------------------------------------
; ¿Que debo destacar de mi curriculum?
;
;	DATOS ENTRADA:
;		- estado_actual = "curriculum"
;		- tipo_estudios = ["universitarios", "no_universitarios"]
;		- experiencia = ["si", "no"]
;		- numero_paginas_CV = <numero entero positivo>
;------------------------------------------------------------------------------

(defrule destacar_curriculum1 "Comprobamos si debe destacar la formacion"
    ; SI (estado_actual=curriculum & tipo_estudios=universitarios & destacar_formacion=no)
    ;	ENTONCES destacar_formacion=si
	?dat <- (datos (estado_actual "curriculum") (tipo_estudios "universitarios")(destacar_formacion "no"))
	=> 
    
    ;cumple los requisitos para ser apto para la beca (destacar_formacion=si)
    (modify ?dat (destacar_formacion "si"))
)

(defrule destacar_curriculum2 "Comprobamos si debe destacar la experiencia"
    ; SI (estado_actual=curriculum & experiencia=si & destacar_experiencia=no)
    ;	ENTONCES destacar_experiencia=si
	?dat <- (datos (estado_actual "curriculum") (experiencia "si")(destacar_experiencia "no"))
	=> 
    
    ;cumple los requisitos para ser apto para la beca (destacar_experiencia=si)
    (modify ?dat (destacar_experiencia "si"))
)

(defrule destacar_curriculum3 "Comprobamos si debe reducir el numero de paginas del curriculum"
    ; SI (estado_actual=curriculum & numero_paginas_CV>=3)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "curriculum")  (numero_paginas_CV ?numero_paginas_CV &:(>= ?numero_paginas_CV 3))(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Debe de reducir el número de páginas de tu CV. Para ello, destaca tus habilidades interesantes para 
        la oferta deseada en una página y elimina las que no sean necesarias para el puesto solicitado. 
        Recuerda que la persona que revisa los currículum tiene 15 segundos para decidir seleccionarte.
        
     	" crlf)
    (close ?fichero) 
)

(defrule destacar_curriculum4 "Comprobamos si debe mostrar el consejo para destacar su formacion"
    ; SI (estado_actual=curriculum & destacar_formacion=si)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "curriculum")  (destacar_formacion "si")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Debería destacar su formación indique claramente los cursos, estudios, seminarios así como la titulación que posee. Si su 
        calificación es superior a 2.55 destáquela, si le ha sido otorgada una mención, póngala en negrita y subrayada, igualmente 
        indique su calificación indicando su calificación y el máximo que puede ser, por ejemplo: 2.55 sobre 4
     	
        " crlf)
    (close ?fichero) 
)

(defrule destacar_curriculum5 "Comprobamos si debe mostrar el consejo para destacar su experiencia"
    ; SI (estado_actual=curriculum & destacar_formacion=si)
    ;	ENTONCES mostrar consejo
	?dat <- (datos (estado_actual "curriculum")  (destacar_experiencia "si")(ruta_fichero_salida ?ruta)(fichero_salida ?fichero))
	=> 

    ;agregamos los resultados al fichero deseado (abrimos fichero, añadimos y lo cerramos)
    (open ?ruta ?fichero "a")
    (printout ?fichero " 
        Destaque los trabajos que ha realizado, cual ha sido su función, el cargo y el tiempo que ha desempeñado, haga hincapié 
        en aquellos trabajos que le han sido más fructíferos y haga que se note estos por encima, póngalos en cursiva o subráyelos.
        
     	" crlf)
    (close ?fichero) 
)