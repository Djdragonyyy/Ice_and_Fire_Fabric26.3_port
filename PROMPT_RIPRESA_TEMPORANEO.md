# Prompt temporaneo per la prossima ripresa

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
