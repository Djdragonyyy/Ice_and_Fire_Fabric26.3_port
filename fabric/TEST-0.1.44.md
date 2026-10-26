# Prova 0.1.44 - logout montato e rientro dopo riavvio

Tappa1/port aperti. Checkpoint del26ottobre2026.99classi di produzione del JAR
identiche viaSHA alla43: qui si aggiunge collaudo, non nuovo gameplay.

## Caso dedicato

Due profili sintetici locali, PortTestOwner e PortTestGuest, entrano in un
DedicatedServer ordinario su127.0.0.1:25596. Verificati guest senza monta/comandi
falsificati, controllo del proprietario, volo osservato da entrambi, aggiornamenti
server di sesso, velocita e armature testa/coda. Dopo le prove il proprietario
esce ANCORA MONTATO: assert UNLOADED_WITH_PLAYER, salvataggio nativo nel suo
RootVehicle. Arresto ordinato del server e nuovo processo sullo STESSO mondo.
Due nuovi processi client rientrano con gli stessi UUID di sviluppo.

Asserzioni server e client: stesso UUID drago, proprietario ancora pilota,
ospite senza posto, eta50, maschio, armature0x1001 e volo ripristinato. Nome e
flagNoAI=false conservati sul server. Carcassa separata75giorni, salute0 e
DeathStage2 salvati nel mondo;20parti transienti ricostruite con UUID nuovi,
nessun orfano/duplicato. Due server e quattro processi rendering,6exit0.
Il report finale richiede actualWorldRestart=true e mountedLogoutRelogin=true.
Inventario verificato: armature del drago, non tutto l'inventario del giocatore.
La velocita0.25 viene applicata dal fixture server, non rivendicata come dato
salvato nel player. I profili sono offline locali, non account autenticati.

## Esiti e immagini

Regressione ordinaria build/runGametest/compileClientGametestJava/runClientGametest
verde10m34s (phase44-final.log):94JUnit senza errori,141GameTest mod+baseline142,
14prove client e40PNG correnti nella run44. XML senza failure/error.
Prova dedicata definitiva phase44-dedicated-launch-3.log:6exit0,5PNG tra21:36:43
e21:37:19, totale45PNG nella consegna. Nuove due immagini rientro lette e
controllate, owner effettivamente montato e guest con vista completa distante.
Le tre immagini prima del logout mantengono nomi storici0.1.35, ma sono catture
correnti della44; versione runtime attestata nei log. Quella guest iniziale e
ravvicinata/tagliata: non e prova visiva dell'intero modello.

Primo controllo delle immagini aveva trovato LoadingTerrain nella cattura owner
nonostante metadati pronti. Aggiunta attesa gui.screen e overlay nulli piu10tick,
poi prova dedicata ripetuta. La suite ordinaria non usa quella classe helper e
resta valida. Nuovo export inoltra esplicitamente username e --nogui: il piano
Loom estratto prima dell'esecuzione non conteneva questi programArgs del DSL.
Nessun file account Microsoft letto. I test precedenti non sono falsamente
presentati come nuovi bug della mod o come immagini finali valide.

## Riproduzione e isolamento

Stessa procedura della43, con -PiafDedicatedWorldTest -PiafMountedReloginTest,
piani dedicati e scripts/run-dedicated-world-test.ps1 -SessionDirectory <nuova>
-MountedRelogin. Il modulo seleziona DedicatedDragonMountedReloginTest, separato
dal test di salvataggio dopo dismount della43. Server test locale: IP127.0.0.1,
porta25596,2slot,white-list=false,online-mode=false,enforce-secure-profile=false,
mondo flat NUOVO,creative,peaceful,pause-when-empty-seconds=0 e view/simulation5.
Il lanciatore avvia tre processi, attende fine/salvataggio, riapre e avvia gli
altri due client. Controlla report e TUTTI i6exit finali, ferma ordinatamente i
propri processi agli errori. Non riusare un mondo personale o una sessione gia
marcata ready; eula secondo accettazione locale Minecraft. Nessun harness nelJAR.

Log/report/config del SOLO server test, confronto classi,6exit e5PNG sono in
dedicated-mounted-test-results. Prova precedente43 dopo dismount resta nel suo
pacchetto43; non viene etichettata come prova nuova44. Non certificati arresti
forzati, pacchetti persi/timeout reali, account autenticati o server pubblico.
Gli altri punti di STATO-TAPPA-1.md restano aperti.