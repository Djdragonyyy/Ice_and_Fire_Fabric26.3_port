## Checkpoint 0.1.56 - 28 ottobre 2026 - movimento durante il risveglio

Ripristinata la condizione originale EntityDragonBase.canMove: il drago
attende sleepProgress == 0 prima di camminare o decollare autonomamente.
La minaccia continua a svegliarlo e a essere acquisita immediatamente;
il recupero della posa impedisce lo scivolamento del corpo ancora ripiegato.
Nessun cambiamento a geometria, texture, controllo del pilota o rete.

Prova nativa notturna con AI attiva: a tick45 sonno reale e posa20; bersaglio
Zombie reale acquisito subito; a tick65 posa parziale, nessun decollo e
spostamento orizzontale inferiore a0.05; a tick90 posa0 e movimento abilitato.
Un semplice flag impostato di giorno viene invece rimosso prima che la posa
avanzi: mantiene correttamente il movimento immediato. Il primo focus
falliva questa vecchia fixture modificata erroneamente; corretta solo
l'aspettativa del test. Focus2 verde28s,100JUnit e8GameTest del sonno.

Prima regressione completa fallita: ritorsione controllata a50tick dopo un
primo morso mancato per contraccolpo. La traccia mostra il secondo attacco
iniziato a41tick, non ancora al punto di danno. Solo il test attende ora120
tick entro il limite130, mantenendo danno reale e bersaglio; produzione invariata.
Regressione server finale phase56-focused-3.log PASS9m35s: il nome focused
e storico, testClass non inoltrato, eseguiti tutti178server(177mod+baseline).
Build/client phase56-client-final.log PASS3m 16s,19client e56PNG.
100JUnit verdi nel clean iniziale, sorgenti JUnit invariati; log combinato
phase56-verified.log distingue le prove, senza dichiarare un solo full verde.
Confronto JAR55->56:
124classi identiche, solo FireDragon modificata, nessuna aggiunta/rimozione
o classe di test. Revisione senza difetti residui nella modifica.

Tappa1 e port ancora incompleti. Navigazione complessa/rottura del corpo,
audit delle configurazioni e del ciclo vitale, GUI passeggeri e punte grigie
restano aperti. Prova dedicata Forge52 distinta, non rieseguita in56.

## Checkpoint 0.1.55 - 27 ottobre 2026 - pile di tesori originali

Tre MetalPileBlock oro/argento/rame,8livelli con altezza2L/16 e collisione
2(L-1)/16. Supporti/sogliepath/regoleSourceBlockGoldPile, nonSnowmelt. Uso
NativeItemStackconsume mano effettiva, survival1perlayer/creativefree,
clamp8 e piazzamentoVanilla nuova pila sopra. Tre ricette2nuggets->1pile,
loot2nuggets/layer, CopperNative26Nugget, SilverNuggetSimpleItem solo dipendenza
materiale. Miningpickaxe3/guardedByPiglinsGold originali; creative4icone,
2OriginalOGG/SoundType. Ripristinato ramo allarme break per tutte3pile;
aggiungere pile non simula chestuse. Generazione minerali/strutture ancora
nelle tappe successive, non nuova worldgen o catena argento completa.

Full phase55-final.log PASS12m37s100JUnit/177server(176mod+baseline)/19client/
56PNG. Focus4PASS47s100JUnit5GT+clientcompile. All24shape/collision/loots2N,
actualsurvival8break16nuggets, support/neighbor/path/sounds, native1..8stack/
upper1/consumoexact/creativefree, actual3CraftingMenuResultSlots/mixednegative,
all3breakalarms+nostackalarm. NativeRealServerPlayer+embedded/AItrue fixture.
ClientprobesPASS2m51s19client56PNG;2newimagesopenedreviewed24worldstates+
4icons noMissingTexture/loading, C2SSurvivallayer1->2/items8->7 authoritative
10ticklater. CameraFIRSTPERSONtemporanea/restore;full ripete tutto.

Prima catturalambda test corretta; focus2/3failedlootactual1nuggetperlayer:
Native26ignora vecchi conditions/functions. Verificato JARprimarioSnow/Melon,
convertite3tabelle a condition.type=match_block/blocks/state e modifier.type=
set_count,8alternative. Nessun codicefallback permascherare.37originalasset
byteidentical(modelli/states/PNG/audio/item), vecchivalorilang/sounds invariati.
26.3SpriteContents/ChunkSectionLayer.byTransparency automatico, PNGalpha255;
vecchioCUTOUTbindingGold/Silver nonrichiede nuovaMixin.NativeGoldadvancement
IDrecipes/food preservato, Copperdecorations, Silverhelperunlockaggiunto.

