# Prova 0.1.36 - feeding e audit del modello

Checkpoint parziale della tappa1. Il port completo resta aperto.
Non sospendere o spegnere il computer.

## Comportamento

Il feeding manuale ora avvia EAT sul server, usando lo stesso stato sincronizzato
visibile agli osservatori e gia usato per il cibo a terra. Carne e pasti conservano
consumo, crescita, fame, cura, particelle e suoni esistenti. I clic ravvicinati
non riavviano continuamente il primo fotogramma. Nessun blocco aggiunto all'IA:
NoAI, sonno, volo/hover, cavaliere, trasporto sulla spalla e combattimento hanno
precedenza. Un ciclo interrotto viene annullato e non riappare dopo il pericolo.

Collo e mandibola usano le ampiezze originali di18gradi e traslazione mandibola
0.2, con interpolazione sinusoidale Citadel. Ritorno completo entro20tick.
E' un miglioramento visivo richiesto: l'originale definisce20tick ma include
un ultimo segmento oltre tale durata e non attivava EAT nel feeding manuale.
Il precedente port aveva gia un recupero di20tick, ma interpolazione lineare.
Non viene dichiarata riproduzione letterale di quel segmento originale irraggiungibile.

## Corpo e ali

Esteso l'audit indipendente da22punte a TUTTI i cubi del modello ground originale:
vertici locali, sei facce, normali, winding, mirroring e UV confrontati con il layout
Tabula/Citadel. Verificate anche presenza di corpo e braccia alari e copertura
integrale dei cubi originali. La geometria e i PNG erano gia quelli originali:
nessuna ricostruzione arbitraria, riempimento alpha o spostamento UV applicato.

Dieci nuove tavole GPU: cinque feeding a0/5/10/20tick e cinque con corpo/ali
laterali, posteriori, seduti e in volo. Ogni stadio usa la sua texture originale;
le dimensioni nelle tavole sono NORMALIZZATE per confronto, non mostrano la
crescita fisica in scala reale. Fotogrammi delle tavole impostati nel solo
render-state di test; per ciascuno stadio viene prima verificata un'interazione
client reale con consumo/cura server ed EAT ricevuto dal client. Nessuna
mutazione delle entita di produzione per simulare il risultato grafico.

## Validazione

Suite completa superata:80JUnit,119GameTest mod (120 con baseline vanilla),
11prove client e30immagini. BUILD SUCCESSFUL in8m39s; report senza fallimenti.
Le dieci nuove tavole finali sono state controllate visivamente.
Log finale: phase36-final-3.log. Il primo test mirato ha usato erroneamente un difensore
in comando seduto, che annulla correttamente il bersaglio; corretto il fixture
in modalita libera, senza cambiare il comportamento o indebolire le asserzioni.

Corretto anche un fixture preesistente: la prova di cancellazione schedula un
richiamo esplicito al tick35 ma lasciava attivi i richiami spontanei. Un richiamo
gia in corso non viene riavviato e puo finire prima della verifica al tick40.
Il fixture ora isola il richiamo esplicito tramite ambientSoundTime nativo;
asserzioni di danno, bersaglio e animazione conservate. La prova delle voci
verifica anche una chiamata sovrapposta al tick15 senza riavvio del ciclo.

Nuovo test unitario: pose seduta/in piedi in tutti5gli stadi, ampiezze originali,
confini continui, ritorno alla posa iniziale e corpo/ali invariati durante EAT.
Due nuovi GameTest nativi: alimentazione5stadi, consumo/stats, frame sincronizzato,
clic ripetuti, fine ciclo e precedenza NoAI/volo/cavaliere/bersaglio difensivo.
Nuova prova client: interazione C2S reale e sincronizzazione nei cinque stadi,
seguita dalle dieci tavole deterministiche. Test esclusi dal JAR distribuito.

## Ancora aperto

La verifica dei due client TCP della0.1.35 rimane evidenza di quella versione:
non viene ri-etichettata come nuova esecuzione di rete0.1.36. DedicatedServer
ordinario/account autenticati e persistenza multiplayer prolungata restano aperti.
Un primo tentativo completo ha fallito il vecchio caso di accelerazione escort
dopo cambio0->1; la prova isolata e la suite completa successiva passano.
Causa non provata, nuova diagnostica di percorso/collisioni conservata e nessuna
modifica allo steering o alle asserzioni per mascherarlo.
Punte grigie e contrattacco intermittente non dichiarati risolti. Restano altre
parita della tappa1 e le tappe successive.

Prossimo intervento: movimenti secondari originali di corpo/collo/coda e assetto
in volo. Confermata nel codice upstream Citadel la differenza di fase delle
catene e l'offset verticale bob omesso nel port; nessuna correzione di questi
movimenti e inclusa in questo checkpoint. Riferimenti:
[AdvancedEntityModel](https://github.com/AlexModGuy/Citadel/blob/master/src/main/java/com/github/alexthe666/citadel/client/model/AdvancedEntityModel.java),
[AdvancedModelBox](https://github.com/AlexModGuy/Citadel/blob/master/src/main/java/com/github/alexthe666/citadel/client/model/AdvancedModelBox.java).
