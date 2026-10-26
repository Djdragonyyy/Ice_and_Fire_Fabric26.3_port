# Prova 0.1.43 - DedicatedServer ordinario e riapertura reale del mondo

Checkpoint verificato del26ottobre2026. Prova dedicata verde,4processi exit0.
Regressione completa phase43-final.log: BUILD SUCCESSFUL in10m52s,94JUnit,
141GameTest mod+baseline142 tutti verdi,14prove client e40PNG correnti.
Con le3PNG dedicate,43immagini totali nella consegna. XML senza failure/error.

Questa versione aggiunge soltanto harness e documentazione di collaudo. Tutte
le99classi di produzione sono byte per byte identiche alla0.1.42; nessun harness
DedicatedDragonWorldTest/NetworkTestFiles/DragonNetworkClientTest nel JAR.
L'opzione esplosioni e le dieci hitbox restano quelle gia verificate nella42.

## Prova dedicata

Due processi client rendering reali su TCP127.0.0.1:25596, UUID distinti di
profili di sviluppo locali; processo net.minecraft.server.dedicated.DedicatedServer
ordinario con2slot. Nessuna sostituzione con GameTestServer o mixin della sua
capacita. Proprietario pilota, ospite senza monta e comandi falsificati respinti,
volo osservato da entrambi, sesso/moltiplicatore velocita e armature testa/coda
sincronizzati e verificati sui due client. Tre immagini correnti controllate:
prima/terza persona del pilota e ospite. L'immagine ospite e ravvicinata/tagliata,
non certifica l'intero modello; asserzioni native verificano lo stato sincronizzato.
La chat di morte nelle immagini si riferisce alla carcassa separata del fixture.

Dopo dismount esplicito del proprietario, logout di entrambi e arresto ordinato,
nuovo processo server apre lo STESSO mondo salvato. Verificati UUID del drago,
UUID del proprietario offline, eta50, comando1, maschio, armature head/tail,
NoAI e nome. Inventario verificato: slot armature del drago, non tutto quello
del giocatore. Carcassa separata eta75, salute0 e DeathStage2 persistono. Dieci parti
per ciascuno,20totali e nessun orfano/duplicato: UUID dei proxy nuovi rispetto a
prima del salvataggio, perche le parti non vengono serializzate autonomamente.

Report finale richiede actualWorldRestart=true e4exit0 (server,owner,guest,reopen).
Il log del riavvio carica49chunk persistenti; non e solo un roundtrip NBT in RAM.
Report, log dei4processi, configurazione SOLO del server test, confronto classi
e3immagini sono nella cartella dedicated-world-test-results della consegna.

## Correzioni del fixture e limiti

Attesa dei metadati owner prima dell'asserzione UUID esatta: uno spawn puo
arrivare prima dell'assegnazione owner successiva, non equivale a perdita dati.
Cartelle client create dal lanciatore e whitelist false esplicita per il server
locale; i tentativi precedenti sono conservati fuori repo. Nessuna correzione
alla produzione attribuita a problemi del fixture.

Il tentativo precedente riapriva il mondo dopo logout ANCORA IN SELLA. Il drago
era regolarmente presente nel file player sotto RootVehicle/Entity, non perso:
verificati dati su disco e bytecode PlayerList.remove. Il fixture e stato quindi
corretto per provare persistenza NEL MONDO dopo dismount. Logout/relogin montato,
account autenticati, server pubblico, perdita connessione improvvisa e arresto
non ordinato NON sono certificati da questa prova.

## Riproduzione

Opt-in -PiafDedicatedWorldTest, non incluso nei normali run o nel JAR della mod.
Compilare build/compileDedicatedWorldTestJava/compileDedicatedWorldClientTestJava,
esportare i3piani con runDedicatedWorldServerTest/runDedicatedWorldOwnerTest/
runDedicatedWorldGuestTest e -I scripts/emit-dedicated-launch-plans.gradle,
-PiafNetworkDir=<cartella nuova> e -PiafNetworkPort=25596.
Nel runDir server del piano impostare server-ip=127.0.0.1,server-port=25596,
max-players=2,online-mode=false,enforce-secure-profile=false,white-list=false,
level-type=minecraft:flat,level-name=world,gamemode=creative,difficulty=peaceful,
spawn-protection=0,view-distance=5,simulation-distance=5,pause-when-empty-seconds=0,
sync-chunk-writes=true. Usare soltanto un mondo di test nuovo e configurare eula.txt
secondo l'accettazione locale di Minecraft. Non modificare server/account personali.
Eseguire scripts/run-dedicated-world-test.ps1 -SessionDirectory <cartella piani>.
Il lanciatore avvia server,owner,guest e poi lo stesso server per riaprire il mondo;
agli errori tenta stop ordinato dei propri processi. Mai avviare due server sullo
stesso mondo o sulla stessa porta. Controllare report finale ed exit dei4processi.

Tappa1 e port completo ancora aperti. Restano DragonForge, altre parita/config,
navigazione complessa, uova/riproduzione/sonno e dettagli visivi/animazioni;
vedere STATO-TAPPA-1.md. Questa prova chiude soltanto il caso dedicato descritto.
