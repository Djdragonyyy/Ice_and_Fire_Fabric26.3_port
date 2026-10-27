## Checkpoint 0.1.47 - 27 ottobre 2026 - prima DragonForge manuale

Forge di fuoco funzionante tramite il percorso reale del soffio: multiblocco
3x3x3, inventario a3slot, ricetta ferro+sangue->lingotto di acciaio di fuoco,
progresso/heat originali, fallback in cenere, salvataggio e automazione hopper.
Modelli/texture/GUI originali, blocchi in creativo, luce15 del nucleo assemblato,
blocchi protetti dal soffio e input/griglie disattivati alla perdita del nucleo.
GUI aperta via C2S, progresso32bit e trasferimento output autorevole verificati.

Full phase47-final.log verde10m50s:96JUnit,153GameTest mod+baseline154,16client,
45PNG correnti. XML senza failure/error. Sei nuove prove server: ricetta e codec
rete, struttura/power/output, salvataggio/drop, hopper/menu, fallback/heat,
callback input orfano e produzione con DragonFireCombat.breath/mobGriefingfalse.
Tre nuove PNG finali aperte: icone, tutte3le altezze del multiblocco e GUI.
Nessun test/helper o ricetta di prova lunga nel JAR distribuito.

Adattamenti nativi: un solo ID core con ACTIVE preserva la block entity;
input/brick delegano inventario senza BEs ridondanti. Ricette usano codec26.3
ItemStackTemplate e tag comune c:ingots/iron. CookTime int e dati GUI divisi in
quattro16bit. Test client usa SOLTANTO nel mod test ricetta rame100000/progresso
70000 per verificare overflow; produzione originaleferro1000 testata sul server.
Il risultato in screenshot GUI e fixture, non una finta prova di lavorazione.
Primo test C2S leggeva previsione client prima del pacchetto server: attesa
corretta, asserzione su conteggio effettivo mantenuta, backend testato separato.

Forge NON ancora completa: mancano richiamo/alimentazione automatica del drago,
comparatori originali e ricette di costruzione/progressione. Blocchi disponibili
in creativo/comandi, come altri strumenti del checkpoint. TEST-0.1.47.md spiega
struttura, uso e differenze. Ricognizione prossima48 in lavoro privato
work/phase48-preparation/RESEARCH.txt, nessun codice48 ancora avviato.
Restano le altre parita/config/ciclo vitale, navigazione complessa, anteprima
passeggeri e punte grigie. Tappa1 e port NON conclusi. Precedente46:25b623f89.

Stessa chat/ramo port/fabric-26.3. Email annullate. Controllo quota ogni50minuti,
niente reset o consumo artificiale. Continuare lavoro utile fino a esaurimento,
poi salvare/checkpoint/push e spegnere ordinatamente senza forzare app con dati
non salvati. Dopo tale spegnimento mettere il controllo in pausa fino a nuova
autorizzazione. Le prove dedicate44 restano precedenti, non riciclate come47.

## Checkpoint 0.1.46 - 27 ottobre 2026 - richiamo con stufato di fuoco

Portato TemptGoal originale con tag tempt_dragon: solo fire_stew, velocita 1,
raggio 10 nativo e controllo di entrambe le mani. Il drago libero a terra si
avvicina senza consumare o entrare in riproduzione. Difesa, comandi, sonno/NoAI,
pilota/spalla, volo, corteggiamento e cibo riservato mantengono precedenza.
Il decollo casuale non interrompe il richiamo attivo.

Full phase46-final-2.log verde in 10m52s: 95 JUnit, 147 GameTest mod + baseline
= 148 server, 15 client, 42 PNG correnti. XML senza errori/failure. Due nuove
prove native passate anche in focus28s: camminata tramite selettore reale e
interruzione alla rimozione dello stufato; tag, mani e condizioni di precedenza.
Nessuna nuova risorsa grafica/audio o prova visiva dedicata al richiamo.

Primo full falliva il test preesistente del cambio velocita in volo. Isolato
passava; riprodotto il caso controllato con yaw4: il giro ancora perpendicolare
alla rotta produce accelerazione X circa3e-18, azzerata dal cutoff nativo0.003.
Corretto SOLO fixture: orientamento lungo la rotta prima del cambio velocita,
asserzioni di moto mantenute, controllo aggiunto su destinazione identica.
Focus allineato verde22s, poi intero full ripetuto. Logica di volo invariata.
Vedere TEST-0.1.46.md e REVIEW.md. Due PNG uova/piccolo correnti ricontrollate;
etichette della tavola uova a contatto gia dichiarate nella45.

DragonForge ancora da implementare. Ricognizione originale/API in lavoro privato
work/phase46-preparation/DRAGONFORGE-NEXT.md: multiblocco, inventario/ricette,
soffio, menu, persistenza e prove end-to-end. Non e una forge funzionante.
Restano altre parita/configurazioni/interazioni/ciclo vitale, navigazione
complessa, anteprima passeggeri e punte grigie. Tappa1 e port NON conclusi.
Precedente45 commit693d2d32b e relativo pacchetto verificati. DedicatedServer44
resta prova precedente, non nuova certificazione46.

Ripresa27ottobre autorizzata. Stessa chat/ramo, controllo quota ogni50minuti,
nessuna email o credito di reset. Continuare lavoro utile fino a esaurimento
residuo, poi salvare/checkpoint/push e spegnere ordinatamente senza forzare app
con dati non salvati. Dopo tale spegnimento pausa del controllo fino a nuova
autorizzazione. Fine sessione non equivale a completamento del port.

## Checkpoint 0.1.45 - 27 ottobre 2026 - incubazione e schiusa

Completata la verifica avviata il 26 ottobre: phase45-final.log termina BUILD
SUCCESSFUL in 10m35s. 95 JUnit, 145 GameTest mod + baseline = 146 server,
15 entrypoint client e 42 PNG correnti. XML senza errori/failure. Quattro nuovi
GameTest di incubazione passati anche isolatamente in phase45-focused.log.

