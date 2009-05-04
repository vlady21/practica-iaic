;; PRÁCTICA 2 -IAIC-
;;
;; Reglas y hechos iniciales correspondientes a un
;; asesor laboral con enfoque técnico.
;;
;; Autores: Iñaki Goffard Giménez, Daniel Martín Carabias, Raúl Requero García
;;
;; Curso: 4ºB
;;
;; Para rellenar la base de hechos inicial (BH_0), el usuario deberá responder a ciertas
;; preguntas realizadas desde la interfaz gráfica.
;;
;;  1. Nivel de estudios.
;;  2. Experiencia laboral en años.
;;  3. Tiempo que lleva desempleado en meses.
;;  4. Edad del usuario en años.
;;  5. Sexo del usuario.
;;  6. Currículum disponible.
;;  7. Idiomas extranjeros que habla.
;;  8. Intereses del usuario.
;;  9. Tiene coche o no.
;; 10. Tiene carnet de conducir o no.
;; 11. Pretensiones salariales en euros.
;; 12. Meses desde que realizó el último curso.
;; 13. Última profesión que ha tenido el usuario.
;; 14. Tiempo que llevaba trabajando.
;; 15. Tiempo que lleva desempleado.
;; 16. Años de experiencia en el extranjero.
;; 17. Fue aceptado o rechazado en su última entrevista.
;;

;; Instrucciones de ejecución:
;;
;; 1. Almacenar el fichero Asesor.clp en la carpeta \bin de Jess.
;; 2. Cargar el fichero CLP con el comando (batch Asesor.clp).
;; 3. Cargar la base de hechos iniciales (BH_0) con el comando (reset).
;; 4. Ejecutar el motor de inferencia con el comando (run).
;; 5. Se observarán los consejos obtenidos según se ejecutan las reglas.


;; Base de hechos inicial (preguntas respondidas por el usuario)

(deffacts informacionInicial
	"Datos proporcionados por el usuario"
	
	(estudios Jose universitarios)
	(experiencia Jose 2)
	(tiempo-desempleado Jose 4)
	(edad Jose 21)
	(sexo Jose masculino)
	(tiene-curriculum Jose)
	(idioma Jose ingles)
        (idioma Jose frances)
	(interes Jose informatica)
	(tiene-coche Jose)
	(carnet-conducir Jose)
	(pretensiones-salariales Jose 1000)
	(meses-desde-ultimo-curso Jose 12)
	(ultimo-trabajo Jose programador) 
	(tiempo-trabajando Jose 13)
	(tiempo-desempleado Jose 1)
	(trabajo-no-especializado Jose)
	(experiencia-extranjero Jose 2)
	(ultima-entrevista Jose rechazado)
	
)

;; Reglas


;; Si el usuario es joven y tiene estudios universitarios, da la impresión de tener una gran capacidad para aprender

(defrule capacidadAprender
	"El usuario debe potenciar el hecho de que está muy capacitado para aprender y adaptarse"

	(edad ?usuario ?edad)
	(test (< ?edad 24))
	(estudios ?usuario universitarios)
	=>
	(assert (cap-aprender ?usuario))
	(printout t "- Potencie su capacidad para aprender" crlf)
)

;; Existen las denominadas exposiciones de empleo. Se trata de un evento presencial o virtual en el que empresarios
;; usan su tiempo para descubrir nuevos talentos. Se recomienda asistir solo como visitante si el candidato no tiene
;; mucha experiencia y además hace poco que se ha quedado desempleado.

(defrule asistirExpoVisitante
	"Asiste a una exposición de empleo pero como visitante"

	(tiene-curriculum ?usuario)
	(tiempo-desempleado ?usuario ?tiempo)
	(test (< ?tiempo 5))
        (experiencia ?usuario ?experiencia)
	(test (< ?experiencia 2))
	=>
	(assert (asiste-Expo-visitante ?usuario))
	(printout t "- Seria recomendable que acudiera a una exposicion de empleo como visitante" crlf)
)

;; Si en cambio el usuario dispone de una mínima experiencia laboral, es recomendable que asista y deje su currículum

