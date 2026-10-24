# Stato della tappa 1 — drago di fuoco, Fabric 26.3

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

Aggiornato per 0.1.29-stage1-dev, 24 ottobre 2026. La tappa 1 è ancora incompleta.
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

## Da completare nella tappa 1

- Combattimento autonomo: ulteriori dettagli di presa e
  parità delle configurazioni/temporizzazioni originali, reazioni e
  coordinamento con movimento e volo.
- Altre interazioni originali e dettagli del ciclo delle uova.
- Ulteriori dettagli/parità di riproduzione e sonno originali.
- Comportamento completo in acqua/lava e navigazione aerea più robusta fra
  edifici, alberi e grotte. Il manager attuale verifica spazio e visibilità,
  ma non aggira qualsiasi ostacolo complesso.
- Passeggeri nell'anteprima GUI; differenze visive complete maschio/femmina,
  animazioni secondarie/attacchi, ulteriori suoni e particelle ancora mancanti.
- Configurazioni originali non ancora esposte; integrazione DragonForge ed
  esplosioni opzionali del combattimento.
- Difetto delle punte grigie ancora aperto: non è stato risolto né modificato
  alla cieca. Continuare con confronto equivalente all'originale.
- Collaudo in gioco di camera, seduta, attacchi, armature e volo ai vari stadi;
  mondi salvati/riaperti e due veri client su server dedicato, con controlli di
  proprietà e sincronizzazione. Il riscontro dell'utente resta necessario.

Restano fuori da questa consegna draghi di ghiaccio/fulmine, altre creature,
progressione completa, ricette, strutture e generazione naturale: appartengono
alle tappe successive. Non iniziarle prima del completamento e del riscontro
sulla tappa 1. La 0.1.13 è confermata; collaudare le novità successive, ora fino alla0.1.29.

Nota operativa24ottobre: ripresa effettiva alle22:12Roma dopo reset; automazione stessa chat riportata a controllo5min. Sessione in corso, nessuna sospensione.
