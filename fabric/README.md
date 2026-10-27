# Ice and Fire: port Fabric 26.3 in sviluppo

Verificati 100 JUnit, 171 GameTest della mod (172 con baseline vanilla), 18 prove client e 54 immagini nella 0.1.54. I draghi selvatici reagiscono alle interazioni e alla rottura dei forzieri secondo la configurazione originale. Condizioni e limiti in TEST-0.1.54.md.

**Checkpoint di sviluppo: la tappa 1 e il port completo restano aperti.**

Versione corrente: **0.1.54-stage1-dev**. Reazione originale ai forzieri. [TEST-0.1.54.md](TEST-0.1.54.md). Tappa1 incompleta.

Versione precedente: **0.1.53-stage1-dev**. Reazioni di fuga originali e configurazione. [TEST-0.1.53.md](TEST-0.1.53.md). Tappa 1 incompleta.

Versione precedente: **0.1.52-stage1-dev**. Regressione di persistenza Forge; codice di gioco identico alla 0.1.51. [TEST-0.1.52.md](TEST-0.1.52.md). Tappa 1 incompleta.

Versione precedente: **0.1.51-stage1-dev**. Distanze originali della camera e F7. [TEST-0.1.51.md](TEST-0.1.51.md).

Versione precedente: **0.1.50-stage1-dev**. Prospettiva automatica configurabile. [TEST-0.1.50.md](TEST-0.1.50.md).

Versione precedente: **0.1.49-stage1-dev**. Costruzione della DragonForge e blocchi di scaglie. [TEST-0.1.49.md](TEST-0.1.49.md).

Versione precedente: **0.1.48-stage1-dev**. Alimentazione autonoma e comparatore. [TEST-0.1.48.md](TEST-0.1.48.md).

Versione precedente: **0.1.47-stage1-dev**. Prima DragonForge manuale, GUI e inventario. [TEST-0.1.47.md](TEST-0.1.47.md).

Versione precedente: **0.1.46-stage1-dev**. Richiamo con stufato di fuoco e priorita IA. [TEST-0.1.46.md](TEST-0.1.46.md). Tappa1 incompleta.

Versione precedente: **0.1.45-stage1-dev**. Incubazione, movimento e schiusa originali delle uova. [TEST-0.1.45.md](TEST-0.1.45.md). Tappa1 incompleta.

Versione precedente: **0.1.44-stage1-dev**. Logout in sella, RootVehicle, riavvio e rientro in volo verificati; produzione invariata dalla 0.1.43. [TEST-0.1.44.md](TEST-0.1.44.md). Tappa1 incompleta.

Versione precedente: **0.1.43-stage1-dev**. Collaudo DedicatedServer ordinario con due client e riapertura del mondo salvato; stessi 99 bytecode di produzione della 0.1.42. [TEST-0.1.43.md](TEST-0.1.43.md). Tappa1 incompleta.

Versione precedente: **0.1.42-stage1-dev**. Esplosioni opzionali delle cariche, native e protette dalle regole del terreno. [TEST-0.1.42.md](TEST-0.1.42.md). Tappa1 incompleta.

Versione precedente: **0.1.41-stage1-dev**. Dieci hitbox originali sincronizzate, danni/interazioni e callback native delle armi. [TEST-0.1.41.md](TEST-0.1.41.md). Tappa1 incompleta.

Versione precedente: **0.1.40-stage1-dev**. Carcassa persistente e raccolta originale fino al teschio, con posa e scheletro originali. [TEST-0.1.40.md](TEST-0.1.40.md). Multipart e altre parita ancora aperti; tappa1 incompleta.

Versione precedente: **0.1.39-stage1-dev**. Oggetti della raccolta e teschio originale piazzabile nei cinque stadi. [TEST-0.1.39.md](TEST-0.1.39.md). Carcassa ancora da collegare; tappa1 incompleta.

Versione precedente: **0.1.38-stage1-dev**. XP originali per i cinque stadi tramite hook nativo, senza duplicazioni. [TEST-0.1.38.md](TEST-0.1.38.md). Carcassa/raccolta e tappa1 ancora aperte.