JAR54->55:120identiche,3oldchanged(content/init/chestguard),2newPile/Treasure,
noharness/noDragon/renderchanges. TEST-0.1.55.md documenta materialehelper,
formati e limiti. Tappa1/port ancora aperti; prossimi audit navigazione/rottura
corpo e configurazioni/ciclo vitale, GUIpasseggeri/puntegrigie ancora aperti.
Prova riavvio Forge52 distinta, non nuova prova55/autenticazione pubblica.

## Checkpoint 0.1.54 - 27 ottobre 2026 - reazione ai forzieri

Portato il ramo forzieri originale: interazione e rottura tramite callback
Fabric server, PASS/true senza cancellare azioni. Sveglia selvatici vivi
non posseduti e seleziona il giocatore non creativo; clearsleep/orderedSit
senza cambiare comando. dragonGoldSearchLength30valid0..10000, AABBgiocatore
sui3assi, non sfera/centroforziere e nessun LOSaggiuntivo. Pile metalli non
ancora registrate: non dichiarare completa tutta la reazione ai tesori.

Full phase54-final.log PASS11m52s100JUnit/172server(171mod+baseline)/18client/
54PNG. Quattro GT actualServerPlayer.gameMode.useItemOn menuaperto e
DestroyBlock chestvsstone, creative/tame/owned/deadexcluded, raggio2centro
player. Osservatori testonly provano realicallback, non chiamano alarm.
Ownembeddedplayers disconnessi a fine prova, observerUUIDrimossi.

Primi4focus fallivano fixture: MockServerPlayerInLevel26inner3.gameMode sempre
CREATIVE; poi NoAI drago fa rifiutare bersaglio a Mob.setTarget/asValidTarget.
Bytecodeverificato; fixturefinale RealServerPlayer+embedded+modeassert e AI
normale, asserzioni tick0 prima caccia. Nativequery includeva d e callback
svegliava maNoAIrifiutavatarget; nessuna produzione aggirata pertestverde.
Focus5 PASS38s100JUnit4GT, cleanupaggiunto prima full e incluso nel full.

JAR53->54:120classi identiche,2oldchanged(config/init),1nuovaChestGuard,
no harness, nessuna modifica a classi drago/render. TEST-0.1.54.md descrive
NoAI nativo, raggio0nonoff e callback prima risultatoazione. Prova restart
Forge52 e precedente, non nuova certificazione54. Tappa1 e port aperti.
Prossimo: ricerca rottura blocchi corpo/navigazione e digWhenStuck originali;
navcomplessa, GUIpasseggeri, puntegrigie e altre parita restano aperti.

## Checkpoint 0.1.53 - 27 ottobre 2026 - reazioni di animali e villager

Portati i goal di fuga originali per il drago di fuoco: villagersFearDragons
ed animalsFearDragons true, tag originali18animali/2villager byteidentici.
AvoidEntityGoal nativo priority1, raggio8 velocita0.8/0.8 villager e raggio30
velocita1/0.5 bestiame. Predicateoriginale esclude tamepet/villager/golem
nel ramo animali, non dipende da owner/tame del drago. ENTITY_LOAD ripristina
goal transitori anche per mob salvati, controllo duplicati, APIpubblica
getGoalSelector senza nuoveMixin. Opzioni al caricamento, non reload dinamico.

Verifica finale phase53-final-2.log PASS 11m 36s,99JUnit/168server(167mod+
baseline)/18client/54PNG. Focus finale5PASS43s: actualcow/villagerBrain/trader
fuga durante goal attivo entro300tick e distanzaSquared>iniziale+16, salute
piena; configfalse, savedcowreload1goal, dedup, zombieexcluded, tamecat,
range8/30 e dragondead. Primo full fallito per vecchia distanza finale100tick:
non verde, client non eseguiti. Diagnostica focus4 identificava cow goalattivo
ma distanza insufficiente a100. Corretto SOLO test: campiona goal realmente
attivo e movimento durante fuga, stessa soglia, pianificazione naturale300tick;
niente destinazioni forzate/RNGseed/productionfix. Nativeassert evita crash.

JAR52->53:118classi identiche,2precedenti cambiate(config/init),2nuoveFear;
nessun harness distribuito, classi del drago e renderer invariate. Riavvio
Forge52 resta prova precedente, non nuova certificazione53. TEST-0.1.53.md
contiene condizioni e limiti. Tappa1/port aperti: config/ciclo vitale/AI e
navcomplessa, passeggeri GUI e puntegrigie richiedono altro lavoro.

