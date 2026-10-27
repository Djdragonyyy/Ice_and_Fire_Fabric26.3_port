# Prova 0.1.54 - reazione dei draghi selvatici ai forzieri

Checkpoint verificato del 27 ottobre 2026; tappa1 e port ancora aperti.

## Comportamento originale

Un giocatore non creativo che interagisce con un forziere o lo rompe sveglia
i draghi di fuoco selvatici vicini e diventa il loro bersaglio. Sono esclusi
i draghi addomesticati, morti o posseduti dal giocatore. Vengono rimossi sonno
e ordine di seduta, senza riscrivere il campo del comando del drago.

La regione originale e la bounding box del giocatore espansa lungo tutti
i tre assi, non una sfera e non un raggio centrato sul forziere. La nuova
opzione dragonGoldSearchLength in config/iceandfire-combat.json mantiene il
valore originale30 e l'intervallo0..10000. Un vecchio JSON senza campo usa30.
Zero limita la ricerca alla bounding box del giocatore: non e un interruttore
di disattivazione. Non e richiesto un controllo di visibilita aggiuntivo.

Si usa AbstractChestBlock, come nella sorgente: non qualsiasi contenitore.
La reazione viene valutata nel callback di interazione o prima della rottura,
come gli eventi Forge originali, quindi non certifica che ogni tentativo sia
andato a buon fine. Non cancella o aggira controlli del gioco o di altri mod:
UseBlockCallback restituisce PASS e BeforeBreak restituisce true.
L'esecuzione produttiva avviene esclusivamente sul server, senza nuovi packet
o Mixin. Si conservano i filtri di validita bersaglio nativi della 26.3:
un drago NoAI non viene forzato ad accettare un bersaglio rifiutato dal gioco.

Questa fase porta il ramo forzieri di ServerEvents.onPlayerRightClick e
onBreakBlock. Le pile di oro, argento e rame non sono ancora registrate nel
port: il relativo ramo resta aperto. Non si sostituiscono con i normali blocchi
di metallo e non si dichiara completa ogni reazione ai tesori dell'originale.

## Verifiche native

Focus finale phase54-focused-5.log:100JUnit e4GameTest verdi,38s,exit0.
Apertura tramite ServerPlayerGameMode.useItemOn, menu nativo effettivamente
aperto; rottura tramite destroyBlock e blocco realmente rimosso. Stone non
attiva l'allarme. Coperti creativi, draghi tame, proprieta separata da tame,
carcassa/morte e regione configurata attorno al giocatore. I callback Fabric
sono osservati dal mod di prova, senza invocare direttamente l'allarme.

I primi tentativi fallivano per due fixture errate, conservate nei log:
il mock makeMockServerPlayerInLevel della26.3 sovrascrive gameMode sempre
CREATIVE anche dopo setGameMode(SURVIVAL); e il drago NoAI non puo ricevere
il bersaglio attraverso Mob.setTarget/asValidTarget nativo. Il focus finale
usa un ServerPlayer reale con connessione embedded e modalita verificata,
e AI normale. Controlla la reazione nello stesso tick prima della caccia
spontanea. Nessuna correzione produttiva ha aggirato i filtri nativi.
I giocatori embedded e le loro osservazioni vengono chiusi a fine prova.

Full pulito phase54-final.log superato in11m52s,exit0:100JUnit senzaerrori,
172server(171mod+baseline),18client e54PNG correnti. La pulizia delle
connessioni embedded e delle osservazioni e inclusa nel full finale. Il precedente riavvio dedicato della Forge52
rimane una prova distinta, non un nuovo collaudo di riavvio54.

Tappa1 e port completo restano aperti: configurazioni e interazioni mancanti,
ciclo vitale, navigazione complessa, anteprima passeggeri e punte grigie.
