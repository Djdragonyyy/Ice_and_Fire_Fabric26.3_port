# Revisione del port Fabric

Verificati61 JUnit,66 GameTest della mod (67 con baseline vanilla escluso) e3 prove client con sei screenshot ispezionati. Nessuna prova con due veri client.

## Revisione 0.1.19 — 24 ottobre 2026

Review-agent locale applicata in sola lettura, senza delega, al diff completo da
a2684afe7: produzione, nuove risorse, test, build/versione e documenti. No findings.
Confrontati DragonAIMate, isFood/createEgg e EntityDragonEgg originali, con API
26.3 locali. Verificate priorità amore/sonno/volo, validazione simmetrica della
coppia, sesso/stadio/dimensione/tipo, singolo spawn da due goal attivi, cooldown
persistente separato dalla crescita e fallimento atomico prima del consumo.

Il primo test aveva trovato un uovo spostato dal centro del nido dai genitori:
migrati isPushable=false e doPush vuoto originali. Asserzione otto blocchi nido
più centro sentiero mantenuta, poi superata. Controllati terreno protetto,
mobGriefing, contenitori, durezza negativa, fluidi e chunk/bordo; nuovo ciclo
server completo fino alla schiusa del nuovo proprietario. Asset originali integri.

## Revisione 0.1.18 — 24 ottobre 2026

Skill locale review-agent applicata read-only senza delega al diff completo
da824e05454, inclusi nuovi file client/server/test, configurazione, documenti e
versione. No findings. Confrontati IafDragonLogic/EntityDragonBase per sonno,
LayerDragonEyes per HeadFront ed EntityDragonEgg.hurt per recupero uova.
Controllati consenso targeting separato dal movimento, risveglio prima dei goal,
protezione proprietario, persistenza, supporto, ordine occhi/armature e doppio drop.

60 JUnit,60 GameTest della mod (61 col baseline vanilla escluso) passati.3 test
client con mondi temporanei e screenshot ispezionati: armature, coda/ali/statistiche,
Sleeping salvato NoAI e risveglio. Le ultime modifiche server alle uova sono state
verificate successivamente con build/runGametest, senza ulteriori modifiche client.
Due nuovi JUnit,7 GameTest sonno e2 GameTest uova rispetto alla0.1.17.

Il test difesa proprietario era inizialmente scaduto al default20tick prima della
sua asserzione al45; timeout corretto a80, asserzioni mantenute e prova passata.
NoAI congela automazione del sonno, che parte solo in libero; non dichiarare
parità di ogni dettaglio originale. Prova client NoAI è grafica, il ciclo notturno
è verificato nei GameTest server. Nessun collaudo con due veri client, shader
esterni o misure prestazionali del doppio modello degli occhi.

Port completo e tappa1 ancora incompleti. Restano riproduzione, shake prey/caccia,
stendardo e altre lacune nello stato cumulativo. Consegnare il checkpoint e
sospendere il PC, come richiesto; riprendere quando PC/quota saranno disponibili.


## Revisione 0.1.17 — 24 ottobre 2026

Review-agent locale in sola lettura senza delega al diff completo daed4537539,
inclusi server, pose, GUI, nuove prove client/server e documenti. Confrontati
IafDragonFlightManager/IafDragonLogic/EntityFireDragon e API GUI26.3 locali.

[P2] Azzerare la carica nel passaggio aria-terra — FireDragon.tickAutonomousCombat.
La carica hover blast poteva proseguire come animazione vuota sulla terra per
il bersaglio invariato. Corretto fuori revisione azzerando stato e precedente
bersaglio alla transizione; nuovo GameTest richiede morso con carica fresca
entro2tick. Rilettura finale: No findings.

Prova grafica delle pose estese ha motivato il ritaglio esplicito della preview
al riquadro GUI. Test client legge pixel chiari di tutte le sei righe delle
statistiche sia durante coda sia durante ali; screenshot ispezionati.

58 JUnit,51 GameTest mod (più always_pass escluso) e2 test client passati.
Cinque nuove prove server includono movimento/danno, proiettile, inversione,
decollo per bersaglio in quota e reset della carica aria/terra. Aggiornato il
vecchio fallback di difesa aerea per richiedere attacco effettivo in volo.
Limiti espliciti: selezione modalità180tick, vettore scorch max30 e navigazione
lineare con atterraggio se bloccata; non è parità completa del pathfinder.
Nessuna prova con due veri client o grafica dei tre attacchi aerei in mondo.


## Revisione 0.1.16 — 24 ottobre 2026

Review-agent locale applicata in sola lettura al diff completo da17364c28c,
compresi nuovi goal/test, rendering, documenti, versione e registrazioni.
No findings. Confrontati DragonAITargetItems, IafDragonLogic.updateDragonAttack,
DragonTabulaModelAnimator e EntityFireDragon.shootFireAtMob, con API26.3 locali.
Verificati stack/portata/pickup delay, stato transitorio, reset su target diverso,
contraccolpo server, emissione20 della carica, pose e controlli già confermati.