## Checkpoint 0.1.52 - 27 ottobre 2026 - Forge durante riavvio reale

Nuova prova opt-in su DedicatedServer ordinario e due profili locali stabili.
Il drago fabbro produce autonomamente un lingotto e progresso parziale22;
il server chiude e riapre lo stesso mondo. Tre slot/progresso preservati,
20 tick senza nuovo calore non avanzano, AI riattivata riprende il soffio e
produce esattamente un nuovo lingotto consumando un ferro e un sangue.
Conservati rientro montato, volo, armatura, proprieta, inventario owner sangue2,
carcassa parziale e tre famiglie multipart con30parti senza duplicati.

phase52-dedicated-launch-2.log:6processi exit0, tre flagForge e riavvio/monta
veri.5PNG freschi aperti/rivisti, nessun LoadingTerrain. I suffissi35/44 sono
nomi storici del client condiviso, non report riciclati. Primo tentativo
respinto da whitelist prima gameplay; chiuso e conservato, secondo nuovo
mondo/UTF8/loopbackonly/white-listfalse. Nessun bug produttivo inventato.

Full pulito phase52-final.log PASS11m37s,99JUnit/163server/18client/54PNG;
120classi del JAR identiche51, nessun harness distribuito. Solo versione,
sourceSet di test e script export/launch modificati. Review ha corretto il
flag export ForgeRestart da solo: profili stabili verificati con export10s,
nessun nuovo riavvio attribuito a quella verifica. TEST-0.1.52.md distingue
condizioni controllate, limiti e prove precedenti. Nessuna certificazione
account autentici/server pubblici o chunk unload con server acceso.

Tappa1/port aperti. Prossimo: reazioni/fuga animali e villager, configoriginali,
navcomplessa/anteprima passeggeri/puntegrigie ancora da migrare o verificare.
Ricerca privata NEXT-PARITY.md, nessuna funzione53 implementata finora.

## Checkpoint 0.1.51 - 27 ottobre 2026 - distanze camera originali e F7

Portate quattro distanze originali0/1.2/3/5*(renderSize/3), con resetmedio2
alla monta del proprio drago addomesticato. F7rimappabile/nativeSDL, una
pressione per avanzamento; F5continua a scegliere la prospettiva. Distanza
extra dalla taglia visiva uncapped, prima persona e fuori cavalcata native.
Camera26.3 giausa CAMERA_DISTANCE e scalegiocatore/veicolo: valore mantenuto,
extraoriginale aggiunto prima del getMaxZoom tramite piccolaMixin client.
Una sola collisione nativa copre l'intera distanza, senza passare nei muri.
Nessuna modifica a geometria/texture/seduta/SCALE/attributi o rete/server.

Verifiche distinte: phase51-final.log fullverde 11m 38s con99JUnit/163server/
18client; successiva guardiaproprieta SOLOclient ricompilata e tutti18client
ripetuti in phase51-client-final-2.log verde 2m 48s.99JUnit restano verdi;
163server valgono sulle stesse classi comuni, non sono una nuova esecuzione
del secondo comando. Logcombinato phase51-verified.log conserva entrambe.
54PNG correnti,6nuovefinali aperte/riviste: media50/75/100/125giorni, frontale
125 e collisione muro. Prove F7/inputhold20tick, distanza effettiva camera,
quattro modi, crescita, clip/recupero, prima persona e discesa senza extra.

Primo probe falliva ultima verifica dopo discesa per scelta vista prima del
listenerdi ripristino; attesa10tick, asserzione uguale, probe2verde2m45s.
Fixture finale usa6chunk SOLOnel mondo di prova, comunica richiesta al server
con broadcastOptions, controlla clientInformation e chunkcamera caricato;
opzioni iniziali ripristinate. Non cambia la distanza grafica del gioco.
JAR50/51:116classi identiche,2precedenti modificate SOLOclient e2nuoveclient;
nessuna classe comune cambiata, nessun test incluso. Revisione senza rilievi
residui dopo resetmedio limitato all'owner come nell'originale.

Tappa1/port NON conclusi: auditconfig/ciclo vitale, navigazionecomplessa,
anteprima passeggeri e puntegrigie ancora aperti. Prossima52: ricerca test
DedicatedForgePersistence, nessuna implementazione o certificazione attuale.
Precedente50:aba673f5a, opzione prospettiva automatica. TEST-0.1.51.md.
Ultima istruzione: finoal5%residuo/95%usato poi salvare/testcleanup/checkpoint/
push e shutdownordinato senza forzareapp. Quotaogni50min, noreset/consumo
artificiale, dopo shutdown controllo inpausa fino nuovaautorizzazione.
Emailannullate, stessa chat/ramo port/fabric-26.3.

