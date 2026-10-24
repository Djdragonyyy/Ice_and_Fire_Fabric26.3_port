# Prompt completo per continuare il porting su un altro PC

## Checkpoint 0.1.29 — ruggiti, effetti e battito d'ali

Ruggito normale40/epico60tick, sei pose Tabula e voci originali per età;
pitch aggiuntivo del corpo epico interpolato. Trigger casuale1/500 e reazione
al danno1/4, con precedenza di combattimento/pasto/sonno/pilota/NoAI/spalla.
Aura adulti con zona expandTowards positiva originale30/60, forza a proprietario
e pet, debolezza agli altri, esclusione draghi di stadio uguale/superiore.
Riconosciuto ID earplugs per il ramo epico; oggetto ancora della fase3.
Battito d'ali originale al ciclo2, quattro OGG e configurazione originale
dragonFlapNoiseDistance4 (0..10000); broadcast server unico, audio HOSTILE.

Suite finale ripetuta sul codice definitivo:73JUnit,102GameTest mod più baseline,
7prove client e17immagini, tutte passate. Vedere TEST-0.1.29.md.
Ripresa effettiva24ottobre22:12 dopo reset quota, controlli5min ripristinati.
Non sospendere/spegnere. Prossimi: acqua/lava e navigazione complessa, oltre alle
lacune cumulative. Tappa1 e port completo restano aperti.

## Ultima istruzione utente — niente sospensione, ripresa al reset

Il 24 ottobre, dopo il risveglio della0.1.23, l'utente ha annullato esplicitamente
ogni futura sospensione del PC: continuare fino al completamento del port.
La sospensione precedente è riuscita e il helper è terminato. NON lanciare più
suspend-after-delivery.ps1 o altri comandi di sospensione/spegnimento. Questa
istruzione prevale su TUTTE le note storiche sotto, negli altri documenti e sulla clausola di sospensione rimasta nei metadati del vecchio goal. Obiettivo operativo: porting completo con test e consegna, senza sospensione. Il vecchio script è disabilitato con un errore immediato e non richiama più API di alimentazione.

Usare la quota residua per progresso reale e riprendere appena il limite d'uso
si reimposta, nella stessa chat e con lo stesso goal completo, senza nuove chat
o lavorazioni duplicate. Non consumare quota artificialmente e non dichiarare
completate fasi con funzioni mancanti. Al risveglio controllo quota93%; reset
indicato dall'app24ottobre12:38:03UTC (14:38Roma). Verificare stato attuale.

Verificati 73 JUnit, 102 GameTest della mod (103 includendo la baseline vanilla) e 7 prove client con diciassette screenshot. Nessuna prova con due veri client.

## Checkpoint 0.1.28 — cristallo e caricamento dei draghi

Cristallo fuoco: UUID/nome in CUSTOM_DATA, flag CrystalBound nativo salvato e
sincronizzato, modelli/texture originali attivo/vuoto. Legame e richiamo tramite
interazioni native, stesso drago/UUID e inventario; dati svuotati solo al successo.
Overworld come originale, proprietà corrente/permessi e veicoli occupati protetti;
corpo/ostacoli/fluidi/bordo/quote e chunk destinazione controllati. Legami non
sovrascritti e fallimenti conservano dati. Creativo usa il vero stack in mano.

SavedData nativo dragon_positions per livello, provato con disco/riapertura.
Indice all'entrata/uscita tracking e ogni20tick per draghi legati; scarto/morte
rimuovono. Callback con motivo nullo conserva posizione (tracking può terminare
prima della rimozione). chunkLoadSummonCrystal defaulttrue originale: false
ammette solo entità caricate. Ticket sorgente temporaneo non persistente2001tick,
richiesta con timeout2000; cleanup su successo/annullamento/timeout/serverstop.
Richieste separate per mondo/UUID/giocatore/mano/stack/destinazione; ticket di uno
stesso chunk mantenuto finché resta una richiesta. Nessuno stato sul singleton Item.

Sei nuove prove isolate passate, poi anche la settima con reale salvataggio,
scaricamento/assenza UUID dal mondo, caricamento/ricomparsa e richiamo con stesso
proprietario e ticket rilasciato. Fixture corregge callback Runnable (riusarlo
cancellava il successore) e dà5ms ai worker reali del server GameTest senza attesa.
Timeout mod/orologio mondo invariati. Tappa1 ancora aperta, niente sospensione.

## Checkpoint 0.1.27 — flauto e palpebre

Flauto originale dragon_flute, due mani, cooldown60tick, suono e texture/modello
originali. Effetti server su draghi propri caricati nel livello; box originale
con16*dragonFluteDistance suX/Z e256 suY. Configdefault4,0..10000, vecchi JSON
compatibili. Interrompe Flying/Hovering mantenendo comando/target/pilota e regole
di caduta; riattivazione successiva segue controlli/IA originali del port.

Blink originale tickCount%50>43: closed skin sleeping e occhi emissivi spenti,
posa/IA del sonno invariate. Nessun dato salvato o payload aggiunto. Nuove prove
server per zona/proprietà/cooldown/pilota/caduta/ciclo; client esteso con native
useItem/cooldown/volo, icona/audio e preview deterministiche aperto/chiuso.

Una precedente prova di fine combattimento richiedeva sempre NONE dopo rimozione
del target: ora la nuova voce SPEAK è legittima. Fixture sazia contro nuove
cacce adiacenti e voce esplicita al35: al40 target nullo/SPEAK, danno dietro il
muro ancora nullo. Altri controlli di fine attacco/pasto ammettono solo NONE o
SPEAK; NoAI mantiene NONE rigoroso. Logica di combattimento non modificata.
Tappa1 incompleta e nessuna sospensione. Suite completa finale passata:70JUnit,92GameTest mod,5prove client/14screenshot.

## Checkpoint 0.1.26 — corno del drago

Item dragon_horn, raccolta proprietario/main hand e rilascio su faccia UP.
Entità salvata nativamente in CUSTOM_DATA, UUID/inventario e tutti i dati
mantenuti; cancellazione contenuto solo dopo addFreshEntity riuscito.
No cattura con passeggeri/veicolo/spalla, dati occupati mai sovrascritti.
Rilascio valida tipo/UUID, permessi, corpo/bordo/quote e chunk già caricati;
UUID già caricato cercato in tutte le dimensioni. Nessun registro globale
anti-copia per entità non caricate. Originali texture/modelli vuoto/fuoco,
flag modello nativo, tooltip nome/sesso/stadio/giorni. Ricette restano fase3.

Revisione: Player.interactOn usa una copia in creativo; aggiornare il vero
corno in mano come originale. Prova dedicata su quel ramo nativo. MockPlayer
non è registrato nel mondo: roundtrip server verifica EntityReference UUID;
client connesso verifica isOwnedBy dopo rilascio. Abilità Creative/Adventure
inizializzate esplicitamente nei mock, helper semplice non lo fa. Cinque prove
isolate passate, poi suite completa e nuova prova client passate.
Tappa1 e goal completo ancora incompleti. Nessuna sospensione del PC.

## Checkpoint 0.1.25 — voci e mandibola

Nove eventi originali idle/hurt/death con fasce stage1/2 child,3teen,4/5adult,
intervallo90 e campioni/sottotitoli/volumi originali. File OGG byte-identici.
SPEAK20tick sincronizzato, sola mandibola18gradi eZ0.2, interpolazione5/10/5.
Priorità di attacchi/soffio/pasto/sonno/morte/NoAI; cosmetica non persistente.
Una regressione NoAI rilevata nella prima suite ha portato al gate esplicito
su avvio e annullamento. Test aggiunti per risorse, posa, voci e durata/stati;
client verifica sound manager e screenshot SPEAK. Ruggiti restano da migrare.
Non dichiarata prova di ascolto manuale o multiplayer con due veri client.
Nessuna sospensione del PC. Tappa1 ancora incompleta; continuare dopo consegna.

## Checkpoint 0.1.24 — casa e quota di volo

Ripresa dalla 0.1.23 dopo reset quota, nessuna sospensione futura autorizzata.
Migrati maxDragonFlight256 (min100) e dragonWanderFromHomeDistance40 (1..10000)
con nomi e limiti originali. Radius condiviso da restrizione terra e scelta
casuale aerea; quota assoluta delle destinazioni autonome limitata anche dal
mondo e dalla taglia. Nessuna barriera al pilota o modifica della velocità.
Vecchi JSON compatibili. Nuovo GameTest salva/ricarica casa e verifica raggio,
confine e due limiti di quota; estesa validazione JUnit. Tappa1 ancora aperta.

## Checkpoint 0.1.23 — configurazioni del ciclo vitale

Base e7d3d4cc2. Migrati dragonHealth500, dragonAttackDamage17,
dragonHungerTickRate3000 e dragonEggTime7200, con limiti originali e validazione.
Vecchi JSON conservano default; filename iceandfire-combat.json invariato.
Curve crescita originali e cap nativi Minecraft conservati, riavvio richiesto.
Hunger modulo intervallo positivo. Schiusa strettamente oltre soglia; contatore
incubazione long/NBT long con lettura compatibile dei vecchi int, per supportare
anche eggTime=Integer.MAX_VALUE senza overflow. Config invalida non sovrascritta.

