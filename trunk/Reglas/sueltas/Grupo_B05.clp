(deftemplate persona
    (slot nombre)
    (slot tiempodesocupado)
    (slot titulacion (allowed-values GradoMedio GradoSuperior))
    (slot notatitulacion)
    (slot master)
    (slot sector)
    (slot edad)
    (slot añosexperiencia)
    (multislot cursos)
)
(deftemplate curso
    (slot persona)
    (slot sector)
    (slot duracion)
    )

(deftemplate tipo-cv
    (slot persona)
	(slot concreto (allowed-values 0 1))
    (slot ambito (allowed-values Público Privado ))
    (slot sector)
   )
(deftemplate tipo-organizacion
    (slot persona)
    (slot tipo (allowed-values funcional cronológico))
    )
(deftemplate tipo-extension
  	(slot persona)
    (slot tipo (allowed-values clásica americana))  
    )
(deftemplate incluir-cursos
    (slot persona)
    (slot opcion (allowed-values 0 1))
    
    )
(deftemplate incluir-calificaciones
    (slot persona)
    (slot opcion (allowed-values 0 1))
 )


/* Un cv funcional se configura por temas. Es adecuado cuando ha habido grandes periodos de inactividad, cambios de trabajo o de rumbo, o cuando
	el cv se crea específicamente para optar un puesto de trabajo concreto*/ 
(defrule organizacion-cv-funcional 
    (or (persona (nombre ?n)(tiempodesocupado ?t&:(> ?t 24)))
     	(tipo-cv (persona ?n)(concreto 1))	   
    )    
    =>
    (assert (tipo-organizacion (persona ?n)(tipo funcional)))
 )
/* Un cv cronológico se configura de manera cronológica, primero las actividades/formación más reciente. Es adecuado en condiciones normales*/
(defrule organizacion-cv-cronologico 
    (persona (nombre ?n)(tiempodesocupado ?t&:(<= ?t 24)))
     =>
    (assert (tipo-organizacion (persona ?n)(tipo cronológico)))
 )
/* Un cv clásico tiene una extensión de varias páginas y un nivel alto de detalle. Es adecuado cuando se quiere optar a una plaza en la 
Administración Pública o se tiene un largo historial que se quiere subrayar*/
(defrule extension-cv-clasica
    (persona (nombre ?n))
    (or (tipo-cv (persona ?n)(ambito Público))
     	(and 
            (persona (nombre ?n)(añosexperiencia ?e&:(>= ?e 10)))
            (tipo-cv (persona ?n)(ambito Privado))
            )   
     )
    =>
    (assert (tipo-extension (persona ?n)(tipo clásica)))
 )
/*Un cv americano tiene una extensión de una o dos páginas y una estructura de titulares que busca llamar la atención de un vistazo, sin atender
a los detalles. Es adecuado cuando se opta a un puesto en la empresa privada y no se cuenta con una larga trayectoria*/
(defrule extension-cv-americana
    (persona (nombre ?n))
    (tipo-cv (persona ?n)(ambito Privado))
    (persona (nombre ?n)(añosexperiencia ?e&:(< ?e 10)))
    =>
    (assert (tipo-extension (persona ?n)(tipo americana)))
)
/* La nota final de la titulación sólo debe ponerse si es inusualmente alta o se trata de un cv clásico, con alto nivel de detalle*/
(defrule incluir-notastitulacion
    (or (persona (nombre ?n)(notatitulacion ?nt&:(>= ?nt 8)))
        (tipo-extension (persona ?n)(tipo clasica))
        )
    =>
    (assert (incluir-calificaciones (persona ?n)(opcion 1)))
 )