Uova di fuoco: lava valida per incubare come nel Forge originale; FIRE tag
preesistente conservato (anche soul fire). Acqua pausa il progresso senza
azzerarlo; magma/campfire non aggiunti come fonti arbitrarie. Schiusa centrata
sul blocco, nome/proprietario/colore/fame50 conservati. Guardia isRemoved dopo
super.tick impedisce di generare un piccolo da un uovo gia raccolto durante
il tick nativo: provato con effetto WITHER reale, un oggetto e zero draghi.
Suono egg_hatch originale e spegnimento fuoco trasmessi dal server; OGG invariato.
Movimento originale X/Z dell'uovo soltanto sul calore valido, reset a freddo.

Nuova prova client con quattro colori, fuoco/lava e freddo, controllo del reset
sul modello condiviso e schiusa effettiva con nome/owner. Due PNG nuove lette:
uova complete e piccolo completo. Le etichette della tavola uova sono troppo
lunghe in alcune colonne e si toccano; limite della tavola test, non del renderer
in gioco. Istantanee non costituiscono da sole prova di movimento o audio udito.
Nessuna nuova prova DedicatedServer nella45: quella44 resta nel suo pacchetto.
Vedere TEST-0.1.45.md e REVIEW.md. Tappa1 e port NON conclusi.

Restano DragonForge, parita/config/temporizzazioni e altre interazioni originali,
dettagli riproduzione/sonno, navigazione complessa, anteprima passeggeri e punte
grigie. Prima del prossimo intervento confrontare le differenze con l'originale.
Stessa chat/ramo port/fabric-26.3, nessuna email. Ripresa27ottobre autorizzata.
Controllo quota ogni50 minuti; nessun credito di reset. Proseguire lavoro utile
fino a esaurimento residuo, poi salvare/checkpoint/push e spegnere ordinatamente
senza forzare applicazioni con dati non salvati; pausa del controllo dopo tale
spegnimento fino a nuova autorizzazione. Non confondere fine sessione con port finito.

## Checkpoint 0.1.44 - 26 ottobre 2026 - logout montato e rientro in volo

Verificato DedicatedServer ordinario: logout ANCORA IN SELLA, salvataggio nativo
RootVehicle, arresto/riavvio dello stesso mondo e rientro dei medesimi due profili
locali. Il proprietario torna pilota dello stesso UUID drago; volo ripristinato
su server e su entrambi i client, eta/sesso/armature head-tail/nome e flagNoAI
conservati. Carcassa75/DeathStage2 nel mondo,20proxy ricreati senza duplicati.
Sei processi exit0: due server e quattro client (due profili prima/dopo).
Piani esportati con username sintetici stabili e --nogui esplicito per server.
Nessuna autenticazione/account personale modificata, tutto su loopback.

Suite ordinaria phase44-final.log verde10m34s:94JUnit,141modGameTest+baseline142,
14client e40PNG correnti. Prova dedicata finale phase44-dedicated-launch-3.log,
cinque PNG correnti,45totali. Nuove immagini rientro owner/guest controllate:
attesa GUI nulla corretta prima dello screenshot (il primo tentativo mostrava
LoadingTerrain). Report/log/6exit0 in dedicated-mounted-test-results.
Tutte99classi produzione SHA identiche alla43, nessun harness nelJAR. Si aggiunge
collaudo, non una finta correzione di perdita drago: RootVehicle e nativo.
TEST-0.1.44.md descrive caso e limiti.43unmounted-world resta prova precedente.

Restano DragonForge, altre parita/config/temporizzazioni, ciclo uova e dettagli
riproduzione/sonno, navigazione complessa, anteprima passeggeri e punte grigie.
Tappa1 e port completo NON conclusi. Prossimo candidato: condizioni ed effetti
originali di schiusa del fuoco (lava, posizione centrata, suoni e movimento uovo).
Nessuna implementazione45 ancora; ricerca sul codice originale/Forge.

Stessa chat/ramo, controllo OGNI50minuti, non5. Ultimo controllo21:10Roma:
53%primaria/39%settimanale usata. Email ANNULLATE. Proseguire lavoro utile fino
quota esaurita; poi salvataggi/checkpoint/push e spegnimento ordinato senza
forzare applicazioni con dati non salvati. Vecchi divieti storici superati.

## Checkpoint 0.1.43 - 26 ottobre 2026 - server dedicato e mondo riaperto

Collaudato un DedicatedServer ORDINARIO con due processi client rendering via
TCP locale, profili di sviluppo distinti. Owner/ospite, rifiuto dei comandi
falsificati, volo, velocita/sesso e armature head-tail sincronizzati verificati.
Dopo dismount, logout e arresto ordinato, un NUOVO processo riapre lo STESSO mondo:
UUID/owner offline, eta/comando/nome/NoAI/armature del drago e carcassa75giorni
DeathStage2 conservati.20parti ricreate con UUID nuovi, nessun duplicato/orfano.
Quattro processi exit0, report actualWorldRestart=true;49chunk persistenti letti.
Tre immagini dedicate correnti controllate; quella ospite e tagliata, non prova
l'intero modello. Harness opt-in separato, nessun helper nel JAR distribuito.
99classi di produzione SHA identiche alla0.1.42; nessun nuovo gameplay inventato.

Anche regressione ordinaria phase43-final.log verde10m52s:94JUnit,141modGameTest
+baseline142,14client e40PNG correnti, oltre ai2client/3PNG dedicati.43PNGtotali.
Report/log/config test e confronto classi in dedicated-world-test-results.
Vedere TEST-0.1.43.md per riproduzione, correzioni fixture e limiti precisi.
Il drago del tentativo montato non era perso: trovato su disco in RootVehicle
player nativo. Per il test di persistenza NEL MONDO si smonta prima del logout.
Logout/relogin montato, account autenticati e arresti improvvisi NON certificati.

