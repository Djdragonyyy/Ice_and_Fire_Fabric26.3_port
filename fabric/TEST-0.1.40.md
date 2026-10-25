# Prova 0.1.40 - carcassa e raccolta del drago del fuoco

Checkpoint tappa1 ancora incompleta. Ultima istruzione operativa25ottobre:
continuare fino a esaurimento quota, poi spegnimento ordinato dopo checkpoint;
controllo automatico ogni50minuti. I vecchi divieti storici sono superati.

## Comportamento

Alla morte il drago conserva una carcassa con NBT originali ModelDead/DeathStage.
Ferma IA, volo, combattimento, crescita e fame; lascia pilota, spalla e preda.
Niente nuova immunita da caduta al pilota in aria o modifica del NoAI della preda.
Corno/cristallo/monta/inventario e altri draghi non trattano i resti come vivi.
XP e inventario vengono rilasciati una sola volta dalla pipeline nativa di morte.
Il solo isAlive dei resti segue la convenzione originale per consentire raccolta
/persistenza; tutte le azioni vive vengono bloccate esplicitamente.

Bottiglia di vetro: un sangue nei primi passi, consumata salvo creativo.
Mano vuota: drop originali di carne/scaglie, cuore al punto intermedio, ossa e
infine teschio con eta/stadio. Le femmine adulte danno un uovo di colore casuale
insieme al cuore; la loot table femmina mantiene anche l'entrata uovo con peso2.
Il teschio finale elimina la carcassa. Prima dei5giorni il limite originale e0:
nessuna carcassa raccoglibile; a5giorni un solo passo da teschio.

Limite interazione min(eta/5,25), soglia scheletro (eta/5)/2. Conservata la diversa
soglia delle eta oltre125giorni. Mano vuota incrementa lo stato prima del tiro di
loot, come originale. Loot table male/female/skeleton migrate al formato nativo,
pesi60scaglie/40carne e2uovo femmina, funzione customize_to_dragon con quantita e
variante originali; scaglie giovanili possono avere quantita0. Nessun minimo
inventato. Si conserva il primo stack della tabella come nel codice sorgente.

Config server in iceandfire-combat.json: dragonDropBlood, dragonDropHeart,
dragonDropSkull, tuttetrue di default anche caricando un vecchio config.
Semantica originale esplicita: prendere sangue nel passo del cuore rinuncia a
cuore/uovo; disabilitare dragonDropSkull blocca TUTTA la raccolta a mano vuota,
comprese le ossa. I resti possono essere rimossi con /kill.

Adattamenti: sangue lasciato a terra se inventario pieno (anziche perso);
/kill/bypass invulnerabilita elimina i resti senza una seconda morte/ricompensa;
input di raccolta caricati limitati per evitare valori invalidi. Corpse con
ModelDeadtrue viene caricata a salute0. Non si modifica alcun PNG/UV/geometria.

## Rendering

Posa originale firedragon_dead.tbl, progressione.5/tick fino20 e interpolazione
originale delle cosce. Niente idle, head tracking, ali o attacchi sui resti.
Pelle originale con occhi chiusi finche integra, texture scheletro per stadio
successivamente; overlay maschile mantenuto solo sulla pelle, occhi esclusi.
Niente rotazione/rimozione vanilla a20tick.

## Verifiche

Suite definitiva build/compileClientGametestJava/runGametest/runClientGametest
verde in17m14s:94JUnit,133GameTest mod (134 con baseline),13prove client.
Log phase40-final-2.log incluso come build-e-test.log; XML senza failure/error.
Prima suite completa gia verde in9m49s, poi revisione ha trovato aggiornamento
di crescita/fame dopo una morte dentro super.tick e gravita non ripristinata
immediatamente. Correzioni fuori revisione, nuovi test,9prove mirate verdi in57s
e intera suite ripetuta. Si consegna esclusivamente l'ultima esecuzione.

Nove nuove prove native coprono: retention/XP-inventario e /kill, ciclo completo
cuore/uovo/ossa/teschio, treconfig e priorita sangue, salvataggio/caricamento con
proprieta risolta, morte dentro tick al confine giorno/fame, rilascio pilota in
volo/spalla/preda, tutti quattro colori e quantita originali del loot, inventario
pieno/permessi, confine eta4/5. I test XP38 e tutti quelli precedenti ancora verdi.
Per la persistenza e stato usato un giocatore server registrato: il mock non
registrato conservava lo UUID ma non poteva risolversi dopo load. Asserzione sulla
proprieta mantenuta, modificato il fixture anziche indebolire il controllo.

Nuovi JUnit: soglie/configurazioni, confronto pesi/roll/item/codec con le tre JSON
originali, posa DEAD e cosce lineari, nessuna animazione viva o accumulo sui resti.
Nuovo client: morte reale e sincronizzazione nei cinque stadi; poi22interazioni
C2S di raccolta su femmina stadio5/112giorni,3sangue, skin scheletro al passo11,
un teschio finale e rimozione.39PNG correnti, cinque nuove tavole lette. Le tavole
normalizzano le dimensioni e confrontano gli stati render pelle/scheletro; il
cambio di skin e verificato anche sulla carcassa reale durante la raccolta.

## Limiti

Multipart non ancora portati. Altre parita/configurazioni, camera/animazioni e
navigazione complessa rimangono nello stato cumulativo. DedicatedServer ordinario,
account autenticati e mondo MP salvato/riaperto non sono provati dal roundtrip NBT
qui eseguito. La prova separata a due client resta0.1.37, non riusata come prova40.
Punte grigie e cause intermittenti sonno/escort non dichiarate risolte. Tappa1 e
port completo restano aperti; proseguimento autonomo autorizzato.