Versione precedente: **0.1.37-stage1-dev**. Movimenti originali del corpo, collo/coda e ali protette durante hover/tackle/attacchi; rete a due client rieseguita. [TEST-0.1.37.md](TEST-0.1.37.md). Tappa1 incompleta.

Versione precedente: **0.1.36-stage1-dev**. Feeding manuale e animazione nei cinque stadi, audit geometria/UV di corpo e ali e dieci tavole GPU. [TEST-0.1.36.md](TEST-0.1.36.md). Tappa1 incompleta.

Versione precedente: **0.1.35-stage1-dev**. Due client in processi separati su GameTestServer locale, proprieta/comandi e sincronizzazione verificati. [TEST-0.1.35.md](TEST-0.1.35.md). Tappa1 incompleta.

Versione precedente: **0.1.34-stage1-dev**. Overlay maschile originale e ricaricamento nativo delle texture. [TEST-0.1.34.md](TEST-0.1.34.md). Tappa1 incompleta.

Versione precedente: **0.1.33-stage1-dev**. Camminata originale e velocita sincronizzata dal server. [TEST-0.1.33.md](TEST-0.1.33.md). Tappa1 incompleta.

Versione precedente: **0.1.32-stage1-dev**. Movimento originale sopra la lava. [TEST-0.1.32.md](TEST-0.1.32.md). Tappa1 incompleta.

Versione precedente: **0.1.31-stage1-dev**. Acqua/lava e controlli verificati con vero client. [TEST-0.1.31.md](TEST-0.1.31.md). Tappa1 incompleta.

Versione precedente: **0.1.30-stage1-dev**. Navigazione del corpo completo, deviazioni locali e prova reale dello stadio5 oltre una parete fino all'atterraggio. [TEST-0.1.30.md](TEST-0.1.30.md). Tappa1 incompleta.

Versione precedente: **0.1.29-stage1-dev**. Ruggiti/aura, pose e voci originali, battito d'ali configurabile e audio HOSTILE. [TEST-0.1.29.md](TEST-0.1.29.md). Tappa1 incompleta.

Versione precedente: **0.1.28-stage1-dev**. Cristallo fuoco: legame persistente, richiamo stessa entità e caricamento temporaneo del chunk sorgente. [TEST-0.1.28.md](TEST-0.1.28.md). Tappa1 incompleta.

Versione precedente: **0.1.27-stage1-dev**. Flauto originale con configurazione e palpebre con texture originali. [TEST-0.1.27.md](TEST-0.1.27.md). Tappa1 incompleta.

Versione precedente: **0.1.26-stage1-dev**. Corno del drago, salvataggio nativo e rilascio, icone vuoto/fuoco originali e tooltip. [TEST-0.1.26.md](TEST-0.1.26.md). Tappa1 incompleta.

Versione precedente: **0.1.25-stage1-dev**. Voci originali ambientali/ferita/morte per età e animazione della mandibola SPEAK. [TEST-0.1.25.md](TEST-0.1.25.md). Tappa1 incompleta.

Versione precedente: **0.1.24-stage1-dev**. Configurazioni originali della quota autonoma e del raggio casa. [TEST-0.1.24.md](TEST-0.1.24.md). Nessuna sospensione del PC, annullata dall'utente.

Versione precedente: **0.1.23-stage1-dev**. Configurazioni originali di salute,
danno, fame e schiusa; default/curve conservati e incubazione senza overflow.
[TEST-0.1.23.md](TEST-0.1.23.md). Tappa1 ancora incompleta.

Versione precedente: **0.1.22-stage1-dev**. Presa e scuotimento della preda,
pose originali, danno/cura/rilascio e ripresa dal salvataggio nativo dei passeggeri.
Un solo occupante, giocatori esclusi dalla presa. [TEST-0.1.22.md](TEST-0.1.22.md).
Tappa1 ancora incompleta.

Versione precedente: **0.1.21-stage1-dev**. Caccia autonoma tramite tag originali,
fame e taglia; priorità della difesa e del cibo raggiungibile. Configurazione
della distanza di ricerca. [TEST-0.1.21.md](TEST-0.1.21.md). Tappa1 incompleta.

