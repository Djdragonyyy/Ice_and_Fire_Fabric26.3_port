# Prova 0.1.46 - richiamo con stufato di fuoco

Checkpoint verificato il 27 ottobre 2026. Full phase46-final-2.log verde in 10m52s: 95 JUnit, 147 GameTest mod + baseline148, 15 client e42PNG correnti. XML senza failure/error. Tappa1 aperta.

Originale EntityDragonBase.registerGoals: TemptGoal priorita6, velocita1 e
canScare=false. IafItemTags.TEMPT_DRAGON contiene soltanto FIRE_STEW. Portato tag
nel percorso nativo singolare tags/item. Goal vanilla26.3 usa un Predicate e
l'attributo TEMPT_RANGE: impostato10, equivalente al raggio vanilla originale.
Rispetta difesa/comandi/pilota/sonno/NoAI/volo/corteggiamento e cibo gia riservato;
resta sopra wander. I decolli casuali non interrompono il richiamo attivo.
Nessun cambiamento al cibo necessario per riproduzione o consumo manuale.

Due nuovi GameTest nativi passati in phase46-focused-3.log,28s: tag reale,
mano principale/secondaria, manzo non valido, comandi/sonno/NoAI/volo/bersaglio/
cibo a terra/corteggiamento/cavaliere. Altra prova lascia il selettore IA reale
muovere il drago100tick verso il giocatore con stufato, poi rimuove l'oggetto e
verifica fine del richiamo. Nessun consumo/fame/riproduzione al solo tenere
l'oggetto. Non e una nuova prova visiva del movimento a tutti gli stadi.
I primi due tentativi focus fallivano prima dei test per percorso init relativo
e filtro con nomi anziche snake_case registrato; corretti senza cambi gameplay.

Nessun nuovo audio, texture, modello o screenshot dedicato al richiamo. Le
42PNG attese del full sono regressione client attuale; i nomi storici delle
catture restano, non copiarle da vecchie run. DedicatedServer44 non rieseguito.
DragonForge ancora assente; ricognizione separata conservata nel lavoro privato.

## Regressione e correzione della fixture

Primo full:148test, un fallimento nella prova preesistente del cambio velocita
in volo; le nuove prove del richiamo passavano anche nel full. La prova di volo
isolata passava senza modifiche. Esperimento controllato con yaw4 riproduce
velocita orizzontale zero: sterzata di4gradi ancora perpendicolare alla rotta est,
accelerazione X3.4064220784115324E-18, poi cutoff nativo per assi sotto0.003.
Rotta libera, meta identica, modifier1: quel primo fotogramma non dimostra un
blocco dell'IA. La fixture ora guarda lungo la rotta prima del tick di ripresa,
verifica la precondizione sopra il cutoff e l'identita esatta della destinazione;
asserzioni di speed0 e movimento reale mantenute. Focus allineato verde22s e
full finale ripetuto senza errori. Nessuna modifica alla sterzata produttiva.

42PNG attuali nella run46, non ricopiate dalla45: alcuni nomi restano storici.
I due pannelli uova/piccolo sono stati aperti di nuovo e controllati nella46.
Etichette della sola tavola uova parzialmente a contatto gia dichiarate nella45;
entita complete. Nessuna nuova prova visiva del richiamo a tutti gli stadi.
Consegna due JAR, sorgenti completi, report/log e checksum. Tappa1 incompleta.