Ultimo gameplay0.1.42: esplosioni opzionali native (defaultfalse), multipart41.
Restano DragonForge, altre parita/config/temporizzazioni, uova/riproduzione/sonno,
navigazione complessa, anteprima passeggeri e punte grigie. Tappa1/port aperti.
Prossimo: valutare prova mounted logout/relogin con profili di sviluppo stabili;
ricerca privata work/phase43-preparation/RESEARCH.txt. Nessuna nuova fase avviata.

Stessa chat e ramo; controllo ogni50minuti, non5; email ANNULLATE. Continuare
fino a quota esaurita, poi salvare/checkpoint/push e spegnere ordinatamente senza
forzare applicazioni con dati non salvati. Vecchi divieti storici superati.

## Checkpoint 0.1.42 - 26 ottobre 2026 - esplosioni opzionali

Aggiunta explosiveDragonBreath=false nel config server, come originale. Se
abilitata, solo l'impatto delle cariche aggiunge esplosione nativa26 con potenza
min(2,stadio-2), omessa negli stadi1/2. Owner/alleati/carcasse e tutte le parti
multipart protetti da danno e impulso; il genitore non riceve dieci colpi proxy.
Distruzione vincolata a mobGriefing, dragonGriefing e tamedDragonGriefing;
blacklist originale e blocchi infrangibili preservati. Danno/effetti rimangono
quando il terreno e protetto. Nessuna modifica PNG/mesh/UV o immunita dismount.
Adattamento esplicito: effetti e drop dell'esplosione sono nativi, non replica
integrale del vecchio BlockLaunchExplosion. Vecchi JSON mantengono false.

Suite completa phase42-final.log verde in11m06s:94JUnit,141modGameTest+baseline
=142,14prove client e40PNG correnti. Tre nuove prove native mirate verdi in28s:
carica reale, opzione/stadio, owner registrato e alleati, proxy, terrain gates e
blacklist. Non ancora prova visiva client dell'esplosione abilitata; vedere
TEST-0.1.42.md. La0.1.41 precedente e pubblicata al commit7ce22198f.

Prossimo: collaudo DedicatedServer ordinario con due client e mondo realmente
salvato/riaperto. Ricerca privata work/phase43-preparation/RESEARCH.txt, nessuna
implementazione43 ancora. Restano altre parita/config/temporizzazioni,
DragonForge, uova/riproduzione/sonno, navigazione complessa, anteprima passeggeri
e punte grigie. Tappa1 e port completo NON conclusi.

Istruzioni operative: stessa chat/ramo; controllo ogni50minuti, non5. Email
ANNULLATE dall'utente: niente invii/ricerche/ritentativi. Continuare fino a quota
esaurita, poi salvataggi, checkpoint/push e spegnimento ordinato senza forzare
applicazioni con dati non salvati. Vecchi divieti storici superati.

## Checkpoint 0.1.41 - 26 ottobre 2026 - multipart nativo

Portate le dieci parti originali (testa, collo, quattro ali e quattro segmenti
coda) come entita native transitorie sincronizzate, associate tramite ID e UUID.
Posizioni/dimensioni seguono renderSize/3 originale nei cinque stadi, senza
cambiare collider fisico, navigazione o posto pilota. Danno originale: testa e
punta coda x1.5, ali x0.5, altre parti x1, con cooldown/armatura del genitore.
Interazione nativa completa inoltrata una sola volta: bastone, corno, monta e
raccolta della carcassa. Armi survival consumano durabilita una volta grazie
alla callback nativa risolta sul genitore; nessun secondo danno nel mixin.
Crescita ridimensiona le stesse parti; cattura/rimozione/unload eliminano la
famiglia. Parti mancanti ricostruite, orfane scadono, nessun salvataggio autonomo.
Cariche proprie non colpiscono la testa; pilota non seleziona le proprie parti.

Verifica completa phase41-final-4.log: BUILD SUCCESSFUL in16m04s,94JUnit
(build up-to-date),138GameTest mod+baseline=139,14prove client e40PNG correnti.
Cinque nuovi GameTest e una prova C2S reale; XML senza errori. Il log final-3
interrotto NON e la prova consegnata. Panoramica0039 controllata: non mostra
wireframe; dimensioni e selezione sono provate dalle asserzioni native/C2S.
Nessuna modifica PNG, UV o geometria. Vedere TEST-0.1.41.md e REVIEW.md.

Restano nella tappa1: altre parita/config/temporizzazioni, DragonForge ed
esplosioni opzionali, ulteriori interazioni/uova/riproduzione/sonno, navigazione
complessa, anteprima passeggeri, punte grigie, DedicatedServer ordinario e
persistenza MP su mondo riaperto. Tappa1 e port completo NON conclusi.
Prossimo: audit e completamento delle configurazioni originali di combattimento.

Istruzioni operative correnti: ripresa autorizzata26ottobre18:50; stessa chat e
ramo port/fabric-26.3; controllo ogni50minuti, non5; email annullate dall'utente.
Continuare fino a esaurimento quota, poi salvare/test/checkpoint/push e spegnere
ordinatamente senza forzare applicazioni. Vecchi divieti storici superati.

# Stato della tappa 1 — drago di fuoco, Fabric 26.3

## Checkpoint 0.1.37 - movimenti originali del corpo e protezione delle ali

Ripristinati fase normalizzata delle catene, offset verticale bob, onde di collo e
coda, oscillazioni del corpo, zampe e dita in volo e respiro lento nel sonno.
Conservata la dissolvenza nativa al decollo/atterraggio. Nessuna modifica a mesh,
UV, PNG, dimensioni fisiche, velocita o pacchetti della mod.
Braccia alari e figli diretti (incluse membraneL6/R6) seguono il criterio
originale: gait escluso per WING/EPIC_ROAR, ciclo alare mantenuto in hover/tackle.
Feed, agganci del cavaliere, pose e reset senza accumulo ancora verificati.