Due JUnit e due GameTest nuovi, poi suite completa server/client. Proseguire dopo ultimo controllo, commit/push/pacchetto; sospensione annullata.
Goal completo resta attivo/incompleto: riprendere quando PC/quota disponibili;
non iniziare processi mentre il helper di sospensione è in attesa o PC sospeso.
Prossimi: configurazioni di volo, interazioni/animazioni/suoni, acqua/lava,
limiti navigazione e prove multiplayer nello stato cumulativo. Tappa1 aperta.

## Checkpoint 0.1.22 — shake prey

Base7eeb6cfd5. Attacco SHAKE65tick con pose grab1/2 e grab_shake1/2/3 originali;
movimento preda da updatePreyInMouth. Prima azione nuovo target resta morso,
poi scelta4attacchi; SHAKE tenta presa1/2, altrimenti morso. Solo libero, terra,
stage2+, mob largo<metàcorpo, visibile/vicino e privo di altri veicoli/passeggeri.
Giocatori esclusi intenzionalmente dalla presa in questa fase; restano solo
piloti proprietari. Un solo occupante: pilota oppure preda, nessuna seconda sella.

Presa nativa passeggero+ID sincronizzato separato dal pilota, NoAI preda invariato.
Drago fermo, niente sonno/decollo casuale. Dopo55tick: danno2*ATTACK_DAMAGE una
volta, cura50% se abilitata e +1fame; rilascio e recupero fino65. Comandi/NoAI,
morte, nuovo alleato, acqua/volo o bersaglio diverso interrompono. Pilota che
sale dopo il rilascio ha precedenza sui fotogrammi residui. Nessun cambiamento
al dismount/immunità o al dorso. Corpo yaw allineato come originale durante presa.

NBT UUID+GripTick e passeggeri vanilla, caricatore ricorsivo nativo verificato;
riprende windup residuo. refreshDimensions prima di validare preda caricata,
altrimenti il primo tick usa ancora dimensioni baby e la sgancia erroneamente.
Fixture create istantaneamente devono aggiornare dimensioni e simulare anche
l'incremento tickCount che normalmente fa ServerLevel prima di Entity.tick;
non alterare produzione fame per adattarla a fixture manuali ferme a tick0.

Aggancio visivo originale testa/collo e rotazione preda migrati con mixin client;
nessuna copia del mob. Stato rotazione azzerato al rilascio, scala del mob
indipendente dalla crescita del drago. Visibilità preda delegata al portatore,
regressione frustum riprodotta e corretta; screenshot ravvicinato in mondo.
Anteprima GUI solo drago, passeggero verificato nel mondo, shader non provati.

Cinque nuovi GameTest, quattro JUnit (movimento/aggancio), regressioni posa estese e test client
combattimento esteso a presa/passeggero/pose e statistiche. Non equivalgono a
riavvio completo del processo con due veri client. Tappa1 ancora incompleta.

## Checkpoint 0.1.21 — caccia autonoma

Base d3bfca45b. DragonAITarget/NonTamed e tag originali migrati: ricerca selvatico
con intervallo5, fame probabilistica e giocatori survival; alimentare visibile
intervallo3 con fame<90/taglia/dieta. Addomesticati non cacciano giocatori,
abitanti/golem o pet già addomesticati. Comandi, sonno, NoAI, controllo, ritorno
notturno e amore hanno precedenza. Difesa priorità1..3 resta prima della caccia5/6.
Una nuova caccia non sostituisce mai un target esistente, anche assegnato fuori
dai goal: regressione cambio bersaglio aveva rilevato il problema, corretta.

Carne raggiungibile riservata prima della NUOVA caccia; foodGoal conserva il
candidato fra target selector e movement selector. Politica esplicita del port,
non parità di ogni dettaglio temporale della confusa priorità MOVE originale.
Nuova config originale dragonTargetSearchLength128, validazione1..10000,
attributo cap2048 e ricerca3D. Tag nel nuovo percorso tags/entity_type.

Scenario difesa dal sonno aggiornato da mucca a zombie NoAI: la mucca adesso era
una legittima preda prima del riposo. Mantenute tutte le asserzioni sonno,
risveglio, bersaglio e danno. Test client combattimento ora acquisisce la mucca
tramite caccia reale, senza setTarget manuale, poi attende pose coda/ali.
Aggressori zombie nei test resi persistenti: l'altro giocatore reale faceva
despawn vanilla a distanza, osservato come DISCARDED e lastHurt=null. La prova
isolata passava; non indebolite le asserzioni della suite completa.
Tappa1 ancora incompleta; prossimi shake prey e le altre lacune cumulative.

## Checkpoint 0.1.20 — stendardo

Base6a20582f3. LayerDragonBanner originale migrato a render state/submit26.3:
BodyUpper animato, offset(0,-0.2,0.4), rotazioneX180 e scala inversa del drago.
Renderer vanilla dello stendardo conserva colori/motivi. Inventario0 resta unica
fonte salvata/drop; snapshot ItemStack sincronizzato con copia difensiva, anche
a menu chiuso. Caricamento ricrea lo snapshot; rimozione/invalidi lo svuotano.
Invisibilità nasconde il layer. Nuova prova server, matematica e quarto test
client con pattern due strati, equipaggiamento/rimozione e screenshot reali.

Prossimi: caccia e shake prey, acqua/lava/configurazioni e animazioni mancanti.
Tappa1 ancora incompleta. Autonomia continua; sospensione richiesta a fine test.

## Checkpoint 0.1.19 — riproduzione

Ripresa dopo la sospensione S3 richiesta: il registro conferma sospensione e
successivo risveglio riusciti; nuova finestra di quota disponibile. Autonomia
utente confermata, continuare tappa1 e sospendere di nuovo a fine sessione.

Fire stew originale in creativo, due adulti stage4+ (75giorni), sesso opposto,
liberi a terra senza bersaglio/controllo, entrambi innamorati. Ricerca entro180,
cammino1, corteggiamento60tick, distanza<35 e visibilità. Un solo uovo casuale
senza proprietario; raccolta/ripiazzamento assegna il proprietario. Cooldown6000
vanilla salvato, indipendente dai giorni del drago. Sonno/volo casuale sospesi
in amore. Nido3x3 con centro sentiero, protetto da griefing/blacklist/contenitori/
fluidi/durezza/chunk/bordo; mancato spawn non consuma amore o modifica terreno.
Uovo non spingibile come originale: impedisce ai genitori di spostarlo dal nido.
Asset originali byte-identici, nessuna ricetta/progressione aggiunta prima fase3.

Prossimi: stendardo grafico, caccia/shake prey, acqua/lava e altre lacune nello
stato cumulativo. Tappa1 ancora incompleta, nessun passaggio alla2.


## Checkpoint 0.1.18 — 24 ottobre 2026

Base824e05454. Utente a dormire, autonomia completa e test di ogni blocco.
Quota5ore prossima al limite: checkpoint verificato, push/consegna e sospensione
PC richiesta. Goal completo resta incompleto; riprendere quando PC/quota saranno
disponibili, senza avviare nuovi processi locali durante la sospensione.

Sleeping sincronizzato/persistito separato dal letto vanilla. Solo libero,
notte, a terra/supportato, senza target/pilota/spalle; casa prima del riposo.
Risveglio per danno/target/comandi/interazione/acqua/volo/supporto perso; giorno
per addomesticati, selvatici già addormentati al coperto possono restare a dormire.
Difesa proprietario dal sonno: consenso selezione target separato dal movimento,
setTarget sveglia prima dei goal. Attesa20tick post combattimento/interazione,
memoria vanilla aggressore può prolungarla. NoAI congela automazione. Nuova
config doDragonsSleep=true; sonno non avviato in seduto/seguimi prioritari.

Posa sleeping originale/transizione40tick, texture di4varianti*5stadi e occhi
emissivi HeadFront, nascosti dormendo/invisibili/morti. Sottomodello con skipDraw
preserva trasformazioni evitando emissione di UV estranee. Ordine pelle0/occhi1/
armature2..5, test e client confermano head/tail. PNG/UV originali invariati.
Terzo test client: Sleeping salvato/NoAI, screenshot sonno/risveglio ispezionati;
il ciclo notturno e la difesa sono provati sul server, non dalla schermata NoAI.

Uova: colpo non di fuoco restituisce1oggetto del colore corretto e rimuove
entità, senza doppio drop. Fuoco/lava ignorati, bypass rimuove senza drop come
originale. Il nuovo oggetto non conserva incubazione/proprietà (getItem originale).

Verificati60 JUnit,60 GameTest della mod (61 con baseline vanilla escluso) e3 test client con mondi/screenshot. Uova verificate sul server dopo le prove client, senza altre modifiche grafiche. Nessun test con due veri client.


## Checkpoint 0.1.17 — 24 ottobre 2026

Baseed4537539. Migrati i percorsi di combattimento aereo di IafDragonFlightManager:
TACKLE verso la sommità del bersaglio, danno stage*3 a contatto/visibilità e
ritorno a terra; HOVER_BLAST a quota5+stage*2, offset20 e cariche con emissione20;
SCORCH_STREAM lungo il vettore iniziale, inversione e soffio server. Decollo per
bersaglio con deltaY>5 dallo stage2; pilota sempre3. Posa attack_tackle sincronizzata.
Scelta nuova modalità ogni180tick e vettore scorch max30 sono semplificazioni
esplicite del port. Destinazione/linea/chunk controllati, fallback atterraggio se
rotta bloccata con attacchi sospesi. Non è un pathfinder per aggirare edifici.

