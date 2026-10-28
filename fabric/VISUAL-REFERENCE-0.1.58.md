# Confronto col renderer originale - 28 ottobre 2026

Il confronto riguarda il modello di fuoco della 0.1.58. Non modifica geometria,
UV o texture e non sostituisce le prove di gameplay della release.

## Ambiente e campioni

Sono stati avviati realmente Minecraft 1.20.1, NeoForge 47.1.79 e Citadel
5075402, compilando i sorgenti originali con Java 17 in una directory e cache
separate. Renderer, modello e immagini originali non sono stati modificati.
Un harness privato aggiunto al client acquisisce le immagini e le trasformazioni.
Il confronto Fabric usa il renderer della mod, non un modello ricostruito offline.

Trenta coppie di immagini: variante rossa, stadi 1-5 ai giorni 0/25/50/75/100;
posa ferma femmina/maschio e quattro pose femmina di alimentazione ai tick
4/9/14/19. Ogni attore resta fuori dal mondo, con tickCount 20, stessa
direzione di corpo e testa, scala visiva originale e viewport 854x480.
Non esegue AI o movimento. NoAI resta false in entrambi i client: il renderer
originale salta le pose secondarie e quelle di genere se NoAI e true.
La testa viene orientata esplicitamente, senza alterare semi casuali.

Le pose di alimentazione vengono selezionate soltanto nell'harness; non sono
trenta nuovi test di alimentazione tramite giocatore. I test nativi del port
che esercitano il feeding reale restano distinti. I tempi di interpolazione
originali sono registrati e riutilizzati nel confronto Fabric.

Originale finale: BUILD SUCCESSFUL in 1m36s. Fabric finale: BUILD SUCCESSFUL
in 1m1s, con trenta immagini e rapporto. Sono stati confrontati diciassette
elementi del modello; tutte le sessanta immagini sono state riviste in
cinque tavole, conservando anche i file originali senza ritaglio.

## Esito e limiti

Corpo e articolazioni delle ali coincidono nei campioni verificati. Le
punte grigie sono visibili anche nel renderer originale sulle ali e sul dorso:
non sono, in questi casi, una prova di UV o texture errate nel port.
Non e quindi giustificato ridipingerle o cambiare la geometria per rimuoverle.
Esistono piccole differenze di illuminazione fra i due motori: non si dichiara
identita di ogni pixel o di tutte le pose possibili.

Nei primi quindici tick del feeding le pose coincidono entro gli scarti
numerici misurati. Negli ultimi cinque tick il port conserva la correzione
gia introdotta: la mandibola recupera entro la durata effettiva di venti tick.
La sequenza originale contiene invece venticinque tick di keyframe per
un'animazione di venti e mantiene la mandibola aperta fino al reset. Questa
differenza e intenzionale; non viene nascosta nei confronti.

E stata osservata anche una differenza specifica ancora aperta: il maschio
appena nato nel port mostra gia la sovrapposizione cromatica maschile.
Nell'originale, a eta zero, RenderDragonBase la esclude attraverso il controllo
isSkeletal, calcolato da DeathStage e giorni anche sull'animale vivo. La
geometria resta la stessa, ma non si dichiara parita completa della pelle del
neonato. Va trattata separatamente dalla rappresentazione delle carcasse,
senza trasformare un drago vivo in scheletro per riprodurre il filtro visivo.

Sono fuori dal campione gli altri tre colori, tutte le pose di combattimento,
terreno e illuminazione del mondo. Le verifiche automatiche di asset e modelli
esistenti restano valide, ma non vengono presentate come nuove catture originali.
La tappa 1 e il port completo restano aperti.
