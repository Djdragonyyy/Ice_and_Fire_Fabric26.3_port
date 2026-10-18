# Ice and Fire 0.1.9 — soffio, terreno e inventario

Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25. Mettere in mods:

- iceandfire-fabric-26.3-0.1.9-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il precedente JAR Ice and Fire, evitando duplicati. Riavviare il gioco
e aggiornare insieme server e client: ci sono nuovi item, blocchi, menu e payload.
Non installare sorgenti ZIP, JAR -sources o la testmod dei GameTest.

## Soffio e prima persona

R tenuto mantiene il soffio; G morso, V decollo, spazio salita e X discesa.
Il getto comincia dalla bocca dopo circa un secondo di carica e cresce fino alla
portata completa in un altro secondo. La scelta casuale delle palle di fuoco non
interrompe più il getto. È usata la texture originale dragon_flame, con dimensione
e densità crescenti con il drago. Il limite di durata del soffio dipende dallo stadio.

In prima persona il fuoco non solleva il collo/seduta davanti al pilota: la mandibola
resta animata. Le particelle troppo vicine alla camera del pilota sono nascoste.
Terza persona e osservatori conservano la posa originale di carica.

Confrontare lo stesso drago a 50 giorni e 125 giorni, con AgeTicks rispettivamente
1200000 e 3000000 e AgingDisabled:1b. Verificare in F5 il getto più grande da adulto,
poi in prima persona il comfort durante carica, salita, virata e discesa. Controllare
anche rilascio di R, rimonta e ostacoli. La verifica visiva resta da fare dall'utente.

## Terreno

Il soffio carbonizza erba, terra, pietra, ciottoli, ghiaia e sentiero; legno e assi
diventano cenere cadente, foglie/fiori/colture/neve bruciano. La palla di fuoco
distrugge e carbonizza un'area crescente con lo stadio. Sono conservati nomi e asset
originali, compresa la grafia chared_*.

Il terreno bruciato può tornare allo stato originale con i tick casuali; blocchi
carbonizzati piazzati dal creativo non tornano indietro. Provare anche ghiaia/cenere
sospese e un blocco solido sopra un sentiero. Casse, bedrock e blacklist sono protetti.
Le trasformazioni rispettano mob_griefing e config/iceandfire-combat.json:
dragonGriefing=2 le disabilita; tamedDragonGriefing=false le disabilita per i draghi
addomesticati. I danni alle entità restano separati. Provare entrambe le impostazioni.

Non sono ancora migrati DragonForge e le esplosioni opzionali: questo aggiornamento
non dichiara equivalenza completa con tutti gli effetti del codice originale.

## Inventario e armature

Maiusc + clic destro a mano vuota sul proprio drago, da terra e vicino, apre la GUI
originale con anteprima, salute, fame, sesso, stadio ed età. I cinque slot sono:
stendardo, testa, collo, corpo e coda. Lo stendardo viene conservato, ma il suo layer
visivo sul drago non è ancora migrato.

Le 32 armature originali sono disponibili nella scheda creativa Combattimento.
Inserire ogni pezzo nel suo slot: cambiano protezione e texture sul drago. Provare
inserimento normale, Maiusc-clic, rimozione, salvataggio/riapertura e due giocatori:
un non proprietario non deve aprire o usare il menu. Allontanandosi il menu si chiude.
Verificare il ritorno degli oggetti alla morte di un drago di prova.

## Risultati e stato

41 test JUnit e 5 GameTest della mod superati. Il report GameTest contiene anche un
baseline vanilla sempre positivo, non incluso nei cinque test della mod. Verificati
nel runtime Fabric: slot/materiali, NBT di un drago a 125 giorni, componenti degli
oggetti, permessi, trasferimenti senza duplicazione, terreno, loot, ritorno allo
stato originale, griefing e blocchi protetti. Non sostituiscono due veri client.

Client avviato in cartella di prova separata; risorse/grafica/audio/atlanti caricati.
Nessuna verifica visiva in mondo di getto/GUI/armature eseguita dall'agente.
Revisione completa in REVISIONE.md della consegna. Comando con JDK 25:

```powershell
.\fabric\gradlew.bat -p fabric build runGametest --console=plain
```

Tappa 1 ancora incompleta: IA autonoma, altri attacchi, interazioni/alimentazione,
layer dello stendardo e altre animazioni restano da migrare o completare. Punte grigie
ancora aperte. Il dorso ghast resta rimosso. Sorgenti e licenza LGPL-3.0 inclusi.