58 JUnit e46 GameTest della mod passati, più baseline vanilla escluso; un test
client grafico in mondo/screenshot passato. Cinque nuovi GameTest con movimento,
consumo, danni, proiettile reale, sincronizzazione e reset. Arene cibo isolate
oltre FOLLOW_RANGE per evitare concorrenza tra draghi di test diversi.

Limiti: niente prova visiva delle nuove pose, due veri client o modalità aeree.
Selezione carica1/5 fra stream invece del ramo originale per tick, esplicitata;
EAT chiude gradualmente in20tick, anziché troncare keyframe originali da25.
Nessuna dichiarazione di tappa1 completa. Continuare il goal autorizzato.


## Revisione 0.1.15 — 24 ottobre 2026

Skill locale review-agent applicata in sola lettura senza delega al diff completo
da208773ef1: sorgenti comuni, configurazione, input, dismount, nuovi file,
registrazione test, versione e documentazione. No findings.

Confrontati breakBlocks/isBreakable originali, soglie durezza, cooldown/drop
originali e shootFireAtMob; letti i percorsi vanilla26.3 DismountHelper,
LivingEntity.stopRiding/dismountVehicle e caduta. Nessun nuovo mixin comune.
Verificati reset fra pilota/IA, portata/alleati/ostacoli, griefing e controllo dei
fronti UP senza contare i pacchetti come tick. La distruzione fisica è limitata
alla vegetazione, distinta dal terreno del soffio già esistente.

56 JUnit,41 GameTest della mod senza fallimenti/errori (42 con baseline vanilla
always_pass escluso dal conteggio),un test client in mondo/screenshot riusciti.
Sette nuovi GameTest includono movimento reale del drago125giorni oltre la
barriera di tronchi/foglie, danni, caduta dopo dismount in aria, uscita laterale,
fallback su pilastro isolato e doppio salto con sollevamento da montato.

Due errori iniziali nella preparazione dei nuovi test sono stati corretti:
salute2000 superava il cap vanilla1024 (usata1000); il primo pilastro era vicino
al pavimento dell'arena e offriva un'uscita laterale valida (alzato di20blocchi).
Le asserzioni di assenza danno prima del warmup e protezione del fallback sono
rimaste. I limiti originali di IA/attacchi aerei e multiplayer reale restano aperti;
nessun collaudo visivo in prima persona delle nuove azioni è dichiarato.


## Revisione 0.1.14 — 24 ottobre 2026

Skill locale review-agent applicata in sola lettura, senza delega, al diff completo
rispetto a05846b062, inclusi file nuovi, integrazione goal, temporizzazioni,
flight manager, test, versione e documentazione. No findings.

Confrontati EntityDragonBase.registerGoals, DragonAIAttackMelee e la finestra
16..24 di IafDragonLogic.updateDragonAttack, più i percorsi delle API vanilla26.3
nei JAR locali. Controllati ingresso/uscita da pilota, seduto/NoAI/spalle,
cambio/rimozione target, visibilità, protezione alleati e interazione col volo.

Build e verifica finali:53 JUnit senza fallimenti/errori,34 GameTest della mod
senza fallimenti (35 totali includendo always_pass vanilla escluso dal conteggio),
un test client grafico con mondo temporaneo/32icone/GUI head-tail superato.
Sette nuovi GameTest con movimento, selezione target e danni server effettivi.
Il bersaglio offensivo del proprietario è simulato tramite la sua memoria di
attacco vanilla; le prove difensive applicano realmente danno a proprietario/drago.

Lacune: nessuna prova visiva del combattimento né due veri client. In aria il
drago atterra prima di combattere, comportamento transitorio esplicito; non sono
migrati attacchi aerei, caccia o soffio autonomo. Tappa1 incompleta, punte aperte.
La0.1.13 è ora confermata dall'utente; le novità0.1.14 richiedono suo collaudo.


## Armature e seguito 0.1.13 — 23 ottobre 2026

Review-agent applicata in sola lettura al diff completo da8476a4143, inclusi
nuovi file, test e build/source set client, senza delega. No findings.
Confrontati ordine submit dei layer vanilla26.3, assegnazione slot/materiali,
asset originali, percorsi owner/comandi/pilota e navigazione. Teletrasporto
richiesto dall'utente: controlli dimensione, proprietà, distanza, NoAI, veicoli,
chunk caricati, world border, AABB/supporto/fluidi e azzeramento momentum.
Letti anche il minimo di crociera0.6 e la guardia per i bersagli ripidi
(fattore orizzontale minimo0.2), con regressione JUnit e movimento GameTest.
Tentativi di teletrasporto limitati a uno al secondo; nessun nuovo payload client autorevole.

