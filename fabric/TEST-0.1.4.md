# Ice and Fire 0.1.4 — prova parziale della tappa 1

## I due JAR

Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Nella cartella `mods` usare:

- `iceandfire-fabric-26.3-0.1.4-stage1-dev.jar`
- `fabric-api-0.163.0+26.3.jar`

Togliere il precedente JAR Ice and Fire 0.1.3 e altre versioni duplicate. Fabric API
non cambia. Non installare JAR dei sorgenti o mod Forge/Citadel. In multiplayer usare
Ice and Fire 0.1.4 sia sul server sia su tutti i client: il comando di decollo è nuovo.
Checksum SHA-256 nel file SHA256SUMS.txt della consegna.

## Comandi di volo

- **V: decolla**, una pressione basta. Riassegnabile nella categoria Ice and Fire,
  voce "Drago: decolla". Non è un interruttore di volo e non fa atterrare.
- **Spazio tenuto per oltre un secondo**: decollo alternativo; in volo fa salire.
- **X**: scendi. Spazio e X insieme annullano il comando verticale.
- **F5**: cambio prima/terza persona. Il tilt resta visibile in terza persona e agli
  altri giocatori; per il solo pilota in prima persona viene nascosto.

La monta resta disponibile da 50 giorni. Un giocatore in piedi sul dorso impedisce
il movimento; scendere dal dorso o montare prima di provare il decollo. Lasciare
libero lo spazio sopra il drago: i blocchi continuano a fermare fisicamente il volo.

## Prova richiesta

1. In un mondo di prova nuovo, piazzare un uovo e schiuderlo subito:
   `/data merge entity @e[type=iceandfire:dragon_egg,sort=nearest,limit=1] {DragonAge:7201}`.
2. Portare il drago di proprietà a 50 giorni:
   `/data merge entity @e[type=iceandfire:fire_dragon,sort=nearest,limit=1] {AgeTicks:1200000,AgingDisabled:1b}`.
   Per 75/100/125 giorni usare AgeTicks 1800000/2400000/3000000. Verificare il selettore.
3. Montare; decollare con una pressione breve di V, atterrare e ripetere più volte
   da fermo e camminando. Ripetere usando una sola pressione continua di spazio.
   Provare anche un nuovo tasto assegnato al decollo.
4. Salire in prima persona: il corpo non deve più attraversare la visuale a causa
   del tilt. Cambiare con F5: in terza persona il tilt deve essere ancora visibile.
   Provare alle quattro età, anche girando la testa.
5. In discesa con X avvicinarsi a terreno piano, gradini, lastre e sporgenze: il
   drago deve raddrizzarsi prima di arrivare a terra. Provare anche in volo sopra
   un pendio e a 125 giorni. Non equivale a una collisione perfetta di ali/coda.
6. Controllare seduta, dismontaggio sul dorso, ripartenza, salvataggio/riapertura.
   In multiplayer verificare tilt visto da un secondo giocatore e impossibilità
   di pilotaggio da parte di un non proprietario.

## Verifiche e stato

Build ordinaria e 28 test superati; revisione in REVIEW.md. Le nuove correzioni non
sono ancora confermate visivamente dall'utente. Restano aperte le punte grigie.
La tappa 1 non è completata: dopo la prova si prosegue con combattimento/soffio e
le altre funzioni dell'HANDOFF. La tappa 2 non è iniziata.

Sorgenti completi e licenza LGPL-3.0 nell'archivio della consegna. Con JDK 25:

```powershell
.\fabric\gradlew.bat -p fabric build --console=plain
```