Corretto il passaggio aria->terra: reset della carica prima del nuovo morso,
provato entro2tick con regressione dedicata. Le rotte senza target riprendono i
normali comportamenti. Il precedente obbligo transitorio di atterrare per ogni
bersaglio è superato dalle modalità aeree; aggiornato il relativo GameTest.

Aggiunto secondo test client: mondo/arena reale, server in combattimento,
attesa sincronizzazione pose TAIL/WING e screenshot GPU, poi stop da seduto.
Anteprima GUI limitata con scissor al riquadro: la posa estesa non deve coprire
le statistiche. Test legge pixel di ogni riga delle due schermate. Screenshot
ispezionati: coda e ali visibili, nuove prove non equivalgono a due veri client.

Build verificata:58 JUnit,51 GameTest mod (52 con baseline vanilla escluso),due test client in mondi temporanei con screenshot e verifica pixel delle statistiche. Nessun collaudo con due veri client.


## Checkpoint 0.1.16 — 24 ottobre 2026

L'utente ha completato i due obiettivi: proseguire il più possibile attraverso
le fasi usando la quota disponibile e sospendere il PC alla fine dei test.
L'autorizzazione nuova permette di avanzare oltre i precedenti checkpoint di
riscontro, dopo aver completato/verificato la tappa corrente; non dichiarare
completa una tappa con funzioni mancanti. Non consumare quota artificialmente.
La sospensione è ora nuovamente autorizzata, dopo salvataggio/push/consegna.
Goal attivo del thread creato per il port completo, nessun budget token impostato.

Base17364c28c. DragonFoodGoal ricerca carne raggiungibile/visibile ogni20tick,
segue a1 e ricalcola10tick. Solo libero affamato a terra senza target/pilota/
spalle/NoAI; comandi prioritari, pickup delay e portata controllati. Consuma1,
rimuove item vuoto, fame/cura foodPoints come originale DragonAITargetItems,
senza crescita. Animazione EAT20tick sincronizzata, suono/particelle; mandibola
chiusa progressivamente per evitare il tronco dei keyframe originali25tick.

Coda40tick (impatto21..29), ali50tick (15/25/35), knockback stage*0.6 con API26.3
DamageSource/damage. Primo attacco nuovo target resta morso; poi scelta casuale
fra i tre dallo stage2. Solo morso cura. Pose Tabula originali tail1..3 e
wing_blast1..7, sollevamento corpo e reset senza accumulo. Shake prey resta fuori.

Palla di fuoco autonoma: probabilità1/5 fra cicli di soffio, scelta stabile durante
carica30tick, emissione20, mira occhi con imprecisione0.0075/accelerazione0.07.
Prima azione a distanza resta stream; distribuzione temporale diversa dal ramo
originale per tick, dichiarata in TEST-0.1.16.md. Cambio target riavvia anche il
warmup del soffio, evitando il riuso della carica del bersaglio precedente.

Verificati 58 JUnit, 46 GameTest della mod (47 con baseline vanilla escluso) e un test client con mondo/screenshot. Revisione completa read-only review-agent senza delega: No findings. Nessun test con due veri client.


## Checkpoint 0.1.15 — 24 ottobre 2026

Ultimo messaggio utente autorizza l'uso del computer per test e annuncia due
obiettivi, ma si interrompe prima dell'elenco. Chiarimento già richiesto:
non inventare i due obiettivi; recepirli prima del lavoro successivo.

Riscontro sulla0.1.14: drago stadio5 bloccato dagli alberi durante la difesa;
richiesta sicurezza del dismount solo a terra, doppio salto per decollare e
proseguimento del port. Base208773ef1, switch/pull --ff-only del ramo personale.

Aggiunto DragonVegetation: dallo stadio3 apre il corridoio locale attraverso
tronchi/foglie, con durezza2/5 originale, cooldown5tick e drop tronchi0.1
configurabili. Rispetta mobGriefing/dragonGriefing/tamedDragonGriefing, blacklist,
fluidi, block entity e chunk caricati. Non è il breakBlocks generale originale:
nessuna demolizione di pietra/casse. Vale anche per legno/foglie piazzati.
DragonMeleeGoal approccia localmente se vanilla non trova il percorso e lo
ricalcola quando la vegetazione cambia. Collisione/dimensioni stadio5 conservate.

Dismount solo con drago vivo/onGround/non flying: ricerca laterale supportata
con DismountHelper, collisione/bordo/fluidi e corpo controllati. Se manca l'uscita,
fallback vanilla sopra il corpo con protezione della caduta corrente tramite
setIgnoreFallDamageFromCurrentImpulse. In aria nessuna protezione aggiunta o
uscita a terra, resta la caduta vanilla. Nessun dorso solido, nessun mixin nuovo.

DragonTakeoff riconosce due fronti UP distinti entro7tick, anche se tutti i pacchetti
arrivano fra due tick server; restano V e spazio>20tick. Reset per stadio/discesa/
dismount; client e server usano lo stesso input esistente, nessun nuovo payload.

Avanzamento: soffio autonomo terrestre stage2+, con bersaglio visibile fuori
portata del morso entro10*stage, warmup20/estensione40/durata stage*25.
Riutilizza danni server, alleati, clipping, terreno e particelle. Si arresta per
comandi/controllo/bersaglio non valido e riprende il morso vicino. Non sono migrate
palla di fuoco autonoma o modalità aeree originali. TEST-0.1.15.md contiene prove
ed esatti limiti; STATO-TAPPA-1.md resta il riepilogo cumulativo.

Build verificata: 56 JUnit, 41 GameTest della mod (42 includendo always_pass
vanilla escluso dal conteggio) e un test client grafico con mondo/screenshot
superati. Nessuna prova multiplayer con due veri client o collaudo visivo delle
novità di combattimento. Revisione completa review-agent read-only: No findings.


## Aggiornamento 0.1.14 — difesa autonoma, 24 ottobre 2026

L'utente conferma la 0.1.13: "ho provato e funziona tutto", e chiede di continuare.
La sospensione del PC richiesta per la consegna precedente è già stata eseguita;
non è un'autorizzazione permanente a sospendere nelle riprese successive.
Ramo personale port/fabric-26.3 aggiornato con switch/pull --ff-only, base05846b062.

Migrati i goal difensivi originali OwnerHurtTarget/OwnerHurtByTarget/HurtByTarget,
priorità1/2/3. FireDragon.canAttack controlla movimento autonomo, bersaglio vivo,
stessa dimensione, proprietario/alleati/animali dello stesso proprietario e
creativo/spettatore. Target oltre FOLLOW_RANGE o non validi eliminati.
DragonMeleeGoal priorità5 insegue a1.5, ricalcola4..10tick, guarda il bersaglio e
si ferma nella portata originale renderSize*0.2574*2+2. Il morso usa35tick e
finestra16..24 di IafDragonLogic, un danno riuscito per animazione, con verifica
all'impatto di distanza/visibilità e cura opzionale. Cambio bersaglio azzera la
carica; seduto/NoAI/pilota/spalle interrompono. Stato sincronizzato già esistente,
nessun nuovo dato persistente e nessuna modifica agli asset originali.

Il flight manager, con un bersaglio, interrompe la destinazione wander/escort
e atterra prima di attaccare a terra. È un comportamento transitorio, non parità
con tackle/hover blast/scorch stream. Mancano caccia, soffio autonomo e attacchi
terrestri restanti: proseguire questi blocchi nella tappa1, senza avviare la2.
Vedere TEST-0.1.14.md e STATO-TAPPA-1.md.

Build riuscita: 53 JUnit, 34 GameTest della mod (più always_pass vanilla escluso)
e una prova client con mondo e screenshot superati. Revisione completa secondo
review-agent in sola lettura senza delega: No findings. Nessun test con due client.
Conservati controlli e attacchi del pilota,
teletrasporto richiesto, unica seduta, nessun dorso ghast. Punte grigie aperte.


## Aggiornamento 0.1.13 — correzioni richieste, 23 ottobre 2026

Ripresa da8476a4143 sul ramo port/fabric-26.3. Utente segnala head/tail invisibili
sul drago e in inventario, seguito lento e chiede recupero a circa150 blocchi,
volo in libero/seguimi e riepilogo completo prima di proseguire. Autorizza inoltre
esplicitamente push del risultato e successiva sospensione del PC.

Armature: stesso gruppo di submit della pelle causava il ridisegno della pelle
sopra head/tail. Migrato collector.order(slot+1), come separazione dei passaggi
vanilla26.3. Riprodotta la sparizione degli overlay usando temporaneamente il
vecchio renderer nello stesso test client; codice corretto ripristinato in finally.
Screenshot prima/dopo della GUI mostrano head/tail equipaggiati, ora visibili.
32 icone verificate con modelli reali e screenshot. Le icone isolate risultano
visibili anche col vecchio renderer nella prova pulita: non dichiarare trovato
un asset PNG mancante. Modelli, UV e PNG originali conservati byte-identici.

