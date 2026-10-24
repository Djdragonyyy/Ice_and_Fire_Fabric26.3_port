# Ice and Fire 0.1.14 — difesa autonoma e morso terrestre

Minecraft Java 26.3, Java 25, Fabric Loader 0.19.5. Installare su client e server:

- iceandfire-fabric-26.3-0.1.14-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il precedente JAR Ice and Fire senza duplicati in mods. Il pacchetto
include sorgenti completi, istruzioni, risultati dei test e SHA-256.
La 0.1.13 è stata confermata funzionante dall'utente il 24 ottobre 2026.

## Nuovo comportamento

Migrati i tre goal difensivi di EntityDragonBase, con le priorità originali:
bersaglio attaccato dal proprietario, aggressore del proprietario, aggressore
del drago. Nessuna selezione indiscriminata degli animali vicini: la caccia
originale deve ancora essere migrata. La reazione può avvenire anche in seguimi,
che riprende quando non c'è più un bersaglio valido.

Inseguimento a terra a velocità 1.5, percorso aggiornato ogni 4..10 tick,
sguardo sul bersaglio e arresto entro la portata originale del morso, derivata
dalla dimensione visiva. Animazione di 35 tick; danno server solo nella finestra
16..24, un colpo riuscito per morso. Portata, linea di vista e alleanze sono
controllate anche all'impatto. Conservata la cura opzionale dal morso.

Seduto, NoAI, pilota, passeggeri e trasporto in spalla escludono l'IA di attacco.
Proprietario, alleati, animali dello stesso proprietario, creativo e spettatore
sono protetti. Bersagli morti/rimossi, di altra dimensione o oltre FOLLOW_RANGE
sono abbandonati. Cambiare bersaglio riavvia la carica del morso.

Un drago già in volo atterra prima di combattere a terra. È un comportamento
transitorio del port: tackle, hover blast e scorch stream originali non sono
ancora migrati. Nessun nuovo soffio autonomo in questa versione; R/G del pilota
restano disponibili. Non confondere questa consegna con il combattimento completo.

## Verifiche automatiche

Build riuscita: 53 JUnit, 34 GameTest della mod (più always_pass vanilla escluso)
e una prova client con mondo e screenshot superati. Revisione completa secondo
review-agent in sola lettura senza delega: No findings. Nessun test con due client.

Con JDK 25, dalla radice della repository:

```powershell
.\fabric\gradlew.bat -p fabric build runGametest runClientGametest --console=plain
```

Le prove coprono reazione al danno reale, movimento verso un bersaglio lontano,
impatto temporizzato e singolo, difesa del proprietario, suo bersaglio offensivo,
protezione alleati e controllo, muro, rimozione/cambio del bersaglio, NoAI e
atterraggio con successivo attacco. Incluse le regressioni delle versioni precedenti.
Il test client verifica ancora 32 icone e GUI con head/tail equipaggiati in un
mondo locale; non è una prova visiva del nuovo combattimento né di due client reali.

## Prova in gioco

1. In un mondo di prova, lasciare un drago di proprietà libero o in seguimi.
   Colpire un mob resistente e osservare inseguimento, animazione e morso.
2. Far attaccare il proprietario e poi il drago da un mob: verificare la reazione.
   Usare sopravvivenza per provare i danni al giocatore.
3. Durante il morso dare seduto, salire sul drago o rimuovere il bersaglio:
   l'attacco autonomo deve interrompersi. Riprovare con piccolo in spalla.
4. Verificare che proprietario e draghi con lo stesso proprietario siano protetti,
   e che un muro blocchi il colpo. Provare stadi 1, 2, 3 e 5.
5. In aria assegnare un bersaglio mediante uno degli eventi difensivi: il drago
   deve atterrare e poi raggiungerlo a piedi. Riprovare volo/seguimi/teletrasporto,
   armature, inventario, camera e R/G del pilota.

Tappa 1 ancora incompleta: riepilogo cumulativo in STATO-TAPPA-1.md.
Punte grigie ancora aperte; nessun passaggio alla tappa 2.
