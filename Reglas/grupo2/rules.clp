(deftemplate persona
; slots de entrada
	(slot extranjero)
	(slot ingles)
	(slot nivelIngles)
    (slot castellano)
    (slot nivelCastellano)
	(slot numIdiomas)
	(slot nivelEstudios)
    (slot practEmp)
    (slot numCursillos)
	(slot mesesExp)
	(slot movimiento)
    (slot viajar)
    (slot jornadaCompleta)
	(slot creditosFin)
	(slot edad)
    (slot mesesFin)
    (slot area)
; slots para cálulos intermedios
    (slot conocimientos)
    (slot flexibilidad)
    (slot empleoCercaUni)
    (slot ultimoCurso)
    (slot perfilValido)
    (slot perfilValidoA)
    (slot perfilValidoB)
    (slot num)
    (slot opositar)
; slots de salida
    (slot becaColaboracionUni)
    (slot becaColaboracionDepart)
    (slot becaFaro)
    (slot becaArco)
    (slot becaIntegrants)
    (slot opositarGrupoA)
    (slot opositarGrupoB)
    (slot aprenderCastellano)
    (slot practEmpresaPrivada)
    (slot empleoSectorPrivado)
    (slot empleoPrivadoFuera)
    (slot profesorParticular)
)

(defrule reglaTotal
    (declare (salience 100))
	(initial-fact)
        =>
    ;INICIO de la petición de datos de entrada
    (printout t "     DATOS DE ENTRADA" crlf)
    (printout t "¿Es extranjero? [0: no | 1: sí]" crlf)
    (bind ?x1 (read))
    (printout t "¿Habla inglés? [0: no | 1: sí]" crlf)
    (bind ?x2 (read))
    (if (= ?x2 1) then
    	(printout t "¿Cuál es su nivel de inglés? [3: Alto | 2: Medio | 1: Bajo]" crlf)
    	(bind ?x3 (read))    	
    else
    	(bind ?x3 0)    
    )
    (if (= ?x1 1) then
    	(printout t "¿Habla castellano? [0: no | 1: sí]" crlf)
    	(bind ?x4 (read))
    	(if (= ?x4 1) then
    		(printout t "¿Cuál es su nivel de castellano? [3: Alto | 2: Medio | 1: Bajo]" crlf)
    		(bind ?x5 (read))    	
    	else
    		(bind ?x5 0)    
    	)
    else
        (bind ?x4 1)(bind ?x5 3)
    )
    (printout t "¿Cuántos idiomas habla?" crlf)
    (bind ?x6 (read))
    (printout t "¿Cuál es su nivel de estudios?" crlf)
    (printout t "[5: Doct. | 4: Licen. | 3: Dipl. | 2: FP | 1: Bach. | 0: Esc.]" crlf)
    (bind ?x7 (read))
    (printout t "¿Cuál es su área de estudios?" crlf)
    (printout t "[4: Ingeniería | 3: Económicas | 2: Humanidades | 1: Derecho | 0: Ciencia]" crlf)
    (bind ?x17 (read))
    (printout t "¿Ha realizado prácticas en empresa? [0: no | 1: sí]" crlf)
    (bind ?x8 (read))
    (if (= ?x8 1) then
    	(printout t "¿Cuál fue la duración en meses de dichas prácticas?" crlf)
    	(bind ?x9 (read))    	
    else
    	(bind ?x9 0)    
    )
    (printout t "¿Cuántos cursillos adicionales a sus estudios ha realizado?" crlf)
    (bind ?x10 (read))
    (printout t "¿A qué distancia máxima estaría dispuesto a desplazarse para trabajar? [en km]" crlf)
    (bind ?x11 (read))
    (printout t "¿Cuál es su disponabilidad para viajar? [0: nula | 1: nacional | 2: europea | 3: global]" crlf)
    (bind ?x12 (read))
    (printout t "¿Qué tipo de jornada? [0: media jornada | 1: jornada completa]" crlf)
    (bind ?x13 (read))
    (printout t "¿Cuántos créditos le quedan para finalizar sus estudios? [Si ha finalizado, introduzca 0]" crlf)
    (bind ?x14 (read))
    (if (= ?x14 0) then
    	(printout t "¿Hace cuánto tiempo que acabó sus estudios? [en meses]" crlf)
    	(bind ?x16 (read))
    else
    	(bind ?x16 0)
    )
    (printout t "¿Cuál es su edad?" crlf)
    (bind ?x15 (read))
    ; FIN de la petición de datos de entrada   
    (assert (persona (extranjero ?x1)(ingles ?x2)(nivelIngles ?x3)(castellano ?x4)(nivelCastellano ?x5)
	   (numIdiomas ?x6)(nivelEstudios ?x7)(practEmp ?x8)(numCursillos ?x10)(mesesExp ?x9)(movimiento ?x11)
       (viajar ?x12)(jornadaCompleta ?x13)(creditosFin ?x14)(edad ?x15)(mesesFin ?x16)(area ?x17)
       (conocimientos 0)(flexibilidad 0)(empleoCercaUni 0)(ultimoCurso 0)(perfilValido 0)(perfilValidoA 0)
       (perfilValidoB 0) (num 0) (opositar 0)(becaColaboracionUni 0) (becaColaboracionDepart 0)(becaFaro 0)
       (becaArco 0)(becaIntegrants 0)(opositarGrupoA 0)(opositarGrupoB 0)(aprenderCastellano 0)
       (practEmpresaPrivada 0)(empleoSectorPrivado 0)(empleoPrivadoFuera 0)(profesorParticular 0)))
	)