Suite ordinaria:84JUnit,119GameTest mod (120 con baseline),11prove client,
33immagini. Build completa passata in9m05s; ripetizione client per inquadrature
1m51s. Due nuove tavole alte di volo e una attacco/camminata, tutte controllate.
Rete rieseguita davvero sulla0.1.37:1GameTest server,2client separati, tre exit0,
proprieta/comandi/volo/sesso/velocita/head-tail verificati. Tre immagini correnti,
36totali. Report/XML/log/versione runtime inclusi; nessun harness nel JAR.
Vedere TEST-0.1.37.md. Non e ancora DedicatedServer ordinario/account autenticati.

PROSSIMO: ciclo completo della carcassa, raccolta originale e XP per stadio.
Manca ancora nel port: oggi la morte usa la rimozione vanilla e il solo drop
inventario. Riferimenti principali EntityDragonBase.tickDeath539, interactAt1087,
getExperienceReward575, getRandomDrop1299, NBT ModelDead/DeathStage; dead pose e
texture scheletro originali. Preservare inventario/proprieta e regola dismount
senza immunita in volo. Dettagli locali work/phase38-preparation/RESEARCH.txt.
Restano anche multipart, altre parita, prove DedicatedServer/persistenza MP,
punte grigie e cause dei casi intermittenti sonno/escort. Tappa1/port aperti.
Proseguire autonomamente nella stessa chat/ramo; non sospendere o spegnere MAI.

## Checkpoint 0.1.36 - feeding e verifica di corpo/ali

Feeding manuale ora sincronizzato nei cinque stadi, collo/mandibola con easing
sinusoidale e recupero completo entro20tick. Clic ripetuti senza riavvio del ciclo;
precedenza a combattimento, volo, cavaliere, spalla, sonno e NoAI. Stats invariati.
Audit di vertici/facce/normali/mirroring/UV esteso a TUTTO il modello originale.
Dieci nuove tavole GPU dei cinque stadi: feeding e corpo/ali laterali/posteriori,
seduti e in volo, a dimensioni normalizzate. PNG e geometria originali invariati.

Suite definitiva:80JUnit,119GameTest mod (120 con baseline),11prove client,
30immagini. Build completa passata in8m39s: phase36-final-3.log, TEST-0.1.36.md.
Corretto il fixture del parlato per isolare il richiamo esplicito dai casuali;
verificata anche chiamata sovrapposta, senza indebolire le asserzioni.
Un precedente tentativo ha fallito il caso escort0->1; isolato e completo poi
passano, causa NON provata e diagnostica mantenuta. Contrattacco intermittente,
punte grigie e DedicatedServer ordinario/persistenza MP restano aperti.
La rete a due client della0.1.35 non e stata rieseguita o ri-etichettata come0.1.36.

PROSSIMO: movimenti secondari originali di corpo/collo/coda e assetto in volo,
fase corretta delle catene e precedenza delle pose alari durante gli attacchi.
Citadel: fase catene=rootOffset*PI/(2*numeroParti), bob non bounce=
(sin(tempo*velocita)-1)*ampiezza*quantita. Nel port fase e offset sono diversi.
Escludere armL1/armR1 e loro figli diretti dal gait per WING/EPIC_ROAR.
Riferimenti in TEST-0.1.36.md; dettagli locali work/phase37-preparation/RESEARCH.txt.
Proseguire autonomamente nella stessa chat/ramo, completando tappa1 prima delle
successive. Non sospendere o spegnere MAI il computer. Tappa1/port ancora aperti.

## Checkpoint 0.1.35 — due client Minecraft separati via TCP

Verifica reale a3JVM: GameTestServer standalone locale, proprietario e osservatore.
Due slot configurati nel solo harness opt-in, profili fittizi, nessun account
personale. Non proprietario respinto da montaggio/comandi; volo del proprietario,
UUID, sesso, head/tail e velocita del server osservati da entrambi. Disconnessioni
native e tre exit code0. Tre immagini di rete controllate. Un test server rete
passato, report/XML/log e codici di uscita inclusi; harness assente dal JAR.
Suite ordinaria finale79JUnit/117modGameTests (118baseline)/10client/20immagini,
passata in8m45s; rete aggiunge3immagini,23totali. Vedere TEST-0.1.35.md.
Non e ancora collaudo di DedicatedServer ordinario con account autenticati.
Contrattacco intermittente e punte grigie aperti; nessuna causa inventata.

PROSSIMA PRIORITA UTENTE: feeding in tutti5gli stadi e modello piu coerente con
texture, in particolare CORPO E ALI (risposta esplicita). Analisi avviata;
nessuna correzione di feeding consegnata qui. Confrontare originale, geometria,
UV e immagini per stadio prima di modificare. Tappa1/port completo aperti.
Non sospendere o spegnere il PC: obiettivo eliminato e confermato all'utente.


## Checkpoint 0.1.34 — overlay maschile originale

Ripristinata la composizione semitrasparente male_red/green/bronze/gray sulle
texture originali per stadio e sonno/blink, tramite ReloadableTexture26.3.
PNG originali invariati; femmina usa la base, cache maschile limitata40chiavi,
metadati conservati e caricamento/ricaricamento gestiti nativamente.
Suite finale79JUnit/117GameTest mod (118 con baseline)/10client/20immagini,
passata in8m35s. Controllate immagini femmina, maschio e dopo ricaricamento.
La prova verifica tutte40combinazioni e attende la scomparsa dell'overlayMojang.
Vedere TEST-0.1.34.md. Punte grigie e contrattacco intermittente restano aperti;
nuova diagnostica di danno/Weakness nel messaggio, asserzioni invariate.
Prossimo: due client separati su server di test standalone; ricerca locale
phase35-preparation. Tappa1/port non conclusi; mai sospendere o spegnere il PC.


