(deftemplate persona
	(multislot nombre)
	(multislot apellidos)
	(slot sexo)
	(slot fumador)
	(slot edad)
	(slot prohibido_fumar)
)


(deffacts estudiante "Personas iniciales"
	(persona (nombre Juan)(apellidos Martin Lopez)(sexo varon)(fumador no)(edad 16)(prohibido_fumar no))
	(persona (nombre Matilde A)(apellidos Pipi Flux)(sexo mujer)(fumador si)(edad 17)(prohibido_fumar no))
	(persona (nombre Jose Miguel)(apellidos Guerrero Hernandez)(sexo varon)(fumador no)(edad 22)(prohibido_fumar no))
	(persona (nombre Maria Jose)(apellidos Martin Lopez)(sexo mujer)(fumador si)(edad 43)(prohibido_fumar no))
)


(defrule puede-fumar "Miramos si es mayor de edad y fuma para no prohibirselo"
	(initial-fact)
	(persona (nombre $?nombre)(edad ?edad &:(< ?edad 18)) (fumador si)(prohibido_fumar no))
	=> 
	(printout t "DIOS!!! " $?nombre " tiene " ?edad " y fuma!! ¿¿Se lo prohibes?? si/no" crlf)	
	(assert(persona (nombre $?nombre)(prohibido_fumar (read))))	
)

(defrule no-fumar 
	(persona (nombre $?nombre)(prohibido_fumar si))
	=> 
	(printout t $?nombre " tiene prohibido fumar" crlf)	
)

; esto se puede ejecutar desde el programa java
; rete.executeCommand("(reset)");
; rete.executeCommand("(run)");

(watch rules)
(watch facts)

(reset)
(run)