;REGLAS VALIDEZ USUARIO
	
(defrule regla1
	(declare (salience 98) (no-loop TRUE))
    ?pers <-(persona (nivelEstudios ?ne)(creditosFin ?cf)(num ?n))
	
    (and (test (< ?n 1))
    (test (> ?cf 0))
    (test (<= ?cf 100))
    (test (>= ?ne 3)))
	=> 
    (modify ?pers(ultimoCurso 1))
    (modify ?pers(num 1))
    (printout t "regla 1" crlf)
    )

(defrule regla2
	(declare (salience 96) (no-loop TRUE))
    ?pers <-(persona (creditosFin ?cf)(edad ?edad)(mesesFin ?mf)(num ?n))
	
    (and (test (< ?n 2))
    (test (= ?cf 0))
    (test (>= ?edad 21))
    (test (<= ?edad 50))(test(<= ?mf 12)))
	=>
    (modify ?pers(perfilValido 1))(modify ?pers(perfilValidoB 1))
    (modify ?pers(num 2))
    (printout t "regla 2" crlf) 
    )

(defrule regla3
	(declare (salience 94) (no-loop TRUE))
    ?pers <-(persona (edad ?edad)(ultimoCurso ?uc)(num ?n))
    
    (and (test (< ?n 3))
    (test (= ?uc 1))
    (test (>= ?edad 21))
    (test (<= ?edad 50)))
	=>
    (modify ?pers(perfilValido 1))(modify ?pers(perfilValidoA 1))
    (modify ?pers(num 3))
    (printout t "regla 3" crlf)
    )

;REGLAS DEL CONOCIMIENTOS

(defrule regla4
	(declare (salience 92) (no-loop TRUE))
    ?pers <-(persona (extranjero ?e)(ingles ?i)(nivelIngles ?ni)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 4)) 
    (test (= ?pv 1))
    (test (= ?e 0))
    (test (= ?i 1))
    (test (= ?ni 3)))
	=>   
    (bind ?co (+ ?co 5))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 4))
    (printout t "regla 4" crlf)
     )

(defrule regla5
	(declare (salience 90) (no-loop TRUE))
    ?pers <-(persona (extranjero ?e)(castellano ?c)(nivelCastellano ?nc)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 5))
    (test (= ?pv 1))
    (test (= ?e 1))
    (test (= ?c 1))
    (test (= ?nc 3)))
	=> 
    (bind ?co (+ ?co 3))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 5))
    (printout t "regla 5" crlf)
     )
	

