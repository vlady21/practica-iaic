; -*- Sistema experto de un Asesor Laboral Afectivo/Coatching -*-

;Descripcion: Se trata de un asesor laboral en el aspecto afectivo, da consejos y anima cuando uno 
;ha sido despedido y busca la ayuda de un asesor para ver en que estado se encuentra laboralmente.


;Contesta una serie de preguntas como por ejemplo:

;Si tengo X años, X sexo, estoy despedido (o sin trabajao en principio) y tengo ciertas caracteristicas
;como la formacion (alta, media, baja) o la experiencia responde en funcion de esos datos. Luego en 
;base de estas primeras conclusiones y en base a mas cosas como el tiempo que llevaba o llevo contratado
;y el tiempo que llevo despedido (en caso de estarlo) genera otra respuesta, consejo y así hasta otra
;mas.

;Ademas de las preguntas sobre despidos o sin trabajo, tambien hace algo sobre la imagen que se requiere
;para obtener un cierto trabajo, por ejemplo, si se tiene X presencia e Y inteligencia eso genera una
;conslucion sobre la posibilidad para gustar en la entrevista y esto a su vez, es usado en funcion
;de tambien mas caracteristicas como el liderazgo y el puesto al que quiere acceder, dando a su vez
;otra nueva conclusion.


;Uso de estas reglas:
;Para su uso y representacion he usado varias plantillas con atributos que representan las cosas
;sobre las que se van a tomar decisiones.

;Los nombres de las reglas he intentado que fuera lo mas explicativas posibles, asi que eso, se
;ha intenado :).

;Autor:   Iván Munsuri Ibáñez

;(printout t "all" crlf)
;(watch all)

;; ------------------------------------------------------------
;; DEFTEMPLATES
;; ------------------------------------------------------------

;(printout t "Definicion de templates" crlf)

;(printout t "Template persona" crlf)
(deftemplate persona
        "Una persona basica."
        (slot edad (type INTEGER)) ;en anios
        (slot sexo)        
)

;(printout t "Template atributosPersona <- persona" crlf)
;atributos laborables de una persona
(deftemplate atributosPersona extends persona
        (slot experiencia (type INTEGER)) ; en meses
        (slot formacion) ;alta, media, baja
        ;(slot formacion (default white))
)

;(printout t "Template estadoPersona <- persona" crlf)
;atributos laborables de una persona
(deftemplate estadoPersona extends atributosPersona
        ;slot estado o motivoFracaso, no se q es mas descriptivo
        (slot motivoFracaso) ;despedido, imagen ,  etc.. (no incorporados - no-despedido, entrevista)
)

;(printout t "Template atributosDespido <- estadoPersona" crlf)
;atributos que condicionan o matizan el despido
(deftemplate atributosDespido extends estadoPersona
        (slot tiempoLlevabaContratado (type INTEGER)) ; en meses
        ;tiempoDesdeDespido, tiempoSinTrabajo
        (slot tiempoDespedido (type INTEGER)) ; en meses
        (slot cantidadDespidos (type INTEGER))
        (slot puesto) ;alto, medio, bajo - Segun sueldo/categoria
        (multislot razonesDespido) ;TODO
        ;(slot formacion (default white))
)

;(printout t "Template atributosDespido <- estadoPersona" crlf)
;atributos que condicionan o matizan el despido
(deftemplate atributosImagen extends estadoPersona
        (slot presencia ) ; buena, regular, mala
        ;tiempoDesdeDespido, tiempoSinTrabajo
        (slot inteligencia ) ;alta, media, baja
        (slot liderazgo ) ;alto, medio, bajo
        (slot puesto) ;alto, medio, bajo - Segun sueldo/categoria
)

;(printout t "Template atributosSinDespido <- atributosPersona" crlf)
;Representa el posible fracaso laboral, sin ser un despido
(deftemplate atributosSinDespido extends estadoPersona
        (slot bajadaSueldo ) ;si, no
        (slot descensoPuesto ) ;si, no
        (slot ascensoPuesto ) ;si, no
        ;representa lo buen profesionales qsomos en desempeniando ese puesto
        ;(slot calidadProfesional ) ;buena, regular, mala
        ;(multislot razonesFracaso) razones por las q es un fracaso laboral
        ;(slot formacion (default white))
)



