# Prova 0.1.39 - oggetti della raccolta e teschio

Checkpoint di sviluppo della tappa1, ancora incompleta. Nessuna sospensione PC.

## Modifiche

Registrati dragonbone, quattro dragonscales del fuoco, fire_dragon_heart,
fire_dragon_blood, fire_dragon_flesh e dragon_skull_fire. Modelli/PNG originali,
nuove definizioni item native26.3 e traduzioni inglesi/italiane. Colore scaglie
nel nome anziche tooltip separato originale; nessuna modifica ai pixel.
Carne originale:8nutrimento, modificatore saturazione.8, consumo normale e non
sempre edibile, classificazione meat/wolf_food, cinque secondi di fuoco sul server.

Teschio piazzabile come entita decorativa dragon_skull (collider originale.9x.65),
NoAI, persistente e non spingibile. Rendering della sola testa e discendenti dal
Tabula originale, skin scheletrica dello stadio, scala e trasformazioni originali
terra/parete. Frustum culling escluso come nel renderer originale. Rotazione con
shift e colpo per recuperarlo. Niente parti del corpo o modelli sostitutivi.
Stage/DragonAge/DragonType nei CUSTOM_DATA, Type/Stage/DragonAge/DragonYaw nel
salvataggio entita; default item stadio4/75giorni disponibile immediatamente.
Input importati limitati per evitare indici/scale invalidi. Tipo fuoco soltanto.

Adattamenti espliciti: mantiene il nome anche dopo rottura, controlla permessi,
ostacoli e confine mondo prima di consumare; consuma solo dopo spawn riuscito.
Il drago non considera il teschio una preda/bersaglio vivo del soffio.
Questi oggetti sono le dipendenze del ciclo carcassa/raccolta ancora da collegare:
non vengono ancora ottenuti raccogliendo una carcassa in questa versione.

## Verifiche

88JUnit verdi, inclusi riferimenti numerici di crescita originali, input invalidi,
sottostruttura testa, posa50gradi e reset fra terra/parete senza accumulo.
124GameTest mod (125 con baseline) verdi nella suite server completa. Tre nuovi:
- metadati/nome ai cinque stadi: item codec, piazzamento, interazione shift nativa,
  salvataggio entita e recupero esattamente una volta;
- default creativo, posa parete, ostacoli, consumo e protezione da caccia/soffio;
- consumo nativo della carne:8nutrimento,12.8saturazione,100tick fuoco, tag corretti.

12prove client verdi. La nuova usa pacchetti C2S reali per piazzare tutti cinque i
teschi e attaccarne uno: sincronizzazione metadati e un solo drop verificati.
34PNG correnti, compresa tavola GPU dei cinque stadi in entrambe le pose e nove
icone. Le dimensioni nella tavola sono normalizzate; la posa parete e uno stato
render di confronto, non una prova fisica prolungata del supporto. Tavola letta.
Tutte9texture item risolte dai modelli e confrontate tramite hash con originali.

Trasparenza delle esecuzioni: prima prova mirata3GameTest passata; la suite completa
ha superato JUnit/server ma il client ha rilevato il riferimento errato della carne
(fire_dragon_meat.png, non fire_dragon_flesh.png). Corretto SOLO include risorsa e
relativa aspettativa client; build e intera suite client ripetute, verdi in2m01s.
Nessun Java produttivo cambiato dopo il server completo. build-e-test.log contiene
la prima esecuzione e quella finale con intestazione esplicita. Non si dichiara
che la prima esecuzione fosse verde. XML server e JUnit senza errori.

## Ancora aperto

Carcassa persistente/raccolta con loot/config originali, multipart e altre parita.
DedicatedServer ordinario/account autenticati e persistenza MP ancora da collaudare.
La prova separata a due client rimane0.1.37, non inclusa come prova39. Punte grigie
e cause intermittenti sonno/escort aperte. Nessuna tappa completa chiusa qui.