## Checkpoint 0.1.33 — camminata e velocita sincronizzata

Camminata originale (velocita/input/sprint/retromarcia e inputY), opzione
server dragonFlightSpeedMod1 finita0..2 e FLOAT nativo inviato al pilota.
Superficie lava e volo autonomo usano il fattore; volo pilotato conserva
la curva originale indipendente. Accelerazione autonoma limitata per passo,
non moltiplicatore cumulativo. Dettagli/adattamenti in TEST-0.1.33.md.
Suite finale79JUnit/117GameTest mod (118 con baseline)/9client/17immagini,
passata in8m40s. Sei prove config e quattro ripetizioni isolate sonno passate.
Un precedente tentativo completo ha riprodotto il contrattacco intermittente:
causa non provata, traccia per tick conservata; non dichiarare il caso risolto.
Prossimo: overlay maschile originale, preparazione locale phase34-preparation.
Tappa1 e port completo ancora aperti. Non sospendere o spegnere il PC.


## Checkpoint 0.1.32 — sopra la superficie della lava

Migrato ramo originale del pilota non immerso/non in volo sopra il blocco lava:
camminata nativa, velocita base*1.8, laterale0.05, sprint1.2, retro0.2,
inputY conservato, collisioneY0.2 e attrito0.7. Autorita locale nativa.
Suite definitiva77JUnit/114GameTest mod (115 con baseline)/8client/17immagini,
passata in8m24s. Controllata immagine nuova delle armature testa/coda.
Cinque prove fluidi isolate passate; due nuove distinguono superficie,
immersione, volo e suolo solido. Dettagli e limiti in TEST-0.1.32.md.
Prossimo: camminata generale e dragonFlightSpeedMod con sincronizzazione
server-client; ricerca e prova client preparate fuori repo in phase33-preparation.
Tappa1 e port completo ancora aperti. Non sospendere o spegnere il PC.


## Checkpoint 0.1.31 — acqua e lava

Soglia originale di immersione per stadio, goal di galleggiamento coerente,
comandi verticali originali del pilota e movimento in lava con sprint/attrito.
Verificata autorita del vero client e posizione registrata sul server.
Suite finale:77JUnit,112GameTest mod (113 con baseline),8client,17immagini;
build completa riuscita in8m35s. Vedere TEST-0.1.31.md per precedenti fallimenti
e correzione della misura del wandering, senza cambiare produzione o scadenze.
Restano superficie lava, moltiplicatori di velocita, altre parita originali,
navigazione complessa e due veri client. Tappa1 e port completo ancora aperti.
Prossimo lavoro studiato nel file locale phase32-preparation.txt, fuori repo.
Non sospendere o spegnere il PC. Ripresa automatica stessa chat al reset.


## Checkpoint 0.1.30 — navigazione aerea del corpo completo

Controllo del volume fisico lungo ogni tratto, chunk già caricati, quote/bordo;
deviazioni sopra/laterali e frenata ai cambi di direzione. Percorsi validi
conservati invece di ricominciare la salita. Collisioni fisiche native mantenute,
compresa la discesa libera quando un altro asse è bloccato. Comandi/bersagli
invalidano la cache. Percorsi lontani per tratti locali32 quando oltre64, senza
rallentamento artificiale della crociera; ricontrolli limitati.

Sette prove nuove native isolate e cinque regressioni aeree passate senza allentare
le asserzioni sul danno. Lo stadio5 supera realmente una parete e atterra vicino
al proprietario, con corpo vivo/dimensioni/collisioni controllati a ogni tick attivo.
Suite finale75JUnit/109GameTest mod (110 con baseline)/7client e17immagini,
tutta passata. Vedere TEST-0.1.30.md. Prossimi acqua/lava e altre parità.
Tappa1 e port completi ancora aperti. Nessuna sospensione/spegnimento.


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

Verificati 75 JUnit, 109 GameTest della mod (110 includendo la baseline vanilla) e 7 prove client con diciassette screenshot. Nessuna prova con due veri client.

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

Aggiornato per 0.1.35-stage1-dev, 25 ottobre 2026. La tappa 1 è ancora incompleta.
Questo riepilogo distingue ciò che è nel codice da ciò che manca: build e test
non sostituiscono il riscontro in gioco, soprattutto per grafica e multiplayer.
L'utente ha confermato la 0.1.13 funzionante il 24 ottobre e chiesto di continuare.
La prova0.1.14 ha rilevato alberi e dismount: correzioni0.1.15 da collaudare. Nessun passaggio alla tappa 2.

## Novità 0.1.23

Configurazioni originali salute/danno fisico/intervallo fame/tempo schiusa,
con default e curve conservati. Vecchi JSON/NBT compatibili, contatore uova
a64bit per soglia massima senza overflow. Ultimo checkpoint della sessione,
poi push/consegna; nessuna sospensione. Goal completo e tappa1 incompleti.

## Novità 0.1.22

Presa/scuotimento di prede mob piccole, native e sincronizzate, pose originali,
un solo colpo dopo55tick, cura/fame e rilascio. Stop per comandi/NoAI/alleati,
nessuna mutazione NoAI della preda; un solo occupante e nessuna cattura giocatori.
Caricatore ricorsivo nativo riprende UUID/tick residuo; primo tick aggiorna scala
fisica prima di validare la preda. Prove server e client dedicate. Aggancio visivo alla testa animata e rotazione
originali, scala indipendente; visibilità al bordo vista corretta e provata.

## Novità 0.1.21

Caccia tramite tag originali, fame/taglia e protezione giocatori/pet per draghi
addomesticati; aggressività selvatici e ricerca3D configurabile. Difesa e target
attivi prioritari; carne raggiungibile prima di una nuova caccia. Prove server
e client combattimento ora senza acquisizione manuale del bersaglio.

## Novità 0.1.20