(defrule asistirExpoActivamente
	"Asiste a una exposición de empleo, y deja su currículum"

	(tiene-curriculum ?usuario)
	(experiencia ?usuario ?experiencia)
	(test (>= ?experiencia 2))
	=>
	(assert (asiste-Expo-visitante ?usuario))
	(printout t "- Seria recomendable que acudiera a una exposicion de empleo y dejara su curriculum" crlf)
)

;; Si una persona llevaba mucho tiempo trabajando y se ha quedado recientemente en paro, es recomendable que reelabore
;; su currículum, para así poner de manifiesto todo lo aprendido durante esos años.

(defrule reelaborarCurriculum
	"Aconseja reelaborar el currículum del candidato"

	(tiempo-trabajando ?usuario ?trabajando)
	(test (> ?trabajando 10))
	(tiempo-desempleado ?usuario ?desempleado)
	(test (< ?desempleado 4))
	=>
	(assert (reelabora-curriculum ?usuario))
	(printout t "- Le aconsejo que reelabore su curriculum" crlf)
)

;; Si una persona llevaba mucho tiempo trabajando y se ha quedado recientemente en paro, también va a tener que renovar
;; sus conocimientos para adaptarse al mercado. En el caso de que no tenga familia se le puede aconsejar cambiar
;; de ciudad.

(defrule cambiarCiudad
	"Aconseja al candidato probar a encontrar empleo en otra ciudad"
	
	(reelabora-curriculum? usuario)
	(not (con-familia ?usuario))
	=>
	(assert (cambia-ciudad ?usuario))
	(printout t "- Considere la posibilidad de cambiar de ciudad" crlf)
)

;; Si es un primer empleo, es recomendable que las pretensiones económicas no sean elevadas (menores a 1200 euros), excepto si
;; el candidato sabe otro idioma extranjero (francés).

(defrule rebajarSalario
	"Aconseja al candidato a reducir sus pretensiones económicas"

	(pretensiones-salariales ?usuario ?salario)
	(test (> ?salario 1200))
	(experiencia ?usuario ?experiencia)
	(test (< ?experiencia 2))
	(not (idioma ?usuario frances))
	=>
	(assert (rebaja-salario ?usuario))
	(printout t "- Seria recomendable que rebajara sus pretensiones salariales" crlf)
)

;; Si el candidato tiene familia, es más recomendable que haga cursos para renovar sus conocimientos, especialmente si no dispone de
;; estudios universitarios

(defrule hacerCursos
	"El usuario debe hacer algunos cursos relacionados con sus intereses para renovarse"

	(not (estudios ?usuario universitarios))
	(con-familia ?usuario)
	(reelabora-curriculum ?usuario)
	=>
	(assert (hacer-cursos ?usuario))
	(printout t "- Es recomendable que haga un curso relacionado con sus intereses" crlf)
)

;; Si se le ha recomendado hacer cursos, y no tiene idiomas, sería interesante que hiciera un curso de idiomas primero.

(defrule hacerCursosIdiomas
	"El usuario debería aprender idiomas para volver a ser competitivo en el mundo laboral"

	(hacer-cursos ?usuario)
	(not (idioma ?usuario ingles))
	=>
	(assert (hacer-curso-ingles ?usuario))
	(printout t "- Es recomendable que haga un curso de idiomas" crlf)
)


;; Si se el usuario tiene estudios universitarios y ya tiene idiomas entre ellos el ingles ponerse en contacto con su universidad para buscar
;; trabajo a partir de ella


(defrule ponerseContactoUniversidad
	"El usuario deberia ponerse en contacto con su universidad para buscar trabajo a partir de ella"

	(estudios ?usuario universitarios)
	(idioma ?usuario ingles)
	=> 
	(assert (ponerseContactoUniversidad ?usuario))
	(printout t "- Pongase en contacto con su universidad para tratar de buscar trabajo a partir de ella" crlf)
)

;;Si el usuario tiene tiene capacidad de aprender y sus aspiraciones son la informatica y el ultimo curso hace mas de 6 meses hacerse cursos de 
;;informatica para ampliar sus conocimiento y estar al dia

