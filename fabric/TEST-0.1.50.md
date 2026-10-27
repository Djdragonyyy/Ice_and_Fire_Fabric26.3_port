# Prova 0.1.50 - visuale automatica durante la cavalcata

Checkpoint verificato del27ottobre2026; tappa 1 e port ancora aperti.

## Opzione originale

Il file locale config/iceandfire-client.json espone dragonAuto3rdPerson,
abilitata per impostazione predefinita come nel ClientConfig originale.
Con valore true, quando il proprietario monta il proprio drago come pilota
la vista passa alla terza persona posteriore. Alla discesa viene ripristinata
la prospettiva che il giocatore aveva prima di montare.

La vista viene cambiata solo sulle transizioni di monta/discesa: durante la
cavalcata resta possibile scegliere manualmente prima o terza persona.
Con valore false il port conserva la scelta del giocatore sia montando sia
scendendo. Il ripristino automatico si applica anche lasciando il mondo
mentre si e ancora in sella. Cambiare l'opzione nel file prima dell'avvio
del client; non e un'opzione del server o sincronizzata agli altri giocatori.

La configurazione viene creata alla prima esecuzione; un file non valido
viene segnalato nel log e usa il valore originale. Restano camera/distanza,
controlli e occultamento della testa nella prima persona gia disponibili.
Nessuna modifica al dismount, alle immunita o alla logica server del drago.

## Verifica

Full pulito phase50-final.log PASS in 11m 14s: 99 JUnit, 162 GameTest della
mod + baseline = 163 server, 17 prove client e 48 PNG. XML senza errori.
PNG finale della visuale montata aperta e rivista. Confronto JAR: 115 classi
byte-identiche, solo inizializzatore client modificato e due classi client
aggiunte; nessuna classe server o di entita cambiata.

Nuova prova con client e server integrato reali: interazione C2S con un drago
adulto posseduto, passaggio prima->terza persona, scelta manuale mantenuta per
20tick e ripristino alla discesa autorevole; false e true letti realmente dal
file locale; false conserva la vista scelta; chiusura del mondo ancora montato
ripristina la precedente vista frontale. Configurazione e opzioni iniziali
del client di prova vengono ripristinate alla fine.

La variazione manuale della prospettiva nel test usa l'opzione nativa del
client; non equivale a una nuova prova fisica del tasto F5. L'uscita dalla
cavalcatura nel test viene richiesta sul server, senza simulare Shift.
Il caso di chiusura del mondo e un disconnect ordinato del client integrato,
non un crash o una nuova prova di server dedicato autenticato.

La nuova PNG mostra la visuale posteriore ottenuta alla monta. Restano gli
audit residui di tappa1, anteprima passeggeri, punte grigie e navigazione
complessa. TEST-0.1.49.md contiene la costruzione della DragonForge completata.