/* La formación no reglada se incluye cuando está relacionada con el trabajo al que se opta o cuando se trata de un cv clásico, donde 
los cursos pueden dar más puntos, como en el caso de las Administraciones Públicas*/
(defrule incluir-otraformacion
    (or (and
           (persona (nombre ?n)(cursos ?c))
    	   (curso (persona ?n)(sector ?s))
    	   (tipo-cv (persona ?n)(sector ?s))
         )
        (tipo-extension (persona ?n)(tipo clasica))
        )   
    =>
    (assert (incluir-cursos (persona ?n)(opcion 1)))
 )

(deftemplate entrevista
    (slot persona)
    (slot oferta)	
    (slot tipocita (allowed-values Telefónica Escrito))
    (slot tipoentrevista (allowed-values Selección Grupo Telefónica))
    (slot tipoentrevistador (allowed-values EmpresaSelección RRHH Técnico))
    )

(deftemplate oferta
    (slot empresa)
    (slot sector)
    (slot puesto (allowed-values Directivo Administrativo Técnico Gerente Creativo Público))
    (slot titulacionrequerida)
    (multislot formacionadicional)
    (slot experienciarequerida)
)
(deftemplate punto-fuerte
    (slot persona)
    (slot oferta)
    (slot punto(allowed-values Sector Titulación FormacionAdicional Edad Experiencia))
)
(deftemplate punto-debil
    (slot persona)
    (slot oferta)
    (slot punto(allowed-values Sector Titulación FormacionAdicional Edad Experiencia))
)

/* A la hora de realizar una entrevista es importante saber qué puntos fuertes se tienen para el puesto solicitado.*/

/*Una titulación o experiencia en el sector de la empresa a la que se quiere entrar es un punto fuerte a destacar en la entrevista*/
(defrule punto-fuerte-sector
    (and (oferta (sector ?s))
             (persona (nombre ?p)(sector ?s))
    )
    =>
    (assert (punto-fuerte (persona ?p)(oferta ?f)(punto Sector)))
)
/*Contar con una titulación adecuada es un punto fuerte*/
(defrule punto-fuerte-titulacion
     (and ?o <-(oferta (titulacionrequerida ?t))
             (persona (nombre ?p)(titulacion ?t))	
            )
    =>
    (assert (punto_fuerte (persona ?p)(oferta ?o)(punto Titulación)))
)
/* En ocasiones la formación no reglada puede ser un punto fuerte, si es útil para el trabajo a desempeñar*/
(defrule punto-fuerte-formacionadicional    
   (and ?o <- (oferta (formacionadicional ?f))
        (persona (nombre ?p)(cursos ?f))
    ) 
        =>
		(assert (punto-fuerte (persona ?p)(oferta ?o)(punto FormacionAdicional )))
)
/* La experiencia adquirida es un punto fuerte*/
(defrule punto-fuerte-experiencia
    (persona (nombre ?p)  (añosexperiencia ?x))
    (oferta (empresa ?e) (experienciarequerida ?x2))
    (>= ?x ?x2)
    =>
    (assert (punto-fuerte (persona ?p)(oferta ?f)(punto Experiencia)))
)
/* Cuando no se cuenta con algun requisito para el puesto de trabajo, tenemos un punto débil del que hay que ser consciente para intentar mitigar durante la entrevista*/

/*No contar con experiencia o una formación adecuada para el sector al que optamos es un punto débil*/
(defrule punto-debil-sector
    (not (punto-fuerte (persona ?p)(oferta ?f)(punto Sector)))
        =>
        (assert (punto-debil (persona ?p)(oferta ?f)(punto Sector)))
    
)
/*No contar con la titulación requerida para el puesto es un punto débil*/
(defrule punto-debil-titulacion
    (not (punto-fuerte (persona ?p)(oferta ?f)(punto Titulación)))
        =>
        (assert (punto-debil (persona ?p)(oferta ?f)(punto Titulación)))
)
/*No contar con formación adicional requerida, es un punto débil*/
(defrule punto-debil-formacionadicional
    (not (punto-fuerte (persona ?p)(oferta ?f)(punto FormacionAdicional)))
        =>
        (assert (punto-debil (persona ?p)(oferta ?f)(punto FormacionAdicional)))
    
)
/*No contar con suficiente experiencia para el puesto es un punto débil*/
(defrule punto-debil-experiencia
    (persona (nombre ?p)  (añosexperiencia ?x))
    (oferta (empresa ?e) (experienciarequerida ?x2))
    (< ?x ?x2)
    =>
    (assert (punto-debil (persona ?p)(oferta ?f)(punto Experiencia)))
)

