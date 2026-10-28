# Prova 0.1.57 - rottura ordinaria attorno al corpo

Checkpoint del28ottobre2026. Tappa1 e port ancora incompleti.

## Comportamento portato

DragonBodyBreaking adatta EntityDragonBase.breakBlocks(false) e viene
richiamato dal customServerAiStep nativo. Mantiene cooldown configurabile,
stadio minimo3 e consenso al movimento o presenza del pilota. Non aggira
NoAI, mobGriefing, dragonGriefing, tamedDragonGriefing, sonno e recupero
della posa. Un proprietario realmente montato mantiene il ramo originale
del pilota anche con command1, rispettando comunque il gamerule.

L'intervallo orizzontale e il bounding box arrotondato esteso di un blocco;
il minimo verticale e un blocco sopra i piedi, o uno sotto quando il nodo
successivo del percorso scende. Massimo due blocchi sopra il corpo, oppure
il suo massimo durante volo con bersaglio. Si interrogano solo chunk gia
caricati. Solidi legacy, forma non vuota, fluidi assenti, durezza non negativa
fino a2 nello stadio3 o grief1 e fino a5 negli adulti maggiori, resistenza
inferiore a1200 e blacklist traducono i filtri originali. La blacklist
include i blocchi IDragonProof gia portati. La26.3 espone legacySolid tramite
isSolid; blocksMotion non esiste piu. Nessuna immunita generica dei BE.

I blocchi sono distrutti dal livello nativo, rallentando XZ di0.6 per ogni
rimozione. Chance drop originale e tag no-drop terra, pietre, cobblestone,
ambiente drago ed erba; le foglie non sono escluse genericamente. I tag
comuni c:stones/cobblestones sostituiscono forge; l'ambiente comprende per
ora solo i sei blocchi carbonizzati esistenti. Nessun blocco di ghiaccio o
fulmine fittizio. Cooldown0 agisce subito anche su un timer gia avviato.

## Evidenze e correzioni

Una caratterizzazione privata della56, dopo la sua consegna, verificava
dimensioni adulte reali aggiornate (scala2.45,larghezza1.911,altezza2.94):
corridoio libero raggiungibile, sporgenza di terra non pianificabile; AI
reale ferma aZ27.0445 dopo200tick, terra intatta. La57 supera lo stesso
ostacolo aZoltre29 mantenendo il bersaglio durante il passaggio. Tetto di
bedrock e pavimento restano intatti. Non sono stati imposti movimenti o
seed casuali. Le prese della preda successive possono azzerare il bersaglio:
il test registra pertanto il bersaglio durante l'attraversamento, non solo
allo scadere dei200tick. La prima prova basata sul solo stato finale falliva;
le tracce successive documentano passaggio, bersaglio e stato della presa.

Focus finale phase57-focused-8.log PASS47s,100JUnit e5nuoveGameTest piu
baseline: distruzione e vero drop plank1, assenza drop terra/pietra,
fluidi/pavimento/chest/resistenza1200, durezza stadio3 e4/grief1 e0, stage2,
gamerule/config/tame/sit/sleep/pose/NoAI negativi, proprietario montato e
gamerule, cooldown nativo5tick, transizione immediata5->0, attraversamento
con AI reale. I cambi config/gamerule sincroni sono ripristinati in finally.
Prima compilazione falliva il vecchio nome API, una traccia aveva una
parentesi in eccesso; log mantenuti. Revisione ha trovato e corretto il
timer pendente quando cooldown diventa0 prima della verifica finale.

Full pulito phase57-final.log PASS13m 15s,exit0:100JUnit,
183testserver(182mod+baseline),19client e56PNG, XML senza errori. Il clean
senza init privati elimina le vecchie classi/metadati di caratterizzazione.
JAR56->57:123classi byteidentiche, FireDragon modificata e nuova
DragonBodyBreaking. La switchmap FireDragon$6 ha solo LineNumberTable
1287->1294 differente, codice/struttura javap identici. Nessun harness o test. Nessuna geometria o UI nuova,
le prove client sono regressioni esistenti, non nuove scene di questa funzione.

## Lavoro aperto

Il pianificatore esplicitamente passabile e lo scavo forzato quando bloccato
non sono ancora portati. La riuscita nel corridoio non certifica navigazione
in grotte o labirinti. Vecchie callback Forge GenericGriefEvent non sono API
Fabric: il port usa filtri nativi e tag disponibili. Riavvio Forge52 distinto,
nessuna nuova prova dedicata/autenticazione pubblica. Configurazioni e ciclo
vitale, GUI passeggeri e resa delle punte grigie restano da completare.
