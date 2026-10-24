# Ice and Fire 0.1.21 — caccia autonoma e priorità del cibo

Minecraft Java26.3, Java25, Loader0.19.5, API0.163.0+26.3. Sostituire il vecchio
JAR della mod senza duplicati. Tappa1 ancora incompleta, nessun passaggio alla2.

## Caccia

Drago libero, sveglio e senza controllo: cerca prede dai tag originali
iceandfire:fire_dragon_targets e iceandfire:dragon_targets, ora nel percorso
singolare tags/entity_type di26.3. Il drago addomesticato caccia animali adatti
alla sua taglia se fame<90; non caccia giocatori, abitanti, golem o animali già
addomesticati. Alleati e proprietario mantengono tutte le protezioni precedenti.

Il selvatico mantiene una ricerca aggressiva separata per giocatori survival,
abitanti e animali dei tag, con probabilità originale nextInt(100)>fame per i
non giocatori; la ricerca alimentare visibile resta disponibile sotto90. Le
priorità sono difesa del proprietario, difesa propria, cibo raggiungibile prima di una nuova caccia e poi
caccia. La carne a terra riservata non viene scartata fra ricerca e movimento.

Seduto, seguimi, sonno, NoAI, pilota/spalle, ritorno notturno al nido e amore
impediscono una nuova caccia; comandi e cambi di stato interrompono la ricerca
in corso. Si riutilizzano gli attacchi e il movimento autonomi già migrati.
Nessun chunk forzato, scansione nello spazio3D entro FOLLOW_RANGE.

Nuova opzione originale dragonTargetSearchLength in iceandfire-combat.json:
default128, valori1..10000, cap effettivo attributo2048 come originale. Vecchi
file senza campo mantengono128. Modifiche richiedono riavvio; non è hot reload.

## Verifica

Sei nuovi GameTest: preda cercata/attaccata senza provocazione; soglia fame e
sicurezza di giocatori/pet; priorità comandi/sonno/amore/NoAI; carne raggiungibile
prima della caccia; aggressore non alimentare prioritario; configurazione/cap.
I tag sono copiati dagli originali. Lo scenario della difesa dal sonno usa ora
uno zombie NoAI persistente: una mucca vicina era diventata una preda legittima prima del
riposo. L'asserzione sonno -> attacco -> risveglio/danno è conservata.

Verificati62 JUnit,73 GameTest della mod (74 con baseline vanilla escluso) e4 prove client con otto screenshot. Nessuna prova con due veri client. Nessun test con
due veri client. Restano shake prey, acqua/lava, configurazioni/animazioni e
altre lacune in STATO-TAPPA-1.md. Questo non completa il port.

La priorità esplicita della carne prima di una nuova caccia è una scelta del
port; non dichiara parità di ogni dettaglio dei due goal originali degli item.
Un bersaglio già attivo non viene sostituito da una nuova ricerca di prede.

La diagnostica della prova difensiva ha rilevato l'aggressore DISCARDED: il
giocatore reale dell'altra prova causava despawn a distanza dei mob ostili.
Aggressori resi persistenti, senza ritardare o allentare l'asserzione di priorità.
La stessa prova isolata era già passata; il filtro ufficiale è rimasto solo
in uno script temporaneo esterno alla repository, assente dal JAR e dalla build.
Il test client delle pose acquisisce ora la preda tramite caccia senza setTarget.