(defrule regla6
	(declare (salience 88) (no-loop TRUE))
    ?pers <-(persona (extranjero ?e)(castellano ?c)(nivelCastellano ?nc)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 6))
    (test (= ?pv 1))
    (test (= ?e 1))
    (test (= ?c 1))
    (test (not(= ?nc 3))))
	=> 
    (bind ?co (- ?co 2))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 6))
    (printout t "regla 6" crlf)
     )


(defrule regla7
	(declare (salience 86) (no-loop TRUE))
    ?pers <-(persona (extranjero ?e)(ingles ?i)(nivelIngles ?ni)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 7))
    (test (= ?pv 1))
    (test (not(= ?e 1)))
    (test (= ?i 1))
    (test (= ?ni 2)))
	=> 
    (bind ?co (+ ?co 3))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 7))
    (printout t "regla 7" crlf)
     )

(defrule regla8
	(declare (salience 84) (no-loop TRUE))
    ?pers <-(persona (extranjero ?e)(ingles ?i)(numIdiomas ?nid)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 8))
    (test (= ?pv 1))
    (test (not(= ?e 1)))
    (test (= ?i 0))
    (test (>= ?nid 2)))
	=> 
    (bind ?co (+ ?co 2))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 8))
    (printout t "regla 8" crlf)
     )

(defrule regla9
	(declare (salience 82) (no-loop TRUE))
    ?pers <-(persona (nivelEstudios ?ne)(practEmp ?pe)(numCursillos ?ncu)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 9))
    (test (= ?pv 1))
    (test (> ?ne 1))
    (test (>= ?ncu 1))
    (test (= ?pe 1)))
	=> 
    (bind ?*co (+ ?co 3))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 9))
    (printout t "regla 9" crlf)
     )

(defrule regla10
	(declare (salience 80) (no-loop TRUE))
    ?pers <-(persona (nivelEstudios ?ne)(practEmp ?pe)(numCursillos ?ncu)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 10))
    (test (= ?pv 1))
    (test (> ?ne 1))
    (test (= ?ncu 0))
    (test (= ?pe 1)))
	=> 
    (bind ?co (+ ?co 2))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 10))
    (printout t "regla 10" crlf)
     )

(defrule regla11
	(declare (salience 78) (no-loop TRUE))
    ?pers <-(persona (nivelEstudios ?ne)(practEmp ?pe)(numCursillos ?ncu)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 11))
    (test (= ?pv 1))
    (test (> ?ne 1))
    (test (>= ?ncu 1))
    (test (= ?pe 0)))
	=> 
    (bind ?co (+ ?co 1))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 11))
    (printout t "regla 11" crlf)
     )

(defrule regla12
	(declare (salience 76) (no-loop TRUE))
    ?pers <-(persona (nivelEstudios ?ne)(practEmp ?pe)(mesesExp ?me)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 12))
    (test (= ?pv 1))
    (test (> ?ne 1))
    (test (>= ?me 0))
    (test (<= ?me 3))
    (test (= ?pe 1)))
	=>
    (bind ?co (+ ?co ?me)) 
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 12))
    (printout t "regla 12" crlf)
     )

(defrule regla13
	(declare (salience 74) (no-loop TRUE))
    ?pers <-(persona (nivelEstudios ?ne)(practEmp ?pe)(mesesExp ?me)(conocimientos ?co)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 13))
    (test (= ?pv 1))
    (test (> ?ne 1))
    (test (>= ?me 3))
    (test (= ?pe 1)))
	=> 
	(bind ?co (+ ?co 3))
    (modify ?pers(conocimientos ?co))
    (modify ?pers(num 13))
    (printout t "regla 13" crlf)
     )

;REGLAS FLEXIBILIDAD

(defrule regla14
	(declare (salience 72) (no-loop TRUE))
    ?pers <-(persona (movimiento ?m)(viajar ?v)(jornadaCompleta ?jc)(flexibilidad ?fx)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 14))
    (test (= ?pv 1))
    (test (>= ?m 0))
    (test (<= ?m 10))
    (test (<= ?v 1))
    (test (= ?jc 0)))
	=>
    (bind ?fx (- ?fx 3))
    (modify ?pers(flexibilidad ?fx))
    (modify ?pers(num 14))
    (printout t "regla 14" crlf) 
     )