Versione precedente: **0.1.20-stage1-dev**. Stendardo sul corpo animato con
colori/motivi vanilla e sincronizzazione fuori dalla GUI. Unica fonte salvata
e rilasciata resta lo slot inventario. [TEST-0.1.20.md](TEST-0.1.20.md).
Tappa1 ancora incompleta.

Versione precedente: **0.1.19-stage1-dev**. Riproduzione con stufato di fuoco,
corteggiamento, uovo/nido e cooldown salvato. Protezioni del terreno e prova
completa da uovo nato a schiusa con nuovo proprietario. [TEST-0.1.19.md](TEST-0.1.19.md).
Tappa1 ancora incompleta, autonomia continua secondo autorizzazione utente.

Versione precedente: **0.1.18-stage1-dev**. Sonno notturno, risvegli difensivi,
posa/texture sleeping, occhi emissivi originali e recupero delle uova danneggiate.
[TEST-0.1.18.md](TEST-0.1.18.md).60 JUnit,60 GameTest mod e3 prove client passati.
Tappa1 ancora incompleta, stato cumulativo aggiornato e port completo non concluso.

Versione precedente: **0.1.17-stage1-dev**. Combattimento aereo autonomo con tuffo,
hover blast e scorch stream; decollo verso bersagli elevati e reset aria/terra.
Nuova prova client delle pose coda/ali e anteprima ritagliata. Istruzioni:
[TEST-0.1.17.md](TEST-0.1.17.md). Tappa1 incompleta.

Build verificata:58 JUnit,51 GameTest mod (52 con baseline vanilla escluso),due test client in mondi temporanei con screenshot e verifica pixel delle statistiche. Nessun collaudo con due veri client.

Versione precedente: **0.1.16-stage1-dev**. Carne raccolta da terra con cammino,
consumo/cura e animazione sincronizzata; attacchi terrestri coda/ali e palla di
fuoco autonoma. Istruzioni: [TEST-0.1.16.md](TEST-0.1.16.md). Tappa1 incompleta.

Versione precedente: **0.1.15-stage1-dev**. Corridoio locale fra tronchi/foglie
per la difesa dei draghi adulti; dismount laterale e protezione della caduta solo
a terra; doppio salto per decollare; primo soffio autonomo terrestre a distanza.
Istruzioni e limiti: [TEST-0.1.15.md](TEST-0.1.15.md). Tappa1 incompleta.

Build verificata: 56 JUnit, 41 GameTest della mod (42 includendo always_pass
vanilla escluso dal conteggio) e un test client grafico con mondo/screenshot
superati. Nessuna prova multiplayer con due veri client o collaudo visivo delle
novità di combattimento. Revisione completa review-agent read-only: No findings.

Versione precedente: **0.1.14-stage1-dev**. Prima IA difensiva autonoma: bersaglio
del proprietario, difesa sua e del drago, inseguimento terrestre e morso con
impatto nella finestra originale dell'animazione. Se in aria atterra per combattere.
Attacchi aerei e altri attacchi terrestri restano da migrare. Istruzioni:
[TEST-0.1.14.md](TEST-0.1.14.md). Tappa 1 ancora incompleta.

Build riuscita: 53 JUnit, 34 GameTest della mod (più always_pass vanilla escluso)
e una prova client con mondo e screenshot superati. Revisione completa secondo
review-agent in sola lettura senza delega: No findings. Nessun test con due client.

Versione precedente: **0.1.13-stage1-dev**, confermata funzionante dall'utente
il 24 ottobre 2026. Corretti gli overlay testa/coda tramite
ordine di disegno; verifica grafica nel client con confronto prima/dopo e 32 icone.
Seguito terrestre/aereo più rapido e stabile, recupero con teletrasporto sicuro
a150 blocchi, volo autonomo libero/seguimi verificato con movimento reale.
53 JUnit, 27 GameTest della mod e una prova client con mondo/screenshot.
[TEST-0.1.13.md](TEST-0.1.13.md) e [riepilogo cumulativo della tappa1](STATO-TAPPA-1.md).
Il riscontro consente ora di proseguire la tappa 1.

