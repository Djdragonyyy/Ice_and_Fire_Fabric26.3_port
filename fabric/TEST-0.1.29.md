# Prova 0.1.29 — ruggiti e battito d'ali

Checkpoint parziale della tappa 1, Minecraft Java 26.3 / Java25 / Fabric.
Non è il completamento della tappa né del port completo. Nessuna sospensione o spegnimento.

## Modifiche

- Ruggito normale40tick ed epico60tick, tre voci originali per fascia d'età,
  sei pose Tabula originali e sequenze/interpolazione/pitch aggiuntivo del corpo.
- Avvio casuale1/500 mentre libero da attività prioritarie e reazione1/4 al danno
  da entità. Attacchi, prede, pasto, sonno, NoAI, spalla e pilota hanno precedenza;
  una richiesta ripetuta non riavvia il ruggito corrente. La voce SPEAK non lo sovrascrive.
- Aura degli stadi4/5: stessa zona originale expandTowards positiva di30/60blocchi,
  forza al proprietario e ai suoi pet, debolezza agli altri viventi; draghi di
  stadio uguale/superiore esclusi. Durate originali900/1800tick normali e
  1500/3000epiche. Riconosciuto l'ID earplugs nel solo ramo epico, come originale;
  l'oggetto stesso non è ancora migrato e appartiene ai contenuti della fase3.
- Battito d'ali: quattro OGG originali e suono al ciclo2, una volta per ciclo30tick,
  solo broadcast server. dragonFlapNoiseDistance default4, limite0..10000; zero
  lo silenzia. JSON precedenti mantengono il default. Nessun cambio ai controlli.
- Categoria audio HOSTILE originale, invece della categoria NEUTRAL ereditata.

## Verifica

Le tre nuove prove server isolate sono passate: durata/riavvio/NoAI, aura con
proprietario/pet/altre creature e draghi più grandi agli stadi4/5, assenza dell'aura
nei giovani e precedenza combattimento/sonno. Due prove delle pose verificano
sequenze, ritorno alla posa iniziale e continuità ai sei confini. Confronto
byte-per-byte degli OGG e uguaglianza delle definizioni audio originali.

Suite completa finale ripetuta dopo la correzione HOSTILE: **73JUnit,
102GameTest mod (103 con baseline vanilla),7prove client e17immagini**, tutte
passate. Log integrale phase29-final.log, build riuscita in8m18s. XML server con
103casi e zero errori. Nuovo client verifica suoni risolti, ruggiti normale/epico
e forza sul proprietario connesso; due immagini controllate visivamente.
Non è una prova con due client reali, né un ascolto manuale dell'audio.

## Prova in gioco

Installare solo il JAR mod0.1.29 e Fabric API0.163.0+26.3, rimuovendo il JAR
precedente. Usare un mondo di prova, conservando una copia dei mondi importanti.
Osservare il drago fermo e in volo alle varie età, audio e pose dei ruggiti e
battiti d'ali. Negli adulti verificare gli effetti sul proprietario e sulle
creature nella zona positiva originale; non è un'aura sferica simmetrica.
Controllare che comandi, combattimento, cavalcatura, flauto/corno/cristallo e
regole del dismount restino funzionanti. Il difetto delle punte grigie resta aperto.

## Consegna

Pacchetto finale: due JAR installabili (mod e Fabric API), sorgenti del commit,
report, log, immagini e SHA-256. Commit/push solo su port/fabric-26.3.
Restano acqua/lava, navigazione complessa, altre parità originali e collaudo
multiplayer dedicato; nessuna dichiarazione di tappa1 completata.
