# Stato della tappa 1 — drago di fuoco, Fabric 26.3

Aggiornato per 0.1.18-stage1-dev, 24 ottobre 2026. La tappa 1 è ancora incompleta.
Questo riepilogo distingue ciò che è nel codice da ciò che manca: build e test
non sostituiscono il riscontro in gioco, soprattutto per grafica e multiplayer.
L'utente ha confermato la 0.1.13 funzionante il 24 ottobre e chiesto di continuare.
La prova0.1.14 ha rilevato alberi e dismount: correzioni0.1.15 da collaudare. Nessun passaggio alla tappa 2.

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
   Lo stendardo è conservato nello slot ma il suo layer grafico manca ancora.
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

## Da completare nella tappa 1

- Combattimento autonomo: caccia/selezione non difensiva, shake prey e
  parità delle configurazioni/temporizzazioni originali, reazioni e
  coordinamento con movimento e volo.
- Altre interazioni originali e dettagli del ciclo delle uova.
- Riproduzione e ciclo collegato, ulteriori dettagli/parità del sonno originale.
- Comportamento completo in acqua/lava e navigazione aerea più robusta fra
  edifici, alberi e grotte. Il manager attuale verifica spazio e visibilità,
  ma non aggira qualsiasi ostacolo complesso.
- Layer dello stendardo; palpebre, differenze visive complete maschio/femmina,
  animazioni secondarie/attacchi, suoni e particelle ancora mancanti.
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
sulla tappa 1. La 0.1.13 è confermata; collaudare le novità0.1.18 alla ripresa del port.
