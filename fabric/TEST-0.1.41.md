# Prova 0.1.41 - multipart del drago del fuoco

Tappa1 e port completo ancora aperti. Checkpoint del26ottobre2026.

## Comportamento e adattamento

Dieci hitbox originali: testa, collo, quattro porzioni alari e quattro segmenti
coda. Coefficienti confrontati con EntityDragonBase.resetParts; lunghezze e
posizioni usano renderSize/3, anche sopra il limite fisico7 dello stadio5.
Restano invariati collider del corpo, PNG/UV/geometria, navigazione e seduta.

Parti come Entity native26 con tracking ordinario, renderer vuoto, nessun
salvataggio o evocazione separata. Parent risolto tramite ID piu UUID, indice
validato. Il server mantiene dieci parti canoniche, ricrea la famiglia se una
manca, pulisce corno/rimozione/unload. Crescita ridimensiona la stessa famiglia
anziche ricrearla ogni giorno; entita orfane scadono senza forzare chunk.

Danno al genitore con DamageSource nativo e moltiplicatore originale: testa e
punta coda1.5, ali.5, collo e altri segmenti1. Cooldown e armatura condivisi.
Immunita ambientali originali, protezione dalle proprie cariche e nessun posto
extra sulle parti. Root vehicle condiviso esclude la selezione del proprio
corpo da parte del pilota. Non viene aggiunta immunita da caduta in volo.

Interazione inoltrata al Player.interactOn completo, inclusa delega all'oggetto,
una volta sola: staff, cattura/rilascio nel corno, monta, sangue e teschio finale.
Mixin comune su Player.itemAttackInteraction risolve solo le callback dell'arma
sul genitore LivingEntity, come il caso vanilla del drago. Il danno resta
applicato dalla parte, non viene raddoppiato. Provata usura survival pari a1.

## Verifica definitiva

Comando: build runGametest compileClientGametestJava runClientGametest --offline.
phase41-final-4.log: BUILD SUCCESSFUL in16m04s.94JUnit senza errori (task test
up-to-date su sorgenti compilati identici),138GameTest mod e1baseline vanilla,
14prove client registrate e40PNG, tutte prodotte tra14:04:52 e14:06:27 del26ottobre.
Report server alle14:04:18, tutti139test richiesti superati. Nessun test o harness
nel JAR di produzione. Vecchio final-3 interrotto escluso dalla consegna.

Cinque nuovi GameTest: tutte le parti e stadi/yaw/dimensioni; danni e cooldown,
arma survival/protezione carica/pilota; staff/corno e nuova famiglia; carcassa e
raccolta finale; crescita/riparazione/orfane/unload. Nuova prova client: tracking
di dieci parti, ray picking sulla coda fuori dal corpo e attacco C2S nativo,
comando una volta, cattura/rilascio, monta e nessuna selezione del proprio drago.

Immagine0039 e una panoramica del modello originale stadio5. Controllata a vista,
non mostra wireframe, non va presentata come prova visiva dei contorni delle
hitbox. Questi sono verificati da bounding box e picking nelle prove native.

## Limiti

Server integrato reale/C2S e GameTestServer, non due account su DedicatedServer
ordinario. Unload/rimozione simulati e NBT precedenti non sostituiscono il test
di mondo multiplayer salvato e riaperto. Prestazioni con molti draghi e ulteriori
combinazioni di armi/incantesimi restano da collaudare. Le parti aggiungono dieci
entita tracciate per drago. Restano gli altri punti aperti di STATO-TAPPA-1.md.