Seguito: terra1.5/2 oltre16 blocchi e ricalcolo5 tick. In aria eliminato il
bersaglio casuale rigenerato quasi ogni tick: destinazione prevista del proprietario
con aggiornamento10 tick, accelerazione di recupero e atterraggio vicino a lui
se a terra. Teletrasporto richiesto dall'utente a >=150 blocchi, tentativo ogni20
tick, solo seguimi/proprietario vivo/stessa dimensione/drago libero da pilota e
passeggeri/NoAI disattivo. Controlli intero AABB, supporto, fluidi, world border,
altezza e chunk caricati; aereo solo stadio2+ con proprietario in aria. Il drago
deve essere simulato: niente caricamento forzato o teletrasporto fra dimensioni.
Questo requisito utente revoca il precedente divieto di teletrasporto del seguito.
La prova finale di wandering ha rilevato uno stallo con bersagli ripidi: il
vecchio fattore orizzontale arrivava a zero e piccoli impulsi erano azzerati
dalla fisica vanilla. Minimo fattore0.2 e minimo crociera0.6; test di regressione
su bersaglio ripido e movimento orizzontale reale, senza ridurre le asserzioni.

Build pulita:53 JUnit,27 GameTest della mod più baseline vanilla escluso;
un test client in mondo temporaneo con screenshot GPU, menu reale e verifica
sincronizzazione slot head/tail. Nuovo source set clientGametest separato,
escluso dal JAR. Provati anche recupero aereo con arrivo/atterraggio e decollo/
movimento orizzontale del wandering senza proprietario. Revisione completa con
skill review-agent read-only senza delega: No findings. Vedere REVIEW.md.

TEST-0.1.13.md contiene installazione e prova utente; STATO-TAPPA-1.md contiene
il riepilogo cumulativo completo implementato/mancante richiesto dall'utente.
Tappa1 e punte grigie ancora aperte, nessun dorso ghast, pilota unico dallo
stadio3. Non proseguire oltre queste correzioni prima del riscontro utente.
Restano combattimento autonomo/altri attacchi, cibo a terra, sonno/riproduzione,
stendardo e lacune di animazioni/configurazioni/navigazione descritte nel riepilogo.

## Aggiornamento 0.1.12 — 23 ottobre 2026, ripresa su questo PC

Clonato origin personale Djdragonyyy e usato esclusivamente port/fabric-26.3;
git switch e git pull --ff-only origin port/fabric-26.3 riusciti. Base di ripresa
91f9d4baa (ultimo funzionale b3c265a78). Nessuna modifica locale preesistente.
JDK 25 assente: scaricato localmente Azul Zulu 25.0.4.1+1, verificato SHA-256
5e91bc55aaa08750370d60b7030bb79193901ce1e93aba31becbf78c3ade5826 dal catalogo
ufficiale. JAVA_HOME/GRADLE_USER_HOME solo nei processi, nessuna config globale.
Il launcher .bat con JAVA_HOME/bin/java.exe falliva nella creazione dei file cache;
separatore Windows corretto in JAVA_HOME\bin\java.exe. Nessun workaround ZipFS.
Baseline 0.1.11 ricostruita: 45 JUnit e 15 GameTest della mod superati.

Checkpoint 0.1.12-stage1-dev: primo flight manager autonomo non di combattimento,
adattato da IafDragonFlightManager, IafDragonLogic, DragonUtils e ReturnToRoost.
Decollo dallo stadio 2, verifica spazio, 40 tick hovering, probabilità 1/1500 per
il vagabondaggio diurno, seguito oltre 30 blocchi/differenza Y>8 entro 2000, niente
teletrasporto o caricamento chunk per inseguire. Sterzo originale con guardie su
bersagli verticali/velocità zero, cap simmetrico dell'accelerazione, target maxY256.
Atterraggio dopo oltre 6000 tick di crociera, comando seduto, proprietario fuori
portata o rotta non disponibile. Simulazione autonoma server distinta dal pilota.
Hovering sincronizzato/persistito, posa originale e battito d'ali; il salvataggio
Flying ammette lo stadio 2 senza sbloccare il pilota prima dello stadio 3.

Ritorno notturno alla casa nella dimensione corretta, con comandi 1/2 prioritari:
a piedi vicino al nido; decollo oltre 40 blocchi, approccio aereo e atterraggio.
La rotta resta attiva durante l'avvicinamento finché il drago atterra; evita di
riprendere il vagabondaggio in aria all'ingresso del raggio del nido. Con casa
impostata il drago non vagabonda a terra di notte. Sonno completo NON migrato.
Limite deliberato: bersagli con visibilità e collisione disponibile, nessun
pathfinder aereo completo per aggirare edifici/caverne. Default originali ancora
fissi, comportamento completo acqua/lava e attacchi aerei restano da migrare.

Build clean build runGametest riuscita: 50 JUnit e 23 GameTest della mod,
più always_pass vanilla non conteggiato. Otto nuovi GameTest con movimento reale,
arrivo a casa e atterraggio, anche ciclo completo a 42 blocchi dal nido; arena
48x96x48 separata perché il cubo standard 8x8x8 bloccava il percorso ai confini.
Non sono state indebolite le asserzioni: il rientro ora richiede arrivo/atterraggio.
Skill locale review-agent trovata, letta e applicata in sola lettura al diff
completo da 91f9d4baa, senza delega; No findings. Dettaglio in REVIEW.md.


Client 0.1.12 avviato in run/0.1.12-smoke con init script locale che cambia
soltanto la directory di esecuzione: caricati mod, OpenGL, audio e atlanti.
Terminato dall'agente dopo il caricamento. Nessuna entrata in mondo o verifica
visiva del drago; avvisi OSHI/Realms del profilo di sviluppo non bloccanti.

Vedere TEST-0.1.12.md. Nessuna conferma utente delle novità 0.1.11/0.1.12;
la 0.1.10 resta l'ultima confermata dall'utente. Nessun collaudo visivo o con due
veri client eseguito dall'agente. Tappa 1 incompleta, punte grigie ancora aperte,
dorso ghast assente. Prossimo blocco: combattimento autonomo e altri attacchi,
cibo raccolto a terra, altre interazioni/animazioni, sonno/riproduzione/stendardo.
Non avviare la tappa 2. Conservare la consegna dei due JAR e i vincoli Git.

## Passaggio a un altro PC — 19 ottobre 2026

Su richiesta dell'utente, aggiunto in radice PROMPT_RIPRESA_TEMPORANEO.md, pronto
da copiare nella nuova chat dopo il pull del ramo port/fabric-26.3. Ultimo commit
funzionale b3c265a78, versione 0.1.11-stage1-dev; il successivo commit documentale
include questo passaggio. La 0.1.10 è confermata dall'utente; la 0.1.11 ha build,
45 JUnit e 15 GameTest passati, ma il collaudo utente delle nuove interazioni resta
da fare. Nessuna modifica funzionale in questo passaggio. Tutti i sorgenti, test,
asset selezionati e documentazione sono versionati. JAR/build, cache, JDK e outputs
locali non vengono trasferiti dal pull: ricostruire e consegnare i due JAR sul nuovo
PC. Il prompt temporaneo si può rimuovere dopo aver recepito la ripresa; conservare
questo handoff. Origin corrente è Djdragonyyy, non gli username storici citati sotto.

## Aggiornamento 0.1.11 — alimentazione, comandi e seguito a terra

L'utente conferma perfettamente funzionante la 0.1.10 (fuoco, anteprima e spalle)
e autorizza a continuare la tappa 1. Nuovo checkpoint 0.1.11-stage1-dev:
alimentazione manuale originale FoodUtils per drago di fuoco: componente FOOD e
tag vanilla MEAT sostituiscono il vecchio isMeat. Nutrimento*10 fame e nutrimento
salute, niente crescita; consuma un solo alimento se affamato/ferito, non se sano
e sazio. Solo proprietario, pesce escluso (fuoco non piscivoro). Pasti originali:
dragon_meal conservato con effetti sonori/particelle; sickly_dragon_meal blocca
AGING_DISABLED persistentemente e cura al massimo, secondo pasto non consumato;
creative_dragon_meal addomestica/riassegna il proprietario anche se già presente,
come originale, senza crescita. Consumi rispettano abilità creative. Particelle
server di alimento/ossa/farina d'ossa e patate velenose per malaticcio.

Dragon staff registrato con ID originale dragon_stick, modello e texture originali;
tre oggetti nuovi nella scheda Ingredienti, senza ricette migrate. Clic destro del
proprietario cicla 0 libero -> 1 seduto -> 2 seguimi -> 0. COMMAND sincronizzato,
ordine vanilla coerente e navigazione fermata a ogni cambio. Animazione usa COMMAND
poiché orderedToSit vanilla non è sincronizzato. Monta e raccolta in spalla azzerano
il comando per evitare ordini seduti residui. Maiusc+staff imposta/rimuove casa,
campi originali HasHomePosition/HomeAreaX/Y/Z/HomeDimension, incluse coordinate zero;
vincolo di vagabondaggio di 40 blocchi solo nella dimensione corretta. Override anche
di isWithinHome(BlockPos/Vec3), perché Mob 26.3 legge i campi privati anziché getter.

DragonEscortGoal migra il percorso terrestre di DragonAIEscort, velocità 1 e limite
2000; ricalcola ogni 10 tick, stop entro dimensione media della collisione, nessun
teletrasporto. Non avvia volo autonomo, che richiede ancora il flight manager.
Vagabondaggio ogni 20 tick come DragonAIWander, escluso da seguimi/seduto/volo/
cavalcatura/spalle. Il punto di casa non implementa ancora ritorno notturno al nido.

