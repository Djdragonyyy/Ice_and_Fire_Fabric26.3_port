# Prova 0.1.51 - distanze originali della camera del drago

Checkpoint verificato del27ottobre2026; tappa 1 e port ancora aperti.

## Uso

F5 continua a scegliere prima persona, terza posteriore e terza frontale.
Il nuovo comando F7, rimappabile nei controlli del drago, cambia invece la
distanza durante la cavalcata: normale, corta, media, lunga e poi normale.
Alla monta del proprio drago addomesticato viene scelta la distanza media,
come nell'AUTO_ADAPT_3RD_PERSON originale. L'opzione dragonAuto3rdPerson della
0.1.50 controlla il tipo di prospettiva; F7 controlla separatamente la distanza.
La prima persona e la camera quando non si cavalca un drago restano native.

Sorgente originale: ClientEvents.onCameraSetup/onEntityMount e IafKeybindRegistry.
Le distanze supplementari sono 0, 1.2, 3 o 5 moltiplicati per renderSize/3.
Si usa la taglia visiva originale, senza il limite della scala fisica. Non
sono cambiate geometria, texture, seduta o dimensioni fisiche del drago.

## Adattamento 26.3

La camera nativa considera gia sia la scala sia CAMERA_DISTANCE del giocatore
e del veicolo. Questo valore viene conservato e sommato alla distanza originale
supplementare. Un'iniezione client limitata all'argomento getMaxZoom di
Camera.alignWithEntity mantiene il controllo collisioni nativo su tutta la
distanza, anziche aggiungere un movimento dopo il suo controllo.
Davanti a un muro la visuale si accorcia: in uno spazio stretto non puo mostrare
contemporaneamente l'intero drago e restare davanti all'ostacolo.

Il vecchio codice GLFW296 viene sostituito dal simbolo SDL nativo KEY_F7.
Il comando avanza per pressione, anziche scorrere venti volte al secondo
tenendolo premuto. Non vengono modificati attributi del drago, rete o server.

## Verifiche

Regressione completa phase51-final.log verde in 11m 38s:99JUnit,162GameTest
mod+baseline163server,18client. Dopo la guardia di proprieta client, build e
tutti18client ripetuti in phase51-client-final-2.log verde in 2m 48s.99JUnit
verdi; la suite server resta quella precedente sulle stesse classi comuni.
54PNG correnti, sei nuove finali aperte e riviste. Logcombinato conserva
entrambi gli esiti. JAR:116classi identiche,2classi client modificate e2aggiunte.

Nuova prova con camera reale e input del framework: quattro distanze a50/75/
100/125giorni, mantenimento F7 per20tick, vista media posteriore per tutte le
taglie, frontale a125giorni, muro a sei blocchi e recupero dopo la rimozione,
prima persona senza distanza extra e ritorno al valore nativo dopo discesa.
Il confronto usa la posizione effettiva della camera rispetto agli occhi del
giocatore e le taglie originali7/12.5/20/30, non il valore restituito dall'helper.

Il primo probe passava distanze, crescita, muro e prima persona, ma falliva
l'ultima verifica dopo discesa: la scelta manuale precedeva il listener di
ripristino. Fixture corretto attendendo10tick; asserzione sulla distanza
conservata. Secondo probe verde2m45s con99JUnit/18client/54PNG.
Raggio del terreno del solo nuovo mondo di prova portato a6chunk, per evitare
immagini prese oltre il terreno caricato quando la camera e lontana; valore
iniziale ripristinato alla fine. Non cambia il raggio del gioco distribuito.

Durante revisione allineato all'originale il reset della distanza media alla
monta: solo drago addomesticato posseduto, non una presa di un drago selvatico.
La modifica e esclusivamente client; il collaudo finale client viene ripetuto
dopo questo controllo di proprieta, mantenendo la suite server gia verificata
con le stesse classi comuni. La presa selvaggia non e un nuovo caso client
certificato da questo test.

Nessun nuovo riavvio di server dedicato e nessuna correzione dichiarata delle
punte grigie. Le sei immagini sono prove di camera; non una nuova ricostruzione
del modello o un confronto pixel per pixel con Forge1.20.