53 JUnit e27 GameTest della mod superati, oltre al baseline vanilla escluso.
Un ClientGameTest crea un mondo temporaneo, risolve i32 modelli item, verifica
slot head/tail ricevuti sul client e fotografa griglia/GUI su GPU. Confronto
anche col vecchio renderer: head/tail assenti dal modello prima, presenti dopo;
icone isolate visibili in entrambe le prove. PNG/UV non modificati. Source set
di test separati e controllati come esclusi dal JAR di produzione.

Limiti: nessun server con due veri client, nessun benchmark con molti draghi;
teletrasporto solo con drago simulato e destinazione sicura caricata. Il volo
non è un pathfinder completo per grotte/edifici; combattimento autonomo e altre
lacune restano aperte. I risultati grafici non confermano il profilo dell'utente.

## Volo autonomo e nido 0.1.12 — 23 ottobre 2026

Skill locale review-agent applicata in sola lettura al diff completo da
91f9d4baa, inclusi nuovi file, test, fixture e launcher Windows, senza delega.
No findings. Controllati anche il test aggiuntivo del ciclo completo da 42 blocchi
e il diff finale senza errori whitespace. Nessuna modifica durante la revisione.

Confrontati IafDragonLogic, IafDragonFlightManager, DragonAIEscort,
DragonAIReturnToRoost, DragonUtils e i default di IafConfig; verificata l'API 26.3
nel bytecode locale (isBrightOutside, world clocks, collisioni e GameTest).
Controllati autorità server, limiti età/pilota/spalle, precedenza dei comandi,
rilascio e reset dello stato, persistenza e sincronizzazione hovering, dimensione
della casa, guardie su bersagli verticali e velocità zero, cap delle accelerazioni,
interruzione al target non raggiungibile. Il ramo di simulazione cavalcato conserva
camera, input, seduta e un solo passeggero; nessun nuovo payload o mixin.

Build pulita con JDK25: 50 JUnit senza errori e 23 GameTest della mod superati,
più baseline vanilla escluso. Nuovi test: sterzo e avanzamento numerico, posa
hovering confrontata agli asset originali, crescita/clearance/NoAI/pilota,
decollo-seguito-seduto con movimento e atterraggio reali, NBT/sync stadio2,
limite 2000, ritorno a piedi e dall'aria, ciclo completo da 42 blocchi, comandi e
dimensione. Fixture 48x96x48: il cubo standard limitava artificialmente le prove.


Client 0.1.12 avviato in run/0.1.12-smoke con init script locale che cambia
soltanto la directory di esecuzione: caricati mod, OpenGL, audio e atlanti.
Terminato dall'agente dopo il caricamento. Nessuna entrata in mondo o verifica
visiva del drago; avvisi OSHI/Realms del profilo di sviluppo non bloccanti.

Rischi residui: il manager aereo verifica visibilità e destinazione ma non aggira
ogni ostacolo complesso; nessun benchmark con molti draghi, collaudo interattivo o
multiplayer con due veri client. Acqua/lava complete, combattimento autonomo,
sonno e altre lacune della tappa 1 ancora aperti. Punte grigie non modificate.

## Alimentazione e comandi 0.1.11 — 18 ottobre 2026

Review-agent applicata in sola lettura al diff completo da 0be657b51, inclusi nuovi
file/test/risorse, senza delega. Due P2 individuati e corretti fuori dalla fase di
revisione: posa seduta basata su orderedToSit vanilla non sincronizzato; controllo
del raggio casa che nella 26.3 legge direttamente i campi interni anziché i getter.
La posa usa COMMAND sincronizzato e isWithinHome è migrato per BlockPos e Vec3.
Riletti codice finale, interazioni e chiamanti senza altri difetti: No findings.

Confrontati EntityDragonBase.mobInteract, FoodUtils, DragonAIEscort, DragonAIWander,
HomePosition, ItemDragonStaff e default di IafConfig. Conservati ID, tre asset item,
valori di nutrimento/cure e comportamento del pasto creativo (può riassegnare anche
un drago già di proprietà). Dieta manuale non è riproduzione: isFood rimane false
finché la relativa meccanica non viene migrata. Nessun nuovo input di rete client.

Controllati autorità server, consumo creativo/sopravvivenza, vita/proprietario,
crescita disabilitata persistita, salute/fame, comandi persistiti e ingresso in
cavalcatura/spalle. Seguito non usa il teletrasporto del FollowOwnerGoal vanilla:
è solo il ramo a terra dell'originale; il volo autonomo è ancora assente. Casa limita
il vagabondaggio nella sua dimensione; ritorno notturno e config raggio non migrati,
si usa il valore originale predefinito 40. Test anche per coordinate zero e confine.