Build riuscita, 45 JUnit e 15 GameTest della mod passati, oltre a baseline vanilla.
Sei nuovi GameTest in DragonInteractionGameTests: alimentazione/consumi/cure e
proprietà, pasto creativo e capacità creative, blocco crescita persistito, ciclo
comandi/casa a coordinate zero/dimensioni/limite40, camminata reale con arresto da
seduto, posa dopo assegnazione dei soli dati sincronizzati senza orderedToSit.
Mock creativo richiede instabuild esplicito; test camminata maxTicks120, stato
iniziale a terra e piattaforma di prova. Nessuna semplificazione dei test per
aggirare difetti funzionali. Skill review-agent in sola lettura senza delega;
due P2 corretti fuori revisione (posa non sincronizzata e isWithinHome), riletto
il diff finale senza altri finding. Client avviato separatamente con risorse,
grafica, audio e atlanti caricati; nessun collaudo visivo/multiplayer reale.

Vedere TEST-0.1.11.md. Restano volo/combattimento autonomi, ritorno al nido, cibo
da oggetti a terra, altre interazioni/animazioni, riproduzione/sonno e stendardo.
La tappa 1 resta incompleta, punte grigie aperte, dorso ghast rimosso. Non avviare
tappa 2. Conservare i vincoli Git e consegnare sempre mod più Fabric API esatta.

## Aggiornamento 0.1.10 — fuoco, anteprima e spalle

Utente conferma funzionante quanto già implementato e chiede getto meno sgradevole,
anteprima adulta corretta e piccoli in spalla. Versione 0.1.10-stage1-dev: texture
originale flame.png 32x32 copiata senza modifiche; UV selezionano 16 celle 8x8 lungo
il viaggio, con fase casuale e dissolvenza ai due estremi. Sostituisce lo sprite
statico dragon_flame.png. Non dichiarare identico al renderer Forge o modificati
il fuoco vanilla sul terreno/proiettile. Conservati scala/stadio e protezione POV.

GUI: nella 26.3 InventoryScreen normalizza state.scale=1 prima del disegno. Rimosso
il vecchio 23/getScale che riduceva ulteriormente l'adulto; ora 23 pixel/unità costanti,
senza cambiare stadio, texture o armatura del render state.

Spalle: solo proprietario, mano vuota, stadio 1 (<25 giorni), tre piccoli come
nell'originale; due spalle e testa, posa originale sitting_shoulder. Maiusc/elitre
rilasciano quando il proprietario non è già passeggero; rilascio anche per morte,
logout, cambio dimensione, crescita e caricamento. Niente dorso ghast o altri posti
sul drago adulto. Le posizioni usano radianti coerenti per due spalle simmetriche,
correggendo il mix di gradi/radianti nell'originale, e rispettano la scala del giocatore.
Aggancio client interpolato; se il giocatore cavalca un adulto segue la seduta animata.

Il primo GameTest ha dimostrato che Entity.startRiding della 26.3 rifiuta veicoli con
EntityType.canSerialize=false, incluso PLAYER, anche con force. Evitato un mixin
per aggirare il vincolo: i piccoli sono entità del mondo indipendenti, agganciate
tramite due dati sincronizzati (carrier entity ID e slot). Stato spalla transitorio,
non salvato: al caricamento vengono rilasciati; proprietà/posizione/inventario normali
restano salvati. Nessun nuovo payload client autorevole. Fisica e viaggio sospesi
soltanto mentre trasportati, ripristinati al rilascio.

Build ordinaria riuscita, 45 JUnit e 9 GameTest della mod superati, oltre al baseline
vanilla. Test runtime: tre piccoli/quarto rifiutato, movimento e rilascio Maiusc,
proprietario e crescita, seduta adulta unica, salvataggio indipendente, ricaricamento,
logout e ripristino fisica. L'asserzione proprietario del mock dopo load è stata
corretta per controllare UUID persistito: il mock offline non è nel lookup del mondo.
Skill review-agent applicata in sola lettura senza delega; corretto fuori revisione
l'aggancio dei piccoli al cavaliere animato, nessun ulteriore finding.
Client avviato separatamente, grafica/audio/atlanti caricati; nessuna verifica visiva
in mondo o multiplayer reale. Vedere TEST-0.1.10.md e REVIEW.md.

Lette le skill skill-installer e plugin-management richieste dall'utente; nessuna
skill da installare necessaria, ricerca Blockbench/Minecraft senza risultati.
Tappa 1 incompleta, punte grigie ancora aperte: mantenere queste limitazioni.

## Aggiornamento 0.1.9 — soffio progressivo, terreno e inventario

Riscontro utente: 0.1.8 funziona, ma soffio poco progressivo, piccolo negli stadi
adulti e posa iniziale fastidiosa in prima persona. Rimosso il salto iniziale del
getto: estensione smoothstep da zero dopo i 20 tick di carica, completa a 40 tick.
La scelta casuale del proiettile non salta più i tick del getto. Ripristinata texture
originale dragon_flame, identica byte per byte, su atlas particelle 26.3; scala originale
clamp(renderSize*0.08, 0.55, 3), densità crescente con dimensione ed estensione.
Un solo payload visuale per tick per osservatore, particelle generate sul client:
nessuna nuova autorità client su mira/danni. Fiamme accelerate verso il punto raggiunto,
crescita iniziale, collisione e rimozione all'arrivo. In prima persona del solo pilota
la posa di fuoco muove la mandibola senza spostare collo/seduta; particelle troppo vicine
alla sua camera non vengono disegnate. Altre viste conservano le animazioni originali.

Migrati sette blocchi originali chared_dirt/grass/stone/cobblestone/gravel/dirt_path e ash,
con PNG/modelli originali, blocchi cadenti per ghiaia/cenere, forma del sentiero e flag
revert. Per il ritorno al terreno originale il codice usa randomTick (nell'originale
era dichiarato randomTicks ma il callback era tick). Corretta anche la condizione
irraggiungibile gravel && tag sand della conversione originale. Soffio con area per
stadio e proiettile con distruzione/conversione probabilistica dell'area; rispettati
mob_griefing, config, resistenza e blacklist casse/end_stone/iron_bars. Nessun caricamento
esplicito di chunk mancanti nel ciclo di trasformazione. Esplosioni opzionali e DragonForge
non ancora migrati: non dichiarare parità totale degli effetti originali.

Maiusc + clic destro a mano vuota sul drago di proprietà apre DragonMenu, cinque slot
originali: stendardo, testa, collo, corpo, coda. Texture GUI originale, anteprima e
statistiche. Registrate 32 armature dei materiali originali per il drago di fuoco,
disponibili in creativo; bonus originali e quattro overlay originali sincronizzati.
Controlli proprietario/distanza, shift-click, salvataggio componenti ItemStack e drop
alla morte. Lo stendardo è conservato nello slot; il suo layer visivo non è ancora migrato.
Non sono stati aggiunti draghi di ghiaccio/fulmine, ricette o generazione naturale.
Non reintrodurre il dorso ghast. Punte grigie ancora aperte.

Build 0.1.9-stage1-dev: 41 test JUnit passati, 5 GameTest della mod passati in vero
runtime Fabric e mondo temporaneo (oltre al baseline vanilla always_pass, da non contare
come test della mod). Verificati tutti i materiali/slot, NBT di un drago a 125 giorni
con componenti degli oggetti, autorizzazione, trasferimenti senza duplicazioni,
terreno/loot/revert, griefing e protezione casse/bedrock. Il tentativo iniziale di far
girare la registrazione in JUnit falliva senza i mixin Fabric: spostato correttamente
in src/gametest; nessuna manomissione dei registri. Testmod separata, esclusa dal JAR.
Comando riproducibile: .\fabric\gradlew.bat -p fabric build runGametest --console=plain.
Skill review-agent applicata al diff completo in sola lettura, senza delega: No findings.
Client 0.1.9 avviato in directory separata, risorse/grafica/audio/atlanti caricati; non
verificata visivamente la nuova GUI o il getto in un mondo dall'agente. Prova utente
richiesta in TEST-0.1.9.md. Restano IA autonoma, altri attacchi/interazioni/animazioni,
alimentazione completa e le altre lacune della tappa 1: non avviare la tappa 2.

## Aggiornamento 0.1.8 — combattimento controllato, primo passaggio

Ripresa tappa 1 su richiesta dell'utente. Migrati i percorsi del cavaliere:
G morso con selezione del bersaglio lungo lo sguardo, danno da ATTACK_DAMAGE e
guarigione opzionale; R soffio, carica oltre 20 tick, estensione a 40 tick, durata
massima stage*25, spegnimento dello stato dopo rilascio e reset al dismount.
Selezione casuale originale del ramo palla di fuoco (1/5 per tick), animazione
di carica da 30 tick, emissione al tick 20 quando selezionato il ramo originale.
Registrato fire_dragon_charge, accelerazione 0.07, impatto server e danno dragon_fire;
renderer a cubo di magma come l'originale, suono originale firedragon_breath incluso.
Sincronizzati stato soffio, animazione e tick; migrate pose originali bite1/2/3,
blast_charge1/2/3 e stream_breath con keyframe. La seduta segue ancora la posa del collo.
Protezione di drago, proprietario, passeggeri e alleati/animali dello stesso proprietario;
bersagli creativi/spettatori esclusi. Raycast dei blocchi limita il getto, controllo
di visibilità limita il danno ad area. Gli effetti sono esclusivamente server.

