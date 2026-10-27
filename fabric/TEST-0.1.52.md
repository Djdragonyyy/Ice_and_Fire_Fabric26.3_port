# Prova 0.1.52 - persistenza della Forge su server dedicato

La 0.1.52 aggiunge una prova di regressione; le 120 classi del JAR di gioco
sono identiche byte per byte alla 0.1.51. Non introduce una modifica alla Forge.
La tappa 1 e il port completo restano aperti.

## Riavvio reale durante la produzione

Un DedicatedServer ordinario, separato dal GameTestServer, ospita due client
con profili locali distinti. Un terzo drago, addomesticato e con AI attiva,
alimenta autonomamente una Forge con 64 lingotti di ferro e 64 unita di sangue.
Si attende la produzione reale del primo lingotto e un progresso parziale
prima di fermare il server. Nella prova verificata il progresso salvato era 22.

Dopo la chiusura e la riapertura dello stesso mondo su disco si verificano:

- Le tre pile e il progresso parziale all'evento nativo di caricamento del chunk.
- Il progresso fermo per 20 tick senza nuovo calore: l'AI del fabbro viene
  temporaneamente disabilitata dalla fixture appena l'entita viene caricata.
- Riattivata l'AI, il drago riprende autonomamente il soffio e produce un nuovo
  lingotto, consumando esattamente un ferro e un sangue.
- Inventario reale del proprietario con due unita di sangue, rientro ancora
  in sella, proprieta, armatura, eta, sesso, nome e volo del drago pilotato.
- Carcassa parzialmente raccolta e tre famiglie multipart ricostruite:
  30 parti, nuovi UUID, nessuna parte salvata come entita indipendente.

`phase52-dedicated-launch-2.log` termina con sei processi a exit 0:
server, owner, guest, reopen, reowner e reguest. Il report richiede tutte
le tre asserzioni della Forge e i marcatori di riavvio e rientro montato.
Non si sostituisce questa prova con i precedenti report 0.1.44.

Il primo tentativo e stato respinto dalla whitelist del server di prova prima
del gameplay. I processi sono stati chiusi e i log conservati. Il secondo
usa un mondo nuovo, proprieta UTF-8 e whitelist disattivata esclusivamente
sul server di sviluppo vincolato a 127.0.0.1 con due posti.

Cinque immagini fresche sono state aperte e riviste. Mostrano i client nel
mondo, senza Loading Terrain; la vista owner dopo il rientro include il fabbro
che soffia nella Forge. I nomi dei PNG conservano i suffissi storici 0.1.35 e
0.1.44 del test client condiviso, ma sono prodotti dalla sessione 0.1.52.
Il nome storico first-person non certifica la prima persona: la prospettiva
attuale viene scelta dal comportamento automatico introdotto nella 0.1.50.
Sono screenshot di regressione, con HUD e avvisi dei profili di sviluppo.

## Riproduzione e isolamento

Esportare i piani con `emit-dedicated-launch-plans.gradle` e le proprieta
`iafDedicatedWorldTest`, `iafForgeRestartTest`, `iafNetworkDir`,
`iafNetworkPort`; poi usare `run-dedicated-world-test.ps1 -ForgeRestart`
con una directory di sessione nuova e le proprieta del server locale.
ForgeRestart seleziona anche il percorso montato e i due nomi profilo stabili.
Le sorgenti sono in `src/dedicatedForgeTest`; il mod di prova e opt-in e
non entra nel JAR distribuito. Non vengono letti account Microsoft.

Questa e una prova locale con profili di sviluppo, non una certificazione di
server pubblico o autenticazione. Non verifica uno scaricamento di chunk
mentre il server rimane acceso: verifica un arresto e riavvio reali su disco.
La pausa NoAI e una condizione controllata della fixture, non una modifica
alla logica produttiva. Non e una nuova prova della GUI della Forge.

## Regressione ordinaria

`phase52-final.log`: build pulita e suite completa superate in 11m 37s,
exit 0. Report XML: 99 JUnit senza errori, 163 GameTest server (162 della mod
e uno baseline vanilla), 18 prove client e 54 PNG correnti. Cinque ulteriori
PNG appartengono alla prova dedicata separata, inclusa nel pacchetto.
L'esportazione con il solo flag ForgeRestart e stata ricontrollata dopo la
revisione: phase52-flag-only-plans.log verde, entrambi i profili stabili presenti.
Non e un'ulteriore esecuzione del riavvio: la sessione dedicata gia verde
usava entrambi i flag e gli stessi nomi profilo.