## Checkpoint 0.1.50 - 27 ottobre 2026 - visuale automatica del pilota

Portata opzione client originale dragonAuto3rdPerson, defaulttrue, nel nuovo
config/iceandfire-client.json. Solo montando come pilota il proprio drago:
cattura vista precedente, passa a THIRD_PERSON_BACK, ripristina alla discesa
o disconnessione. I cambi manuali durante la cavalcata restano disponibili;
false conserva le scelte del giocatore. Nessun nuovo pacchetto/serverconfig.
Vecchi cameraoffset, firstpersonheadhide, controlli e dismount invariati.

Full pulito phase50-final.log PASS in 11m 14s:99JUnit,162GameTest mod
+baseline163server,17client e48PNG correnti, XML senzaerrori/failure.
Probes99JUnit/17client verdi2m35s. Nuova prova nativeC2Smount, scelta manuale
tenuta20tick, dismount autorevole e uscita mondo ancora montato; true/false
caricati realmente da file poi bytes/config/opzioni ripristinati. Non e prova
fisica del tastoF5/Shift o nuovo DedicatedServer. PNG finale montato aperta e
rivista, hint nativo lasciato svanire80tick nel solo fixture prima dello scatto.

Confronto JAR49/50:115classi produttive byte-identiche, una sola precedente
modificata(IceAndFireFabricClient) e due nuove classi client; nessuna rimossa.
Logica server/entity/modello/rete non cambiata. Test/helper esclusi dal JAR.
Revisione diff e nuovi file completata senza rilievi residui.

Tappa1/port NON conclusi: restano audit parita/config/ciclo vitale, navigazione
complessa, anteprima passeggeri e diagnosi punte grigie. Prossimo51 candidato
ricerca di persistenza Forge in ordinaryDedicatedServer, non implementazione.
Precedente49:1dfb5eb80, Forge costruibile e blocchi scaglie. TEST-0.1.50.md.

Ultima istruzione27ottobre: continuare fino al5%residuo/95%usato, poi finire
test/salvareancheWIP/checkpoint/push e spegnere ordinatamente senza forzareapp.
Quota ogni50minuti, nessun reset/consumoartificiale; dopo shutdown automazione
in pausa fino nuova autorizzazione. Emailannullate, stessa chat/ramo.

## Checkpoint 0.1.49 - 27 ottobre 2026 - costruzione della DragonForge

Portati quattro blocchi di scaglie di fuoco, compressione/decompressione,
ricette originali di ossa/mattoni/nucleo/apertura e dodici advancement per
ricettario. La forgia ora si costruisce con i materiali originali, senza
necessita di creativo/comandi per ottenere i suoi blocchi. Vecchio risultato
core_disabled adattato all'ID unico CORE con ACTIVE; tag comuni c: nativi.
Proprieta30/500, strumenti, protezione drago, loot e tooltip colore originali;
stati/modelli/texture byte-identici. Nessuna modifica al motore del drago.

Full pulito phase49-final-2.log PASS in 11m 29s:99JUnit,162GameTest mod
+baseline163server,16client e47PNG correnti; XML senza errori/failure.
Quattro nuove prove native: tutti4colori tramite CraftingMenu/ResultSlot e
consumo/trasferimento, costo e output delle ricette forgia, proprieta/tag/loot,
sblocco di tutte12ricette dai cambi inventario del giocatore. Decompressione
usa il blocco effettivamente prodotto. JUnit confronta16risorse originali.
Probes99JUnit/3GT/16client verdi2m47s, quartoGT ricettario/loot verde24s poi
incluso nel full. PNG finali icone e quattro blocchi piazzati aperte e riviste.

Revisione ha rilevato/corretto lettura Python cp1252 delle vecchie traduzioni:
104valori per lingua ripristinati da HEAD, aggiunte sole nuove chiavi e UTF8
esplicito. Aggiunto anche il parent tag c:storage_blocks per scaglie e ossa
come nel datagen originale; nuove asserzioni native. Full2 ripete il collaudo
dopo tale aggiunta, senza riciclare il primo full pre-tag. Prima tavola9icone aveva etichette sovrapposte: ora2righe5+4,
verificata visivamente dopo il full. Nessun rilievo residuo. Test esclusi JAR.