Configurazione parziale config/iceandfire-combat.json, valori originali:
dragonAttackDamageFire=2, dragonGriefing=0, tamedDragonGriefing=true,
canDragonsHealFromBiting=true. File non valido: log di errore e default, senza riscriverlo.
Accensione base del fuoco subordinata a mob_griefing e opzioni griefing. NON sono
ancora migrati conversioni in blocchi carbonizzati, distruzione dell'area da charge,
DragonForge, esplosioni opzionali, particella originale DragonFire (temporaneamente
FLAME vanilla), IA autonoma o gli altri attacchi. Non dichiarare completo il combattimento
originale o la tappa 1. Prossima attività: completare questi effetti/IA e inventario,
oltre alle altre lacune elencate nel documento; nessun passaggio alla tappa 2.

Build 0.1.8-stage1-dev: 38 test superati, inclusi 11 nuovi test su tempi/bersagli/pose.
Revisione read-only con skill review-agent, senza delega: nessun ulteriore difetto
individuato. I test non simulano danni in un vero mondo: collaudo utente necessario.
Vedere TEST-0.1.8.md e REVIEW.md. Dorso ghast assente; punte grigie ancora aperte.
Client 0.1.8 avviato in run/0.1.8-smoke separata: mod inizializzata, risorse,
backend grafico, audio e atlanti caricati. Configurazione predefinita generata.
Avvisi OSHI/Realms del profilo di sviluppo non bloccanti. Nessuna entrata in mondo
né prova degli attacchi eseguita dall'agente; client lasciato aperto per il collaudo.

## Aggiornamento 0.1.7 — funzione ghast rimossa su richiesta

L'utente revoca la richiesta del dorso solido: rimuovere completamente la funzione
derivata dal ghast felice e continuare il porting originale. Questo stato prevale
sulle indicazioni storiche 0.1.3–0.1.6 sotto. Eliminati collisione solida aggiuntiva,
BACK_OCCUPIED, scansione dei giocatori, pausa sul dorso/dismount, posizione personalizzata
di dismontaggio, helper DragonBackPlatform e relativi test. Tolti i blocchi di movimento,
rotazione e decollo dipendenti dal dorso occupato e il parametro blocked di DragonTakeoff.
Conservati seduta principale unica, camera, tilt e controlli di volo originali del port.
Build pulita 0.1.7-stage1-dev: 27 test superati (tre test della funzione eliminati).
Nessuna prova interattiva svolta per questa versione: vedere TEST-0.1.7.md.
La tappa 1 resta incompleta, punte grigie aperte. Riprendere combattimento e soffio
originali, non la tappa 2; non reintrodurre il dorso solido. Origin aggiornato dopo
il cambio username comunicato dall'utente:
https://github.com/Djdragonyyy/Ice_and_Fire_Fabric26.3_port.git

## Aggiornamento 0.1.6 — un solo passeggero

L'utente vuole esclusivamente il posto principale del drago e la superficie solida
per stare in piedi; nessuna seduta aggiuntiva del ghast felice. Individuata la monta
forzata startRiding(this, true, true), che bypassava canAddPassenger. Sostituita con
startRiding(this) e controllo proprietario/stadio/posto libero. Aggiunto override
couldAcceptPassenger con !isVehicle(): il bytecode vanilla lo controlla prima del
ramo force, quindi il limite vale anche per monta forzata e passeggeri caricati da NBT.
Non aggiunta alcuna disposizione di posti del ghast. Collisione sul dorso, dismontaggio,
seduta principale, camera, tilt e volo conservati. Versione 0.1.6-stage1-dev;
build riuscita, suite 30 test superati. Prova in gioco con due giocatori ancora da fare,
vedere TEST-0.1.6.md. Punte grigie aperte e tappa 1 incompleta.

## Diagnosi punte grigie — screenshot utente del 18 ottobre 2026

L'utente segnala il problema in ogni modello/stadio e fornisce una vista ravvicinata
del drago rosso dall'alto, con punte chiare che sembrano scatole aperte. Non è ancora
stata isolata la causa visiva: non dichiarare il difetto risolto.
Confrontato ogni vertice delle 22 punte del modello ground con layout, winding,
normali, UV e mirroring di Citadel TabulaModelRenderUtils.ModelBox/TexturedQuad:
528 vertici corrispondenti, nuovo test superato. Suite attuale: 30 test senza errori.
Il materiale EntityModel 26.3 usa già entityCutout, senza culling. Le texture originali
512x256 usano la griglia UV logica 256x128: questo rapporto 2x è coerente, non un errore.
Il colore osso chiaro è presente nelle texture originali. Alcune facce hanno alpha
trasparente: ad esempio SOUTH della punta 1x1x2 ha 0/4 texel visibili in red_1 e 2/4
in red_5. Le altre tre varianti allo stadio 5 condividono questi pixel osso.
Nessuna modifica a modello, texture o renderer e nessuna nuova versione JAR per questa
diagnosi. L'utente conferma che il difetto compare anche da fuori e di lato, usando
solo Ice and Fire e Fabric API: non attribuirlo alla prima persona o a shader esterni.
Controllata anche la composizione originale ArrayLayeredTexture: overlay maschile
trasparente sui campioni delle punte. PNG base con alpha 0/255, salvo un texel quasi
trasparente; pipeline vanilla entityCutout con soglia 0.1 e culling disattivato.
Ricevute altre tre immagini dello stesso drago: vista laterale dall'alto, longitudinale
da dietro e frontale. Le punte dorsali appaiono come piccoli parallelepipedi, con vuoti
particolarmente visibili da dietro. Non chiedere nuovamente queste viste all'utente.
La geometria cuboidale e l'alpha originale sono compatibili con questo aspetto, ma
non dimostrano da soli se esista una differenza rispetto alla resa originale in gioco.
Prima di cambiare UV/geometria serve un confronto visivo equivalente con l'originale
o una riproduzione isolata della punta: non spostare UV alla cieca, non riempire tutti
i pixel trasparenti (la loro distribuzione cambia anche con lo stadio).
Restano validi i JAR 0.1.5, difetto visivo ancora aperto.

## Aggiornamento 0.1.5 — dismontaggio sul dorso

L'utente richiede esplicitamente di restare in piedi sulla superficie solida del drago
quando smonta, come sul ghast felice. Aggiunta pausa immediata nel metodo removePassenger,
sia sul server sia come previsione client: azzerati velocità del drago, vecchi input,
richiesta di decollo e bonus della planata. Come HappyGhast, la pausa dura dieci tick
e viene rinnovata dal rilevamento del giocatore sul dorso già esistente.
Il punto di dismontaggio è esplicitamente al centro superiore della collisione; azzerate
velocità del passeggero e distanza di caduta prima del dismountTo vanilla sul server.
Il centro superiore era già il default vanilla; la nuova pausa evita che la superficie
continui a muoversi prima della scansione del giocatore appena sceso.
Versione 0.1.5-stage1-dev: build ordinaria e 29 test superati; revisione in sola lettura
senza delega con review-agent. Nessun nuovo avvio client o collaudo interattivo del
dismontaggio eseguito dall'agente. Nuova prova richiesta a terra e in volo a ogni stadio
cavalcabile, soprattutto a 125 giorni. V, spazio, tilt e camera della 0.1.4 conservati.
Tappa 1 incompleta e punte grigie ancora aperte. Vedere TEST-0.1.5.md e REVIEW.md.

## Aggiornamento 0.1.4 — volo e visibilità

Riscontro utente: tilt della 0.1.3 circa corretto, ma il drago deve raddrizzarsi in
discesa vicino ai blocchi; in salita il corpo attraversa la prima persona; decollo
ancora intermittente. Richieste implementate nella 0.1.4-stage1-dev:

- Nove raycast contro le forme di collisione dei blocchi sotto corpo/collo/testa,
  calcolati per tick solo in discesa. L'inclinazione positiva si attenua fino a zero
  vicino al suolo/ostacoli, con distanza proporzionata alla dimensione visiva.
- Solo il pilota in prima persona vede il modello senza tilt; terza persona e altri
  giocatori mantengono il tilt. Nessuna modifica alla distanza della camera.
- Tasto V riassegnabile per decollare; spazio tenuto oltre un secondo resta disponibile.
  Input raccolto all'inizio del tick; breve pressione memorizzata fino al tick server,
  attesa massima di un secondo se il dorso è occupato e nessun rilancio automatico
  tenendo V. Stato di volo impostato prima della simulazione vanilla, impulso sul lato
  che simula il veicolo e conservato fino al distacco da terra.

Nuovo bit TAKEOFF nel payload: installare 0.1.4 su server e client insieme; non usare
client 0.1.4 con server 0.1.3. Controllo proprietario e monta dallo stadio 3 invariati.
Build ordinaria riuscita, 28 test superati. Revisione in sola lettura con review-agent,
senza delega. Dettaglio in REVIEW.md e TEST-0.1.4.md. Resa visiva, rilevamento reale dei
blocchi e affidabilità del decollo ancora da confermare in gioco dall'utente.
La tappa 1 resta incompleta e le punte grigie sono ancora aperte. Dopo il riscontro,
continuare combattimento e soffio, non la tappa 2.
Client 0.1.4 avviato in directory separata con mod, grafica, audio e atlanti caricati;
lasciato aperto per la prova. Nessuna verifica visiva del drago eseguita dall'agente.

## Aggiornamento 0.1.3 — riscontro e richieste utente

