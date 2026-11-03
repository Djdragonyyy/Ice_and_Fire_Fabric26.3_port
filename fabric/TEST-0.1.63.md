# Prova 0.1.63 - protezioni originali dai danni

Ripristinati nel corpo principale i rami espliciti di EntityDragonBase.hurt:
nessun danno da IN_WALL, FALLING_BLOCK o CRAMMING; nessun colpo dal pilota
attuale; durante il trasporto nessun danno da frecce o dal proprio veicolo.
Per i cuccioli sulla spalla si usa la relazione shoulderCarrier del port,
che conserva entita indipendenti nel mondo. Le hitbox delegano al genitore.
La protezione termina immediatamente smontando o rilasciando la spalla.
Nessuna immunita generale del proprietario o finestra dopo lo smontaggio.
Il ramo amministrativo delle carcasse resta prima di queste protezioni.

Tre nuove prove native: cinque stadi/corpo/hitbox e sonno invariato dai
colpi respinti; danno ordinario e ANVIL (tipo distinto) ancora accettati;
pilota reale e colpi alla testa, proprietario dopo dismount e altro attaccante;
vero trasporto sulla spalla, frecce, portatore, melee estraneo e rilascio.
NoAI isola i fixture, senza nuovi seed o modifiche all'AI. Baseline sul codice62:
3prove fallite per le tre omissioni, baseline vanilla verde. Stesse prove dopo
la correzione PASS35s:101JUnit4server(3mod+baseline), senza cambiare i fixture.

Full pulito phase63-final.log PASS13m 6s:101JUnit189server
(188mod+baseline)20client66PNG, XML senza errori. JAR62->63:128classi identiche,
2differenti, nessuna aggiunta/rimozione. FireDragon$6 ha codice/struttura javap
identici e metadati diversi; solo FireDragon cambia comportamento. Tutte
le classi client identiche62, nessun test nel JAR. Tappa1/port incompleti.

Nessuna nuova prova di riavvio dedicato o autenticazione;52 resta distinta.