Build e 45 JUnit passati, 15 GameTest della mod passati (più baseline vanilla non
conteggiato). Sei nuovi GameTest verificano dieta/cure/consumi, proprietà, creativo,
crescita bloccata e NBT, comandi/casa/dimensioni/confine, camminata effettiva e stop,
animazione dopo applicazione dei soli dati sincronizzati. Fixture del mock creativo
ha instabuild esplicito; navigazione prova avviata a terra e limite 120 tick.
Client avviato con risorse, grafica, audio e atlanti; nessuna prova visiva in mondo
o con due veri client svolta dall'agente. Volo/combattimento autonomi e altre lacune
della tappa 1 restano aperti. Funzioni 0.1.10 confermate dall'utente, punte grigie aperte.

## Fuoco, anteprima e spalle 0.1.10 — 18 ottobre 2026

Skill review-agent applicata al diff completo da 237f00e83 in sola lettura, senza
delega. Finding P2 corretto fuori dalla fase di revisione: il piccolo portato mentre
il giocatore cavalca un adulto usava la posizione fisica del giocatore anziché la
seduta animata. Ora ricava la stessa trasformazione del mixin cavaliere. Riletto il
percorso finale senza ulteriori difetti concreti: No findings.

Confrontati EntityDragonBase.updateRiding, interazioni e ridingProgress, posa originale
sitting_shoulder e DragonTabulaModelAnimator; controllati proprietà, stadio, capacità,
rilascio, dati sincronizzati e conservazione delle entità nel salvataggio del mondo.
La 26.3 non permette Entity.startRiding su PLAYER: verificato con GameTest e bytecode.
Aggancio indipendente con stato transitorio evita di bypassare questo vincolo e di
affidare il salvataggio dei piccoli ai passeggeri del giocatore. Ricaricamento rilascia
i piccoli, conservandone posizione/proprietario/inventario. Nessun mixin nuovo.

Flame.png originale byte-identica; celle UV 8x8 sulla griglia 4x4. Verificata la chiamata
virtuale a getU0/U1/V0/V1 nel renderer SingleQuadParticle della 26.3. Opacità graduale
all'emissione e all'arrivo; invariati danni/portata e rete del soffio. InventoryScreen
azzera la scala del render state: rimossa la divisione ridondante nella GUI.

Build finale e 45 JUnit passati, 9 GameTest della mod passati (baseline vanilla escluso).
Coperti persistenza e logout con veri registri Fabric, tre piccoli e quarto rifiutato,
proprietario, crescita e rilascio, ripristino della fisica, posa e reset senza accumulo.
Client avviato con risorse/grafica/audio/atlanti caricati. Resa visiva di getto, anteprima,
spalle e trasporto durante cavalcatura non verificata dall'agente in mondo; multiplayer
con due veri client ancora da provare. Tappa 1 e punte grigie restano aperte.

## Soffio, terreno e inventario 0.1.9 — 18 ottobre 2026

Skill review-agent applicata in sola lettura, senza delega, al diff completo dalla
0.1.8 (3267d64f8), inclusi nuovi sorgenti, risorse generate, Gradle e test. No findings.
Confrontati ParticleDragonFlame, ContainerDragon/GuiDragon, ItemDragonArmor,
BlockReturningState/BlockCharedPath, IafBlockRegistry e IafDragonDestructionManager.

Controllati percorso server del danno e payload solo visuale, osservatori/pilota,
scala e crescita della fiamma, soppressione locale del movimento del collo durante
il fuoco, reset della posa, raggio di trasformazione e blocchi protetti. La particella
usa PNG originale non modificata. La revisione numerica della crescita non sostituisce
il confronto visivo del getto originale o la prova di comfort in prima persona.

Inventario: cinque slot originali, convalida degli oggetti sul server, controllo di
proprietà e distanza, trasferimenti, aggiornamento armatura/overlay, NBT con componenti
e rilascio alla morte. Registri e asset delle 32 armature e dei sette blocchi controllati;
39 definizioni item risolte e 32 overlay presenti nel JAR. Testmod esclusa dalla consegna.
Il layer dello stendardo resta da migrare, pur essendo conservato correttamente nello slot.

Verifiche finali: 41 test JUnit, zero fallimenti/errori; 5 GameTest della mod nel
runtime Fabric, tutti superati. Il report contiene anche minecraft:always_pass, che
non è conteggiato fra i cinque test della mod. GameTest coprono registrazione/slot,
salvataggio e ricarica di un vero FireDragon, componenti ItemStack, accesso proprietario,
shift-click senza duplicazioni e chiusura a distanza, trasformazioni/loot/revert,
blocco del griefing e protezione casse/bedrock. Il bootstrap JUnit da solo non poteva
registrare gli item Fabric: i test relativi sono stati trasferiti al runner corretto.

Client di sviluppo avviato con grafica/audio/atlanti e nuove risorse caricate.
Nessuna verifica visiva in mondo della GUI, del getto o delle armature eseguita
dall'agente; nessun test di rete con due veri client. GameTest non coprono tutta
l'IA o gli effetti opzionali. DragonForge/esplosioni opzionali e altre funzioni
della tappa 1 rimangono da migrare; punte grigie ancora aperte.

