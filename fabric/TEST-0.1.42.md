# Prova 0.1.42 - esplosione opzionale delle cariche

Checkpoint verificato del26ottobre2026. Tappa1 e port completo ancora aperti.

Configurazione server iceandfire-combat.json: explosiveDragonBreath=false.
I vecchi JSON mantengono false e il comportamento precedente. Quando true,
l'impatto di una carica aggiunge l'esplosione: potenza originale min(2,stadio-2),
con valori non positivi degli stadi1/2 omessi. Solo cariche, non ogni tick del
soffio continuo. DamageSource del fuoco mantiene attribuzione al pilota/drago.

Pipeline nativa26 per danno, occlusione, impulso, suoni, particelle e drop.
Non e una replica dei dettagli visivi/drop di BlockLaunchExplosion legacy;
non vengono create finte falling-block entita o modificati PNG/modelli.
Le trasformazioni del terreno e il danno ordinario delle cariche restano presenti.
L'esplosione non crea ulteriore fuoco vanilla.

Owner, alleati, passeggeri e carcasse protetti da danno e knockback. Tutte le
parti multipart escluse dall'area per non moltiplicare il danno sul genitore.
Il corpo riceve il singolo danno nativo e condivide i normali cooldown. Niente
nuova immunita al dismount aereo. Le regole del terreno rimangono vincolanti:
mobGriefing, dragonGriefing=2, tamedDragonGriefing=false. In quel caso esplosione
con danno/effetti ma senza distruzione. Blacklist originale e blocchi infrangibili
non esplodono e schermano i raggi. Config false lascia intatta la0.1.41.

Tre nuove prove native mirate passate in28s (phase42-focused-2.log): defaultfalse
e stadio2 senza danno/impulso, carica reale stadio5 con ulteriore danno/impulso;
proprietario realmente registrato e drago alleato protetti,10parti senza impulso;
regole del terreno e cassapanca/sbarre/bedrock preservate, lana ordinaria distrutta.
Il primo tentativo compilava i test con vecchi nomi API (PIG/WHITE_WOOL): corretti
in EntityTypes.PIG e Blocks.WOOL.white(), nessuna modifica al codice produzione.

Suite finale phase42-final.log: BUILD SUCCESSFUL in11m06s,94JUnit senza errori,
141GameTest mod+baseline142 tutti verdi,14prove client e40PNG generate nella
run0.1.42 tra20:29:22 e20:31:24. Task unit up-to-date sugli stessi input.
Panoramica0039 corrente controllata; nessuna nuova modifica visiva.
Non e un collaudo visivo della nuova esplosione abilitata con un client o una
nuova prova DedicatedServer. Gli altri gap tappa1 restano in STATO-TAPPA-1.md.