Tappa1/port NON conclusi: restano audit configurazioni/interazioni/temporizzazioni,
casi complessi di navigazione, passeggeri anteprima GUI e diagnosi punte grigie.
Nessuna nuova certificazione dedicata di restartForge o account autenticati.
Ricerca prossima50 SOLO in appunti privati; nessuna implementazione50.
Precedente48:a9eacd2c3. TEST-0.1.49.md descrive costi e limiti.

Ultima istruzione utente27ottobre: continuare fino al5%residuo, poi salvare
ancheWIP/finire test/checkpoint/push e spegnere ordinatamente senza forzare app.
Soglia95%USATO sostituisce esaurimento100%. Quota ogni50minuti, niente reset o
consumo artificiale. Dopo shutdown automazione in pausa fino nuova autorizzazione.
Email annullate. Stessa chat e solo ramo port/fabric-26.3.

## Checkpoint 0.1.48 - 27 ottobre 2026 - DragonForge autonoma e comparatore

Il nucleo richiama un drago di fuoco addomesticato, sveglio, a terra, stage2+,
con visuale libera e distanza originale al quadrato <115*stage. Prenotazione
transiente di un input, rinnovo/timeout e rilascio alla rimozione del nucleo,
ingredienti esauriti o produzione bloccata. Getto reale continuo con warmup,
nessun proiettile o teletrasporto. Difesa, scorta, pilota, NoAI, sonno e volo
hanno precedenza; durante il lavoro fermo non vaga o decolla casualmente.
Comparatore nativo proporzionale ai tre slot, valori0/1/15 verificati.

Full pulito phase48-final.log PASS in 11m 11s:98JUnit,158GameTest mod
+baseline159server,16client e46PNG correnti. XML senza errori/failure.
Cinque nuove prove native: produzione con IA e consumo esatto, priorita/range/
vista/prenotazione, interruzione scorta/NoAI/difesa, comparatore e rilascio
immediato alla distruzione del nucleo. Due nuove JUnit per warmup/continuita
e transizione al melee. Probes precedenti verdi2m35s includevano solo4nuove
prove native; la quinta e inclusa nel full. Nuova PNG client del getto reale
aperta e rivista dopo il full; fixtureGUI70000 resta test separato dichiarato.

Correzioni dei test: quattro accessi package-private risolti con bridge SOLO
nel gametest; target di combattimento impostato in comando libero, perche il
comando fermo impedisce attacchi. API produttiva non allargata per la fixture.
Revisione diff completata; nessun rilievo residuo. Nessuna nuova certificazione
di riavvio dedicato/chunk unload della forge: prove44 restano precedenti.

Forge ancora incompleta: ricette costruzione e catena blocchi di scaglie da
portare. Ricognizione originale completata in appunti privati. Restano altre
parita/config/ciclo vitale, navigazione complessa, anteprima passeggeri e punte
grigie. Tappa1 e port NON conclusi. Precedente47:52bd0afbd.
Stessa chat/ramo; email annullate. Quota ogni50minuti, niente reset/consumo
artificiale. All'esaurimento salvare/testcleanup/checkpoint/push e spegnere
ordinatamente senza forzare app; poi automazione in pausa fino ad autorizzazione.
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

# Prompt temporaneo per la prossima ripresa

## Checkpoint 0.1.40 - carcassa e raccolta originali

Ciclo completo collegato: carcassa persistente, posa DEAD e texture chiuse/scheletro,
NoAI e crescita/fame ferme, sangue con bottiglia, raccolta a mano vuota, cuore,
uovo di femmina adulta, ossa e teschio finale con eta/stadio. Loot table native26
con pesi originali e customize_to_dragon per variante/quantita. XP/inventario alla
morte restano nativi e una sola volta. Carcasse escluse da monta, corno, cristallo,
inventario e targeting; rilasciati pilota/spalla/preda senza immunita in volo.
NBT ModelDead/DeathStage; persistenza con proprietario realmente risolto nel mondo.
Tre config originali dragonDropBlood/Heart/Skull; preservata anche la priorita del
sangue sul cuore e il gate originale del teschio su tutta la raccolta a mano.
Sangue in eccesso cade se inventario pieno; /kill rimuove i resti senza nuovo loot.

