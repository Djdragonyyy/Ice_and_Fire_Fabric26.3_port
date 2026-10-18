# Ice and Fire 0.1.8 — primo combattimento controllato

Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Installare in mods:

- iceandfire-fabric-26.3-0.1.8-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il precedente JAR della mod, evitando duplicati. Riavviare il gioco;
server e client devono usare entrambi 0.1.8. I sorgenti ZIP non vanno in mods.

## Comandi migrati

- G tenuto: morso sul bersaglio vicino indicato dallo sguardo del pilota.
- R tenuto: soffio dopo circa un secondo di carica; estensione completa dopo due.
  Il raggio di mira è 10 blocchi per stadio. Mirare al terreno vicino al bersaglio.
  Il danno interessa l'area raggiunta dal getto, non tutta la sua lunghezza.
- Il ramo casuale originale può lanciare una palla di fuoco durante la carica:
  non esiste un tasto separato e non viene lanciata a ogni pressione.
- V decollo, spazio salita, X discesa, F5 camera restano disponibili.

Pose di morso e carica/getto, suono del soffio e danno crescente con lo stadio sono
migrati dall'originale. Le palle di fuoco hanno l'aspetto originale a cubo di magma.
Il dorso solido del ghast resta completamente rimosso.

## Configurazione server

Al primo avvio viene creato config/iceandfire-combat.json. Valori predefiniti originali:
dragonAttackDamageFire=2, dragonGriefing=0, tamedDragonGriefing=true,
canDragonsHealFromBiting=true. Modifiche lette al riavvio. Per disabilitare
l'accensione causata dal drago usare dragonGriefing=2 oppure tamedDragonGriefing=false
per i draghi addomesticati. È rispettata anche `/gamerule minecraft:mob_griefing false`.
Le opzioni agiscono soltanto sulle funzioni già migrate in questo checkpoint.

## Prova richiesta in un mondo nuovo con trucchi

1. Schiudere il proprio uovo nel fuoco, accelerando con:
   `/data merge entity @e[type=iceandfire:dragon_egg,sort=nearest,limit=1] {DragonAge:7201}`.
2. Crescita a 50 giorni:
   `/data merge entity @e[type=iceandfire:fire_dragon,sort=nearest,limit=1] {AgeTicks:1200000,AgingDisabled:1b}`.
   Ripetere a 75/100/125 giorni con AgeTicks 1800000/2400000/3000000.
3. Preparare un bersaglio, ad esempio `/summon minecraft:iron_golem ~ ~ ~`.
   Montare, avvicinarsi e tenerlo nel mirino; premere G e verificare animazione e danno.
4. A distanza, mirare al terreno vicino al bersaglio e tenere R alcuni secondi.
   Verificare carica, getto dalla testa, suono, fuoco e perdita di salute.
   Tenere più a lungo per osservare l'eventuale proiettile casuale e il suo impatto.
5. Mettere un muro pieno tra drago e bersaglio: non devono attraversarlo né proiettile
   né getto. Provare anche un muro tra il punto d'impatto e un bersaglio vicino.
6. Rilasciare R, aprire un menu o smontare: l'emissione deve cessare. La posa del
   soffio può richiudersi gradualmente. Riprovare dopo decollo, discesa e atterraggio.
7. Controllare con due giocatori in sopravvivenza: il proprietario e i suoi animali
   non devono essere bersagliati; un non proprietario non deve controllare il drago.
8. Disabilitare mob_griefing: il danno alle entità resta, l'accensione dei blocchi
   da parte dell'attacco deve fermarsi. Provare anche salvare/riaprire e acqua.

## Verifiche e limiti

Build riuscita e 38 test JUnit superati, revisione in REVISIONE.md della consegna.
I test coprono tempi e matematica, non un combattimento in un vero mondo multiplayer.
Le nuove funzioni richiedono ancora conferma in gioco dell'utente.
Client di sviluppo avviato in una cartella separata, con mod, grafica, audio e atlanti
caricati. Nessuna entrata in mondo o prova interattiva degli attacchi eseguita dall'agente.

Questo è un primo passaggio: particelle vanilla temporanee e sola accensione base.
Mancano ancora conversioni in blocchi carbonizzati, distruzione dell'area da proiettile,
DragonForge, esplosioni opzionali, IA autonoma e altri attacchi originali; restano inoltre
inventario/interfacce e le altre lacune della tappa 1. Punte grigie ancora aperte.
Non è il completamento della tappa 1. Sorgenti e LGPL-3.0 inclusi nell'archivio.
