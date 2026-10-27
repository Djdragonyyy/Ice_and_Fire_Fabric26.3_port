# Prova 0.1.55 - pile di tesori originali

Checkpoint verificato del27ottobre2026; tappa1 e port ancora aperti.

## Materiali e comportamento

Portate le pile di oro, argento e rame di BlockGoldPile con otto livelli.
I modelli, i blockstate, le texture e i due suoni sono gli asset originali.
L'altezza visiva e 2/16 per livello; la collisione e un livello piu bassa,
come nell'originale. Un livello e senza collisione, otto livelli sono visibili
come un blocco intero ma hanno collisione alta14/16. Una pila piena supporta
un'altra pila, anche di un altro materiale. Ghiaccio normale, compatto e
barriera non supportano; miele, soul sand e ghiaccio blu seguono le regole
originali. Il percorso terrestre passa sotto cinque livelli; acqua/aria no.
Le pile non ereditano la logica di scioglimento della neve.

Uso del blocco con la stessa pila aggiunge un livello e consuma un oggetto
in sopravvivenza, conservandolo in creativa. Si usa la mano effettiva della
chiamata nativa. A otto livelli il blocco resta pieno e il normale BlockItem
puo iniziare una nuova pila sopra. La rimozione del supporto aggiorna il blocco.
Due pepite dello stesso metallo producono una pila tramite ricetta shapeless;
la rottura restituisce due pepite per livello, fino a sedici. Rame usa la
pepita vanilla della26.3; la pepita d'argento originale chiude la dipendenza
di crafting/loot. La sua progressione mineraria appartiene alle tappe successive.

La rottura di ognuna delle tre pile ora attiva lo stesso allarme dei forzieri
per i draghi selvatici, secondo dragonGoldSearchLength. Aggiungere livelli non
simula l'apertura di un forziere e non li sveglia. Si completa cosi il ramo
originale di ServerEvents.onBreakBlock per i tre metalli; generazione di tane
e strutture resta nella tappa successiva. Tag piccone e guarded_by_piglins
per l'oro sono quelli originali. Sono presenti icone e tab creativa.

## Adattamento 26.3 e verifiche

Le vecchie tabelle loot venivano accettate ignorando conditions/functions:
il primo focus runtime trovava una sola pepita al livello1. Il formato nativo,
verificato nei dati Snow/Melon del JAR Minecraft, usa condition con type
match_block/blocks/state e modifier con type set_count. Le tre tabelle sono
convertite mantenendo gli otto casi e i conteggi originali2*livello.

Focus finale phase55-focused-4.log:100JUnit e5GameTest superati in47s, piu
compilazione della nuova prova client. Coperti tutti24shape/collision/loot,
vera rottura sopravvivenza di8livelli con16pepite, supporto e rimozione della
pila superiore, soglie percorso/suoni, stacking1..8/nuova pila superiore e
consumo esatto/creativa, tre veri ResultSlot di crafting e negativo metalli
misti, allarme dei draghi su tutte3pile e assenza allarme durante aggiunta.
Il primo tentativo aveva un errore di cattura lambda nel solo test; i due
successivi rendevano evidente il vecchio formato loot, non sono prove verdi.

phase55-client-probes.log:19prove client superate in2m51s,56PNG. Due nuove
immagini aperte e riviste: tutti24stati nel mondo e quattro icone originali.
La camera firstperson e ripristinata alla fine; file modelli/audio presenti.
Il vero pacchetto C2S incrementa1->2livelli e8->7oggetti in sopravvivenza,
con controllo autorevole sul server dopo10tick per superare la predizione.
La scena mostra rame a sinistra, argento al centro e oro a destra, da1a8livelli.

La26.3 calcola il passaggio di rendering dalla trasparenza delle texture
(SpriteContents/ChunkSectionLayer.byTransparency), senza la vecchia mappa
ItemBlockRenderTypes. Tutte3texture hanno alpha255, con gli stessi pixel
originali: non e stata aggiunta una Mixin per riprodurre un vecchio binding.
Gli avanzamenti mantengono l'ID gold_pile nella cartella food e copper_pile
in decorations; per il nuovo supporto argento e presente lo sblocco nativo.

Full pulito phase55-final.log superato in12m37s,exit0:100JUnit senzaerrori,
177testserver(176mod+baseline),19client e56PNG correnti.37assetoriginali
verificati byteperbyte e valori preesistenti lang/sounds invariati. Tappa1 e port completo ancora aperti:
restano configurazioni, ciclo vitale, navigazione complessa/rottura blocchi,
anteprima passeggeri e punte grigie. Non e una nuova certificazione di riavvio
Forge o server pubblico con account autenticati; la prova52 resta distinta.