;; ------------------------------------------------------------
;; Deffunctions
;; ------------------------------------------------------------

;(printout t "Definicion de funciones" crlf)

;
;(deffunction consejoEdad1 () (
;printout t "No te preocupes tio, eres un joven crack" crlf)
;(if (> ?a ?b) then
;        (return ?a)
;    else
;        (return ?b))

;)

;(deffunction consejoEdad2 () (
;printout t "No te preocupes tio, eres un viejo crack" crlf)
;(if (> ?a ?b) then
;        (return ?a)
;    else
;        (return ?b))

;)


;; ------------------------------------------------------------
;; Deffrules ( normales, block? )
;; ------------------------------------------------------------

;(printout t "Definicion de reglas" crlf)


(defrule despidoExperienciaFormacionAlta
      (or ( atributosDespido (motivoFracaso despedido) ( experiencia alta ) ( formacion alta ) )
          ( atributosDespido (motivoFracaso despedido) ( experiencia alta ) ( formacion media ) )
          ( atributosDespido (motivoFracaso despedido) ( experiencia media ) ( formacion alta ) )
      )
      =>
      (printout t "Tienes muy buenas posibilidades de encontrar trabajo" crlf)
      (assert (posibilidadEncontrarTrabajo alta))
)

(defrule despidoExperienciaFormacionMedia
      (or ( atributosDespido (motivoFracaso despedido) ( experiencia media ) ( formacion media ) )
          ( atributosDespido (motivoFracaso despedido) ( experiencia alta ) ( formacion baja ) )
          ( atributosDespido (motivoFracaso despedido) ( experiencia baja ) ( formacion alta ) )
      )
      =>
      (printout t "Aunque todo es mejorable, tienes posibilidades para encontrar trabajar" crlf)
      (assert (posibilidadEncontrarTrabajo media))
)

(defrule despidoExperienciaFormacionBaja
      ( atributosDespido (motivoFracaso despedido) ( experiencia baja ) ( formacion baja ) )
      ( atributosDespido (motivoFracaso despedido) ( experiencia media ) ( formacion baja ) )
      ( atributosDespido (motivoFracaso despedido) ( experiencia baja ) ( formacion media ) )
      =>
      (printout t "No tienes un buen panorama para encontrar trabajo, deberias aumentar tu formacion u optar a trabajos de menor 'standing'" crlf)
      (assert (posibilidadEncontrarTrabajo baja))
)

(defrule despidoTiemposAlta
      (posibilidadEncontrarTrabajo alta)
      (atributosDespido (tiempoLlevabaContratado ?x) (tiempoDespedido ?y) )
      =>
      (if ( and (> ?x 60 ) (<= ?y 5) ) then ;Si ha trabajado mas de 60 meses( 5 años ) y lleva despedido menos o igual q 5 meses
        (printout t "Llevas poco tiempo desempleado y con el tiempo que llevabas trabajando, esto son como unas vacaciones" crlf)
        (assert (relacionTiemposContratadoDespido buena) )
       else
         (if ( and (>= ?x 6 ) (<= ?y 13) ) then ;Si ha trabajado mas de 6 meses pero menos de 60 y lleva desempleado menos o igual q 13 meses
           (printout t "Seguro que no tardas mucho mas en encontrar trabajo" crlf)
           (assert (relacionTiemposContratadoDespido regular))
          else 
            ;Si llevaba menos de 6 meses contratado y mas de 13 meses sin trabajo... 
            ;(Si lleva mas de 14 meses sin curro da igual el tiempoLlevContr)
            (printout t "No te preocupes demasiado, son malos tiempos, ya mejorara tu situacion" crlf)
            (assert (relacionTiemposContratadoDespido mala))
         )
      )
)

