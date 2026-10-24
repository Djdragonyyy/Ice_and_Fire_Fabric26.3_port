# Ice and Fire 0.1.24 — configurazioni di volo e casa

Minecraft Java 26.3, Java 25, Loader 0.19.5, Fabric API 0.163.0+26.3.
Checkpoint parziale della tappa 1, ancora incompleta.

In config/iceandfire-combat.json si possono aggiungere:

| Campo | Default | Intervallo |
| --- | --- | --- |
| maxDragonFlight | 256 | 100..2147483647 |
| dragonWanderFromHomeDistance | 40 | 1..10000 |

Nomi, valori e limiti provengono dal ServerConfig originale. Riavviare dopo
le modifiche. I vecchi JSON mantengono i default; configurazioni invalide non
vengono sovrascritte e causano il ripiego sui default con un messaggio nel log.

maxDragonFlight limita la quota Y assoluta delle destinazioni del manager di
volo autonomo; resta applicato anche il limite verticale nativo, lasciando
spazio per il corpo. Non impone una barriera fisica al pilota e non modifica
la velocità di volo. Il raggio della casa limita il vagabondaggio terrestre e
le destinazioni casuali aeree; le soglie originali di ritorno al nido restano.

La nuova prova nel runtime salva/ricarica la casa, controlla interno/confine
del raggio configurato e verifica entrambe le quote, configurata e del mondo.
Le prove JSON includono nuovi default e valori invalidi. Le regressioni
cumulative server/client vengono rieseguite prima della consegna.

L'utente ha annullato OGNI sospensione del PC. Continuare il porting dopo
salvataggio e consegna, riprendendo al reset della quota quando necessario.
Il vecchio script locale di sospensione è disabilitato: termina con errore
prima di qualsiasi API di alimentazione. Non sospendere o spegnere il PC.

Restano le lacune elencate in STATO-TAPPA-1.md, incluse le prove con due veri
client. Nessuna modifica agli asset originali, alle punte grigie o alla seduta.

Verificati 68 JUnit, 81 GameTest della mod (82 con baseline vanilla escluso) e 4 prove client con dieci screenshot. Nessuna prova con due veri client.
Build completa e revisione del diff: superate. Dieci screenshot rigenerati con la0.1.24; i nomi mantengono la versione che introdusse la regressione.