Stendardo sul corpo animato con colori e motivi vanilla, dimensione indipendente
dalla crescita, sincronizzazione fuori dalla GUI, salvataggio/rimozione senza
duplicazione. Prove server, matematica e quarto test client con screenshot.

## Novità 0.1.19

Riproduzione con stufato di fuoco fra adulti stage4+ di sesso opposto, ricerca,
corteggiamento, uovo casuale e nido. Cooldown6000 salvato, terreno protetto e
spawn fallito atomico. Il nuovo uovo resta senza proprietario finché raccolto e
ripiazzato; test completo fino alla schiusa. Uova non spingibili come originale.
Autonomia continua dopo sospensione S3/risveglio verificati e quota rinnovata.

## Novità 0.1.18

Sonno notturno sincronizzato/salvato, nido prima del riposo, risveglio per difesa
propria/del proprietario e comandi, alba/supporto/acqua. Posa/texture originali,
occhi emissivi HeadFront prima delle armature, asset invariati. Uova recuperate
al colpo con variante/drop singolo, fuoco ignorato e bypass senza drop.
60 JUnit,60 GameTest mod più baseline escluso e3 prove client (sonno/risveglio).

Sessione prossima al limite di quota: salvare e sospendere dopo consegna. Port
completo ancora aperto; riprendere quando PC/quota disponibili. Nessuna fase
successiva dichiarata completa.

## Novità 0.1.17

Tre modalità aeree: tackle con tuffo/danno/atterraggio, hover blast con quota e
proiettili, scorch stream con passaggi/inversione/soffio. Decollo per bersaglio
in quota, stage2+. Posa tackle originale e reset fra attacchi aerei e terrestri.
Percorsi bloccati: atterraggio, senza sparare durante la discesa. Restano i limiti
di navigazione fra edifici/grotte e alcune temporizzazioni/configurazioni originali.
Due prove client separate, incluse le pose di coda e ali in combattimento reale
con screenshot e leggibilità della GUI; non multiplayer con due veri client.

## Novità 0.1.16

Cibo a terra: ricerca, percorso, consumo singolo, fame/cura originali, animazione
EAT sincronizzata. Priorità a comandi/combattimento, pickup delay/dieta/portata.
Attacchi coda e ali con pose originali, tempi e contraccolpo; prima azione contro
un nuovo bersaglio resta il morso. Palla di fuoco autonoma con carica stabile ed
emissione al tick20, scelta1/5 fra cicli di soffio. TEST-0.1.16.md esplicita limiti
e differenze temporali; shake prey ancora da migrare; modalità aeree aggiunte nella0.1.17.

L'utente ora autorizza a proseguire il più possibile nelle fasi; restano ordine
e verifiche di completamento, senza equiparare una build al port completo.

## Novità 0.1.15

- Adulti stage3+: apertura del passaggio locale fra tronchi e foglie durante
  difesa/inseguimento, secondo durezza e griefing originali; percorso ricalcolato.
  Nessuna riduzione della dimensione stadio5 e nessun attraversamento dei blocchi.
- Dismount laterale supportato solo a terra. Se non trova uscita, protezione
  della caduta corrente dal corpo; in aria resta la caduta vanilla senza immunità
  aggiunta. Nessun dorso solido o nuovo posto.
- Doppio salto entro7tick per decollare da montato; restano V e spazio tenuto.
- Soffio autonomo terrestre stage2+ a distanza e con linea di vista, warmup e
  danni server originali; vicino torna al morso. Palla di fuoco autonoma e attacchi
  aerei restano da migrare. Configurati cooldown e drop della vegetazione.

Vedere TEST-0.1.15.md. Navigazione aerea fra ostacoli complessi ancora incompleta.

Build verificata: 56 JUnit, 41 GameTest della mod (42 includendo always_pass
vanilla escluso dal conteggio) e un test client grafico con mondo/screenshot
superati. Nessuna prova multiplayer con due veri client o collaudo visivo delle
novità di combattimento. Revisione completa review-agent read-only: No findings.

## Novità 0.1.14

Prima difesa autonoma: priorità originali per bersaglio attaccato dal proprietario,
aggressore del proprietario e aggressore del drago. Percorso terrestre 1.5,
aggiornamento 4..10 tick, sguardo, morso di 35 tick con impatto 16..24 e portata
originale dipendente dalla dimensione visiva. Un colpo riuscito per morso,
verifiche server di distanza, linea di vista, alleati e stati di controllo.
Seduto/NoAI/pilota/spalle interrompono; cambio bersaglio riavvia l'animazione.
Se già in volo, atterra prima di combattere: soluzione transitoria fino alle
modalità aeree originali. Caccia e altri attacchi restavano da migrare; il soffio terrestre è aggiunto nella0.1.15.
Vedere TEST-0.1.14.md per prove automatiche e collaudo in gioco.

Build riuscita: 53 JUnit, 34 GameTest della mod (più always_pass vanilla escluso)
e una prova client con mondo e screenshot superati. Revisione completa secondo
review-agent in sola lettura senza delega: No findings. Nessun test con due client.

## Modifiche richieste per la 0.1.13

- Armatura indossata: passaggi di disegno esplicitamente successivi alla pelle,
  in ordine testa/collo/corpo/coda. La 26.3 raggruppa le chiamate per texture:
  inviare pelle e armature nello stesso gruppo poteva coprire di nuovo gli overlay.
  Asset e UV originali conservati. Prova grafica delle icone e della GUI nel client.
- Seguimi a terra più rapido: moltiplicatore 1.5, 2 oltre 16 blocchi, ricalcolo
  del percorso ogni 5 tick invece di 10.
- Seguimi in volo: rotta stabile aggiornata ogni 10 tick verso la posizione
  prevista del proprietario, anziché una destinazione casuale diversa quasi a
  ogni tick. Accelerazione di recupero della distanza; atterraggio vicino a un
  proprietario a terra, poi ripresa del seguito terrestre.