(defrule despidoTiemposMedia
      (posibilidadEncontrarTrabajo media)
      (atributosDespido (tiempoLlevabaContratado ?x) (tiempoDespedido ?y) )
      =>
      (if ( and (> ?x 60 ) (<= ?y 5) ) then ;Si ha trabajado mas de 60 meses( 5 años ) y lleva despedido menos o igual q 5 meses
        (printout t "Llevas poco tiempo desempleado y este paron en tu experiencia puede requerir algo mas de formacion" crlf)
        (assert (relacionTiemposContratadoDespido buena) )
       else
         (if ( and (>= ?x 6 ) (<= ?y 13) ) then ;Si ha trabajado mas de 6 meses pero menos de 60 y lleva desempleado menos o igual q 13 meses
           (printout t "No deberias tardar mucho mas en encontrar trabajo, pero deberias pensar en aumentar tu formacion o experiencia en otros trabajos" crlf)
           (assert (relacionTiemposContratadoDespido regular))
          else 
            ;Si llevaba menos de 6 meses contratado y mas de 13 meses sin trabajo... 
            ;(Si lleva mas de 14 meses sin curro da igual el tiempoLlevContr)
            (printout t "Tal vez sea un buen momento para aumentar tu formacion antes que la experiencia" crlf)
            (assert (relacionTiemposContratadoDespido mala))
         )
      )
)

(defrule despidoTiemposBaja
      (posibilidadEncontrarTrabajo baja)
      (atributosDespido (tiempoLlevabaContratado ?x) (tiempoDespedido ?y) )
      =>
      (if ( and (> ?x 60 ) (<= ?y 5) ) then ;Si ha trabajado mas de 60 meses( 5 años ) y lleva despedido menos o igual q 5 meses
        (printout t "Llevas poco tiempo desempleado, pero para poder encontrar trabajo mas facilmente debes aumentar tu formacion y experiencia" crlf)
        (assert (relacionTiemposContratadoDespido buena) )
       else
         (if ( and (>= ?x 6 ) (<= ?y 13) ) then ;Si ha trabajado mas de 6 meses pero menos de 60 y lleva desempleado menos o igual q 13 meses
           (printout t "Es importante que te concentres en tu formacion ya que esto puede limitar el crecer en experiencia" crlf)
           (assert (relacionTiemposContratadoDespido regular))
          else 
            ;Si llevaba menos de 6 meses contratado y mas de 13 meses sin trabajo... 
            ;(Si lleva mas de 14 meses sin curro da igual el tiempoLlevContr)
            (printout t "Tu mejor opcion es dedicarte por completo a aumentar tu formacion" crlf)
            (assert (relacionTiemposContratadoDespido mala))
         )
      )
)

