# Ice and Fire 0.1.28 — cristallo di richiamo del drago di fuoco

Minecraft Java26.3, Java25, Loader0.19.5, Fabric API0.163.0+26.3.
Checkpoint parziale della tappa1. Il port completo resta in corso.

/give @s iceandfire:summoning_crystal_fire

Il cristallo vuoto si lega con clic destro al proprio drago, anche in creativo.
Conserva UUID e nome in un componente oggetto nativo; il drago conserva il flag
CrystalBound salvato e sincronizzato. Modelli/texture inattivo/attivo originali,
tooltip originale e scheda creativa ingredienti. Il legame non rimuove il drago.

Usarlo su un blocco richiama il drago nella casella adiacente alla faccia cliccata.
Come originale, il richiamo opera nell'Overworld. La proprietà attuale viene
ricontrollata al richiamo: un cristallo passato ad altri non consente di spostare
il drago altrui. Piloti, prede e draghi in spalla/veicolo impediscono il richiamo.
Sono protezioni aggiuntive esplicite; nessun passeggero viene sganciato o copiato.

Il richiamo conserva la stessa entità/UUID/inventario. Controlla corpo completo,
collisori, fluidi, limiti verticali/bordo e chunk destinazione già caricati.
Fermi navigazione e movimento residuo; comando, età e dati del drago restano.
Contenuto del cristallo, flag e indice cancellati SOLO dopo teletrasporto riuscito.
Fallimenti conservano il legame e mostrano il messaggio originale. I suoni di
legame e arrivo sono quelli originali. Le versioni ghiaccio/fulmine restano fase2.

## Draghi scaricati

chunkLoadSummonCrystal in config/iceandfire-combat.json, defaulttrue originale.
Riavvio richiesto; vecchi JSON mantengono true. Se false restano possibili i
richiami di entità già caricate, e quelli scaricati falliscono senza consumare
il cristallo o aggiungere richieste. Nessun caricamento di altre dimensioni.

Il registro nativo SavedData iceandfire:dragon_positions conserva UUID/posizioni
per livello. È verificato con salvataggio su disco e riapertura. I draghi legati
aggiornano la posizione ogni20tick e all'uscita dal tracking; uccisione/scarto
rimuovono l'indice. La posizione durante scaricamento è registrata anche quando
il callback di fine tracking precede il motivo di rimozione. Nessuna promessa di
importazione diretta dei vecchi SavedData Forge1.20: questa è persistenza nativa26.3.

Per un drago non caricato viene chiesto un ticket soltanto sul chunk
ORIGINE noto del drago. Richieste indipendenti per mondo/UUID con giocatore,
mano, oggetto e destinazione propri: nessuno stato mutabile sul singleton Item.
Caricamento asincrono, tolleranza originale2000tick, ticket nativo non persistente
con scadenza2001. Successo, timeout, morte/cambio livello o cambio dell'oggetto in
mano rilasciano il ticket; lo spegnimento del server svuota le richieste. Due
richieste dallo stesso chunk condividono il ticket fino alla conclusione dell'ultima.
I ticket del cristallo non modificano quelli di giocatori, portali o altri sistemi.

## Prove

Sette nuovi GameTest: roundtrip ItemStack/drago e richiamo stessa entità; ostacolo,
proprietario diverso e pilota; creativo senza scrivere su copia temporanea; SavedData
su disco e configurazione disabilitata; richieste concorrenti/cancellazione; confine
esatto del timeout; vero scaricamento e ricaricamento da entity storage nativo.
Quest'ultimo genera/salva il chunk, osserva la scomparsa dell'UUID dal mondo,
richiede il richiamo e verifica stesso UUID/proprietario e ticket rilasciato.

La fixture asincrona usa callback nuovi a ogni controllo (GameTestInfo rimuove
quello appena eseguito) e cede5ms ai worker disco, poiché GameTestServer esegue
i tick senza attesa. Non cambia timeout della mod o orologio del mondo. Il caso
reale è passato isolato; la consegna richiede anche tutta la suite finale.

Nuova prova client: vera interazione per legare, icone vuoto/attivo, native useItemOn
per richiamare e sincronizzazione di UUID/flag/oggetto e posizione della stessa
entità. Nessuna prova con due veri client contemporanei. Le altre lacune della
tappa1 sono nello stato cumulativo. Nessuna sospensione del PC.

Verificati 70 JUnit, 99 GameTest della mod (100 con baseline vanilla escluso) e 6 prove client con quindici screenshot. Nessuna prova con due veri client.
Suite completa finale passata, inclusa di nuovo la prova di scaricamento/ricaricamento reale. Build completa riuscita. Quattro asset originali del cristallo verificati byte per byte nel JAR; icone vuoto/attivo ispezionate nel client.