(defrule regla15
	(declare (salience 70) (no-loop TRUE))
    ?pers <-(persona (movimiento ?m)(viajar ?v)(jornadaCompleta ?jc)(flexibilidad ?fx)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 15))
    (test (= ?pv 1))
    (test (>= ?m 0))
    (test (<= ?m 10))
    (test (> ?v 1))
    (test (= ?jc 1)))
	=> 
    (bind ?fx (+ ?fx 2))
    (modify ?pers(flexibilidad ?fx))
    (modify ?pers(num 15))
    (printout t "regla 15" crlf) 
     )

(defrule regla16
	(declare (salience 68) (no-loop TRUE))
    ?pers <-(persona (movimiento ?m)(viajar ?v)(jornadaCompleta ?jc)(flexibilidad ?fx)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 16))
    (test (= ?pv 1))
    (test (>= ?m 10))
    (test (< ?m 30))
    (test (> ?v 1))
    (test (= ?jc 1)))
	=> 
    (bind ?fx (+ ?fx 4))
    (modify ?pers(flexibilidad ?fx))
    (modify ?pers(num 16))
    (printout t "regla 16" crlf) 
     )

(defrule regla17
	(declare (salience 66) (no-loop TRUE))
    ?pers <-(persona (movimiento ?m)(viajar ?v)(jornadaCompleta ?jc)(flexibilidad ?fx)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 17))
    (test (= ?pv 1))
    (test (>= ?m 30))
    (test (<= ?v 1))
    (test (= ?jc 1)))
	=> 
    (bind ?fx (+ ?fx 2))
    (modify ?pers(flexibilidad ?fx))
    (modify ?pers(num 17))
    (printout t "regla 17" crlf) 
     )

(defrule regla18
	(declare (salience 64) (no-loop TRUE))
    ?pers <-(persona (movimiento ?m)(viajar ?v)(jornadaCompleta ?jc)(flexibilidad ?fx)(perfilValido ?pv)(num ?n))
	
    (and (test (< ?n 18))
    (test (= ?pv 1))
    (test (>= ?m 30))
    (test (> ?v 1))
    (test (= ?jc 1)))
	=> 
    (bind ?fx (+ ?fx 5))
    (modify ?pers(flexibilidad ?fx))
    (modify ?pers(num 18))
    (printout t "regla 18" crlf) 
     )



;REGLAS INTERMEDIAS
(defrule regla19
	(declare (salience 62) (no-loop TRUE))
    ?pers <-(persona (conocimientos ?co)(flexibilidad ?fx)(empleoCercaUni ?ec)(perfilValidoA ?pva)(num ?n))
	
    (and (test (< ?n 19))
    (test (= ?pva 1))
    (test (<= ?fx 0))
    (test (>= ?co 0)))
	=>
    (bind ?ec 1)
    (modify ?pers(empleoCercaUni ?ec))
    (modify ?pers(num 19))
    (printout t "regla 19" crlf))

(defrule regla20
	(declare (salience 60) (no-loop TRUE))
    ?pers <-(persona (conocimientos ?co)(flexibilidad ?fx)(area ?a)(perfilValidoB ?pvb)(num ?n))
	
    (and (test (< ?n 20))
    (test (= ?pvb 1))
    (test (< ?fx 4))
    (test (<= ?a 2))
    (test (>= ?co 0)))
	=>
    (modify ?pers(opositar 1))
    (modify ?pers(num 20))
    (printout t "regla 20" crlf)) 

;REGLAS SALIDA
(defrule regla21
	(declare (salience 58) (no-loop TRUE))
    ?pers <-(persona (edad ?edad)(conocimientos ?co)(empleoCercaUni ?ec)(num ?n))
	
    (and (test (< ?n 21))
    (test (= ?ec 1))
    (test (<= ?edad 28))
    (test (>= ?co 2)))
	=> 
	(modify ?pers(becaColaboracionUni 1))
    (modify ?pers(num 21))
    (printout t "regla 21: Solicite una beca de Colaboración con la Universidad" crlf))