- Recupero del proprietario a distanza di almeno 150 blocchi, controllato circa
  ogni secondo: teletrasporto verso una posizione libera e sostenuta vicino a lui.
  Se è in aria, solo dallo stadio 2 è ammessa una destinazione aerea libera.
  Controllati corpo intero, supporto, fluidi, limiti verticali, bordo e chunk già
  caricati. Nessun teletrasporto per seduto/libero, NoAI, passeggeri o pilota;
  niente cambio dimensione, niente destinazioni forzate dentro blocchi o lava.
- Prove di movimento effettivo per decollo, inseguimento con recupero della
  distanza, atterraggio e volo durante vagabondaggio senza proprietario.

Il teletrasporto è una modifica al gameplay richiesta dall'utente e sostituisce
il precedente vincolo di seguito senza teletrasporto. Il drago deve essere
caricato e simulato dal server perché la sua IA possa eseguire questi controlli.
Se non trova una destinazione sicura, continua il comportamento disponibile e
riprova: non viene piazzato alla cieca sulle coordinate del giocatore.

## Implementato finora

1. Progetto Fabric indipendente in fabric/, codice client/comune separato,
   registrazioni e rete, con il sorgente Forge originale conservato in src/.
   Minecraft26.3, Java25, Loader0.19.5 e Fabric API0.163.0+26.3 fissati.
2. Quattro varianti di uova di fuoco: rosso, verde, bronzo e grigio. Piazzamento,
   dimensioni originali, incubazione nel fuoco oltre 7200 tick, schiusa,
   trasferimento di nome e proprietario. Persistenza dell'incubazione.
3. Drago di fuoco con crescita, cinque stadi, sesso/variante, fame, statistiche
   derivate dall'età, dimensione e scala, proprietà e stati salvati nel mondo.
4. Geometria Tabula e texture originali, lettore dedicato, interpolazione delle
   pose di camminata/volo/seduta/hovering/spalle e prime animazioni di attacco.
   Movimenti secondari di corpo/coda, transizioni e aggancio animato del pilota.
5. Un solo pilota proprietario dallo stadio3 (50 giorni). Movimento a terra,
   decollo V o spazio tenuto, salita spazio, discesa X, sprint/planata, tilt,
   atterraggio, rimappatura comandi, camera adattata alla crescita e F5 vanilla.
   Nessun dorso solido o posto aggiuntivo del ghast.
6. Piccoli di stadio1 sulle spalle/testa: massimo tre, mano vuota del proprietario;
   rilascio con Maiusc e negli altri casi previsti. Restano entità del mondo
   salvate indipendentemente dal giocatore, senza usare PLAYER come veicolo.
7. Combattimento controllato dal pilota: G morso; R soffio progressivo e ramo
   palla di fuoco, animazioni e temporizzazioni. Danno server, ostacoli e protezione
   di proprietario, alleati e passeggeri; fiamma originale animata e scalata.
8. Conversione del terreno e distruzione/conversione dell'area da proiettile;
   sette blocchi carbonizzati/cenere, ritorno al terreno originale dove previsto,
   rispetto di griefing, mob_griefing, resistenza e blocchi protetti.
9. Inventario del drago: cinque slot (stendardo, testa, collo, corpo, coda),
   apertura Maiusc+mano vuota, GUI originale, anteprima e statistiche;
   controlli di accesso/distanza, shift-click, salvataggio e rilascio alla morte.
   32 armature, otto materiali per quattro parti, bonus e overlay sincronizzati.
   Stendardo sul corpo animato, componenti sincronizzati e renderer vanilla completo.
10. Alimentazione manuale con carne: fame e cura, consumo corretto anche in
    creativo e rifiuto se sazio/sano. Pasto del drago per crescere; pasto malaticcio
    per bloccare la crescita e curare; pasto creativo per assegnare proprietà.
11. Bastone dragon_stick: libero/seduto/seguimi, stati sincronizzati e salvati;
    casa impostabile/rimuovibile con Maiusc, dimensione e coordinate zero gestite,
    raggio predefinito40. Seguito terrestre e aereo, ora con recupero a150 blocchi.
12. Volo autonomo dallo stadio2: spazio libero per il decollo, hovering40 tick,
    vagabondaggio diurno con probabilità originale1/1500, seguito in aria,
    bersagli nei chunk caricati e discesa su comando. Ritorno notturno al nido
    a piedi o in volo, con atterraggio e precedenza di seduto/seguimi.
13. Prima configurazione del combattimento, risorse originali selezionate,
    licenza/attribuzioni, test numerici e GameTest nel runtime Fabric. Prove client
    separate; mondi e mod di test esclusi dal JAR distribuito.
14. Difesa autonoma del drago e del proprietario, assistenza al suo attacco,
    inseguimento a terra e morso temporizzato; interruzione per comandi e controllo,
    protezione alleati e atterraggio prima dello scontro terrestre.

15. Attacchi terrestri temporizzati coda/ali e pasto da terra con percorso e
    consumo/cura; soffio e carica autonomi, più tre modalità aeree (tuffo,
    hover blast e scorch stream). Difesa e bersagli attivi restano prioritari.
16. Sonno notturno e ritorno alla casa, risvegli difensivi/da comandi, posa e
    texture sleeping; layer occhi originale disabilitato durante sonno/morte.
17. Riproduzione con stufato di fuoco: genitori stadio4+, sesso opposto,
    corteggiamento e cooldown salvato; uovo/nido con protezioni del terreno,
    poi raccolta/piazzamento e schiusa verificati nel runtime.
18. Caccia autonoma originale tramite tag/fame/taglia e raggio configurabile;
    protezione di proprietari/pet e precedenza del cibo raggiungibile prima
    di una nuova caccia. Non sostituisce un bersaglio difensivo già attivo.
19. Presa/scuotimento di piccole prede mob: occupante nativo, danno/cura e
    rilascio temporizzati, persistenza UUID/tick e aggancio visivo alla testa.
    Niente cattura giocatori o secondo posto; visibilità ai bordi vista corretta.
