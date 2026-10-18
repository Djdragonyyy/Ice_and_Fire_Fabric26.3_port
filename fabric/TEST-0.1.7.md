# Ice and Fire 0.1.7 — rimozione del dorso solido

Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. In mods installare:

- iceandfire-fabric-26.3-0.1.7-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il JAR precedente di Ice and Fire, senza duplicati. Riavviare il gioco
e usare la stessa versione su server e client. Fabric API resta invariata.

Rimossa l'intera funzione derivata dal ghast felice: superficie solida, rilevamento
del giocatore sul dorso, arresto automatico, dismontaggio sulla piattaforma e blocchi
del decollo/movimento. Non si può più camminare sul drago come sopra un blocco.
Resta soltanto la cavalcatura principale del proprietario dallo stadio 3.
Camera, tilt, V per decollare, spazio per salire e X per scendere sono conservati.

Prova in gioco, in un mondo di test:

1. Montare il drago di proprietà a 50/75/100/125 giorni; controllare la seduta in F5.
2. Provare camminata, decollo con V o spazio tenuto oltre un secondo, salita e X.
3. A terra, smontare e rimontare: non deve esserci la pausa della vecchia piattaforma.
4. Passare sopra un drago non montato: il dorso non deve più sostenere il giocatore
   come un blocco né bloccare il drago. Non c'è più il supporto dopo dismount in aria.
5. Con due giocatori, verificare che il secondo non possa aggiungersi come passeggero.
6. Salvare e riaprire il mondo, verificando proprietà e cavalcatura.

Build pulita e 27 test superati: tre test relativi alla funzione rimossa sono stati
eliminati. Nessuna prova interattiva eseguita dall'agente per questa versione.
Punte grigie aperte e tappa 1 incompleta. Prossimo lavoro del port: combattimento
e soffio originali del drago. Sorgenti e licenza LGPL-3.0 inclusi nell'archivio.
