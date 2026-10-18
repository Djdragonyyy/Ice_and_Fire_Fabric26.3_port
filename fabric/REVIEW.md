# Revisione del port Fabric

## Volo e visibilità 0.1.4 — 18 ottobre 2026

Skill `review-agent` applicata in sola lettura, senza delega, al diff completo dalla
0.1.3 (`284f4d25e`), inclusi nuovo stato di decollo, test e traduzioni. No findings.
Letti i percorsi input client, payload, verifica proprietario, aggiornamento FLYING,
simulazione del veicolo e trasformazione di rendering. Verificati con le classi
vanilla locali la lettura di spazio e il momento della simulazione cavalcata.

Correzioni: avvio del volo prima di `super.tick`, richiesta V memorizzata anche se
arriva il rilascio prima del tick server; impulso applicato dal simulatore e non
consumato prima del distacco. Un contatto a terra momentaneamente perso non elimina
la spinta iniziale. Mantiene il volo già in corso senza ripetere impulsi in aria.
Richieste annullate senza proprietario/controlli recenti, allo stadio errato o con
discesa premuta; attesa breve sul dorso occupato e nessuna richiesta conservata
indefinitamente. Il nuovo bit TAKEOFF richiede la stessa 0.1.4 su server e client.

Il tilt positivo usa un limite graduale in funzione della distanza dai blocchi
calcolata con nove raycast sotto corpo e collo/testa; forme COLLIDER e fluidi ignorati.
La distanza di attenuazione è max(2 blocchi, renderSize / 4). La ricerca avviene a
20 Hz, solo quando il tilt è positivo, e non modifica collisioni fisiche o blocchi.
La prima persona disattiva il tilt solo se la camera è quella del pilota locale;
la seduta renderizzata usa lo stesso stato. Camera in terza persona invariata.

Build ordinaria JDK 25 riuscita: **28 test, 0 fallimenti, 0 errori**. Nuovi controlli
su pressione breve V e rimappatura, pressione/rilascio tra due tick server, spazio
continuo, richiesta bloccata/scaduta, tasto tenuto durante atterraggio, stadio/rilascio
controlli, impulso con contatto perso, curva continua di raddrizzamento a ogni età
cavalcabile e tilt locale di prima persona. Restano tutti i test precedenti.

Limiti: i test matematici non sostituiscono un collaudo del rilevamento di terreno,
gradini e sporgenze reali; non è stata misurata la prestazione con molti draghi.
I raycast campionano il corpo e non sono una collisione esatta del modello completo.
Serve prova utente di decollo, prima persona e atterraggio a 50/75/100/125 giorni,
oltre al test multiplayer reale. Nessuna dichiarazione di tappa 1 completata.
Avvio client 0.1.4 verificato in directory separata: mod, grafica, audio e atlanti
caricati. Client lasciato aperto per la prova; nessuna verifica visiva o interazione
del drago eseguita dall'agente. Avvisi OSHI/Realms del profilo di sviluppo già noti.

## Correzioni e dorso solido 0.1.3 — 18 ottobre 2026

Riscontro utente sulla 0.1.2: camera e comandi di schiusa funzionanti; segnalati decollo
incerto, assenza di inclinazione in discesa con X e cavaliere dentro al modello allo
stadio 5. Richiesto esplicitamente il dorso solido come quello del ghast felice.

Skill `review-agent` applicata in sola lettura, senza delega, a tutto il diff dalla
0.1.2 (`7e4c9fae5`), inclusi i nuovi helper/test. Confrontati `EntityDragonBase.updatePitch`,
`RenderDragonBase.scale`, geometria Tabula originale e bytecode vanilla 26.3 di
Entity/EntitySelector, HappyGhast, LivingEntityRenderer e controllo del movimento.

- [P1] Gestire le query di collisione senza richiedente —
  `fabric/src/main/java/com/github/alexthe666/iceandfire/fabric/dragon/FireDragon.java`,
  `canBeCollidedWith`. La prima implementazione chiamava `isPassengerOfSameVehicle`
  anche con argomento null. Il predicato vanilla di EntitySelector chiama proprio
  `canBeCollidedWith(null)` e quel percorso avrebbe generato NullPointerException.
  Corretto dopo la revisione con guardia null prima del confronto del veicolo.

Rilettura della correzione: No findings. Non rilevati ulteriori difetti concreti.

Implementazione: impulso iniziale di decollo sul lato che simula il movimento del
veicolo; inclinazione da spostamento verticale con limiti originali -60/+40 gradi,
smorzamento e stato sincronizzato per gli osservatori; identica rotazione sul modello
e sulla seduta. La seduta deriva ora dalla superficie del cubo Neck1, anziché dal
perno interno. Compensazione dell'altezza del bacino legata alla scala del giocatore,
separata dalla scala visiva non limitata del drago. Distanza camera non modificata.

Collisione solida sull'AABB centrale di tutti i draghi di fuoco registrati, passeggeri
esclusi; pausa del movimento mentre un giocatore non montato sta sopra e per dieci
tick dopo. Spettatori e giocatori senza collisione non bloccano il drago. Nuova funzione
di gameplay autorizzata dall'utente, non proveniente dalla base Ice and Fire originale.
Usate API vanilla: nessun nuovo mixin comune né nuova dipendenza.

Build ordinaria con JDK 25: **21 test, 0 fallimenti, 0 errori**. I nuovi controlli
coprono superficie effettiva del modello e bacino a 50/75/100/125 giorni, inclinazione
-60/0/+40 gradi e scala del giocatore 0.5/1/2, limiti/smorzamento dell'inclinazione,
impulso di decollo e riconoscimento dei piedi sopra il dorso.

Avvio della nuova 0.1.3 verificato dai log in una directory di prova separata:
mod, modello/risorse, grafica, audio e atlanti caricati; ingresso in un mondo locale
confermato. Client lasciato aperto. Nessuna interazione del drago o verifica visiva
eseguita dall'agente; presenti gli avvisi OSHI/Realms del profilo di sviluppo già noti.

Queste sono verifiche automatiche; non equivalgono a una prova interattiva del volo,
del dismontaggio o dell'appoggio in un mondo/server con due giocatori. La superficie
solida segue la collisione centrale, non la geometria animata di ali/coda. Resa della
seduta e del tilt da confermare in gioco. Punte grigie ancora aperte, tappa 1 incompleta.

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