20. Configurazioni originali già esposte per combattimento, ciclo vitale,
    salute/danno/fame/schiusa, quota autonoma e distanza dalla casa. Validazione,
    compatibilità vecchi JSON e incubazione64bit; altre impostazioni restano.
21. Voci originali ambientali/ferita/morte per fascia d'età e SPEAK20tick sulla
    mandibola. File audio originali invariati; NoAI e animazioni prioritarie
    interrompono la posa cosmetica. Ruggiti/effetti e battito d'ali aggiunti nella0.1.29; altri dettagli restano.
22. Corno riutilizzabile del drago: salvataggio nativo nel componente oggetto,
    raccolta/rilascio e UUID/inventario persistenti, tooltip e icone originali.
    Proprietà, permessi, ostacoli, corpo completo e UUID caricati controllati;
    cattura di veicoli occupati/spalle vietata. Disponibile creativo/comando.

23. Flauto originale: due mani, cooldown60, zona/configurazione originale,
    interruzione volo dei draghi propri e suono originale; nessun cambio di pilota
    o immunità in aria. Palpebre6tick su50 con skin originale chiusa e stop del
    glow, conservando posa del corpo e IA sveglia.

24. Cristallo del drago di fuoco: legame UUID/nome persistente, icone originali,
    richiamo stessa entità in Overworld con controlli di proprietà e destinazione.
    Registro posizioni SavedData, caricamento temporaneo del chunk sorgente se
    abilitato, richieste indipendenti e cleanup/ticket condivisi. Prova reale di
    scaricamento/ricaricamento e sincronizzazione client completa passate.

25. Ruggiti normale/epico con pose/voci originali e aura degli adulti, con
    precedenza dei comportamenti attivi. Battito d'ali, campioni originali e
    distanza audio configurabile; categoria HOSTILE. Vedere TEST-0.1.29.md.

26. Navigazione aerea con controllo dell'intero volume fisico e deviazioni
    locali sopra/ai lati, cache e frenata; tratti limitati per mete lontane.
    Lo stadio5 supera una parete e atterra vivo senza attraversare blocchi.
    Collisioni native e regole separate per caduta del giocatore conservate.

27. Acqua/lava, soglie per stadio, superficie lava e comandi verticali pilotati;
    camminata originale e moltiplicatore server sincronizzato. TEST-0.1.31/32/33.

28. Overlay maschile originale semitrasparente, cache limitata e ricaricamento
    nativo delle risorse, con prova di tutte40combinazioni. TEST-0.1.34.

29. Due client separati su GameTestServer locale: proprieta, comandi, volo,
    sesso, armature e velocita server osservati da entrambi. TEST-0.1.35.

30. Feeding manuale nei cinque stadi, easing e recupero20tick; verifica completa
    delle UV/geometrie originali e dieci tavole corpo/ali/feeding. TEST-0.1.36.

31. Movimenti secondari originali di corpo/collo/coda/arti, sonno lento e
    protezione delle ali durante hover/tackle e WING/EPIC_ROAR. TEST-0.1.37.

32. XP originali per i cinque stadi, hook nativo e verifiche contro duplicazioni.
    Morte ambientale/mobDrops verificati. TEST-0.1.38.

33. Oggetti di raccolta, carne originale e teschio piazzabile/salvabile nei cinque
    stadi, con C2S e tavola GPU. TEST-0.1.39. Carcassa ancora da collegare.

34. Carcassa persistente e raccolta completa, posa/skin DEAD originali,9prove
    native e raccolta C2S completa. TEST-0.1.40. XP/inventario senza duplicazioni.

## Da completare nella tappa 1

Multipart completato nella0.1.41 e esplosioni opzionali native nella0.1.42; caso DedicatedServer ordinario con due client e mondo riaperto verificato nella0.1.43.

- Combattimento autonomo: ulteriori dettagli di presa e
  parità delle configurazioni/temporizzazioni originali, reazioni e
  coordinamento con movimento e volo.
- Altre interazioni originali e dettagli del ciclo delle uova.
- Ulteriori dettagli/parità di riproduzione e sonno originali.
- Comportamento completo in acqua/lava e ulteriori casi complessi di navigazione.
  La0.1.30 include corpo completo e deviazioni locali; labirinti/grotte e
  assetti stretti restano da collaudare, senza dichiarare un pathfinder universale.
- Passeggeri nell'anteprima GUI; ulteriori dettagli visivi maschio/femmina,
  animazioni secondarie/attacchi, ulteriori suoni e particelle ancora mancanti.
- Configurazioni originali non ancora esposte; alimentazione autonoma, comparatore e ricette di costruzione della DragonForge (prima parte manuale nella0.1.47); ulteriori dettagli del combattimento.
- Difetto delle punte grigie ancora aperto: non è stato risolto né modificato
  alla cieca. Continuare con confronto equivalente all'originale.
- Collaudo in gioco di camera, seduta, attacchi, armature e volo ai vari stadi;
  ulteriori scenari MP con account autenticati e interruzioni improvvise. Logout/relogin montato dopo riavvio verificato nella0.1.44. Il caso locale con due client e riapertura ordinata e verificato nella0.1.43. Il riscontro dell'utente resta necessario.

Restano fuori da questa consegna draghi di ghiaccio/fulmine, altre creature,
progressione completa, ricette, strutture e generazione naturale: appartengono
alle tappe successive. Non iniziarle prima del completamento e del riscontro
sulla tappa 1. La 0.1.13 è confermata; collaudare le novità successive, ora fino alla0.1.44. L'utente ha autorizzato il proseguimento autonomo; il riscontro non impone una nuova pausa.

Nota operativa25ottobre: ultima richiesta continua fino a esaurimento quota, poi spegni il PC dopo checkpoint sicuro. Controllo automatico50min, non5. Superati i vecchi divieti storici; proseguimento del port autorizzato.