(defrule hacer-curso-informatica
	"El usuario deberia hacer un curso de informatica ya sus aspiraciones son de trabajar de informatico, tiene capacidad de aprender y el ultimo curso 		hecho es de informatica"
	
	(intereses ?usuario informatica)
	(cap-aprender ?usuario)
	(meses-desde-ultimo-curso ?meses)
	(test (> ?meses 6))
	=> 	
	(assert (hacer-curso-informatica ?usuario))
	(printout t "- Hagase un curso de informatica para actualizar sus conocimientos en la materia" crlf)
)

;; Si se el usuario tiene estudios de modulo y ya tiene idiomas entre ellos el ingles ponerse en contacto con su universidad para buscar
;; trabajo a partir de ella

(defrule ponerseContactoInstituto
	"El usuario deberia ponerse en contacto con su instituto para buscar trabajo a partir de ella"

	(estudios ?usuario modulo)
	(idioma ?usuario ingles)
	=> 
	(assert (ponerseContactoInstituo ?usuario))
	(printout t "- Pongase en contacto con su instituto para tratar de buscar trabajo a partir de ella" crlf)
)

;; Si el usuario tiene carnet de conducir, tiene coche y no tiene familia que incluya en su curriculum la posibilidad de trasladarse

(defrule posibilidadTrasladarse
	"El usuario deberia incluir en su curriculum la posibilidad de trasladarse si tiene carnet de conducir, tiene coche y no tiene familia"
	
	(carnet-conducir ?usuario)
	(tiene-coche ?usuario)
	(not(con-familia ?usuario))
	=>
	(assert (posibilidad-trasladarse ?usuario))
	(printout t "- Introduce en tu curriculum la posibilidad de trasladarte para mejorar tus posibilidades" crlf)
)



;;Si el usuario no ha tenido trabajo y ha hecho alguna entrevista y ha sido rechazado en la ultima entrevista que haga algun curso para mejorar 
;; la forma de hacer las entrevistas

(defrule curso-mejorar-entrevista
	"El usuario deberia hacer un curso para mejorar la forma de hacer sus entrevistas si ha no ha tenenido trabajo todavia y ha hecho entrevistas en las 		que has sido rechazado"

	(not(ultimo-trabajo ?usuario ?trabajo))
	(ultima-entrevista ?usuario rechazado)

	=>
	(assert (curso-mejorar-entrevista ?usuario))
	(printout t "- Hazte un curso para aprender ha hacer entrevistas para que cuando tengas que hacer una estes preparado" crlf)
)

;; Si el usuario ha trabajado y el total de tiempo que ha trabajado es mayor a 12 meses que pida el paro

(defrule pedir-subvencion-paro
	"El usuario puede solicitar el paro si cumple la condicion de haber trabajado mas de 12 meses"
	(ultimo-trabajo ?usuario ?trabajo)
	(tiempo-trabajando ?usuario ?meses)
	(test (> ?meses 12))
	=>
	(assert (pedir-subvencion-paro ?usuario))
	(printout t "- Para conseguir algo de dinero mientras buscas trabajo puedes pedir la subvencion que da el inem por desempleo" crlf)
)
;; Si el usuario no se ha apuntado a los principales lugares de busqueda de trabajoo que se apunte al inem, ett, paginas de busqueda de trabajo por internet

(defrule apuntarse-busqueda-trabajo
	"Si el usuario no se ha apuntado en las principales sitios para buscar trabajo que se apunte"
	(tiene-curriculum ?usuario)
	(not(apuntado-inem ?usuario))
	(not(apuntado-ett ?usuario))
	(not(apuntado-paginas-trabajo ?usuario))
	=>
	(assert (apuntado-inem ?usuario))
	(assert (apuntado-ett ?usuario))
	(assert (apuntado-paginas-trabajo ?usuario))
	(printout t "- Para mejorar tus posibilidades de buscar trabajo apuntate a las principales fuentes de ofertas de empleo" crlf)
)


