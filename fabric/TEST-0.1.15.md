# Ice and Fire 0.1.15 — alberi, dismount, doppio salto e soffio autonomo

Minecraft Java 26.3, Java 25, Fabric Loader 0.19.5. Installare su client e server
solo iceandfire-fabric-26.3-0.1.15-stage1-dev.jar e fabric-api-0.163.0+26.3.jar,
sostituendo il precedente Ice and Fire senza duplicati in mods.

## Modifiche

- Alberi durante la difesa: dallo stadio 3 il drago apre un corridoio locale nel
  proprio ingombro e nei due blocchi davanti, rimuovendo tronchi/foglie con le
  soglie di durezza originali. Quando vanilla non trova il percorso, approccia
  localmente il bersaglio per aprire il passaggio e ricalcolare il percorso.
  Non riduce dimensioni o collisioni del drago allo stadio 5.
- Rispetta mobGriefing, dragonGriefing, tamedDragonGriefing, blacklist, fluidi e
  chunk caricati. Questa funzione non rompe pietra/casse o altri blocchi fuori
  dalle categorie tronchi/foglie. Vale anche per tronchi/foglie piazzati dai
  giocatori: proteggerli con blacklist o disabilitando griefing, se necessario.
  Il soffio conserva separatamente la conversione del terreno già implementata.
- Dismount a terra: ricerca una posizione laterale sostenuta, fuori dal corpo,
  senza collisioni o fluidi. Richiede drago vivo, onGround e non flying. Se non
  trova una posizione, mantiene l'uscita vanilla sopra il corpo e protegge la
  caduta corrente con l'API vanilla. Non crea un dorso solido.
- Dismount in aria: rimane alla quota corrente con il comportamento vanilla,
  senza aggiungere protezione dalla caduta o teletrasporto a terra. La normale
  caduta in sopravvivenza può ferire o uccidere il giocatore.
- Due pressioni distinte del salto entro 7 tick (circa 0,35 secondi a 20 TPS)
  fanno decollare il drago montato dallo stadio 3. Confermati V e spazio tenuto
  oltre un secondo. Richieste azzerate per stadio errato, discesa o dismount;
  tenere premuto non viene interpretato come doppio salto.
- Avanzamento tappa 1: soffio autonomo terrestre dallo stadio 2 quando il
  bersaglio è visibile, fuori dalla portata del morso ed entro 10*stadio blocchi.
  Carica oltre 20 tick, estensione a 40, durata massima stadio*25. Si interrompe
  per seduto, pilota, spalle, acqua o bersaglio non valido; vicino usa il morso.
  Riutilizza danni server, protezione alleati, ostacoli, particelle e terreno.

In iceandfire-combat.json aggiunti dragonBreakBlockCooldown (default 5, 0..10000)
e dragonBlockBreakingDropChance (default 0.1, 0..1), con nomi/default originali.
I vecchi file restano validi e ricevono i default in memoria per campi assenti.
Le foglie eliminate da questo passaggio non rilasciano oggetti.

## Verifica riproducibile

Build verificata: 56 JUnit, 41 GameTest della mod (42 includendo always_pass
vanilla escluso dal conteggio) e un test client grafico con mondo/screenshot
superati. Nessuna prova multiplayer con due veri client o collaudo visivo delle
novità di combattimento. Revisione completa review-agent read-only: No findings.

```powershell
.\fabric\gradlew.bat -p fabric build runGametest runClientGametest --console=plain
```

Sette nuovi GameTest: drago di 125 giorni che attraversa una barriera di tronchi
e foglie e attacca; esclusioni griefing/età e conservazione casse/pietra;
dismount laterale ai vari stadi; caduta effettiva con danno dopo dismount in aria;
doppio salto con sollevamento reale del drago montato; dismount da pilastro senza
uscita laterale; warmup/danno del soffio autonomo e interruzione da seduto.
Tre nuovi JUnit verificano pressioni rapide, lente, ripetute e richieste invalide.
La regressione client controlla ancora le armature; non è collaudo visivo delle
novità o multiplayer con due veri client.

## Prova utente

1. Drago di 125 giorni in libero/seguimi: farlo reagire a un aggressore dietro
   alberi, osservare passaggio e attacco. Ripetere con mobGriefing=false:
   la vegetazione deve restare intatta, senza aggirare la protezione.
2. Dismount a terra agli stadi 3 e 5: uscita laterale senza danno. Ripetere su
   piattaforma stretta. In un mondo di prova, in sopravvivenza, dismount in aria:
   nessuna uscita a terra o immunità aggiunta. 
3. A bordo, doppio salto rapido, salto singolo, spazio tenuto e V. Verificare X,
   rilascio, atterraggio e cambio dei tasti; un piccolo non deve portare il pilota.
4. Bersaglio distante e visibile: carica e soffio autonomo; avvicinandosi, morso.
   Verificare protezione alleati e interruzione da seduto/pilota.

Il pathfinder aereo completo e le modalità tackle/hover blast/scorch stream
restano da migrare, insieme alla palla di fuoco autonoma e agli altri attacchi.
Griefing disattivato non autorizza attraversamento dei blocchi. La tappa 1 è
incompleta; vedere STATO-TAPPA-1.md. Punte grigie ancora aperte.
