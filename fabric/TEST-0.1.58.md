# Prova 0.1.58 - passeggeri nell'anteprima dell'inventario

Checkpoint del28ottobre2026. Tappa1 e port ancora incompleti.

## Modifica

Il renderer originale aggiungeva LayerDragonRider anche al ritratto GUI.
Il port posizionava correttamente i passeggeri nel mondo ma il helper nativo
dell'inventario estraeva solo il drago. La caratterizzazione privata57 ha
aperto il menu con un proprietario realmente montato e verificato che il
ritratto non lo mostrava, mantenendo intatta la relazione nativa di veicolo.

La nuova Mixin intercetta soltanto l'estrazione del helper dell'inventario,
prima della sua normalizzazione e rotazione per il mouse. Snapshot freschi
del pilota o della preda vengono renderizzati nello stesso collector3D e
con la stessa profondita del drago, usando le ancore gia presenti nel port.
La scala del passeggero viene divisa per quella visiva reale del drago una
sola volta; la trasformazione della preda riceve la scala reale come parametro
esplicito e non cambia lo stato padre. Ombre, nomi duplicati, outline e
offset del mondo non sono trasferiti alla GUI. La posa in prima persona
del mondo non tronca il drago nell'inventario. Posizioni, rotazioni, AI,
relazioni di veicolo, geometrie, UV, texture e messaggi di rete restano intatti.

## Prove effettive

DragonInventoryClientTest usa un proprietario nativo realmente montato,
DragonMenu aperto sul server, camera in prima persona e screenshot del
ritratto; poi vero smontaggio e verifica dell'assenza di copie residue.
Una fixture di test chiama il percorso produttivo tryGrabPrey, senza
allargarne la visibilita nella mod, per zombie, mucca e cavallo reali.
Attende la fase nativa della presa oltre18tick e fotografa le tre varianti
alla bocca. Una sesta immagine ingrandisce il cavallo con lo stesso helper
nativo: e solo una vista di test, non una nuova schermata del prodotto.
Tutte le relazioni vengono rilasciate normalmente dopo il riscontro.

Un observer Mixin confinato al mod di test confronta posizione, rotazioni,
bounding box, veicolo, eta e fame prima e dopo le estrazioni effettive;
controlla scala unica, separazione fra snapshot GUI e mondo e assenza di
vecchi passeggeri. Non vengono usati contatori produttivi o mosse imposte
alle entita per ottenere l'immagine. La camera originale viene ripristinata.

Compilazione iniziale verde47s,100JUnit. Il primo avvio runtime falliva
l'overload ambiguo della Mixin: corretto col descrittore completo verificato
nel JAR. Focus2 verde38s; nel focus3 la vista ingrandita di test metteva in
pausa il server e la pulizia della presa scadeva. Corretto il solo Screen
di test, senza cambiare la produzione. Focus4 passa in36s con100JUnit,
una prova client e6PNG riviste. Le immagini delle prove mirate sono state
preservate privatamente prima del run finale per evitare file residui.

Full pulito phase58-final.log PASS13m 2s,exit0:100JUnit,
20prove client/62PNG e183testserver(182mod+baseline), XML senza errori.
Il client modificato viene eseguito prima del server. Include le vecchie
prove di feeding/corpo/ali, armature, camera e materiali, piu la nuova GUI.
Confronto JAR57->58:122classi identiche,4differenti e3nuove, tutte client.
PreyRenderPose ha codice/struttura javap identici: differenza di metadati.
Tutte le classi comuni identiche. Nessun observer, bridge o Mixin di test
nel JAR; i sorgenti dei test sono nell'archivio sorgenti.

## Limiti

Non e una nuova prova con client autenticati o riavvio dedicato. Il caso52
resta distinto. Non cambia le geometrie o risolve le punte grigie: serve
il confronto equivalente col renderer originale, non solo identita degli
asset. Restano navigazione avanzata/scavo forzato e audit delle configurazioni
e del ciclo vitale. Non viene dichiarata completata la tappa1 o il port.