;;Si el usuario busca un trabajo no altamente especializado, que repase sus conocimientos generales sobre la actualidad. Se está poniendo de moda que muchas entrevistas de trabajo deriven hacia conversaciones sobre últimas noticias, libros o películas de moda que el usuario conozca,etc...

(defrule cultura-general
	"Si el trabajo no es altamente especializado y el usuario es muy joven, repasar sus conocimientos de cultura general y actualidad"
	(trabajo-no-especializado ?usuario)
	(edad ?usuario ?edad)
	(test (< ?edad 21))
	=>
	(assert (cultura-general-preparada ?usuario))
	(printout t "- Preparese para una posible entrevista sobre temas de cultura general y de actualidad" crlf)
)

;;Si el usuario ha fracasado varias ocasiones y ya ha hecho algún curso para mejorar las tácticas a la hora de realizar una entrevista, puede intentar informarse previamente de qué perfiles buscan y a qué se dedican exactamente las próximas empresas con las que contacte, para así dar una mejor impresión

(defrule estudiar-empresas-objetivo
	"EL usuario puede estudiar previamente los perfiles de las empresas, para dar mejor impresión en las entrevistas"
	(tiene-curriculum ?usuario)
	(ultima-entrevista ?usuario rechazado)
	(curso-mejorar-entrevista ?usuario)
	=>
	(assert (estudiada-empresa-objetivo ?usuario))
	(printout t "- El usuario puede estudiar previamente el perfil de los empleados y a que se dedica la empresa, para causar mejor impresion en la entrevista" crlf)
)

;;Si el usuario tiene experiencia laboral y ha reelaborado su currículum ,puede solicitar cartas de recomendación de sus anteriores empleadores

(defrule solicitar-recomendacion
	"El usuario puede solicitar recomendaciones de sus anteriores trabajos"
	(experiencia ?usuario ?experiencia)
	(reelabora-curriculum ?usuario)
	=>
	(assert (pedir-recomendacion ?usuario))
	(printout t "- Para aumentar la buena imagen frente a un nuevo empleo, solicita una recomendacion" crlf)

) 

;;Si el usuario ha elaborado un currículum, es muy útil que lo acompañe con una carta de presentación

(defrule carta-presentacion
	"Es muy útil acompañar el currículum con una carta de presentación"
	(tiene-curriculum ?usuario)
	=>
	(assert (adjuntar-carta-presentacion ?usuario))
	(printout t "- Es muy util adjuntar al curriculum, una carta de presentacion" crlf)
)

;;Si el usuario tiene experiencia en viajes, ha vivido en el extranjero más de un año, etc... es muy útil que amplie el currículum incluyendo estos puntos

(defrule ampliar-curriculum
	"Incluir experiencias personales óptimas para el trabajo buscado"
	(tiene-curriculum ?usuario)
	(experiencia-extranjero ?usuario ?experiencia)
	(test (> ?experiencia 1))
	=>
	(assert (ampliar-curriculum-usuario ?usuario))
	(printout t "- Si se tiene experiencia en el extranjero superior a un anio, es muy util incluirla en el curriculum" crlf)
)


;;Si el usuario ha estudiado una empresa objetivo, es útil que estudie además la demanda actual del mercado laboral en ese sector
(defrule estudiar-mercado-laboral
	"Estudiar las demandas del mercado laboral"
	(estudiada-empresa-objetivo ?usuario)
	(curso-mejorar-entrevista ?usuario)
	=>
	(assert (estudiar-mercado-laboral-usuario ?usuario))
	(printout t "- Ademas de estudiar a una empresa objetivo, es util estar informado de la situacion actual del mercado laboral en ese sector" crlf)
)

;;Si el usuario tiene capacidad de aprender, es muy útil que realice algún curso de postgrado

(defrule curso-postgrado
	"Realizar algún máster, etc..."
	(cap-aprender ?usuario)
	(hacer-cursos ?usuario)
	=>
	(assert (curso-postgrado-usuario ?usuario))
	(printout t "- Si el usuario tiene capacidad de aprender, deberia seguir realizando algún curso de postgrado" crlf)
)

