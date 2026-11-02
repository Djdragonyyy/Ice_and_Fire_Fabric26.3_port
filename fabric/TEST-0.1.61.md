# Prova 0.1.61 - limite della ricerca dei percorsi terrestri

Checkpoint3novembre2026. Tappa1 e port ancora incompleti.

L'opzione originale maxDragonPathingNodes ora e disponibile nel file server
iceandfire-combat.json, con valore predefinito5000 e intervallo1..2147483647.
Esempio di campo: "maxDragonPathingNodes": 5000. I file esistenti che non lo
specificano usano5000. Il limite si applica alle nuove ricerche terrestri;
un percorso gia disponibile puo essere riutilizzato dalla cache nativa.

DragonGroundNavigation delega alla factory e al PathFinder nativi per
preservare le regole di valutazione. Il numero di nodi e limitato anche
dal quadrato del raggio effettivo, come in AbstractPathJob originale.
Gli aggiornamenti nativi non cancellano il cap; il retry puo ridurlo ma
non aumentare il massimo. Nessun thread, Mixin o dato di rete aggiunto.
La selezione della navigazione cambia soltanto per FireDragon.

Due nuove prove con adulto reale in un'area caricata osservano il risultato
della ricerca: cap1 parziale/assente,5000 canReach per lo stesso target,
nuovo cap1 rispettato sulla stessa navigazione dopo stop della cache;
callback setRequiredPathLength e retry16 non aggirano1. Verificati valore
massimo e guardia nativa per un target lontano, che rimane non caricato.
Il fixture usa NoAI e onGround per isolare la pianificazione, non certifica
nuovo movimento/inseguimento. La configurazione e ripristinata in finally.
JUnit aggiunto per default nei vecchi JSON e limiti interi validi/invalidi.

Focus phase61-focused.log PASS53s:101JUnit e9server(8mod+baseline).
Full pulito phase61-final.log PASS13m 10s,exit0:101JUnit,
20client/66PNG e185server(184mod+baseline), XML senza errori. Nessun init
mirato nel run finale. JAR60->61:121classi identiche,8differenti e1nuova;
sei anonimi hanno codice/struttura javap identici e metadati diversi.
Le modifiche funzionali sono config, factory del drago e adattatore.
Tutte le classi client identiche60; nessun test o harness nel JAR.

Restano navigazione avanzata/scavo forzato e altre configurazioni/ciclo
vitale. Il volo mantiene le deviazioni locali gia implementate: qui non
viene introdotto un planner a nodi di volo o asincrono. Nessuna nuova
certificazione di riavvio dedicato o client autenticati;52 rimane distinta.
La sessione termina per richiesta esplicita dell'utente, non per port completo.