Suite definitiva build/server/client verde in17m14s:94JUnit,133GameTest mod
(134 con baseline),13prove client,39PNG tutte correnti. Nove nuove prove native;
C2S reale22passaggi di raccolta,3sangue e un solo teschio stadio5/112giorni.
Cinque tavole GPU originali lette, includono maschi/femmine, occhi chiusi e ossa.
Revisione ha trovato aggiornamento post-morte nello stesso tick: corretto e
nuova regressione completa superata. TEST-0.1.40.md contiene regole e limiti.
Non confondere salvataggio NBT con riapertura di un mondo su DedicatedServer.

PROSSIMO: multipart originale (10hitbox) con interazione/danno nativi e corretta
vita delle parti, senza modificare collider fisico, posto pilota o danno da caduta.
Ricerca e bozza coefficienti work/phase41-preparation/, fuori repo; NON ancora
implementati nella0.1.40. Restano altre parita/config/animazioni, navigazione
complessa, DedicatedServer/persistenza MP e punte grigie. Tappa1/port aperti.

ULTIMA ISTRUZIONE UTENTE25ottobre2026: continuare fino a esaurimento quota residua,
POI SPEGNERE il PC dopo salvataggi/test/checkpoint e push verificati. Questo
sostituisce tutti i vecchi divieti di spegnimento/sospensione nei checkpoint storici.
Controllo automatico OGNI50MINUTI, NON5; stessa automazione, niente duplicati.
Non consumare crediti reset o quota artificialmente. Email della sessione24ottobre
entrambe gia registrate nello stato privato: non ripetere invii.


## Checkpoint 0.1.39 - oggetti di raccolta e teschio piazzabile

Portati osso, quattro scaglie del fuoco, cuore, sangue, carne e teschio con asset
originali. Carne:8nutrimento/.8saturazione, cibo per lupi/carne,5secondi di fuoco.
Teschio: entita decorativa originale, testa Tabula/texture scheletriche dei cinque
stadi, posa terra/parete, rotazione con shift, metadati Stage/DragonAge in componenti
nativi. Salvataggio/recupero conserva anche nome; un solo drop per rottura.
Piazzamento protetto contro ostacoli/confine mondo e nessuna caccia del drago al
teschio. Scaglie: colore nel nome localizzato anziche tooltip separato originale.

88JUnit,124GameTest mod (125 con baseline),12prove client,34PNG. Suite server
completa superata; il primo client ha rilevato l'asset carne chiamato meat e non
flesh. Corretto il solo riferimento e ripetuti build e tutti i client (2m01s).
Tavola nuova controllata;9PNG item confrontate byte per byte con originali.
Vedere TEST-0.1.39.md e log combinato con intestazioni esplicite. Nessuna prova
rete a due client nuova: quelle37 non sono riusate nella consegna39.

PROSSIMO: collegare questi oggetti alla carcassa persistente e alla raccolta.
Carcassa NON ancora attivata: morte vanilla con drop inventario e XP originali.
Ricerca dettagliata/API26 in work/phase40-preparation/RESEARCH.txt, fuori repo.
Preservare morte/drop/XP una sola volta, proprieta e dismount senza immunita in
volo. NBT originali ModelDead/DeathStage, pose/texture originali e loot table con
codec26; controllare esplicitamente tutti i percorsi che usano isAlive.
Restano multipart, altre parita/configurazioni/animazioni, DedicatedServer
ordinario e persistenza MP, punte grigie e cause intermittenti sonno/escort.
Tappa1 e port completo aperti. Proseguire autonomamente nella stessa chat/ramo.
Non sospendere/spegnere MAI; entrambe le email24ottobre gia registrate, niente duplicati.


## Checkpoint 0.1.38 - esperienza originale per stadio

Ripristinati gli XP base originali:5/20/150/300/650 per gli stadi1..5.
Override del solo getBaseExperienceReward: accredito del giocatore, incantesimi,
regola mobDrops e generazione degli orb restano gestiti dal codice nativo.
Nessuna generazione manuale o ripetuta di XP; inventario ancora rilasciato una volta.

Build completa passata in9m11s:85JUnit,121GameTest mod (122 con baseline),11prove
client e33PNG correnti. Due nuove prove server uccidono realmente i draghi dei
cinque stadi, contano gli orb includendo Count e verificano inventario, tick di
morte, morte ambientale e mobDrops=false. Vedere TEST-0.1.38.md.
La prova rete a due client resta quella pubblicata sulla0.1.37: NON rieseguita
sulla0.1.38, quindi esclusa dalle prove correnti e dal nuovo archivio.