(defrule despidoCalidadAltaBuena ;como es una encadenacion necesita de otra cond mas para tener sentido
      (posibilidadEncontrarTrabajo alta) (relacionTiemposContratadoDespido buena)
      (atributosDespido (puesto ?y) (cantidadDespidos ?x) )
      =>
      ;(printout t ?y crlf)
      ;(if ( and ( <= ?x 5 ) ( eq ?y alto ) ) then
      (if ( <= ?x 5 ) then
        (printout t "Eres un buen profesional en tu campo, tal vez deberias aspirar a mejores puestos" crlf)
        (assert (calidadProfesional buena))
       else
        (if ( <= ?x 10 ) then
          ;(printout t "Todo es mejorable, pero de momento tu puesto parece ajustarse a ti" crlf)
          (assert (calidadProfesional regular))
          (if ( eq ?y alto ) then
             (printout t "Parece que tu puesto se ajusta a ti, pero incluso podrias probar con un puesto menos exigente" crlf)
           else ;si es medio o bajo
             (printout t "Todo es mejorable, pero de momento tu puesto parece ajustarse a ti" crlf)
          )
         else
          (printout t "Tal vez no has encontrado todavia tu puesto o trabajo adecuado" crlf)
          (assert (calidadProfesional mala))
        )
      )
)

(defrule despidoCalidadAltaRegular
      (posibilidadEncontrarTrabajo alta) (relacionTiemposContratadoDespido regular)
      (atributosDespido (puesto ?y) (cantidadDespidos ?x) )
      =>
      ;(printout t ?y crlf)
      ;(if ( and ( <= ?x 5 ) ( eq ?y alto ) ) then
      (if ( <= ?x 5 ) then
        (printout t "Pese a que no llevas mucho tiempo trabajando, parece que te desenvuelves bien" crlf)
        (assert (calidadProfesional buena))
       else
        (if ( <= ?x 10 ) then
          ;(printout t "Todo es mejorable, pero de momento tu puesto parece ajustarse a ti" crlf)
          (assert (calidadProfesional regular))
          (if ( eq ?y alto ) then
             (printout t "Parece que poco a poco vas encajando, aunque igual deberias buscar algo menos exigente" crlf)
           else ;si es medio o bajo
             (printout t "Date tiempo para encontrar tu sitio, no se encuentra a la primera" crlf)
          )
         else
          (printout t "Debes buscar otro tipo de trabajos mas afines a ti" crlf)
          (assert (calidadProfesional mala))
        )
      )
)

(defrule despidoCalidadAltaMala
      (posibilidadEncontrarTrabajo alta) (relacionTiemposContratadoDespido mala)
      (atributosDespido (puesto ?y) (cantidadDespidos ?x) )
      =>
      ;(printout t ?y crlf)
      ;(if ( and ( <= ?x 5 ) ( eq ?y alto ) ) then
      (if ( <= ?x 5 ) then
        (printout t "Eres un diamente en bruto, todavia tienes que demostrar lo que vales" crlf)
        (assert (calidadProfesional buena))
       else
        (if ( <= ?x 10 ) then ; ver mejor que decir, aunque puede valer
          ;(printout t "Todo es mejorable, pero de momento tu puesto parece ajustarse a ti" crlf)
          (assert (calidadProfesional regular))
          (if ( eq ?y alto ) then
             (printout t "Algunos trabajos de periodos cortos en puestos altos no sacan lo mejor de nosotros" crlf)
           else ;si es medio o bajo
             (printout t "Sopesa las razones de la corta duracion de tu trabajo, tal vez necesites mayor responsabilidad" crlf)
          )
         else
          (printout t "Tienes grandes posibilidades, pero tal vez tu personalidad choque con el trabajo" crlf)
          (assert (calidadProfesional mala))
        )
      )
)

(defrule despidoCalidadMediaBuenaORegular
      (posibilidadEncontrarTrabajo media) (relacionTiemposContratadoDespido buena|regular)
      (atributosDespido (puesto ?y) (cantidadDespidos ?x) )
      =>
      ;(printout t ?y crlf)
      ;(if ( and ( <= ?x 5 ) ( eq ?y alto ) ) then
      (if ( <= ?x 5 ) then
        (printout t "Has hecho bien tu trabajo, no tendras mucho problema en encontrar trabajo" crlf)
        (assert (calidadProfesional buena))
       else
        (if ( <= ?x 10 ) then ; ver mejor que decir, aunque puede valer
          (printout t "Debes detectar los motivos o causas por las que encuentras obstaculos en el trabajo (ej: mucha presion)" crlf)
          (assert (calidadProfesional regular))          
         else
          (printout t "Analiza tus trabajos anteriores, el ultimo parece que te fue y/o trabajaste bien" crlf)
          (assert (calidadProfesional mala))
        )
      )
)

(defrule despidoCalidadMediaMala
      (posibilidadEncontrarTrabajo media) (relacionTiemposContratadoDespido mala)
      (atributosDespido (puesto ?y) (cantidadDespidos ?x) )
      =>
      ;(printout t ?y crlf)
      ;(if ( and ( <= ?x 5 ) ( eq ?y alto ) ) then
      (if ( <= ?x 5 ) then
        (printout t "Si es de tus primeros trabajos no desesperes, sino es asi, pronto encontraras uno mas adecuado a ti" crlf)
        (assert (calidadProfesional buena))
       else
        (if ( <= ?x 10 ) then ; ver mejor que decir, aunque puede valer
          (printout t "Has pasado seguramente por ciertos trabajos y hay de todo, llegara el trabajo adecuado" crlf)
          (assert (calidadProfesional regular))
         else
          (printout t "Puede que sea buen momento para mejorar tu formacion o la experiencia en otro tipo de trabajos" crlf)
          (assert (calidadProfesional mala))
        )
      )
)

(defrule despidoCalidadMala ; solo hay una posibilidad de calidad, independientemente de los despedidos o cualquier otra cosa
      (posibilidadEncontrarTrabajo mala) (relacionTiemposContratadoDespido mala)
      ;(atributosDespido (puesto ?y) (cantidadDespidos ?x) )
      =>
      ;(printout t ?y crlf)
      ;(if ( and ( <= ?x 5 ) ( eq ?y alto ) ) then
      (printout t "Debes replantearte tus opciones, siendo necesario una formacion para tener donde elegir" crlf)
      (assert (calidadProfesional mala))
)

(defrule despidoConsejoEdadAltaBuena
      (persona (edad ?x ) )
      (posibilidadEncontrarTrabajo alta) (relacionTiemposContratadoDespido buena) 
                        (calidadProfesional buena|regular)
      =>
      ;Apartir de 38 considero una persona "no-joven"
      (if (< ?x 38) then
        (printout t "Eres una persona joven con un futuro prometedor, no te faltaran las oportunidades" crlf)
      else
        (printout t "Pese a tener ya una cierta edad, tienes muchas posibilidades laborales" crlf)
      )
)

(defrule despidoConsejoEdadAltaRegularOMala  ; Verrrrrrrrrrrrr
      (persona (edad ?x ) )
      (posibilidadEncontrarTrabajo alta) (relacionTiemposContratadoDespido regular|mala) 
                        (calidadProfesional buena|regular)
      =>
      ;Apartir de 38 considero una persona "no-joven"
      (if (< ?x 38) then
        (printout t "Todavia tienes mucho tiempo para demostrar tus posibilidades y capacidades" crlf)
      else
        (printout t "Tal vez no te han dado posibilidades de mostrar lo que realmente vales" crlf)
      )
)

(defrule despidoConsejoEdadMediaBuena
      (persona (edad ?x ) )
      (posibilidadEncontrarTrabajo media) (relacionTiemposContratadoDespido buena)
                        (calidadProfesional buena|regular)
      =>
      ;Apartir de 38 considero una persona "no-joven"
      (if (< ?x 38) then
        (printout t "Tienes buenas posibilidades para demostrar tus destrezas" crlf)
      else
        (printout t "Nunca es tarde para lograr demostrar lo que uno vale" crlf)
      )
)

(defrule despidoConsejoEdadMediaRegularOMala
      (persona (edad ?x ) )
      (posibilidadEncontrarTrabajo media) (relacionTiemposContratadoDespido regular|mala)
                        (calidadProfesional buena|regular)
      =>
      ;Apartir de 38 considero una persona "no-joven"
      (if (< ?x 38) then
        (printout t "Vas por medio-buen camino, pero todavía tienes que pulirte laboralmente" crlf)
      else
        (printout t "No dejes para mañana lo que puedas hacer hoy, tienes que poner mas empeño en el trabajo" crlf)
      )
)

(defrule despidoConsejoEdadMala
      (persona (edad ?x ) )
      (posibilidadEncontrarTrabajo mala) (relacionTiemposContratadoDespido regular|mala)
                        (calidadProfesional mala)
      =>
      ;Apartir de 38 considero una persona "no-joven"
      (if (< ?x 38) then
        (printout t "Todavia tienes tiempo para resarcirte, obteniendo una formacion y experiencia adecuada" crlf)
      else
        (printout t "Nunca es tarde si la dicha es buena, no se puede ir siempre a peor" crlf)
      )
)

;Algunos casos, no todos
(defrule imagen1
      (atributosImagen (motivoFracaso imagen) ( presencia buena ) (inteligencia alta|media) )
      =>
      (printout t "Una buena presencia e inteligencia son buenas cosas para convencer en una entrevista" crlf)
      (assert (posibilidadEntrevista alta))
)

(defrule imagen2
      (atributosImagen (motivoFracaso imagen) ( presencia regular ) (inteligencia alta) )
      =>
      (printout t "Tienes ciertas posibilidaes de gustar segun el tipo de entrevista" crlf)
      (assert (posibilidadEntrevista media))
)

(defrule imagen3
      (atributosImagen (motivoFracaso imagen) ( presencia mala ) (inteligencia alta|media) )
      =>
      (printout t "Para facilitarte las cosas tal vez seria bueno mejorar primero algo la presencia" crlf)
      (assert (posibilidadEntrevista baja))
)

(defrule imagen4
      (atributosImagen (motivoFracaso imagen) ( presencia buena ) (inteligencia baja) )
      =>
      (printout t "No todo es la presencia, pueden exigirte algo mas que ofrecer segun el puesto" crlf)
      (assert (posibilidadEntrevista baja))
)

(defrule imagen5
      (atributosImagen (motivoFracaso imagen) ( presencia regular ) (inteligencia media) )
      =>
      (printout t "El equilibrio de ciertas caracteristicas es un buen aliciente" crlf)
      (assert (posibilidadEntrevista media))
)

(defrule imagen1-1
      (posibilidadEntrevista alta)
      (atributosImagen (liderazgo ?x) (puesto ?y) )
      =>
      (if (eq ?x alto) then
        (if (eq ?y alto) then
          (printout t "Eres una persona con buenas dotes de mando que desempeñara bien puestos altos" crlf)
         else
          (printout t "Seguro que no te costara dirigir desde un puesto medio/bajo, aunque no seria un reto" crlf)
        )
      else
        (if (eq ?x medio) then
          (if (eq ?y alto) then
            (printout t "Tal vez no seas lo suficientemente capaz como para dirigir un puesto con mucha presion" crlf)
           else
            (printout t "Es probable que no te cueste demasiado dirigir desde esa posicion" crlf)
          )
         else
           (if (eq ?y bajo) then
              (printout t "No deberias establecerte metas a largo plazo, debes ir poco a poco" crlf)
           )
        )
      )
)

(defrule imagen1-3
      (posibilidadEntrevista media|baja)
      (atributosImagen (liderazgo ?x) (puesto ?y) )
      =>
      (if (eq ?x medio|bajo) then
        (printout t "Debes aprender todavia sobre las personas antes de querer mandar sobre ellas" crlf)
      else
        (if (eq ?y alto) then
          (printout t "Debes aprovechar tu capacidad innata, pero con mucha presion tal vez te sobrepase la situacion" crlf)
         else
          (printout t "Seguramente puedas controlar la situacion y dirigir bien el grupo de trabajo" crlf)
        )
      )
)


;; ------------------------------------------------------------
;; Initial conditions
;; ------------------------------------------------------------

;(printout t "Introduzco hechos iniciales" crlf)
;(deffacts my-facts "Buscando trabajo"
 ;       (atributosDespido (edad 53) (sexo masculino) (motivoFracaso despedido) (experiencia media) (formacion baja) 
 ;               (tiempoLlevabaContratado 1) (tiempoDespedido 156) (cantidadDespidos 2)
 ;               (puesto alto)
 ;       )
;)

;Obviamente no todos los casos estan contemplados, pero si la mayoria. No todas encadenan 3 reglas

;Algunos ejemplos:

;De despido, encadena 3
(assert (atributosDespido (edad 53) (sexo masculino) (motivoFracaso despedido) (experiencia alta) (formacion alta) 
                (tiempoLlevabaContratado 7) (tiempoDespedido 8) (cantidadDespidos 1)
                (puesto alto)
        )
)

;Encadena 2 alta -buena - mala
;(assert (atributosDespido (edad 25) (sexo femenino) (motivoFracaso despedido) (experiencia alta) (formacion media) 
;                (tiempoLlevabaContratado 7) (tiempoDespedido 8) (cantidadDespidos 12) (puesto alto)
;        )
;)

;De despido, encadena 3
;(assert (atributosDespido (edad 23) (sexo masculino) (motivoFracaso despedido) (experiencia media) (formacion media) 
;                (tiempoLlevabaContratado 65) (tiempoDespedido 8) (cantidadDespidos 1)
;                (puesto medio)
;        )
;)


;De imagen, encadena 2
;(assert (atributosImagen (edad 43) (sexo masculino) (motivoFracaso imagen) (puesto alto) (experiencia alta) (formacion alta)
;                (presencia buena) (inteligencia alta)
;                (liderazgo alto)
;        )
;)

;De imagen, encadena 2
;(assert (atributosImagen (edad 33) (sexo femenino) (motivoFracaso imagen) (puesto medio) (experiencia alta) (formacion alta)
;                (presencia buena) (inteligencia media)
;                (liderazgo alto)
;        )
;)


;Para usar el deffacts hay que hacer un reset previo - pero si usar el assert no hace falta
;(reset)


;(printout t "Veo los hechos" crlf)
;(facts)

;Para eliminar un hecho concreto, a través de su numero identificativo
;(retract (fact-id 1))

;(printout t "Veo los hechos" crlf)
;(facts)

;(printout t "Ejecuto" crlf)
(run)
;(printout t "Veo los hechos de nuevo" crlf)
;(facts)
(reset)