## Combattimento controllato 0.1.8 — 18 ottobre 2026

Skill locale review-agent applicata in sola lettura e senza delega al diff completo
rispetto a c772f0331, inclusi nuovi file, configurazione, risorse e test. No findings.

Confrontati EntityDragonBase, EntityFireDragon, EntityDragonCharge, EntityDragonFireCharge,
IafDragonLogic, IafDragonDestructionManager e DragonTabulaModelAnimator. Copiati dati del
danno dragon_fire e suono breath originali; modello del proiettile resta un cubo di magma.
Input continua a essere validato dal server tramite veicolo/proprietario; timeout dei
comandi esistente e reset dello stato di combattimento senza cavaliere. Nessuna modifica
di collisione, cavalcatura, camera o decollo. Esclusi proprietario/alleati/passeggeri dai
bersagli, inclusi animali con riferimento allo stesso proprietario anche offline.
Intersezioni reali del raycast, ostacoli e lato server controllati nel percorso del danno.

Build riuscita: 38 test, zero fallimenti/errori. Undici test aggiunti: sei sullo stato
di combattimento, tre sulla geometria e due su keyframe/continuità/assenza di accumulo
delle pose. Verificate le risorse nel JAR. Questi test non istanziano un mondo server
e non dimostrano danni, collisioni del proiettile o assenza di fuoco amico in multiplayer.

Limiti intenzionali del checkpoint: sola cavalcatura; nessuna IA di attacco autonoma,
conversione in blocchi carbonizzati, distruzione dell'area da charge, DragonForge o
esplosione opzionale. Particelle FLAME vanilla temporanee: migrazione della particella
originale incompleta. Non è parità completa con l'originale. Verifica in gioco richiesta
con bersagli, muri, acqua, dismount e due giocatori. Tappa 1 e punte grigie ancora aperte.

Avvio client 0.1.8 riuscito in run/0.1.8-smoke: mod, grafica, audio e atlanti caricati;
configurazione di combattimento generata. Avvisi di contatori OSHI e autenticazione
Realms non bloccanti. Nessuna entrata in mondo e nessun collaudo del combattimento
eseguito dall'agente. Il primo comando di avvio aveva un percorso relativo errato
dell'init script; corretto con il percorso assoluto prima dell'avvio verificato.

## Rimozione funzione ghast 0.1.7 — 18 ottobre 2026

Revisione del diff rispetto alla 0.1.6: rimossi tutti i collegamenti alla piattaforma
del dorso in definizione dati, tick, travel, tickRidden, decollo e dismontaggio.
Eliminati helper e due test geometrici, oltre al test di decollo con dorso occupato.
I tre test rimanenti di DragonTakeoff conservano le verifiche di pressione breve,
spazio tenuto, mancato rilancio automatico e invalidazione dei comandi.
Unica seduta, aggancio grafico, camera, tilt e controlli non modificati. Dismontaggio
e collisione tornano ereditati da vanilla. Nessun dato persistente della piattaforma
richiede conversione: BACK_OCCUPIED era solo sincronizzato, non scritto in NBT.

Build clean build riuscita, 27 test senza errori. Controllo del JAR per escludere la
classe eliminata. Nessuna prova interattiva eseguita; istruzioni TEST-0.1.7.md.
Nessun ulteriore difetto individuato nella rilettura. Tappa 1 incompleta, punte aperte.

## Un solo passeggero 0.1.6 — 18 ottobre 2026

Difetto trovato: mobInteract usava startRiding(this, true, true), saltando il controllo
vanilla canAddPassenger. Corretto usando la monta ordinaria, con verifica esplicita
di posto libero/proprietario/stadio. Aggiunto couldAcceptPassenger per impedire più
di un passeggero anche nei percorsi force e caricamento NBT. Nel bytecode Entity 26.3,
startRiding controlla couldAcceptPassenger prima di verificare force: il controllo non
viene saltato. Nessun nuovo posto, offset di seduta o comportamento HappyGhast importato.

Rilettura del diff, dei percorsi interazione/passeggeri e dell'aggancio grafico: nessun
ulteriore difetto individuato. Collisione e dismontaggio conservati. Build e 30 test
riusciti; nessun nuovo collaudo interattivo. Verificare con due giocatori, comando ride,
rimonta dopo dismount e mondo salvato con passeggeri multipli: TEST-0.1.6.md.

## Diagnostica UV delle punte — 18 ottobre 2026

