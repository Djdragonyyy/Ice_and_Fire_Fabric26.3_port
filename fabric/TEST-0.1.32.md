# Prova 0.1.32 — movimento sopra la superficie della lava

Checkpoint parziale della tappa1. Non sospendere o spegnere il PC.

## Modifiche

Migrato il ramo originale EntityFireDragon.travel per pilota fuori dal volo,
non immerso in lava ma con lava nel blocco nativo che influisce sul movimento.
Velocita base*1.8 (valore originale predefinito), laterale*0.05, avanti*sprint1.2,
retromarcia*0.2, input verticale entrante conservato. Una sola elaborazione
dell'input tramite il percorso nativo getRiddenInput/tickRidden/travel26.3.
Il simulatore autorevole esegue camminata nativa, poi Y0.2 contro collisione
orizzontale e attrito0.7; gli osservatori remoti non simulano questo movimento.
Immersione, volo e suolo solido mantengono i propri rami.

## Prove

Due nuove prove native, insieme alle tre precedenti dei fluidi: superficie
reale distinta dall'immersione, input/sprint/retromarcia/velocita e moto fisico,
gravita nativa seguita da attrito, esclusione del volo e del suolo solido.
Primo tentativo della fixture confrontava la gravita con un input verticale
non nullo, che genera legittima accelerazione prima della gravita: separate
le misure di conservazione dell'inputY e di gravita/attrito. Nessuna modifica
alla fisica di produzione per soddisfare il test. Cinque prove isolate passate.

Suite completa definitiva passata: **77JUnit,114GameTest mod (115 con baseline),8prove client e17immagini**, build riuscita in8m24s. Log phase32-final.log. Controllata immagine fresca delle armature testa/coda. Nessuna prova con due client reali.

## Limiti

Il moltiplicatore configurabile originale dragonFlightSpeedMod non e ancora
migrato: questa versione usa il valore originale1. Il caso sopra lava e una
transizione fisica; non introduce blocchi solidi o camminata magica sulla lava.
Restano parita della camminata generale, altri comportamenti/configurazioni,
navigazione complessa e due client reali su server dedicato. Tappa1 incompleta.
Nessuna modifica alle texture o alle punte grigie.