(defrule regla22
	(declare (salience 56) (no-loop TRUE))
    ?pers <-(persona (creditosFin ?cf)(edad ?edad)(conocimientos ?co)(empleoCercaUni ?ec)(num ?n))
	
    (and (test (< ?n 22))
    (test (= ?ec 1))
    (test (<= ?edad 28))
    (test (>= ?co 4)))
	=> 
    (modify ?pers(becaColaboracionDepart 1))
    (modify ?pers(num 22))
    (printout t "regla 22: Solicite una beca de Colaboración con un Departamente de la Facultad" crlf))
    
(defrule regla23
	(declare (salience 54) (no-loop TRUE))
    ?pers <-(persona (conocimientos ?co)(flexibilidad ?fx)(perfilValidoA ?pva)(num ?n))
	
    (and (test (< ?n 23))
    (test (= ?pva 1))
    (test (>= ?fx 4))
    (test (>= ?co 7)))
	=> 
    (modify ?pers(becaFaro 1))
    (modify ?pers(num 23))
    (printout t "regla 23: Realice prácticas fuera con una beca Faro" crlf))

(defrule regla24
	(declare (salience 52) (no-loop TRUE))
    ?pers <-(persona (conocimientos ?co)(flexibilidad ?fx)(perfilValidoB ?pvb)(num ?n))
	
    (and (test (< ?n 24))
    (test (= ?pvb 1))
    (test (>= ?fx 4))
    (test (>= ?co 7)))
	=> 
    (modify ?pers(becaArco 1))
    (modify ?pers(num 24))
    (printout t "regla 24: Realice prácticas en el extranjero con una beca Arco" crlf))

(defrule regla25
	(declare (salience 50) (no-loop TRUE))
    ?pers <-(persona (conocimientos ?co)(flexibilidad ?fx)(perfilValidoB ?pvb)(num ?n))
	
    (and (test (< ?n 25))
    (test (= ?pvb 1))
    (test (>= ?fx 5))
    (test (>= ?co 8)))
	=> 
    (modify ?pers(becaIntegrants 1))
    (modify ?pers(num 25))
    (printout t "regla 25: Realice prácticas en el extranjero con una beca Integrants" crlf))

(defrule regla26
	(declare (salience 48) (no-loop TRUE))
    ?pers <-(persona (opositar ?o)(nivelEstudios ?ne)(flexibilidad ?fx)(num ?n))
	
    (and (test (< ?n 26))
    (test (= ?o 1))
    (test (= ?ne 3))
    (test (< ?fx 4)))
	=> 
    (modify ?pers(opositarGrupoB 1))
    (modify ?pers(num 26))
    (printout t "regla 26: Prepare unas oposiciones para un puesto del grupo B" crlf))

(defrule regla27
	(declare (salience 46) (no-loop TRUE))
    ?pers <-(persona (opositar ?o)(nivelEstudios ?ne)(flexibilidad ?fx)(num ?n))
	
    (and (test (< ?n 27))
    (test (= ?o 1))
    (test (>= ?ne 4))
    (test (< ?fx 4)))
	=> 
    (modify ?pers(opositarGrupoA 1))
    (modify ?pers(num 27))
    (printout t "regla 27: Prepare unas oposiciones para un puesto del grupo A" crlf))

(defrule regla28
	(declare (salience 44) (no-loop TRUE))
    ?pers <-(persona (perfilValidoA ?pva)(num ?n)(conocimientos ?co)(flexibilidad ?fx)(area ?a))
	
    (and (test (< ?n 28))
    (test (= ?pva 1))
    (test (>= ?a 3))
    (test (>= ?co 0))
    (test (> ?fx 0)))
	=> 
    (modify ?pers(practEmpresaPrivada 1))
    (modify ?pers(num 28))
    (printout t "regla 28: Realice prácticas en empresa privada" crlf))
    
