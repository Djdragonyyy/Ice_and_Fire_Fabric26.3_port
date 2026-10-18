# Revisione del primo checkpoint

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