L'utente conferma camera e comandi di schiusa della 0.1.2. Nuove richieste implementate:
decollo più affidabile sul client che simula la cavalcatura; inclinazione salita/discesa
adattata dal codice originale e sincronizzata; seduta ricavata dalla superficie Neck1
per evitare che il bacino finisca dentro al modello nello stadio 5. Distanza camera invariata.
L'utente autorizza espressamente un'aggiunta al gameplay: camminare sul drago non montato
come sul ghast felice. La collisione centrale è solida in tutte le varianti di fuoco;
il drago si ferma mentre un giocatore è sul dorso, con dieci tick di pausa dopo la discesa.
Non è una collisione dettagliata di ali/coda. Ghiaccio/fulmine non sono ancora registrati.
Versione corrente 0.1.3-stage1-dev; build ordinaria e 21 test riusciti. Revisione completa
con skill review-agent in sola lettura senza delega; corretto il caso null delle query
vanilla di collisione. Vedere REVIEW.md e TEST-0.1.3.md per dettaglio e collaudo richiesto.
Le nuove modifiche non sono ancora confermate dall'utente in gioco. Punte grigie ancora
aperte. Dopo questa prova continuare combattimento/soffio e il resto della tappa 1;
non considerare autorizzato il salto alla tappa 2 dal solo esito di queste correzioni.
Avvio 0.1.3 in directory di prova separata riuscito dai log, con risorse/grafica/audio
caricati e ingresso in mondo locale; client lasciato aperto. Nessuna interazione
del drago eseguita dall'agente.

## Aggiornamento della ripresa — 18 ottobre 2026

Checkout recuperato dall'origin personale e verificato pulito su `port/fabric-26.3`,
HEAD e ramo remoto entrambi `417f5a8d28bf62599e3ff84cf7bbdae06d625ba1`.
JDK 25 assente nel PATH iniziale: scaricato localmente Azul Zulu 25.0.4.1+1 e verificato
SHA-256 contro il catalogo ufficiale. Nessuna modifica alle impostazioni Java globali.
Build ordinaria riuscita senza workaround ZIP sul nuovo PC. Revisione integrale 0.1.2
completata con la skill locale `review-agent`, in sola lettura senza delega; corretto
successivamente il bob del volo che saltava a fine atterraggio. Test di regressione
prima fallito e poi superato; suite finale **17 test superati**, nessun fallimento.
Vedere `REVIEW.md`. Ricerca plugin Blockbench ripetuta: nessun risultato.
Client di sviluppo avviato: mod, grafica, audio e atlanti caricati; log di ingresso
nel mondo locale `New World`. Nessuna prova del drago eseguita dall'agente; client
lasciato aperto per il collaudo dell'utente. Avvisi OSHI/Realms non hanno bloccato l'avvio.
Le istruzioni sotto descrivono il precedente passaggio e restano il riferimento per i vincoli.
Punte grigie ancora aperte; prova utente della 0.1.2 e tappa 1 ancora incomplete.

Riprendi questo progetto nella stessa repository e sul ramo indicato sotto. Comunica in italiano.
Leggi prima gli eventuali AGENTS.md applicabili, questo file, fabric/README.md e fabric/REVIEW.md.
Controlla stato Git, commit e sorgenti effettivi: questo documento descrive il punto di passaggio
del 18 ottobre 2026 e non sostituisce la verifica del checkout. Non ripartire da zero e non
sovrascrivere modifiche dell'utente. Il porting è ancora una prova parziale della tappa 1.

## Repository, base e organizzazione

- Origin personale: https://github.com/albertofiocco24-star/Ice_and_Fire_Fabric26.3_port.git
- Upstream: https://github.com/AlexModGuy/Ice_and_Fire.git
- Ramo di lavoro: port/fabric-26.3.
- Base originale: ramo upstream 1.20, Minecraft 1.20.1,
  commit 0526aad94119b705a86207623481b087c4d052ca.
- Il codice Forge originale è conservato nella cartella principale src/.
- Il port Fabric è un progetto Gradle indipendente in fabric/, con src/main e src/client separati.
  Non tentare di compilare il port usando per errore il Gradle Forge nella radice.
- Commit precedenti: 7c0f9cd50, primo checkpoint; d0a80e73f, correzione keybinds 0.1.1.
  Le modifiche 0.1.2 sono incluse nel commit che aggiunge questo documento.

## Obiettivo e vincoli permanenti

Portare completamente Ice and Fire a Minecraft Java 26.3 con Fabric, per tappe, mantenendo
modelli, texture, animazioni, statistiche, identificatori e meccaniche originali dove possibile.
Non introdurre deliberatamente modifiche al gameplay. Usare mondi nuovi; nessuna conversione
di salvataggi Forge. Conservare storia Git, licenza LGPL e attribuzioni originali.

Confrontare sempre le funzionalità migrate con il codice originale presente in src/.
Verificare l'eventuale disponibilità di Citadel compatibile prima di riutilizzarlo; attualmente
non è dipendenza del progetto Fabric. Le funzioni necessarie vengono migrate conservando
provenienza/licenze, senza sostituire automaticamente modelli e animazioni con un altro sistema.
Separare codice client/comune e migrare registrazioni, eventi, rete, configurazioni,
persistenza, rendering e accessi interni. Preferire API disponibili; mixin soltanto quando mancano.

Lavorare e fare commit comprensibili su port/fabric-26.3, con push all'origin personale.
Non creare release pubbliche e non unire il ramo principale. Spiegare i comandi Git in italiano.
L'utente non vuole condividere la sua email nella chat: non chiederla o stamparla; usare il
proprio autore Git già configurato o una noreply GitHub verificata, senza cambiare config globali.
Le skill e le integrazioni locali del vecchio PC non vengono trasferite tramite Git: scoprirle
nel nuovo ambiente. Non dichiarare usata una skill assente o una connessione non verificata.

## Tappe concordate: fermarsi al completamento di ciascuna

1. Drago di fuoco giocabile: uovo, schiusa, crescita, persistenza; modelli, varianti, texture e
   animazioni originali; terra, volo, combattimento e soffio; addomesticamento, interazioni,
   cavalcatura e volo controllato; oggetti, controlli, interfacce e sincronizzazione necessari.
   Elementi di prova disponibili in creativo o con comandi, senza richiedere generazione naturale.
   Compilare o avere un'entità incompleta NON conclude la tappa.
2. Draghi di ghiaccio e fulmine con attacchi, inventari, equipaggiamenti e sistemi collegati,
   incluse le funzioni del drago di fuoco rimaste fuori dalla prima tappa.
3. Altre creature con IA e interazioni, poi blocchi, oggetti, macchinari, bestiario, ricette,
   bottini e progressione. Inventariare i contenuti originali per controllarne la copertura.
4. Strutture, tane, risorse, comparsa naturale e integrazione finale. Segnalare le integrazioni
   accessorie prive di equivalente verificato per Fabric 26.3 e collaudare il ciclo di gioco.

Fermarsi alla fine di ogni tappa, consegnare JAR di prova identificabile, dipendenze esatte,
istruzioni e risultati dei test/revisione, attendere il riscontro dell'utente e correggere i
problemi prima della successiva. Le prove visive e di interazione dell'utente sono necessarie.

## Toolchain fissata nel progetto

Minecraft 26.3; JDK 25; Fabric Loader 0.19.5; Fabric API 0.163.0+26.3;
Loom 1.18.4; Gradle Wrapper 9.7.1; JUnit Jupiter 5.13.4.
fabric.loom.disableObfuscation=true è intenzionale. Non sostituire versioni senza verificarle.
Versione corrente della mod: 0.1.2-stage1-dev.

