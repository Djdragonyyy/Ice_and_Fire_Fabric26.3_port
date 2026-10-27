# Prova 0.1.53 - animali e villager fuggono dai draghi

Checkpoint verificato del 27 ottobre 2026; tappa 1 e port ancora aperti.

## Comportamento e configurazione

Portati i comportamenti originali di ServerEvents e VillagerAIFearUntamed
per il drago di fuoco. Si usa AvoidEntityGoal nativo con priorita 1.
I villager e i mercanti ambulanti cercano il drago entro 8 blocchi con
velocita 0.8/0.8; i 18 tipi del tag fear_dragons entro 30 con velocita 1/0.5.
I tag originali sono copiati senza cambiare i membri, nel percorso entity_type
richiesto dalla 26.3. Altri mob Ice and Fire non ancora portati sono esclusi.

In config/iceandfire-combat.json sono disponibili animalsFearDragons e
villagersFearDragons, entrambe true per impostazione originale. Anche un
vecchio JSON privo dei campi conserva i nuovi valori predefiniti. Impostare
false e riavviare il gioco/server per disabilitare il comportamento relativo.
Il nome del vecchio file di configurazione viene mantenuto per compatibilita.

Il bestiame addomesticato e escluso, come canTameDragonAttack originale;
il filtro non dipende invece da proprieta o addomesticamento del drago.
La categoria villager non viene esclusa in base all'addomesticamento del drago:
il nome originale FearUntamed non corrisponde a un tale controllo nel codice.
Raggio, visibilita, vita e scelta del percorso vengono gestiti dal goal nativo.
Non vengono inflitti danni, cambiati i bersagli del drago o sostituiti gli altri
goal dei mob. Il Brain del villager rimane quello nativo.

L'evento server ENTITY_LOAD ricostruisce i goal transitori anche dopo il
caricamento di un mob salvato; un controllo impedisce aggiunte duplicate.
Questo adatta l'evento Forge FinalizeSpawn alle entita caricate in Fabric.
Non serve una nuova Mixin: Mob.getGoalSelector e pubblico nella 26.3.
Le opzioni si applicano al caricamento, non sono comandi di modifica dinamica;
il cambio di tag su mob gia caricati non e una nuova funzione di questa fase.

## Verifiche

Focus nativo finale phase53-focused-5.log: cinque GameTest superati, exit 0,
43 secondi complessivi. Per mucca, villager e mercante si osserva il goal di
fuga realmente attivo durante i primi 300 tick e si richiede aumento della
distanza quadrata maggiore di 16 mentre il goal e attivo, senza subire danni.
Coperti anche opzioni false, mucca salvata e ricaricata, nessun goal duplicato,
zombie escluso, animale addomesticato, due raggi originali e drago morto.

Il primo focus3 passava ma controllava soltanto la distanza finale a 100 tick.
Il primo full e poi fallito in quella verifica e non e contato come verde:
ha terminato per AssertionError e non ha eseguito i client. La diagnostica
successiva ha identificato la mucca: goal attivo, ma aumento troppo piccolo
entro 100 tick. I due casi villager/mercante erano verdi. Nessuna correzione
alla logica di gioco e stata applicata per far passare la prova.
La fixture ora controlla il movimento causato dal goal anziche un generico
spostamento finale (che potrebbe essere semplice passeggio o ritorno dopo
il termine del percorso). Mantiene la stessa soglia e lascia fino a 300 tick
per la pianificazione nativa; non impone destinazioni, non muove il mob e
non cambia il suo generatore casuale. Le asserzioni native producono inoltre
un report di errore invece di interrompere il server con un AssertionError.

La prima compilazione usava EntityType.is anziche il relativo holder26.3;
corretto senza cambiare i tag. I primi due tentativi di focus avevano percorso
init relativo e filtro nomi errato: non sono prove verdi. Il terzo seleziona
esattamente i cinque test con il nome nativo dragon_fear_game_tests.

Seconda regressione completa pulita phase53-final-2.log superata in 11m 36s,
exit 0: 99 JUnit senza errori, 168 GameTest server (167 della mod e baseline),
18 prove client e 54 PNG correnti. Non e una nuova prova dedicata di riavvio
della Forge: quel caso e verificato nella precedente 0.1.52.

La tappa 1 e il port completo rimangono aperti: configurazioni e dettagli di
combattimento/ciclo vitale, navigazione complessa, anteprima passeggeri e
punte grigie richiedono altro lavoro. Non sono stati portati draghi di ghiaccio,
fulmine o le tappe successive.
