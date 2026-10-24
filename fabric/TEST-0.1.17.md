# Ice and Fire 0.1.17 — combattimento aereo autonomo

Minecraft26.3, Java25, Loader0.19.5. Installare la mod0.1.17-stage1-dev e
Fabric API0.163.0+26.3 su client/server, sostituendo il precedente JAR della mod.

## Comportamento

Il drago già in aria può difendersi con le tre modalità di IafDragonFlightManager:

- TACKLE: rotta verso la parte superiore del bersaglio, tuffo, controllo di
  contatto/visibilità, danno stage*3 e ritorno al combattimento terrestre.
  Posa attack_tackle originale sincronizzata e transizione progressiva.
- HOVER_BLAST: cerca una posizione orizzontale entro gli offset originali
  +/-10, quota 5+stage*2 sopra la preda, si stabilizza vicino alla destinazione e
  usa cariche30tick con proiettile al20. Ricalcola se troppo vicino/lontano.
- SCORCH_STREAM: mantiene il vettore iniziale fra drago e preda, passa sul lato
  opposto alla quota originale e inverte il passaggio avvicinandosi al punto;
  soffio con danni server, ostacoli e protezione degli alleati.

Un bersaglio con differenza verticale superiore a5blocchi fa decollare il drago
dallo stage2, se c'è spazio; il pilota resta dallo stage3. Conservato l'hover di
decollo40tick. Dopo180tick viene scelta una nuova modalità: è una temporizzazione
esplicita del port, non parità con tutti i trigger originali di randomizeAttacks.
Il vettore scorch è limitato a30blocchi e ha un fallback se inizialmente nullo.

Rotte nei chunk caricati, destinazione libera e linea di vista; rotta bloccata
porta all'atterraggio, con attacchi sospesi durante la discesa. Un urto orizzontale
interrompe la rotta e tenta hover blast. Questo non è un pathfinder aereo completo
per aggirare edifici/grotte. Comandi, pilota, spalle, NoAI e alleati restano
prioritari. Perso il bersaglio, tornano i normali comportamenti disponibili.

La GUI ritaglia ora l'anteprima nel proprio riquadro, contenendo le pose estese
di coda e ali. I nuovi test client osservano veri attacchi del server, attendono
lo stato sincronizzato e catturano le due pose nell'inventario, poi verificano
che seduto azzeri l'attacco. Non sono prove con due veri client.

## Verifica

Build verificata:58 JUnit,51 GameTest mod (52 con baseline vanilla escluso),due test client in mondi temporanei con screenshot e verifica pixel delle statistiche. Nessun collaudo con due veri client.

```powershell
.\fabric\gradlew.bat -p fabric build runGametest runClientGametest --console=plain
```

Cinque nuovi GameTest, incluso reset carica aria/terra entro2tick: tuffo con movimento/discesa/danno/atterraggio; hover blast
con movimento/altezza/proiettile/danno; scorch con inversione/soffio/danno e stop;
decollo reale per bersaglio elevato. Il precedente test del volo difensivo ora
richiede attacco in aria, superando il fallback transitorio che imponeva atterrare.
Pose senza accumulo e tutte le regressioni precedenti incluse.

Prova utente: provocare difesa mentre è già in volo, osservare i tre comportamenti
e seduto/seguimi/pilota durante lo scontro. Ripetere con bersaglio in quota e con
ostacoli, ai diversi stadi; controllare camera, R/G e dismount. La scelta è casuale:
una singola breve prova non deve mostrare obbligatoriamente tutte le modalità.

Tappa1 ancora incompleta: shake prey/caccia, sonno/riproduzione, stendardo e
configurazioni/interazioni/rendering mancanti sono in STATO-TAPPA-1.md.