Sul nuovo PC installare/selezionare JDK 25, verificare java -version e JAVA_HOME.
La cache Gradle e il JDK scaricato sul vecchio PC non sono inclusi nella repository.
Da PowerShell nella radice:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -version
.\fabric\gradlew.bat -p fabric build --console=plain
```

JAR installabile in fabric/build/libs/, senza suffisso -sources.
Aprire fabric/ come progetto Gradle nell'IDE. Conservare il vecchio progetto Forge come riferimento.

## Implementato nel primo checkpoint

Registrazioni Fabric di fire_dragon, dragon_egg, quattro varianti di uova e dragon_meal;
incubazione nel fuoco, trasferimento del proprietario e del nome alla schiusa, crescita e
statistiche di base, fame, salvataggio dei primi stati, pasto che fa crescere di un giorno,
monta dal terzo stadio (50 giorni), controlli iniziali di terra/volo e rete con controllo del
proprietario. Asset Tabula e texture originali caricati da un lettore dedicato; pose iniziali
di camminata, volo, seduta e sesso. Non equivale alla migrazione completa delle animazioni Citadel.

File principali: IafContent, DragonGrowth, FireDragonEgg, FireDragon, DragonControlPayload,
TabulaLoader, FireDragonPoseModel, FireDragonRenderer, DragonKeyMappings, sotto fabric/src/.

## Prove effettivamente fatte e riscontro dell'utente

- Build Forge originale fallita in createMcpToSrg per mapping/archivi e problemi ZIP nel sandbox;
  non è stato dimostrato un difetto della repository originale. Problema separato dal porting.
- Build Fabric standard nel sandbox Windows del vecchio PC bloccata da Java ZipFS:
  Files.isWritable=false e ReadOnlyFileSystemException anche su ZIP nuovi scrivibili con altre API.
- Build diagnostiche riuscite con cache Minecraft preparata localmente e init script descritto sotto.
  La build ordinaria fuori dal sandbox non è stata ancora verificata: provarla prima sul nuovo PC.
- Client di sviluppo avviato: mod, renderer, grafica, audio e atlanti caricati. Nessuna prova
  di gameplay in un mondo eseguita dall'agente. L'ultimo avvio verificava il nuovo mixin;
  dopo quell'avvio sono state apportate piccole correzioni matematiche, ricompilate e testate.
- Server eseguito soltanto con runServer --args=--initSettings: generazione configurazione e
  inizializzazione, NON avvio di un mondo/server multiplayer. EULA non accettata automaticamente.
- L'utente usa CurseForge e ha installato Fabric API 0.163.0+26.3. La precedente 0.161.0+26.3
  era incompatibile con il requisito del JAR: non abbassare il requisito per aggirare il problema.
- L'utente ha confermato avvio, schiusa e, dopo 0.1.1, discesa corretta del drago.
  L'incubazione originale richiede oltre 7200 tick nel fuoco; i contatori crescevano correttamente.
- Ultima build: 16 test JUnit superati, nessun fallimento. Comprendono crescita/statistiche,
  incubazione, caricamento Tabula, keybind/rimappatura, continuità di tutti i confini dei cicli
  camminata/volo, aggancio alla posa senza accumulo, trasformazione del cavaliere confrontata
  con la matrice vanilla a varie scale/rotazioni, seduta e distanza camera con crescita.
  Un test verifica tutte le 22 punte dorsali: sei facce e UV finite/in intervallo per ciascuna.
  Questo NON dimostra che il difetto visivo delle punte sia risolto.

## Correzione 0.1.1 già consegnata e provata dall'utente

I keybind usavano interi GLFW della 1.20.1 invece dei codici SDL della 26.3.
Ora usano InputConstants.KEY_X/KEY_R/KEY_G, categoria Ice and Fire, traduzioni italiane e
identificatori nuovi key.iceandfire.* per evitare il riutilizzo dei vecchi default errati.
Salto sale; X scende ed è riassegnabile. R/G sono trasmessi ma gli attacchi NON sono implementati.
L'utente ha confermato che la discesa funziona.

## Ultime segnalazioni dell'utente e modifiche 0.1.2 salvate

Segnalazioni: punte grigie che scompaiono/si vedono solo da alcuni lati; animazione errata o
a scatti; cavaliere non agganciato alla seduta alle varie dimensioni; visuale ostruita,
soprattutto allo stadio 5, in prima e terza persona.

Modifiche 0.1.2 implementate e compilate, ma NON ancora consegnate né provate dall'utente:

- Interpolazione continua anche quando il ciclo è zero e ai confini delle pose;
  fattore corretto di due unità/tick per il volo; eliminato il salto quando una posa di volo
  coincide con la posa di riferimento; transizione proporzionata al progresso di volo.
- Ripristinati alcuni movimenti secondari originali di corpo e coda; rimosso l'azzeramento
  dell'interpolazione di yaw del drago cavalcato. Nessuna misura FPS o promessa di eliminare ogni lag.
- DragonRiderLayout migra le curve originali di altezza/distanza della seduta e gli offset
  per volo, cammino e sguardo. La seduta segue la dimensione visiva anche quando la scala
  della collisione si ferma a 7 nello stadio 5; non applicare due volte la scala fisica.
- L'attributo nativo CAMERA_DISTANCE adatta la terza persona: distanza totale 4 + renderSize,
  usando ancora la collisione vanilla della camera contro i blocchi. F5 resta il selettore vanilla.
  Nessuna prova visiva confermata per la prima persona dopo questo intervento.
- La posizione renderizzata del cavaliere segue BodyUpper -> Neck1 animati, come il layer
  originale; gli offset fisici/POV conservano la logica originale. Un mixin SOLO client su
  EntityRenderer.extractRenderState applica la posizione. Non era disponibile un callback
  Fabric equivalente per sostituire la posizione di rendering di un passeggero esistente.
- La posizione usa le coordinate interpolate del render state del drago, non xo/yo/zo.
  Corretto anche il segno laterale della trasformazione; il test la confronta con la matrice
  effettiva vanilla, compreso l'offset verticale 1.501.

Punte grigie: NON risolte. Verificati asset originali, cubi/UV e materiale già senza culling.
Non dichiarare che basti disattivare il culling. Il nuovo test geometrico è solo diagnostico.
Sono utili due screenshot dello stesso drago da lati diversi, con stadio/variante ed eventuali
shader/resource pack/mod di rendering. Non cambiare modello o texture originali alla cieca.
Ricerca plugin Blockbench sul vecchio ambiente: nessun risultato; nessun MCP Blockbench usato.
Verificare nuovamente sul nuovo PC se disponibile, senza affermare che sia collegato.

## Revisione e test richiesti alla ripresa

La skill review-agent è stata applicata al primo checkpoint e alla correzione 0.1.1.
Durante la revisione delle ultime modifiche sono stati individuati/corretti problemi di
interpolazione e trasformazione laterale; l'ultimo test aggiunto li copre. Prima di consegnare
0.1.2, completare una revisione finale in sola lettura di TUTTO il diff rispetto a d0a80e73f,
inclusi i nuovi file e il mixin, e aggiornare fabric/REVIEW.md. Quel documento attualmente
descrive soprattutto i checkpoint precedenti; non interpretarne i conteggi storici come attuali.
Usare la skill review-agent se presente; se manca, cercarla e segnalare l'assenza, senza
dichiararla applicata. Nessuna delega durante la revisione se la skill la vieta.
Segnalare tutti i difetti concreti introdotti in ordine P0/P1/P2/P3; correggerli fuori dalla
fase di revisione e rieseguire i controlli pertinenti. Non inventare difetti di stile.

Azioni immediate, in ordine:

1. Verificare checkout e toolchain, compilare con il comando standard sul nuovo PC.
2. Completare la revisione finale 0.1.2, correggere problemi e ripetere i controlli interessati.
3. Preparare JAR 0.1.2 chiaramente marcato prova parziale, checksum, dipendenze e istruzioni.
   I JAR/build/log locali del vecchio PC e outputs/ NON sono trasferiti dal clone della repository:
   ricostruirli. Non consegnare il JAR -sources e non lasciare più versioni della mod in mods/.
4. Far provare all'utente camminata, virate, decollo, discesa X, atterraggio, aggancio del
   cavaliere e F5/visuale prima-terza persona a 50, 75, 100 e 125 giorni, a terra e in volo.
   Non allargare la monta allo stadio 2: nella base originale è dal terzo stadio.
5. Raccogliere le immagini necessarie e riprodurre il difetto delle punte, correggerlo con
   confronto al modello originale e prova visiva, senza dichiararlo risolto dal solo test geometrico.
6. Continuare la tappa 1, senza iniziare la tappa 2.

Restano da migrare: combattimento, soffio e proiettili/effetti sui blocchi; IA e volo autonomo;
comportamento acqua/lava; inventario/interfacce e interazioni complete; alimentazione e comandi;
gestione dell'uovo danneggiato; animazioni di attacco e altre animazioni secondarie, occhi,
differenze visive complete maschio/femmina, suoni e particelle; configurazioni originali.
Verificare anche dismontaggio sicuro, salvataggio/riapertura e sincronizzazione in veri mondi.
La tappa 1 è incompleta anche se i problemi visivi segnalati venissero corretti.

Procedura finale riproducibile della tappa 1: mondo nuovo, quattro varianti di uova, schiusa
nel fuoco, crescita, proprietà/addomesticamento, cavalcatura, decollo, volo, atterraggio, soffio,
combattimento, salvataggio/riapertura anche durante incubazione/volo, server dedicato con due
client e verifica che un non proprietario non controlli il drago. Confrontare resa e gameplay
con l'originale; distinguere sempre prove automatiche, avvio, test in mondo e conferma dell'utente.

## Nota ambiente diagnostico precedente: solo se il problema si ripresenta

Sul vecchio PC il JDK era in work/toolchains/jdk-25.0.4.1+1 e GRADLE_USER_HOME in
work/gradle-home, fuori dalla repository. Non copiare questi percorsi assoluti sul nuovo PC.
L'init script locale work/public-api-only.gradle conteneva:

```groovy
gradle.beforeProject { project ->
    project.pluginManager.withPlugin('net.fabricmc.fabric-loom') {
        project.loom.enableTransitiveAccessWideners.set(false)
        project.loom.interfaceInjection.enableDependencyInterfaceInjection.set(false)
        project.loom.enableModProvidedJavadoc.set(false)
    }
}
```

Veniva passato con -I e percorso assoluto al Gradle Fabric. Erano stati preparati anche gli
archivi Minecraft separati nella cache locale tramite .NET ZIP. Questo script da solo NON
ricrea quella preparazione. Non applicare automaticamente il workaround sul nuovo PC e non
commetterlo come configurazione di produzione. Diagnosticare eventuali errori reali prima.

Il push dall'ambiente dell'agente sul vecchio PC falliva nel gestore credenziali Git/MSYS
con NtCreateDirectoryObject 0xC0000022, prima dell'autenticazione GitHub. Provare il normale
terminale PowerShell/VS Code dell'utente o il client Git autenticato; non estrarre credenziali
e non aggirare i controlli. Verificare che il commit di questo handoff sia effettivamente remoto
prima di cambiare PC: senza push riuscito il nuovo clone non contiene queste modifiche.

Nota operativa24ottobre: ripresa effettiva alle22:12Roma dopo reset; automazione stessa chat riportata a controllo5min. Sessione in corso, nessuna sospensione.