Versione precedente: **0.1.12-stage1-dev**. Volo autonomo dallo stadio 2,
hovering e posa originale, seguito in aria senza teletrasporto, atterraggio su
comando e ritorno notturno alla casa a piedi/in volo. Pilota sempre dallo stadio 3.
Build pulita, 50 JUnit e 23 GameTest della mod passati (baseline vanilla escluso).
Istruzioni e limiti: [TEST-0.1.12.md](TEST-0.1.12.md). Nessuna conferma visiva o
multiplayer delle novità. Tappa 1 incompleta e punte grigie ancora aperte.

Versione precedente: **0.1.11-stage1-dev**. Alimentazione con carne, pasto malaticcio
per bloccare la crescita, pasto creativo e bastone originale dragon_stick.
Comandi libero/seduto/seguimi, seguito a terra, casa persistente e limite di
vagabondaggio. 45 JUnit e 15 GameTest della mod superati; client avviato.
[TEST-0.1.11.md](TEST-0.1.11.md). Tappa 1 incompleta: volo/combattimento autonomi
e ritorno al nido restano da migrare; punte grigie aperte.

Versione precedente **0.1.10-stage1-dev**, confermata funzionante dall'utente.
Getto con 16 fotogrammi di flame.png originale
e dissolvenza; anteprima GUI senza riduzione aggiuntiva degli adulti; draghetti di
stadio 1 in spalla (mano vuota, massimo tre; Maiusc per rilasciare). Conservati
salvataggio indipendente e unica seduta adulta. 45 JUnit e 9 GameTest della mod
passati, client avviato; resa in mondo ancora da provare. [TEST-0.1.10.md](TEST-0.1.10.md).
Tappa 1 incompleta, punte grigie aperte e dorso ghast rimosso.

Versione precedente: **0.1.9-stage1-dev**. Soffio progressivo con fiamma originale,
dimensione/densità crescenti con il drago e posa meno invasiva per il pilota in prima
persona. Migrati terreno carbonizzato, distruzione dell'area da proiettile e inventario
originale a cinque slot con armature. Apertura: Maiusc + clic destro a mano vuota.
Build, 41 test JUnit e 5 GameTest della mod superati; resa visiva ancora da provare.
Istruzioni: [TEST-0.1.9.md](TEST-0.1.9.md). Unica seduta e nessun dorso solido.
I paragrafi seguenti descrivono versioni storiche; la funzione ghast citata sotto è rimossa.

Versione 0.1.4 (precedente): tilt in discesa attenuato vicino ai blocchi;
tilt nascosto soltanto al pilota in prima persona. Nuovo comando **V: decolla**,
riassegnabile, oltre a spazio tenuto premuto per oltre un secondo. Richiesta di decollo
conservata tra i tick di rete e impulso iniziale mantenuto fino al distacco da terra.
Build ordinaria e 28 test superati. Istruzioni: [TEST-0.1.4.md](TEST-0.1.4.md).

Versione 0.1.3 (precedente): inclinazione del drago durante salita/discesa,
impulso di decollo sul client pilota, seduta sulla superficie del collo anche allo stadio 5;
dorso solido richiesto dall'utente, con arresto mentre un giocatore è sopra senza montare.
Build ordinaria e 21 test superati. La camera della 0.1.2 è stata confermata dall'utente;
le nuove correzioni richiedono prova visiva. Istruzioni: [TEST-0.1.3.md](TEST-0.1.3.md).

Aggiornamento nuovo PC, 18 ottobre 2026: verificato il checkout pulito del ramo
`port/fabric-26.3` al commit remoto `417f5a8d2`. Build ordinaria riuscita con JDK
25.0.4.1+1, senza workaround ZIP. Completata la revisione 0.1.2 e corretto un salto
del corpo/seduta nella transizione del volo; **17 test superati**. Dettagli in
[REVIEW.md](REVIEW.md). Le punte grigie e le prove visive/in mondo rimangono aperte.