Nuovo test `everySpikeVertexKeepsOriginalTabulaFaceOrientationAndUv`: confronto dei
528 vertici delle 22 punte ground con facce, winding, normali, UV e mirroring di
Citadel TabulaModelRenderUtils.ModelBox/TexturedQuad. Sorgente di riferimento ufficiale:
https://github.com/AlexModGuy/Citadel/blob/master/src/main/java/com/github/alexthe666/citadel/client/model/TabulaModelRenderUtils.java
Il riferimento matematico è indipendente dal costruttore vanilla sottoposto a test;
non si limita a verificare UV finite o nell'intervallo. Test superato senza modifiche
al lettore Tabula. Suite completa: 30 test, 0 fallimenti, 0 errori.

Rilettura in sola lettura secondo review-agent, senza delega: No findings.
Nessuna modifica di produzione, asset o versione della mod. Analizzati i pixel delle
texture originali: colore osso e trasparenze presenti già negli asset; rapporto 2x tra
PNG e dimensioni UV logiche coerente. Questo non riproduce il rendering effettivo sul
profilo dell'utente né dimostra che l'aspetto vuoto dello screenshot sia corretto.
La causa visiva resta aperta. L'utente conferma riproduzione anche da fuori/di lato
con sole Ice and Fire e Fabric API. Necessaria un'immagine laterale ravvicinata per
il confronto visivo. Verificati successivamente alpha base 0/255 (un solo texel alpha 1),
soglia vanilla cutout 0.1, culling già disattivato e overlay maschile trasparente sui
campioni delle punte. Non individuata una differenza di soglia o UV da correggere.

## Dismontaggio sul dorso 0.1.5 — 18 ottobre 2026

Richiesta: restare sulla superficie solida del drago dopo il dismontaggio, come sul
ghast felice. Skill `review-agent` applicata in sola lettura, senza delega, all'intero
diff dalla 0.1.4 (`980dbe614`), inclusi helper, test e versione. No findings.

Confrontati i percorsi vanilla `LivingEntity.stopRiding`/`dismountVehicle`,
`Entity.removePassenger` e i metodi corrispondenti di HappyGhast. Il punto superiore
era già quello ereditato da Entity, ma mancava la pausa immediata di HappyGhast alla
rimozione del passeggero: il drago poteva continuare il movimento prima della scansione
del giocatore sopra. Aggiunta pausa di dieci tick, rinnovata dalla scansione esistente,
con arresto della navigazione e azzeramento dell'inerzia e dei vecchi comandi.

Posizione del dismontaggio esplicita al centro superiore della collisione. Il server
mantiene il percorso vanilla `dismountTo`, azzerando velocità e distanza di caduta del
passeggero; il client anticipa soltanto la pausa del drago. Nessun nuovo mixin, nessun
blocco piazzato nel mondo e nessuna modifica alla collisione centrale o alla camera.
Draghi morti/rimossi mantengono il fallback vanilla. I controlli V/spazio della 0.1.4
rimangono disponibili; la monta resta dallo stadio 3.

Build ordinaria JDK 25 riuscita: **29 test, 0 fallimenti, 0 errori**. Nuovo test:
giocatore dismontato entro i bordi, sopra la superficie senza intersecarla e riconosciuto
dalla scansione a 50/75/100/125 giorni, a tre quote del mondo. Questo è un controllo
geometrico, non un collaudo della sequenza di rete o dell'arresto in un mondo reale.
Nessun nuovo avvio client eseguito per la 0.1.5. Provare a terra/in volo, a ogni età,
con dismontaggi ripetuti e due client; la conferma in gioco è ancora necessaria.

## Volo e visibilità 0.1.4 — 18 ottobre 2026

Skill `review-agent` applicata in sola lettura, senza delega, al diff completo dalla
0.1.3 (`284f4d25e`), inclusi nuovo stato di decollo, test e traduzioni. No findings.
Letti i percorsi input client, payload, verifica proprietario, aggiornamento FLYING,
simulazione del veicolo e trasformazione di rendering. Verificati con le classi
vanilla locali la lettura di spazio e il momento della simulazione cavalcata.

Correzioni: avvio del volo prima di `super.tick`, richiesta V memorizzata anche se
arriva il rilascio prima del tick server; impulso applicato dal simulatore e non
consumato prima del distacco. Un contatto a terra momentaneamente perso non elimina
la spinta iniziale. Mantiene il volo già in corso senza ripetere impulsi in aria.
Richieste annullate senza proprietario/controlli recenti, allo stadio errato o con
discesa premuta; attesa breve sul dorso occupato e nessuna richiesta conservata
indefinitamente. Il nuovo bit TAKEOFF richiede la stessa 0.1.4 su server e client.

Il tilt positivo usa un limite graduale in funzione della distanza dai blocchi
calcolata con nove raycast sotto corpo e collo/testa; forme COLLIDER e fluidi ignorati.
La distanza di attenuazione è max(2 blocchi, renderSize / 4). La ricerca avviene a
20 Hz, solo quando il tilt è positivo, e non modifica collisioni fisiche o blocchi.
La prima persona disattiva il tilt solo se la camera è quella del pilota locale;
la seduta renderizzata usa lo stesso stato. Camera in terza persona invariata.

