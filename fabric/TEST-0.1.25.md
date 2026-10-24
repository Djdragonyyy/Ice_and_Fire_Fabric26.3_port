# Ice and Fire 0.1.25 — voci originali e animazione SPEAK

Minecraft Java 26.3, Java 25, Loader 0.19.5 e Fabric API 0.163.0+26.3.
Checkpoint parziale della tappa 1: il port completo resta in corso.

Ripristinati i nove eventi originali di suono ambientale, ferita e morte:
stadi 1/2 voce child, stadio 3 teen, stadi 4/5 adult. Intervallo ambientale90,
selezione dei campioni, volumi e sottotitoli dal materiale originale. Gli OGG
sono copiati senza modifiche. L'evento di morte adulto conserva la scelta
originale dei campioni hurt; non viene sostituita con una scelta inventata.

Quando una voce ambientale o di ferita trova il drago senza un'altra animazione,
la mandibola segue SPEAK20tick: apertura5, mantenimento10 e ritorno5,18gradi
e spostamentoZ0.2 originali, interpolazione sinusoidale. Stato sincronizzato,
nessun danno associato e nessuna modifica a movimento o seduta. Attacchi,
soffio, pasto, sonno, morte e NoAI interrompono/impediscono SPEAK. Il sonno
sopprime anche la voce ambientale. L'animazione cosmetica non viene salvata.

Le prove aggiunte verificano definizioni e byte dei campioni inclusi, posa
della mandibola senza accumulo, voci durante crescita e sequenza runtime di
20fotogrammi/ritorno al riposo. Verificata la soppressione con sonno e NoAI.
Il test client controlla la risoluzione dei nove eventi nel sound manager
reale e cattura la mandibola aperta dopo sincronizzazione dal server.

La prima suite ha segnalato l'animazione incidentale ancora attiva con NoAI:
corretto il gate di avvio e annullamento, conservate le asserzioni difensive
preesistenti. I risultati finali devono essere quelli della suite ripetuta.
Non è dichiarata una prova di ascolto manuale o un test con due veri client.
Ruggiti/effetti, altre interazioni e le lacune nello stato cumulativo restano
aperti. Asset, UV, punte grigie e geometria originali non modificati.

La sospensione del PC è ANNULLATA: continuare dopo test, commit/push e consegna.

Verificati 70 JUnit, 83 GameTest della mod (84 con baseline vanilla escluso) e 4 prove client con undici screenshot. Nessuna prova con due veri client.
Build completa finale riuscita, revisione in sola lettura completata; screenshot SPEAK ispezionato.
