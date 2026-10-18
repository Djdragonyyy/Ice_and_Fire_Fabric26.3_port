# Ice and Fire 0.1.5 — dismontaggio sul dorso solido

## Installazione

Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Mettere in `mods` questi due JAR:

- `iceandfire-fabric-26.3-0.1.5-stage1-dev.jar`
- `fabric-api-0.163.0+26.3.jar`

Sostituire il precedente JAR Ice and Fire, senza tenere versioni duplicate. Fabric API
resta la stessa. Usare la stessa 0.1.5 sul server e sui client. Non installare JAR dei
sorgenti, Ice and Fire Forge o Citadel Forge. Checksum in SHA256SUMS.txt della consegna.

## Cosa cambia

Quando smonti, il drago si ferma subito e il giocatore viene posizionato sul centro
della superficie solida del dorso. Velocità residua e distanza di caduta del giocatore
vengono azzerate. Il drago resta fermo mentre qualcuno è in piedi sopra, poi riparte
dopo circa mezzo secondo quando il dorso è libero. Il supporto è la collisione centrale,
non l'intera geometria animata di ali, testa e coda; non sono blocchi piazzati nel mondo.

La 0.1.5 conserva decollo V riassegnabile, spazio tenuto per oltre un secondo,
salita con spazio, discesa X, cambio camera F5, tilt nascosto al pilota in prima persona
e raddrizzamento durante la discesa vicino ai blocchi.

## Prova richiesta

1. Usare un mondo di prova nuovo con trucchi. Schiusa rapida dell'uovo più vicino:
   `/data merge entity @e[type=iceandfire:dragon_egg,sort=nearest,limit=1] {DragonAge:7201}`.
2. Portare il drago di proprietà a 50 giorni:
   `/data merge entity @e[type=iceandfire:fire_dragon,sort=nearest,limit=1] {AgeTicks:1200000,AgingDisabled:1b}`.
   Per 75/100/125 giorni usare AgeTicks 1800000/2400000/3000000. Verificare il selettore.
3. Montare e premere il tasto di dismontaggio (Maiusc se non riassegnato), senza
   premere i tasti di movimento. Il giocatore deve restare in piedi sul dorso.
4. Ripetere dopo camminata, volo in avanti e discesa con X. Il drago deve fermarsi
   appena smonti e sostenerti anche in aria. Controllare soprattutto 100 e 125 giorni.
5. Camminare al centro del dorso, poi allontanarsi dal suo bordo: il drago deve
   restare fermo mentre sei sopra e riprendere solo quando il dorso è libero.
   È normale cadere se esci dalla superficie; non viene applicato un aggancio invisibile.
6. Rimontare e provare V/spazio e X; verificare anche F5, seduta e prima persona.
   Ripetere con due giocatori e salvando/riaprendo il mondo.

Build ordinaria e 29 test riusciti; revisione in REVIEW.md. Nessun nuovo collaudo
interattivo o avvio client 0.1.5 eseguito dall'agente: il dismontaggio va confermato
in gioco. Le punte grigie restano aperte e la tappa 1 è incompleta.

Sorgenti completi e licenza LGPL-3.0 nell'archivio della consegna. Con JDK 25:

```powershell
.\fabric\gradlew.bat -p fabric build --console=plain
```