Build ordinaria JDK 25 riuscita: **28 test, 0 fallimenti, 0 errori**. Nuovi controlli
su pressione breve V e rimappatura, pressione/rilascio tra due tick server, spazio
continuo, richiesta bloccata/scaduta, tasto tenuto durante atterraggio, stadio/rilascio
controlli, impulso con contatto perso, curva continua di raddrizzamento a ogni età
cavalcabile e tilt locale di prima persona. Restano tutti i test precedenti.

Limiti: i test matematici non sostituiscono un collaudo del rilevamento di terreno,
gradini e sporgenze reali; non è stata misurata la prestazione con molti draghi.
I raycast campionano il corpo e non sono una collisione esatta del modello completo.
Serve prova utente di decollo, prima persona e atterraggio a 50/75/100/125 giorni,
oltre al test multiplayer reale. Nessuna dichiarazione di tappa 1 completata.
Avvio client 0.1.4 verificato in directory separata: mod, grafica, audio e atlanti
caricati. Client lasciato aperto per la prova; nessuna verifica visiva o interazione
del drago eseguita dall'agente. Avvisi OSHI/Realms del profilo di sviluppo già noti.

## Correzioni e dorso solido 0.1.3 — 18 ottobre 2026

Riscontro utente sulla 0.1.2: camera e comandi di schiusa funzionanti; segnalati decollo
incerto, assenza di inclinazione in discesa con X e cavaliere dentro al modello allo
stadio 5. Richiesto esplicitamente il dorso solido come quello del ghast felice.

Skill `review-agent` applicata in sola lettura, senza delega, a tutto il diff dalla
0.1.2 (`7e4c9fae5`), inclusi i nuovi helper/test. Confrontati `EntityDragonBase.updatePitch`,
`RenderDragonBase.scale`, geometria Tabula originale e bytecode vanilla 26.3 di
Entity/EntitySelector, HappyGhast, LivingEntityRenderer e controllo del movimento.

- [P1] Gestire le query di collisione senza richiedente —
  `fabric/src/main/java/com/github/alexthe666/iceandfire/fabric/dragon/FireDragon.java`,
  `canBeCollidedWith`. La prima implementazione chiamava `isPassengerOfSameVehicle`
  anche con argomento null. Il predicato vanilla di EntitySelector chiama proprio
  `canBeCollidedWith(null)` e quel percorso avrebbe generato NullPointerException.
  Corretto dopo la revisione con guardia null prima del confronto del veicolo.

Rilettura della correzione: No findings. Non rilevati ulteriori difetti concreti.

Implementazione: impulso iniziale di decollo sul lato che simula il movimento del
veicolo; inclinazione da spostamento verticale con limiti originali -60/+40 gradi,
smorzamento e stato sincronizzato per gli osservatori; identica rotazione sul modello
e sulla seduta. La seduta deriva ora dalla superficie del cubo Neck1, anziché dal
perno interno. Compensazione dell'altezza del bacino legata alla scala del giocatore,
separata dalla scala visiva non limitata del drago. Distanza camera non modificata.

Collisione solida sull'AABB centrale di tutti i draghi di fuoco registrati, passeggeri
esclusi; pausa del movimento mentre un giocatore non montato sta sopra e per dieci
tick dopo. Spettatori e giocatori senza collisione non bloccano il drago. Nuova funzione
di gameplay autorizzata dall'utente, non proveniente dalla base Ice and Fire originale.
Usate API vanilla: nessun nuovo mixin comune né nuova dipendenza.

Build ordinaria con JDK 25: **21 test, 0 fallimenti, 0 errori**. I nuovi controlli
coprono superficie effettiva del modello e bacino a 50/75/100/125 giorni, inclinazione
-60/0/+40 gradi e scala del giocatore 0.5/1/2, limiti/smorzamento dell'inclinazione,
impulso di decollo e riconoscimento dei piedi sopra il dorso.

Avvio della nuova 0.1.3 verificato dai log in una directory di prova separata:
mod, modello/risorse, grafica, audio e atlanti caricati; ingresso in un mondo locale
confermato. Client lasciato aperto. Nessuna interazione del drago o verifica visiva
eseguita dall'agente; presenti gli avvisi OSHI/Realms del profilo di sviluppo già noti.

Queste sono verifiche automatiche; non equivalgono a una prova interattiva del volo,
del dismontaggio o dell'appoggio in un mondo/server con due giocatori. La superficie
solida segue la collisione centrale, non la geometria animata di ali/coda. Resa della
seduta e del tilt da confermare in gioco. Punte grigie ancora aperte, tappa 1 incompleta.

## Revisione finale 0.1.2 sul nuovo PC — 18 ottobre 2026

