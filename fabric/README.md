# Ice and Fire: port Fabric 26.3 in sviluppo

**Checkpoint tecnico parziale. La tappa 1 NON è completata e questo non è un rilascio giocabile approvato.**

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

Presenti e compilati: separazione client/comune, registrazioni Fabric, quattro uova, incubazione
nel fuoco, trasferimento del proprietario alla schiusa, crescita, statistiche di base, salvataggio
dei primi stati, pasto del drago, interazione per salire dal terzo stadio, controlli di volo,
modelli/texture originali e interpolazione iniziale delle pose di movimento.

Da completare prima di chiudere la tappa 1:

- Combattimento originale, soffio, proiettile di fuoco ed effetti sui blocchi; R e G sono attualmente
  trasmessi come input ma NON eseguono ancora gli attacchi.
- IA originale e volo autonomo, comportamento in acqua/lava, posizione del cavaliere e discesa sicura.
- Inventario/interfaccia e interazioni necessarie; alimentazione completa, comandi e gestione dell'uovo danneggiato.
- Animazioni di attacco, animazioni secondarie, occhi, differenze visive maschio/femmina, suoni e particelle.
- Configurazione originale: questo checkpoint usa ancora i valori predefiniti per le regole estratte.
- Test di persistenza e rete in un vero mondo/server e verifica visiva di animazioni e cavalcatura.

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