(defrule regla29
	(declare (salience 42) (no-loop TRUE))
    ?pers <-(persona (perfilValidoB ?pvb)(num ?n)(conocimientos ?co)(flexibilidad ?fx)(area ?a))
	
    (and (test (< ?n 29))
    (test (= ?pvb 1))
    (test (>= ?a 3))
    (test (>= ?co 0))
    (test (> ?fx 0)))
	=> 
    (modify ?pers(empleoSectorPrivado 1))
    (modify ?pers(num 29))
    (printout t "regla 29: Busque empleo en el sector privado" crlf))

(defrule regla30
	(declare (salience 40) (no-loop TRUE))
    ?pers <-(persona (perfilValidoB ?pvb)(num ?n)(conocimientos ?co)(flexibilidad ?fx))
	
    (and (test (< ?n 30))
    (test (= ?pvb 1))
    (test (>= ?fx 4))
    (test (>= ?co 0)))
	=> 
    (modify ?pers(empleoPrivadoFuera 1))
    (modify ?pers(num 30))
    (printout t "regla 30: Busque empleo fuera de España en el sector privado" crlf))
            
(defrule regla31
	(declare (salience 40) (no-loop TRUE))
    ?pers <-(persona (perfilValidoA ?pva)(num ?n)(conocimientos ?co)(flexibilidad ?fx)(area ?a))
	
    (and (test (< ?n 31))
    (test (= ?pva 1))
	(test (< ?a 3))
    (test (>= ?co 0))
    (test (< ?co 7))
    (test (>= ?fx 0))
    (test (< ?fx 4)))
	=> 
    (modify ?pers(profesorParticular 1))
    (modify ?pers(num 31))
    (printout t "regla 31: Ofrezca clases particulares de su área" crlf))

(defrule regla32
	(declare (salience 38) (no-loop TRUE))
    ?pers <-(persona (perfilValido ?pv)(nivelCastellano ?nc)(extranjero ?e)(num ?n))
	
    (and (test (< ?n 32))
    (test (= ?pv 1))
    (test (= ?e 1))
    (test (<= ?nc 2)))
	=> 
    (modify ?pers(num 32))
    (modify ?pers(aprenderCastellano 32))
    (printout t "regla 32: Necesita aprender y/o mejorar su castellano para poder encontrar empleo" crlf))

(defrule regla33
	(declare (salience 36) (no-loop TRUE))
    ?pers <-(persona (perfilValido ?pv)(num ?n)(becaColaboracionUni ?bcu)(becaColaboracionDepart ?bcd)
        (becaFaro ?bf)(becaArco ?ba)(becaIntegrants ?bi)(opositarGrupoA ?opA)(opositarGrupoB ?opB)
        (aprenderCastellano ?acast) (practEmpresaPrivada ?pep)(empleoSectorPrivado ?esp)
        (empleoPrivadoFuera ?epf)(profesorParticular ?prpa))

    (and (test (< ?n 33))
    (test (= ?pv 1))
    (test (= ?bcu 0))
    (test (= ?bcd 0))
    (test (= ?bf 0))
    (test (= ?ba 0))
    (test (= ?bi 0))
    (test (= ?opA 0))
    (test (= ?opB 0))
    (test (= ?pep 0))
    (test (= ?esp 0))
    (test (= ?epf 0))
    (test (= ?prpa 0))
    (test (= ?acast 0)))
	=> 
    (modify ?pers(num 33))
    (printout t "regla 33: Lo sentimos, no hemos sido capaces de ofrecerle asesoramiento" crlf))

(defrule regla34
	(declare (salience 34) (no-loop TRUE))
    ?pers <-(persona (perfilValido ?pv)(perfilValidoA ?pva)(perfilValidoB ?pvb)(num ?n))
	
    (and (test (< ?n 34))
    (test (= ?pvb 0))
    (test (= ?pva 0))
    (test (= ?pv 0)))
	=> 
    (modify ?pers(num 34))
    (printout t "regla 34: No cumple los requisitos para usar este asesor laboral" crlf))

(reset)

(run)