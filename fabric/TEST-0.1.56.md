# Prova 0.1.56 - movimento durante il risveglio

Checkpoint di sviluppo del28ottobre2026. Tappa1 e port ancora incompleti.

Il metodo originale EntityDragonBase.canMove richiede sleepProgress == 0.
Il port controllava il flag del sonno ma ometteva il recupero della posa:
dopo un risveglio poteva muoversi con il corpo ancora ripiegato. Aggiunta
questa sola condizione a FireDragon.canMoveAutonomously. I percorsi autonomi
a terra e il decollo che usano il metodo attendono ora la posa completa.
Una minaccia continua a essere acquisita e a svegliare immediatamente il
drago: canAttack, setTarget e i controlli generali non sono stati ristretti.
Nessuna modifica a modello, texture, rete o controllo diretto del pilota.

## Prove eseguite

La nuova prova nativa notturna usa un drago con AI attiva, non un drago
bloccato con NoAI. A tick45 verifica sonno reale e posa20, poi crea un
Zombie reale e verifica acquisizione e risveglio immediati. A tick65
verifica recupero parziale, movimento autonomo negato, nessun volo e meno
di0.05 di spostamento orizzontale. A tick90 verifica posa0 e movimento
nuovamente abilitato. Il bersaglio NoAI resta fermo per isolare la reazione.

Il caso di giorno e distinto: un flag fresco impostato artificialmente
viene rimosso da tickSleep prima che avanzi la posa. La prova preesistente
ora rende esplicita la posa0 e conserva il movimento immediato corretto.
Nel primo focus una nuova aspettativa errata su questo caso falliva;
la nuova prova notturna passava gia. Corretta la sola fixture, nessuna
seconda modifica alla produzione. phase56-focused-2.log passa in28s:
100JUnit e tutti8GameTest del sonno.

La prima regressione completa phase56-final.log fallisce dopo9m41s nella
ritorsione preesistente a50tick: la traccia registra il primo morso mancato
dopo contraccolpo e un secondo attacco iniziato a41tick, ancora senza danno
alla scadenza. Il solo test attende ora120tick entro il limite130, conserva
le asserzioni su risveglio/bersaglio/danno reale e include la posa nella traccia.
Nessuna modifica ulteriore alla produzione o al caso casuale degli attacchi.

phase56-focused-3.log passa in9m35s,exit0: il nome focused e storico, ma
la proprieta testClass non e inoltrata al runner, quindi sono stati rieseguiti
tutti178testserver(177mod+baseline vanilla). phase56-client-final.log passa
in 3m 16s,exit0: build,19client,56PNG correnti. I100JUnit erano verdi
nel clean iniziale e restano invariati/up-to-date nei comandi successivi.
XML senza failure/error. phase56-verified.log combina e distingue le prove;
non e dichiarato un solo comando full pulito finale. Ripetute le regressioni
client esistenti; nessuna nuova scena
visiva per questa condizione server. JAR55->56:124classi identiche,
solo FireDragon modificata, nessuna classe aggiunta/rimossa o harness.

## Limiti

Non e una nuova verifica di riavvio dedicato, autenticazione pubblica o
navigazione complessa: la prova Forge52 resta distinta. Restano da portare
o verificare navigazione/rottura del corpo, configurazioni e ciclo vitale,
GUI passeggeri e resa delle punte grigie. Non chiude la tappa1 o il port.