Per i vincoli operativi, i risultati precedenti e la ripresa su un altro PC,
leggere [HANDOFF.md](HANDOFF.md). I risultati del primo checkpoint riportati sotto sono storici.

Base originale: ramo `1.20`, commit `0526aad94119b705a86207623481b087c4d052ca`, Minecraft 1.20.1.
La cartella `src/` della repository conserva il codice originale. Questo progetto Gradle indipendente
usa esclusivamente i sorgenti in `fabric/src/` e include soltanto le risorse originali selezionate.
Identificatori della mod e dei primi contenuti: `iceandfire`, `fire_dragon`, `dragon_egg`,
`dragonegg_red`, `dragonegg_green`, `dragonegg_bronze`, `dragonegg_gray`, `dragon_meal`.

## Dipendenze fissate

| Componente | Versione |
| --- | --- |
| Minecraft Java | 26.3 |
| Java | 25 |
| Gradle Wrapper | 9.7.1, distribuzione verificata con SHA-256 |
| Fabric Loom | 1.18.4 |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.163.0+26.3 |
| JUnit Jupiter | 5.13.4 |

Il JAR richiede Fabric Loader e Fabric API; non installare il JAR originale Forge insieme a questo port.
Citadel Forge non è una dipendenza di questa base. Il lettore Tabula usa gli asset originali senza
conversioni permanenti e l'interpolazione di movimento è adattata dall'animatore originale.
Il resto delle funzioni Citadel deve ancora essere migrato: questo checkpoint non ne dichiara la sostituzione completa.

## Build ordinaria

Aprire `fabric/` come progetto Gradle nell'IDE e selezionare JDK 25 come JVM di Gradle.
Da PowerShell nella cartella principale della repository:

```powershell
# JAVA_HOME deve indicare una propria installazione di JDK 25.
& "$env:JAVA_HOME\bin\java.exe" -version
.\fabric\gradlew.bat -p fabric build
```

Il JAR viene scritto in `fabric/build/libs/`; quello con suffisso `-sources` contiene i sorgenti.
In un terminale diverso il download locale del JDK effettuato durante il lavoro NON viene selezionato automaticamente.
`fabric.loom.disableObfuscation=true` è esplicito perché Minecraft 26.3 non richiede remapping.

## Risultati verificati il 18 ottobre 2026

- Build originale invariata: fallita durante `createMcpToSrg` per un archivio di mapping non disponibile.
  Il log contiene anche `ReadOnlyFileSystemException`; non è stato dimostrato un difetto dell'upstream.
- Build Fabric ordinaria nel sandbox Windows: fallita nella preparazione/modifica degli archivi Minecraft.
  Un programma Java minimo riproduce `Files.isWritable=false` e `ReadOnlyFileSystemException`
  su un nuovo ZIP nella cartella di lavoro, nonostante la normale creazione di file sia consentita.
- Build diagnostica alternativa: riuscita. La cache dei due archivi Minecraft è stata preparata con
  ZIP streaming; uno script Gradle locale in `work/` ha disattivato i transitive access wideners,
  l'iniezione delle interfacce delle dipendenze e il loro javadoc per questa verifica.
  Queste modifiche diagnostiche NON sono impostazioni predefinite del progetto.
- I sorgenti comuni compilano anche separatamente con `javac` sulle librerie ufficiali scaricate.
- Sei test JUnit superati: quattro su crescita/statistiche/schiusa e due sul caricamento delle pose Tabula.
- Server `runServer --args=--initSettings`: riuscito, mod inizializzata e configurazioni generate.
  Non equivale all'avvio di un mondo/server multiplayer; l'EULA generata non è stata accettata automaticamente.
- Client di sviluppo: caricati mod, backend grafico, audio e atlanti. Processo poi arrestato.
  Nessuna verifica visiva o prova di gameplay è stata eseguita.

La build ordinaria fuori dal sandbox deve ancora essere verificata. Il JAR diagnostico non è
una prova che la tappa sia completa o che il gameplay sia corretto.

## Implementato e ancora da fare