Skill locale `review-agent` applicata in sola lettura, senza delega, all'intero diff
`d0a80e73f..417f5a8d2`, inclusi documenti, nuovi file, test e mixin client. Confrontati
animatore, layer del cavaliere e curve della seduta con i sorgenti originali in `src/`.
Verificata anche nel bytecode vanilla 26.3 la moltiplicazione CAMERA_DISTANCE × scala
del veicolo e la sottrazione dell'attachment del passeggero.

- [P2] Attenuare il bob del volo durante la transizione —
  `fabric/src/client/java/com/github/alexthe666/iceandfire/fabric/client/FireDragonPoseModel.java:111`.
  Con `flying=false` e un progresso di volo ancora positivo, il corpo riceveva tutta
  l'oscillazione da 2,5 unità del modello. A progresso zero l'oscillazione spariva in un
  fotogramma: a 10 tick lo scarto era circa 2,27 unità, amplificato dalla scala visiva
  e trasferito alla seduta animata. Difetto introdotto dalla nuova animazione secondaria.

Correzione eseguita dopo la fase di revisione: moltiplicare il bob per `flyProgress / 20`.
Il test `flightBobFadesContinuouslyAtTakeoffAndLanding` falliva prima della correzione
e passa dopo, verificando corpo e aggancio del cavaliere vicino al confine e l'inizio
del decollo in tre fasi dell'oscillazione. Rilettura della correzione: No findings.
Non sono emersi altri difetti concreti nel diff esaminato.

Build ordinaria `fabric/gradlew.bat -p fabric build --console=plain` riuscita con
Azul Zulu JDK 25.0.4.1+1 e Gradle 9.7.1, senza init script diagnostici o cache preparata
manualmente. Suite completa: **17 test, 0 fallimenti, 0 errori**. Rimangono avvisi
di deprecazione di API/Gradle; non impediscono la build.

I test automatici non validano la resa delle punte grigie né la prima persona.
Client di sviluppo avviato sul nuovo PC: mod, backend grafico, audio e atlanti caricati;
i log confermano anche l'ingresso nel mondo locale `New World`. Nessuna interazione
con il drago o verifica visiva eseguita dall'agente. Client lasciato aperto per la prova.
Avvisi/errore diagnostico OSHI sui contatori prestazioni Windows e autenticazione
Realms del profilo di sviluppo presenti nel log; non hanno impedito il mondo locale.
Restano necessari test in mondo a 50/75/100/125 giorni, persistenza e multiplayer reale.
Le punte grigie sono ancora aperte; la tappa 1 è incompleta.

## Revisione del primo checkpoint (storica)

## Correzione comandi 0.1.1

Skill `review-agent` applicata in sola lettura al diff della correzione, inclusi test e traduzioni.
No findings.

Verificati i codici SDL di Minecraft 26.3, la registrazione tramite Fabric, il percorso input
client/pacchetto/controllo verticale e la categoria con le sue traduzioni. Otto test superati:
i due nuovi test verificano X/R/G, riassegnazione, rilascio, disattivazione e serializzazione
del tasto di discesa. Build eseguita con lo stesso init script locale documentato nel README.
La visibilità nel pannello comandi, la conservazione dopo riavvio e la discesa effettiva in un
mondo richiedono ancora la prova dell'utente con questo JAR. Nessuna dichiarazione di completamento
della tappa 1.

Skill applicata: `review-agent`, dopo il primo ciclo di test. Revisione in sola lettura del diff
rispetto alla base `0526aad94119b705a86207623481b087c4d052ca`; nessuna delega della revisione.

## Risultati iniziali e correzioni

- [P1] Registrare i renderer delle nuove entità — `src/client/java/com/github/alexthe666/iceandfire/fabric/client/IceAndFireFabricClient.java:6`.
  Le entità registrate senza renderer non possono essere visualizzate dal dispatcher client.
  Corretto con renderer dedicati e caricamento del modello originale.
- [P2] Conservare le dimensioni originali dell'uovo — `src/main/java/com/github/alexthe666/iceandfire/fabric/IafContent.java:40`.
  La prima implementazione usava 0,6 × 0,8 invece di 0,45 × 0,55, alterando collisioni e piazzamento.
  Corretto confrontando `IafEntityRegistry` originale.
- [P2] Trasferire il nome personalizzato all'uovo piazzato — `src/main/java/com/github/alexthe666/iceandfire/fabric/IafContent.java:58`.
  Il piazzamento perdeva il componente del nome prima della schiusa. Corretto copiando `CUSTOM_NAME`.

I riferimenti sopra identificano il primo diff revisionato, prima delle successive aggiunte.

## Rischi residui

Sei test automatici superati. Client e server hanno superato soltanto i controlli iniziali descritti
nel README. Nessun mondo è stato usato per collaudare l'entità, la persistenza o il controllo in rete.
La build ordinaria nel sandbox rimane bloccata dal problema ZIP del runtime Windows.
Le funzioni ancora da migrare sono elencate nel README e impediscono di considerare conclusa la tappa 1.
