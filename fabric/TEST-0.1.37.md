# Prova 0.1.37 - corpo, coda e ali

Checkpoint di sviluppo: tappa1 e port completo ancora aperti. Nessuna sospensione
o spegnimento del PC.

## Modifiche

Portati i movimenti secondari originali di DragonTabulaModelAnimator:
oscillazioni di corpo e arti, catene di collo/coda durante idle e camminata,
inclinazione del corpo e contro-onda della coda in volo, onde di collo e dita,
movimenti opposti delle cosce. Sonno con frequenza/ampiezza originali ridotte.
La fase di ogni giunto e rootOffset*PI/(2*lunghezzaCatena), non rootOffset in
radianti per ogni giunto come prima. Il bob include l'offset verticale originale
(sin(tempo*velocita)-1)*ampiezza*quantita, omesso nella versione precedente.

I movimenti secondari sono applicati dopo le pose, cosi anche il sonno conserva
il suo respiro. Adattamento intenzionale: interpolazione fra movimento a terra
e in aria durante le progressioni di decollo/atterraggio, mantenendo i coefficienti
originali a regime. La dissolvenza evita salti nel corpo/aggancio del cavaliere;
non si dichiara copia letterale dello switch istantaneo del vecchio renderer.

Ripristinato isWing originale: armL1/armR1 e i loro figli diretti, compresi
membraneL6/membraneR6. La camminata non aggiunge rotazioni a quei sei giunti
durante wing blast/ruggito epico, ma continua sulle zampe. Hover e tackle
conservano il ciclo alare, evitando le sovrapposizioni di posa precedenti.
Nessuna modifica a mesh, UV, PNG, collider, statistiche, controlli o payload.
Breath shake, IK delle zampe, buffer di roll/pitch e altre parita restano aperti.

Riferimenti originali nella repository: client/model/animator/DragonTabulaModelAnimator.java
(velocita/ampiezze/catene e isWing). Formule Citadel:
[AdvancedEntityModel](https://github.com/AlexModGuy/Citadel/blob/master/src/main/java/com/github/alexthe666/citadel/client/model/AdvancedEntityModel.java),
[AdvancedModelBox](https://github.com/AlexModGuy/Citadel/blob/master/src/main/java/com/github/alexthe666/citadel/client/model/AdvancedModelBox.java).

## Verifiche

Suite completa build/compilazione/test/server/client superata al primo tentativo
in9m05s: **84JUnit,119GameTest mod (120 con baseline),11prove client**.
Quattro nuove prove unitarie con riferimenti numerici indipendenti: fase originale
della coda e contro-bob, corpo/collo/coda/dita in volo, giunti alari durante
hover/tackle, ali protette ma zampe in movimento durante WING/EPIC_ROAR.
Le aspettative sleep/hover ora includono le ampiezze originali effettive;
non sono state indebolite le tolleranze. Restano verdi agganci/scala reale del
cavaliere, reset, continuita ai confini e feeding nei cinque stadi.

33immagini nella suite ordinaria. Controllate le dieci tavole feeding/corpo/ali
nei cinque stadi e le tre di volo/attacchi. Quattro fotogrammi di volo distribuiti
in due tavole alte per mostrare interamente le membrane anche verso il basso;
una tavola confronta wing blast/ruggito epico con e senza camminata.
Le tavole usano dimensioni normalizzate e render-state di test; il feeding prima
delle tavole rimane un'interazione C2S reale. Dopo la correzione delle sole
inquadrature e stata ripetuta l'intera suite client (1m51s), codice produttivo
invariato. build-e-test.log unisce la build completa e quest'ultima esecuzione,
con intestazione esplicita della seconda verifica; XML/JUnit della suite completa.

Rieseguito anche l'harness TCP della0.1.35 sulla versione corrente: GameTestServer
standalone locale e due client con rendering in processi separati, profili
fittizi, loopback.1test server aggiuntivo passato, XML senza errori; tutti tre i
processi terminati con codice0. Proprietario pilota, ospite respinto da montaggio
/comandi, entrambi ricevono sesso, UUID, velocita server e head/tail. Verificate
immagini proprietario in prima/terza persona e osservatore ravvicinato:3immagini
correnti,36complessive. Versione0.1.37 controllata nei tre log prima della raccolta.

network-test-results contiene soltanto la whitelist di13file:6log, report JSON,
manifest exit code, XML, source-version e3PNG correnti. Piani di lancio/UUID di
coordinamento esclusi. Nel riuso era presente un'immagine storica: esclusa da
questa consegna, senza alterare il vecchio archivio. Procedura di avvio in
TEST-0.1.35.md; stessi sorgenti opt-in, assenti dal JAR distribuito.

## Ancora da portare

Carcassa persistente/raccolta e XP originali per stadio sono un blocco importante
ancora assente: attualmente morte vanilla e drop dell'inventario. Seguiranno
multipart e altre interazioni/configurazioni. DedicatedServer ordinario,
account autenticati e persistenza multiplayer prolungata restano da verificare.
Il precedente contrattacco intermittente e il caso escort0->1 non sono falliti
qui ma restano senza causa dimostrata; diagnostica mantenuta. Punte grigie non
dichiarate risolte. Nessuna fase completa viene chiusa con questa consegna.