Lo stato cumulativo aggiornato è in [STATO-TAPPA-1.md](STATO-TAPPA-1.md).
Il port include uova/crescita, proprietà/persistenza, drago di fuoco con modelli
originali, cavalcatura e controlli, spalle, inventario/armature/stendardi,
alimentazione/comandi, combattimento terrestre/aereo e soffio, effetti sul
terreno, seguito/recupero150blocchi, volo libero/ritorno al nido, sonno/palpebre,
riproduzione, caccia/presa delle prede, corno/flauto/cristallo e configurazioni
migrate. La0.1.29 aggiunge ruggiti/aura e battiti d'ali originali. La0.1.30 aggiunge navigazione del corpo completo e deviazioni locali.

Restano navigazione complessa e comportamento completo acqua/lava, dettagli di
parità delle interazioni/combattimento/riproduzione/sonno/configurazioni,
DragonForge ed esplosioni opzionali, passeggeri nell'anteprima GUI, ulteriori
animazioni/effetti e diagnosi delle punte grigie. Occorre il collaudo finale
in gioco e con due client veri su server dedicato. La suite automatica non
sostituisce queste prove e la tappa1 resta aperta. Altri draghi/creature,
progressione e generazione naturale appartengono alle tappe successive.

## Procedura di accettazione della tappa 1

Questa procedura è una checklist per quando le funzioni mancanti saranno completate, non un elenco di prove già superate.

1. Creare un mondo nuovo creativo con trucchi attivi, annotando JAR, Loader e Fabric API utilizzati.
2. Ottenere le quattro uova e `dragon_meal` dalle schede creative o con `/give`.
3. Piazzare ogni variante, verificare nome personalizzato e proprietà, accendere il fuoco e attendere
   7.201 tick effettivi di incubazione per la schiusa con i valori originali predefiniti.
4. Salvare e riaprire durante l'incubazione e dopo la schiusa: controllare variante, proprietario e avanzamento.
5. Usare il pasto per controllare i passaggi a 25, 50, 75, 100 e 125 giorni, salute e dimensioni.
6. In Opzioni → Comandi → Assegnazione tasti, categoria Ice and Fire, verificare "Drago: scendi di quota"
   (X). Provare anche la riassegnazione a un altro tasto libero e la conservazione dopo riavvio.
   I tre comandi del drago usano nuovi identificatori in 0.1.1 per non ricaricare i codici errati
   eventualmente salvati da 0.1.0; eventuali assegnazioni precedenti vanno reimpostate una volta.
   Dal terzo stadio salire con mano vuota; provare movimento, decollo tenendo salto, discesa con X,
   atterraggio, dismontaggio e riapertura del mondo durante il volo.
7. Provare R e G contro bersagli e ostacoli, confrontando danni, effetti e protezione del proprietario
   con la base Forge. Attualmente questo punto NON può essere superato.
8. Ripetere con due client su server dedicato: un giocatore non proprietario non deve poter controllare il drago.
9. Confrontare visivamente geometria, texture e animazioni con la versione originale.

La tappa 2 inizia soltanto dopo completamento di questa checklist, correzioni, revisione e riscontro dell'utente.

## Licenza e provenienza

Codice e risorse originali: Ice and Fire, Raptorfarian, Alexthe666 e collaboratori.
La licenza LGPL-3.0 originale è conservata nella radice e inclusa nel JAR.
Le regole di crescita, le coordinate dell'uovo e l'interpolazione delle pose derivano dal codice
originale conservato in `src/`. Il port è non ufficiale.


Opzione server: dragonFlightSpeedMod in config/iceandfire-combat.json, predefinito1, intervallo finito0..2. Configurazioni precedenti senza chiave usano1; aggiungerla al JSON per modificarla e riavviare Minecraft/server. Vedere TEST-0.1.33.md per comportamento e limiti.


Prova di rete opt-in: un GameTestServer standalone con due client indipendenti, tre immagini aggiuntive (23 totali nella consegna). Il collaudo di DedicatedServer ordinario e account autenticati resta aperto. Prossima priorita richiesta: feeding nei cinque stadi, corpo e ali fedeli alle texture. Vedere TEST-0.1.35.md.