PROSSIMO: carcassa persistente, raccolta, item e teschio piazzabile originale.
XP completati come passo atomico; carcassa e raccolta NON ancora implementate.
Oggi rimozione vanilla e solo drop inventario. Ricerca originale dettagliata in
work/phase38-preparation/RESEARCH.txt fuori repo. Non attivare carcasse persistenti
senza dipendenze di raccolta complete; preservare proprieta, inventario e regola
dismount senza immunita in volo. Restano multipart, altre parita, DedicatedServer
ordinario/persistenza MP, punte grigie e casi intermittenti sonno/escort.
Tappa1 e port completo aperti. Proseguire nella stessa chat/ramo, senza delega.
Non sospendere o spegnere MAI il PC. Entrambe le email della sessione24ottobre
sono gia registrate nello stato privato: non inviarle nuovamente.


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

Aggiornato il 24 ottobre 2026, checkpoint 0.1.30, nuova autorizzazione utente a proseguire nelle fasi.
Conservare fabric/HANDOFF.md: contiene lo storico e i vincoli permanenti.

---

Continua il porting di Ice and Fire. Comunica in italiano. Leggi integralmente
fabric/HANDOFF.md, fabric/README.md, fabric/REVIEW.md, fabric/TEST-0.1.30.md e gli
AGENTS.md applicabili. Non ricominciare da zero o sovrascrivere modifiche locali.

Repository personale:
https://github.com/Djdragonyyy/Ice_and_Fire_Fabric26.3_port.git
Ramo esclusivo: port/fabric-26.3. Controlla stato, remote e git log prima di
modificare; git switch e git pull --ff-only origin port/fabric-26.3, preservando
il lavoro locale. Verifica l'esistenza del commit base 417f5a8d2. Niente reset.

Checkpoint0.1.28: cristallo fuoco, legame UUID/nome e richiamo stessa entità. SavedData native + ticket temporanei non persistenti per sorgente scaricata; proprietà/destinazione/passeggeri controllati, richieste concorrenti isolate, timeout2000 e cleanup. 70JUnit,99GameTest mod,6client/15PNG: suite completa e vero unload/reload passati. Prossimi: ruggiti/effetti e altri suoni, acqua/lava/navigazione e lacune cumulative; cristallo/flauto/corno già presenti. Utente richiede usare residuo e prossima ripresa24ottobre22:12Roma al reset (22:12:11 app). Automazione stessa chat aggiornata alle22:12:20; a quel risveglio ripristinare controlli5min. Mai sospendere/spegnere.

Checkpoint0.1.27: flauto (due mani/cooldown60/config4chunk/range0..10000/suono originale), stop volo dei draghi propri senza smontare/azzerare caduta. Blink6/50 con skin chiusa/glow disabilitato e posa sveglia. 70JUnit,92GameTest mod,5client/14screenshot, suite completa e poi client ripetuti solo per angolo della preview, tutti passati. Prove di cancellazione distinguono SPEAK cosmetico da attacchi; target/danni/NoAI restano verificati. Prossimi: cristallo, ruggiti/altri suoni, acqua/lava e lacune cumulative. Nessuna sospensione.

Checkpoint0.1.26: corno del drago, salvataggio nativo in oggetto e rilascio con inventario/UUID, icone/tooltip originali. Guardie proprietà/passeggeri/permessi/ostacoli/chunk/UUID caricato. Corretto ramo creativo usando vero stack, test native 5 nuovi. 70JUnit,88GameTest mod,5test client/12screenshot, suite completa. Mock owner non registrato: verifica UUID; client connesso verifica isOwnedBy. Prossimi: flauto/cristallo, palpebre/ruggiti, acqua/lava e lacune cumulative. Nessuna sospensione, tappa1 aperta.

Checkpoint0.1.25: 70JUnit,83GameTest mod,4test client/11screenshot. Voci originali per età e SPEAK20tick, NoAI protetto. 0.1.24: quota autonoma e raggio casa configurabili. Prossime interazioni originali; niente sospensione, tappa1 aperta.

Prima leggere fabric/STATO-TAPPA-1.md. Checkpoint0.1.23: config salute/danno/fame/
schiusa, default/vecchi JSON conservati e incubazione long compatibile con vecchi
NBT int, soglia massima senza overflow. Dopo push/consegna continuare senza sospensione. Goal completo attivo e incompleto.

Checkpoint0.1.22: presa/scuotimento di
prede mob con passeggero nativo, ID separato dal pilota e UUID/tick salvati.
Danno singolo dopo55tick, cura/fame e rilascio; cancellazioni e priorità comandi.
Pose originali e recupero della presa dal caricatore ricorsivo. Conservati tutti
i checkpoint precedenti: riproduzione/nido, stendardi, caccia, sonno/occhi,
cibo, attacchi, armature, volo, controlli e proprietà. Tappa1 incompleta.

