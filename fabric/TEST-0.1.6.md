# Ice and Fire 0.1.6 — un solo posto di guida

Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Installare questi due JAR in mods:

- iceandfire-fabric-26.3-0.1.6-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il JAR Ice and Fire precedente, evitando duplicati. Usare la stessa versione
su client e server. Fabric API resta invariata; non installare il JAR dei sorgenti.

Il drago accetta una sola cavalcatura: la seduta principale del proprietario dallo
stadio 3. La monta normale non forza più l'ingresso saltando i controlli vanilla.
couldAcceptPassenger impedisce posti aggiuntivi anche nel percorso di monta forzata
di Minecraft, incluso il caricamento dei passeggeri salvati. Non sono presenti sedute
secondarie: stare in piedi sul dorso resta una collisione e non una monta.

Prova in gioco necessaria:

1. Con un drago di proprietà di almeno 50 giorni, montare a mano vuota e controllare
   l'unica seduta in F5. Provare camminata, V/spazio per decollo e X per discesa.
2. Con un secondo giocatore, tentare di montare il drago già occupato: non deve
   comparire una seconda seduta e il pilota deve conservare il controllo.
3. In un mondo con trucchi, mentre il pilota è montato, usare
   `/ride NomeSecondoGiocatore mount @e[type=iceandfire:fire_dragon,sort=nearest,limit=1]`.
   Anche questo tentativo forzato deve essere rifiutato. Verificare il selettore.
4. Smontare a terra e in volo: restare in piedi sulla superficie centrale del dorso,
   camminarci sopra e rimontare dopo il breve intervallo vanilla di dismontaggio.
   Nessun secondo, terzo o quarto posto deve apparire.
5. Salvare e riaprire un mondo di prova che prima conteneva passeggeri aggiuntivi:
   deve restare al massimo un passeggero. Ripetere a 50/75/100/125 giorni.

Build e suite JUnit: 30 test superati. Verificato nel bytecode vanilla che il controllo
couldAcceptPassenger precede il ramo force di startRiding. Rilettura del diff senza
ulteriori difetti individuati. Queste verifiche non sostituiscono la prova con due
giocatori: nessun collaudo interattivo eseguito dall'agente per questa versione.

Il difetto delle punte grigie resta aperto. La tappa 1 resta incompleta.
Sorgenti completi e licenza LGPL-3.0 sono inclusi nell'archivio della consegna.
