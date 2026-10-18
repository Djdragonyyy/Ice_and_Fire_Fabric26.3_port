# Prova parziale 0.1.2 — tappa 1 incompleta

## Installazione su CurseForge

Profilo Minecraft Java **26.3**, **Fabric Loader 0.19.5**, **Fabric API 0.163.0+26.3**,
runtime **Java 25**. Copiare nella cartella `mods` soltanto
`iceandfire-fabric-26.3-0.1.2-stage1-dev.jar` per questa mod, rimuovendo le versioni
precedenti. Non installare il JAR dei sorgenti, Ice and Fire Forge o Citadel Forge.
Usare un mondo nuovo creativo con trucchi attivi.

La build ordinaria e 17 test sono riusciti sul nuovo PC. Corretta un'oscillazione
verticale che spariva bruscamente all'atterraggio; non è una conferma di gameplay.
Client di sviluppo avviato con risorse caricate e ingresso in un mondo locale
confermato dai log; nessuna verifica visiva del drago eseguita dall'agente.
Il dettaglio della revisione è in `REVIEW.md`. Il checksum SHA-256 del JAR consegnato
è nel file `SHA256SUMS.txt` che accompagna la consegna.

## Prove richieste

1. Ottenere uovo e pasto, per esempio `/give @s iceandfire:dragonegg_red` e
   `/give @s iceandfire:dragon_meal 64`. Ripetere per green, bronze e gray.
   Piazzare l'uovo nel fuoco e attendere oltre 7200 tick effettivi (circa sei minuti
   a 20 tick/s). Il drago nato deve appartenere a chi ha piazzato l'uovo.
2. Far crescere con il pasto; montare a mano vuota da 50 giorni (stadio 3).
   Per fissare l'età di un singolo drago di prova già schiuso e di proprietà, usare
   `/data merge entity @e[type=iceandfire:fire_dragon,sort=nearest,limit=1] {AgeTicks:1200000,AgingDisabled:1b}`.
   Assicurarsi che il selettore identifichi il drago voluto. Per 75, 100 e 125 giorni
   sostituire AgeTicks rispettivamente con 1800000, 2400000 e 3000000.
3. A ciascuna età verificare camminata, virate, decollo tenendo salto, volo,
   discesa X (anche riassegnata), atterraggio e dismontaggio. Controllare che il
   cavaliere segua la seduta, soprattutto durante decollo e atterraggio.
4. Provare F5 e visuale prima/terza persona a terra e in volo, in spazio aperto
   e vicino ai blocchi. Annotare età, variante e situazione di ogni anomalia.
5. Salvare e riaprire durante incubazione e volo; controllare proprietà, età,
   variante e stato. Il test su server dedicato con due client rimane da eseguire.
6. Per le punte grigie: due screenshot dello stesso drago da lati diversi,
   annotando età, variante, shader, resource pack e altre mod di rendering.
   Il test sulle sei facce/UV delle 22 punte è solo geometrico: difetto ancora aperto.

R/G trasmettono input ma gli attacchi non sono implementati. Combattimento, soffio,
IA/volo autonomo, interfacce e altre funzioni elencate nell'HANDOFF restano da migrare.
La tappa 2 non è iniziata. Attendere il riscontro di questa prova prima di estendere
il port e non dichiarare completata la tappa 1.

## Codice e ricompilazione

Il file principale modificato è `FireDragonPoseModel.java`; il bob del corpo durante
il volo ora viene moltiplicato per `state.flyProgress / 20F`. Il nuovo test
`flightBobFadesContinuouslyAtTakeoffAndLanding` falliva prima della correzione e
ora passa. Il mixin del cavaliere e la logica della camera sono stati revisionati.

Estrarre l'archivio completo dei sorgenti e selezionare JDK 25. Dalla radice:

```powershell
& "$env:JAVA_HOME\bin\java.exe" -version
.\fabric\gradlew.bat -p fabric build --console=plain
```

La prima compilazione scarica Gradle e le dipendenze; non richiede la cache del vecchio PC.
Aprire `fabric/` come progetto Gradle. La cartella `src/` conserva il riferimento Forge.
Codice e asset originali LGPL-3.0; licenza e attribuzioni incluse nella repository e nel JAR.
