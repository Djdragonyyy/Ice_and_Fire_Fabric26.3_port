# Ice and Fire 0.1.3 — prova parziale della tappa 1

## Installazione

Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Mettere questi due JAR in `mods`:

- `iceandfire-fabric-26.3-0.1.3-stage1-dev.jar`
- `fabric-api-0.163.0+26.3.jar`

Sostituire il JAR Ice and Fire 0.1.2 e qualsiasi vecchia Fabric API; nessun JAR
Forge/Citadel o dei sorgenti. I checksum sono nel file SHA256SUMS.txt della consegna.
Usare un mondo nuovo di prova con trucchi. La camera confermata dall'utente conserva
la precedente regolazione della distanza. Le nuove modifiche devono essere provate.

## Cosa verificare

1. Piazzare un uovo di fuoco e schiuderlo subito:
   `/data merge entity @e[type=iceandfire:dragon_egg,sort=nearest,limit=1] {DragonAge:7201}`.
   La proprietà resta quella del giocatore che ha piazzato l'uovo.
2. Portare quel drago a 50 giorni:
   `/data merge entity @e[type=iceandfire:fire_dragon,sort=nearest,limit=1] {AgeTicks:1200000,AgingDisabled:1b}`.
   Ripetere a 75/100/125 giorni con AgeTicks 1800000/2400000/3000000. Verificare che il
   selettore punti al drago voluto. La monta rimane disponibile dallo stadio 3.
3. Montare a mano vuota; tenere salto per oltre un secondo per decollare. Provare da
   fermo e in movimento. Salire, rilasciare salto, scendere con X e rilasciare X:
   il corpo deve inclinarsi e recuperare gradualmente. Provare anche virate e atterraggio.
4. Con F5 controllare che il bacino poggi sul collo e non entri nel modello, soprattutto
   a 100 e 125 giorni, a terra, in volo e in discesa. Controllare anche la prima persona.
5. Senza essere montati, atterrare/camminare sulla parte centrale del dorso, oppure
   dismontare sopra il drago: deve sostenere il giocatore. Il drago si ferma finché
   qualcuno è in piedi sopra, poi attende circa mezzo secondo prima di ripartire.
   Il supporto usa la collisione centrale; ali, coda e testa sporgenti non sono piattaforme.
   Volare sopra senza toccarlo o usare la modalità spettatore non deve bloccarlo.
6. Ripetere il test in sopravvivenza vicino a terra, poi con due giocatori: uno pilota
   e uno sul dorso; verificare arresto, discesa, ripresa e sincronizzazione dell'inclinazione.
   Salvare e riaprire per controllare proprietà, età e volo.

Annotare età, variante e situazione dei problemi. Per le punte grigie servono due
immagini dello stesso drago da lati diversi, con shader/resource pack/mod di rendering.
Quel difetto resta aperto e non viene dichiarato risolto dai test geometrici.

## Verifiche e codice

Build ordinaria JDK 25 riuscita: 21 test, nessun fallimento o errore. Controlli su
crescita, input, asset, cicli delle pose, bacino/superficie a ogni stadio, inclinazione,
decollo e rilevamento di un giocatore sul dorso. Revisione in REVIEW.md.
Non sono prove di gameplay del drago né una conferma della resa visiva.

File principali: FireDragon, DragonFlightMotion, DragonBackPlatform, FireDragonRenderer,
FireDragonPoseModel e DragonRiderRenderMixin. Sorgenti e licenza LGPL-3.0 nell'archivio
completo della consegna. Per ricompilare, con JAVA_HOME impostato su JDK 25:

```powershell
.\fabric\gradlew.bat -p fabric build --console=plain
```

La tappa 1 è incompleta: restano combattimento, soffio e le altre funzioni nell'HANDOFF.
Dopo il riscontro su questa prova si procede con quelle funzioni; la tappa 2 non è iniziata.