/* Si la entrevista que vamos a realizar es de Selección, nos da un consejo apropiado*/
(defrule entrevista-seleccion
    (persona (nombre ?n))
    (entrevista (persona ?n)(tipoentrevista Selección))
    =>
    (printout "Consejo- Entrevista de selección " ?n " Prepare una entrevista prestando atención a la parte referente a su personalidad, motivaciones, competencias generales y aptitud" crlf)
    
)
/* Si la entrevista va a ser una dinámica de grupo, aconseja cómo enfrentarse a ella*/
(defrule entrevista-grupo
	(persona (nombre ?n))
    (entrevista (persona ?n)(tipoentrevista Grupo))
    =>
    (printout "Consejo- Entrevista en grupo/dinámica de grupo " ?n "Participe en la dinámica, pero sin monopolizar la atención" crlf "Escuche las opiniones y respuestas de los demás, pero no las critique" crlf "Analice a los entrevistadores y a los demás candidatos" crlf)    
)
/* Si vamos a hacer una entrevista telefónica, nos aconseja qué hacer*/
(defrule entrevista-telefono
    (persona (nombre ?n))
    (entrevista (persona ?n)(tipoentrevista Telefónica))
    =>
    (printout "Consejo- Entrevista telefónica" ?n "Intente evitar este tipo de entrevista en la medida de lo posible.Si no puede conseguir una entrevista personal, intente dar sólo la información necesaria durante la entrevista telefónica, para conseguir una entrevista cara a cara" crlf)
    )
/*Si vamos a tener una entrevista con un miembro de una empresa de selección de personal, nos aconseja cómo prepararla*/
(defrule entrevistador-empresaseleccion
     (persona (nombre ?n))
    (entrevista (persona ?n)(tipoentrevistador EmpresaSelección))
    =>
    (printout "Consejo- Entrevista realizada por empresa de selección " ?n " Prepare una entrevista prestando atención a la parte referente a su personalidad, motivaciones, competencias generales y aptitud" crlf)
    
 )
/*Si vamos a tener una entrevista con una persona de RRHH de la propia empresa, nos aconseja cómo prepararla*/
(defrule entrevistador-rrhh
    (persona (nombre ?n))
    (entrevista (persona ?n)(tipoentrevistador RRHH))
    =>
    (printout "Consejo- Entrevista realizada por RRHH" ?n "Prepare su CV para responder preguntas sobre el mismo, preste atención a la parte de personalidad, motivación y no descuide nada en general" crlf)
    )
/*Si vamos a hacer una entrevista técnica, nos aconseja cómo prepararla*/
(defrule entrevistador-tecnico
    (persona (nombre ?n))
    (entrevista (persona ?n)(tipoentrevistador Técnico))
    =>
    (printout "Consejo- Entrevista técnica" ?n "Preparese para responder preguntas acerca de su cualificación técnica enfocando hacia el trabajo a realizar" crlf "Incida en su capacidad para desempeñar el trabajo que se ofrece y en su motivación para incorporarse al departamento específico" crlf) 
    )


(assert (persona (nombre Juan)(edad 29)(titulacion GradoMedio)(añosexperiencia 2)(cursos Maya)(sector software)))
(assert (entrevista (persona Juan)(oferta Dacartec)(tipocita Telefónica)(tipoentrevista Telefónica)(tipoentrevistador Técnico)))
(assert (oferta (empresa Dacartec)(sector software)(puesto Técnico)(experienciarequerida 1)(titulacionrequerida GradoSuperior)))
	