ULTIMA AUTORIZZAZIONE: utente a dormire, autonomia completa, test di ogni blocco,
proseguire nelle fasi usando quota disponibile. Sospensione PC ANNULLATA: non sospendere o spegnere dopo salvataggio/push/consegna.
Sessione0.1.18 sospesa e ripresa correttamente, nuova quota disponibile; non
avviare nuovi processi durante sospensione. Goal completo attivo e incompleto.
Non consumare quota artificialmente o dichiarare chiuse fasi non migrate.
Nuova richiesta supera attese di riscontro fra checkpoint, conserva ordine delle
tappe e loro verifica. La1 è ancora incompleta.

Prossimi: configurazioni originali ancora fisse, interazioni e animazioni
secondarie/suoni, dettagli acqua/lava e navigazione complessa. Usare lo stato
cumulativo; caccia/stendardo/sonno/riproduzione/presa sono già presenti, non
ricominciare quei blocchi. Giocatori esclusi dalla presa in questa fase.
Nessuna mutazione NoAI della preda, un solo occupante e nessun nuovo posto.
Uova raccolte senza incubazione/proprietà. Sonno libero, targeting separato dal
movimento per difesa dal sonno; occhi ordine1 e armature2..5. Punte grigie aperte.

Toolchain fissata: Minecraft Java 26.3, JDK25, Loader0.19.5, Fabric API
0.163.0+26.3, Loom1.18.4, Gradle9.7.1. Usare fabric/ come progetto Gradle
indipendente, senza compilare per errore il vecchio progetto Forge nella radice.
Sul PC di ripresa è stato usato Azul Zulu25.0.4.1+1 locale, con SHA-256 verificato;
non riutilizzare alla cieca percorsi di altri PC. Il launcher Windows usa
JAVA_HOME\bin\java.exe: il percorso misto con /bin/ falliva in questo ambiente.
Cache, JDK, mondi e build non sono nel Git e vanno ricostruiti dopo un clone.

```powershell
.\fabric\gradlew.bat -p fabric clean build runGametest --console=plain
```

Prosegui la tappa1 confrontando sempre i sorgenti originali conservati in src/.
Prossimi blocchi: configurazioni originali residue, altre interazioni, animazioni
secondarie e suoni; verificare lacune nello stato cumulativo.
Il flight manager attuale non è un pathfinder aereo completo per aggirare edifici
o grotte; acqua/lava complete e configurazioni di volo restano da migrare.
Pianifica e verifica i comportamenti prima di dichiararli migrati.

Vincoli permanenti:

- Tappa1 incompleta: completarla/verificarla prima della2; avanzamento nelle fasi ora autorizzato.
- Punte grigie ancora aperte: diagnosi e immagini già ricevute in HANDOFF.md.
  Non cambiare UV o riempire trasparenze alla cieca; non dichiararle risolte.
- Non reintrodurre il dorso solido derivato dal ghast. Un solo pilota adulto.
  Conservare camera, controlli e tutte le funzionalità confermate.
- I piccoli in spalla sono entità del mondo salvate indipendentemente dal
  giocatore. Non aggirare il divieto26.3 di usare PLAYER come veicolo.
- Volo autonomo dallo stadio2, pilota dallo stadio3: non confondere le due soglie.
- Conservare modelli, texture, statistiche e meccaniche originali, LGPL e
  attribuzioni. Distinguere limiti del port e comportamento originale.
- Ogni consegna deve includere SEMPRE due JAR installabili: mod senza suffisso
  -sources e Fabric API0.163.0+26.3, più istruzioni, sorgenti e checksum.
  Mai più versioni della mod insieme in mods.
- Revisionare il diff completo. Usare review-agent se disponibile, in sola
  lettura e senza delega; correggere fuori dalla revisione e ripetere i controlli.
  Verificare le skill sul PC corrente, senza attribuire uso a skill assenti.
- Commit e push al solo ramo port/fabric-26.3 dell'origin personale autorizzati.
  Niente merge sul principale o release pubbliche. Non stampare email o
  credenziali: usare l'autore noreply verificato dalla storia, niente config globale.
- L'ultimo comando prima della risposta finale deve mostrare 417f5a8d2:
  git show -s --format=%h --abbrev=9 417f5a8d2

Esegui il lavoro autorizzato, non fermarti al piano. Aggiorna handoff, prompt di
ripresa e documentazione. Consegna risultati e limiti senza attribuire ai test
prove mai svolte.
