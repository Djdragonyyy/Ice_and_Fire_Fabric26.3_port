# Prova 0.1.30 — navigazione aerea del corpo completo

Checkpoint parziale della tappa1. Nessuna sospensione o spegnimento del PC.

## Modifiche

Il precedente controllo verificava il corpo solo all'arrivo e un raggio centrale
lungo il tragitto: un tronco laterale poteva essere invisibile al raggio ma
bloccare lo stadio5. Ora ogni tratto considera l'intero volume fisico del drago,
con dimensioni originali, quote/bordo del mondo e tutti i chunk attraversati
già caricati. Nessun caricamento nuovo per questa IA e nessuna riduzione della taglia.

Se il tratto diretto non passa, prova percorsi limitati sopra o ai lati, con
controllo di ogni tratto e della quota autonoma configurata. Cache per drago,
ricontrolli periodici e invalidazione per ostacoli/comandi/bersagli; un percorso
valido non ricomincia la risalita ogni20tick. Frenata in prossimità dei cambi
di direzione. Le collisioni native restano responsabili del movimento fisico,
compresa la conservazione degli assi liberi quando un altro asse incontra un muro.

Le intenzioni lontane, come il ritorno a casa, avanzano per tratti locali32blocchi
quando distano oltre64, limitando il costo dei controlli del volume. Questi punti
intermedi non riducono la velocità di crociera. Nessuna ricerca illimitata e
nessuna destinazione inventata dentro una stanza chiusa.

## Verifica

Sette nuove prove native isolate passate: ostacolo laterale a metà percorso che
il vecchio raggio manca ma il corpo adulto colpisce (un piccolo passa realmente),
deviazione e aggiornamento davanti a un ostacolo aggiunto, chunk non caricati e
coordinate/quote invalide, stanza chiusa, intenzione lontana e seguito reale
stadio5 oltre una parete fino all'atterraggio presso il proprietario. Nell'ultima
prova il corpo è controllato vivo e senza collisioni a ogni tick attivo, fino al controllo finale prima del successo; la parete resta intatta. Verificato anche il danno reale al giocatore smontato in aria, distinto dall'immunità del corpo già presente.

Passate anche tutte le cinque prove degli attacchi aerei, senza allentare danni
o tempi delle asserzioni originali. Il primo tentativo del controllo del passo
fermava tutti gli assi e bloccava la discesa al margine di un ostacolo: rimosso,
conservando il movimento nativo e le sue transizioni di attacco. Corretto anche
il rinnovo del percorso che ricominciava la salita invece di proseguire.

Aggiunte due prove numeriche che confrontano accelerazione e direzione con la crociera originale, preservando il rallentamento all'arrivo finale. Suite finale: **75JUnit,109GameTest mod (110 con baseline vanilla),7prove client e17immagini**, tutte passate. Log integrale phase30-final.log, build riuscita in9m23s. XML server110casi, zero errori. Controllata anche l'immagine fresca delle armature testa/coda. Nessuna prova con due client reali.
Non dichiarare conclusa la tappa1 prima dei collaudi ancora aperti.

## Limiti e prova utente

Sono deviazioni locali conservative, non un pathfinder universale per qualsiasi
labirinto o grotta. La verifica riguarda l'hitbox nativa; ali/coda visive estese
non diventano blocchi solidi. Non sono cambiate le regole del pilota, del dismount,
del recupero150blocchi e della distruzione del terreno. Restano acqua/lava,
altre parità originali e due veri client su server dedicato.

Provare un adulto vicino ad alberi/edifici in seguito e in difesa, poi volo
libero e ritorno al nido, verificando attacchi e atterraggio. Consegna: i due
JAR installabili, sorgenti, prove e checksum, sul solo ramo port/fabric-26.3.
