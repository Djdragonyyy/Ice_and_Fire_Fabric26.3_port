# Prova 0.1.33 — camminata originale e velocita sincronizzata

Checkpoint parziale tappa1. Non sospendere o spegnere il PC.

## Modifiche

Camminata del pilota da EntityDragonBase2160..2194: velocita attributo*1.8*
moltiplicatore; input avanti ulteriormente scalato per quella velocita,
sprint1.2, retromarcia0.2 e laterale0.5. InputY entrante conservato. La
superficie della lava condivide la velocita ma conserva i suoi input distinti.
Acqua, immersione lava e volo pilotato mantengono le curve gia implementate.

Opzione originale dragonFlightSpeedMod, predefinito1 e intervallo finito0..2,
in config/iceandfire-combat.json. Configurazioni vecchie senza chiave usano1;
per modificarla aggiungere la proprieta al JSON e riavviare Minecraft/server.
Il server inizializza e aggiorna un FLOAT nativo sincronizzato per ogni drago:
il client pilota usa quel valore, senza dipendere dal proprio file locale.
Il valore non e salvato nel mondo: un caricamento usa la configurazione attuale.

Il volo autonomo applica il fattore all'accelerazione del passo, prima del
limite per asse0.2, e alla correzione di salita. Conservata la velocita base
limitata della navigazione invece della moltiplicazione cumulativa del vecchio
helper: a0 nessuna divisione per zero e a2 nessuna crescita esponenziale.
Questo e un adattamento limitato, non parita numerica cumulativa del vecchio
helper per impostazioni diverse da1. Il volo pilotato originale non usava
questa opzione: la sua curva resta invariata. Zero non blocca gravita,
atterraggio, inerzia o salita iniziale dell'hover.

## Verifica

Nuove prove numeriche: limiti/configurazioni legacy e accelerazione finita,
limitata e non cumulativa. Tre nuove prove native: input e movimento effettivo
a terra con0.5/0, dati sincronizzati e ricaricamento con nuova configurazione,
seguito aereo reale che riparte passando da0 a1 mantenendo un percorso valido.
Passate le sei prove native di configurazione. Nuova nona prova client:
proprietario connesso su vero pavimento, avanzamento visto da client e server,
aggiornamenti del server0.5/0/2/1 ricevuti dal client e zero che annulla
l'avanzamento comandato dopo lo smorzamento dell'inerzia. Nove prove client
passate nel primo ciclo. Suite definitiva **79JUnit,117GameTest mod (118 con baseline),9prove client e17immagini**, tutta passata in8m40s. Log phase33-final.log. Nessun collaudo con due client reali.

Restano altre parita della tappa1, overlay maschile e altre differenze visive,
navigazione complessa e due client reali su server dedicato. Non dichiarare
completa la tappa1 o risolte le punte grigie.

Un tentativo completo ha riprodotto il problema del contrattacco notturno gia
osservato nella0.1.31: sveglio e bersaglio corretto, salute avversario200 a50tick.
Conservati danno richiesto, distanza e scadenza; aggiunta traccia di animazione,
portata, visibilita, stato e salute. Quattro ripetizioni isolate e successiva
suite completa passate. Causa non provata: resta un rischio intermittente da
indagare, non un difetto dichiarato risolto o un'asserzione allentata.