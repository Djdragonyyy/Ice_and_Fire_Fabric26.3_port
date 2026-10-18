# Ice and Fire 0.1.11 — alimentazione e comandi

Minecraft Java 26.3, Java 25, Fabric Loader 0.19.5. Mettere in mods:

- iceandfire-fabric-26.3-0.1.11-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il vecchio JAR Ice and Fire senza duplicati; aggiornare client e server
insieme. I sorgenti ZIP e la cartella codice non vanno installati in mods.

## Alimentazione

Clic destro con carne cruda o cotta sul proprio drago: fame + nutrimento*10,
salute + nutrimento, con il limite massimo originale. Per esempio manzo crudo:
+30 fame e +3 salute. Non aumenta l'età. Mangia se affamato oppure ferito;
se completamente sazio e sano non consuma carne. Il drago di fuoco non mangia
pesce o frutta. Suoni e particelle degli alimenti sono visibili agli osservatori.

Il pasto del drago continua a far crescere di un giorno. Il pasto malaticcio
blocca la crescita, aggiunge 20 fame e cura completamente. Il blocco si conserva
al salvataggio e non viene annullato dai normali pasti; un secondo pasto malaticcio
non viene consumato. Il pasto creativo addomestica e assegna la proprietà a chi
lo usa, anche se il drago era già di un altro giocatore, come nel codice originale.
Non aumenta l'età. In modalità creativa gli alimenti non vengono consumati.

Nuovi oggetti nella scheda Ingredienti oppure:

```mcfunction
/give @s iceandfire:dragon_stick
/give @s iceandfire:sickly_dragon_meal
/give @s iceandfire:creative_dragon_meal
```

Provare alimentazione con fame bassa, drago ferito ma sazio e drago completamente
sazio/sano; controllare il consumo di un solo oggetto in sopravvivenza. Provare
il blocco della crescita su un drago di prova e dopo salvataggio/riapertura.

## Bastone e seguito a terra

Clic destro con il bastone sul proprio drago alterna:

1. Seduto: interrompe navigazione e vagabondaggio, con posa originale progressiva.
2. Seguimi: segue il proprietario a terra tramite il percorso disponibile.
3. Libero: riprende il vagabondaggio originale a terra.

Lo stato iniziale è libero. Il comando viene salvato e sincronizzato; monta adulta
e raccolta sulle spalle tornano al comando libero. Gli altri giocatori non possono
usare il bastone sul drago altrui. Il bastone non viene consumato. Sono conservati
texture/modello originali e l'identificatore storico dragon_stick.

Maiusc + clic destro con il bastone imposta/rimuove casa alla posizione del drago.
Casa e dimensione persistono; il vagabondaggio usa il raggio originale predefinito
di 40 blocchi. Casa in un'altra dimensione non limita il movimento locale.
Non è ancora migrato il ritorno notturno al nido, né il seguito in volo o il
decollo autonomo: il comando seguimi richiede un percorso a terra percorribile.
Non introduce teletrasporto del drago per raggiungere il proprietario.

Provare il ciclo dei comandi, allontanarsi su terreno piano, poi ordinare seduto;
salvare/riaprire e controllare la posa anche con un secondo giocatore. Verificare
anche spalle e cavalcatura, che restano quelle della 0.1.10 confermata funzionante.
Inventario: Maiusc + clic destro a mano vuota. V decollo, spazio salita, X discesa,
R soffio, G morso invariati. Nessun dorso solido del ghast.

## Verifiche e stato

Build riuscita, 45 JUnit e 15 GameTest della mod passati (il report include anche
un baseline vanilla, escluso dal conteggio). I sei nuovi GameTest coprono dieta,
cure/consumi, proprietà e creativo, crescita bloccata, comandi/casa/NBT/dimensioni,
limite di casa, movimento reale sul terreno e arresto, posa da dati sincronizzati.
Revisione con review-agent in REVISIONE.md. Client avviato con risorse, grafica,
audio e atlanti caricati. Nessuna verifica visiva o multiplayer con due veri client
eseguita dall'agente; serve la prova utente delle nuove interazioni.

```powershell
.\fabric\gradlew.bat -p fabric build runGametest --console=plain
```

La tappa 1 resta incompleta. Proseguire con volo/combattimento autonomi, ritorno al
nido, raccolta automatica del cibo, altre interazioni/animazioni e layer stendardo.
Riproduzione, sonno completo e altre configurazioni originali non sono migrati.
Punte grigie ancora aperte. Sorgenti completi con licenza e checksum inclusi.
