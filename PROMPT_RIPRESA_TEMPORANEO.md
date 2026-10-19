# Prompt temporaneo per riprendere su un altro PC

Preparato il 19 ottobre 2026. Copia il testo seguente nella nuova chat aperta
nella repository dopo il pull. Questo file è un promemoria temporaneo: si può
rimuovere quando il nuovo ambiente avrà letto e recepito l'handoff. Conservare
invece fabric/HANDOFF.md, che contiene lo storico operativo completo.

---

Continua il porting di Ice and Fire su questo PC. Comunica in italiano.
Leggi integralmente fabric/HANDOFF.md, poi fabric/README.md, fabric/REVIEW.md,
fabric/TEST-0.1.11.md e gli eventuali AGENTS.md applicabili. Riprendi dal codice
presente, senza ricominciare da zero e senza sovrascrivere modifiche locali.

Verifica prima stato Git, ramo, remote e commit. Repository personale:
https://github.com/Djdragonyyy/Ice_and_Fire_Fabric26.3_port.git
Ramo da usare: port/fabric-26.3. L'ultimo commit funzionale è b3c265a78
(0.1.11-stage1-dev); un commit successivo aggiunge questo prompt e l'handoff.
Verifica anche l'esistenza del commit base 417f5a8d2. Non fare reset distruttivi.

La 0.1.10 è stata confermata perfettamente funzionante dall'utente: nuovo fuoco,
anteprima nell'inventario e draghetti di stadio 1 in spalla. La 0.1.11 compila,
supera 45 test JUnit e 15 GameTest della mod; il client è stato avviato con risorse,
grafica, audio e atlanti caricati. Le nuove interazioni della 0.1.11 NON hanno
ancora una conferma utente in gioco: non trattarle come già collaudate visivamente.
Il baseline vanilla always_pass nei report GameTest non conta come test della mod.

La 0.1.11 aggiunge alimentazione manuale con carne, pasto malaticcio che blocca
la crescita, pasto creativo che addomestica/riassegna proprietà come nell'originale,
bastone dragon_stick, comandi libero/seduto/seguimi, seguito a terra e punto di casa
persistente con raggio predefinito 40 nella dimensione corretta. Testano anche
movimento reale, arresto, confini di casa e posa da dati sincronizzati.

Azioni immediate:

1. Verifica checkout e toolchain sul nuovo PC. Minecraft Java 26.3, JDK 25,
   Fabric Loader 0.19.5, Fabric API 0.163.0+26.3, Loom 1.18.4 e Gradle 9.7.1.
   Usa fabric/ come progetto Gradle indipendente, non il vecchio Gradle Forge.
   Non riutilizzare alla cieca i percorsi assoluti del vecchio PC.
2. Compila con .\fabric\gradlew.bat -p fabric build runGametest --console=plain.
   Il runner GameTest usa un mondo temporaneo e non è un server pubblico.
3. Se serve il collaudo della 0.1.11, ricostruisci i JAR e usa TEST-0.1.11.md.
   Non dichiarare prove visive o multiplayer che non hai realmente eseguito.
4. Prosegui la tappa 1 confrontando sempre i sorgenti Forge originali in src/.
   Prossimo blocco: volo autonomo e relativo flight manager, seguito in volo e
   ritorno al nido; poi combattimento autonomo e altri attacchi, cibo raccolto da
   oggetti a terra, interazioni/animazioni mancanti, sonno/riproduzione e stendardo.
   Pianifica e verifica i comportamenti prima di dichiararli migrati.

Vincoli permanenti:

- Tappa 1 INCOMPLETA: non passare alla tappa 2 senza completamento e riscontro.
- Difetto delle punte grigie ancora aperto. Le diagnosi sono in HANDOFF.md;
  non cambiare UV o riempire trasparenze alla cieca e non dichiararlo risolto.
- Il dorso solido derivato dal ghast felice è stato completamente rimosso per
  richiesta dell'utente: non reintrodurlo. Un solo pilota sul drago adulto.
- Conservare camera, controlli e funzionalità già confermate. I piccoli in spalla
  sono entità del mondo salvate indipendentemente dal giocatore; non aggirare il
  divieto della 26.3 di usare PLAYER come veicolo senza esaminare la persistenza.
- Conservare modelli, texture, statistiche e meccaniche originali dove possibile,
  licenza LGPL e attribuzioni. Distinguere limiti del port e comportamenti originali.
- Per ogni nuova versione consegnare SEMPRE due JAR installabili: la mod senza
  suffisso -sources e Fabric API 0.163.0+26.3, con istruzioni, sorgenti e checksum.
  Non mettere più versioni della mod nella cartella mods.
- Build, cache, JDK, mondi di prova e outputs del precedente PC NON sono nel Git:
  un pull trasferisce sorgenti e documentazione, non questi artefatti locali.
  Ricostruire la mod e recuperare la dipendenza esatta senza abbassare i requisiti.
- Revisionare il diff completo; usare review-agent se disponibile, in sola lettura
  e senza delega. Correggere fuori dalla revisione e rieseguire i controlli pertinenti.
- Commit e push sul solo ramo port/fabric-26.3 dell'origin personale sono autorizzati.
  Niente merge sul principale o release pubbliche. Non esporre email o credenziali;
  usare l'autore noreply verificato della storia senza cambiare config globali.
- Le skill del vecchio PC non si trasferiscono con Git: verificarne la disponibilità
  e non dichiarare usate skill o connessioni assenti. Nessuna delega automatica.
- L'ultimo comando eseguito prima della risposta finale deve mostrare 417f5a8d2:
  git show -s --format=%h --abbrev=9 417f5a8d2

Non fermarti alla sola proposta di un piano: esegui il lavoro autorizzato, verifica,
aggiorna l'handoff e consegna i risultati senza attribuire ai test prove non fatte.

---

Sul nuovo PC, se la repository è già presente e non ci sono modifiche da proteggere:

```powershell
git status --short
git switch port/fabric-26.3
git pull --ff-only origin port/fabric-26.3
```

Se il ramo non esiste localmente, fare prima fetch e creare il ramo che segue
origin/port/fabric-26.3. Non scartare modifiche locali per eseguire questi comandi.
