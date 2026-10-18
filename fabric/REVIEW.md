# Revisione del port Fabric

## Revisione finale 0.1.2 sul nuovo PC — 18 ottobre 2026

Skill locale `review-agent` applicata in sola lettura, senza delega, all'intero diff
`d0a80e73f..417f5a8d2`, inclusi documenti, nuovi file, test e mixin client. Confrontati
animatore, layer del cavaliere e curve della seduta con i sorgenti originali in `src/`.
Verificata anche nel bytecode vanilla 26.3 la moltiplicazione CAMERA_DISTANCE × scala
del veicolo e la sottrazione dell'attachment del passeggero.

- [P2] Attenuare il bob del volo durante la transizione —
  `fabric/src/client/java/com/github/alexthe666/iceandfire/fabric/client/FireDragonPoseModel.java:111`.
  Con `flying=false` e un progresso di volo ancora positivo, il corpo riceveva tutta
  l'oscillazione da 2,5 unità del modello. A progresso zero l'oscillazione spariva in un
  fotogramma: a 10 tick lo scarto era circa 2,27 unità, amplificato dalla scala visiva
  e trasferito alla seduta animata. Difetto introdotto dalla nuova animazione secondaria.

Correzione eseguita dopo la fase di revisione: moltiplicare il bob per `flyProgress / 20`.
Il test `flightBobFadesContinuouslyAtTakeoffAndLanding` falliva prima della correzione
e passa dopo, verificando corpo e aggancio del cavaliere vicino al confine e l'inizio
del decollo in tre fasi dell'oscillazione. Rilettura della correzione: No findings.
Non sono emersi altri difetti concreti nel diff esaminato.

Build ordinaria `fabric/gradlew.bat -p fabric build --console=plain` riuscita con
Azul Zulu JDK 25.0.4.1+1 e Gradle 9.7.1, senza init script diagnostici o cache preparata
manualmente. Suite completa: **17 test, 0 fallimenti, 0 errori**. Rimangono avvisi
di deprecazione di API/Gradle; non impediscono la build.

I test automatici non validano la resa delle punte grigie né la prima persona.
Client di sviluppo avviato sul nuovo PC: mod, backend grafico, audio e atlanti caricati;
i log confermano anche l'ingresso nel mondo locale `New World`. Nessuna interazione
con il drago o verifica visiva eseguita dall'agente. Client lasciato aperto per la prova.
Avvisi/errore diagnostico OSHI sui contatori prestazioni Windows e autenticazione
Realms del profilo di sviluppo presenti nel log; non hanno impedito il mondo locale.
Restano necessari test in mondo a 50/75/100/125 giorni, persistenza e multiplayer reale.
Le punte grigie sono ancora aperte; la tappa 1 è incompleta.

## Revisione del primo checkpoint (storica)

## Correzione comandi 0.1.1

Skill `review-agent` applicata in sola lettura al diff della correzione, inclusi test e traduzioni.
No findings.

Verificati i codici SDL di Minecraft 26.3, la registrazione tramite Fabric, il percorso input
client/pacchetto/controllo verticale e la categoria con le sue traduzioni. Otto test superati:
i due nuovi test verificano X/R/G, riassegnazione, rilascio, disattivazione e serializzazione
del tasto di discesa. Build eseguita con lo stesso init script locale documentato nel README.
La visibilità nel pannello comandi, la conservazione dopo riavvio e la discesa effettiva in un
mondo richiedono ancora la prova dell'utente con questo JAR. Nessuna dichiarazione di completamento
della tappa 1.

Skill applicata: `review-agent`, dopo il primo ciclo di test. Revisione in sola lettura del diff
rispetto alla base `0526aad94119b705a86207623481b087c4d052ca`; nessuna delega della revisione.

## Risultati iniziali e correzioni

- [P1] Registrare i renderer delle nuove entità — `src/client/java/com/github/alexthe666/iceandfire/fabric/client/IceAndFireFabricClient.java:6`.
  Le entità registrate senza renderer non possono essere visualizzate dal dispatcher client.
  Corretto con renderer dedicati e caricamento del modello originale.
- [P2] Conservare le dimensioni originali dell'uovo — `src/main/java/com/github/alexthe666/iceandfire/fabric/IafContent.java:40`.
  La prima implementazione usava 0,6 × 0,8 invece di 0,45 × 0,55, alterando collisioni e piazzamento.
  Corretto confrontando `IafEntityRegistry` originale.
- [P2] Trasferire il nome personalizzato all'uovo piazzato — `src/main/java/com/github/alexthe666/iceandfire/fabric/IafContent.java:58`.
  Il piazzamento perdeva il componente del nome prima della schiusa. Corretto copiando `CUSTOM_NAME`.

I riferimenti sopra identificano il primo diff revisionato, prima delle successive aggiunte.

## Rischi residui

Sei test automatici superati. Client e server hanno superato soltanto i controlli iniziali descritti
nel README. Nessun mondo è stato usato per collaudare l'entità, la persistenza o il controllo in rete.
La build ordinaria nel sandbox rimane bloccata dal problema ZIP del runtime Windows.
Le funzioni ancora da migrare sono elencate nel README e impediscono di considerare conclusa la tappa 1.
