# Prova 0.1.31 — movimento in acqua e lava

Checkpoint parziale della tappa1. Non sospendere o spegnere il PC.

## Modifiche

Ripristinata la soglia originale di immersione nell'acqua secondo lo stadio:
profondita maggiore di stage/2 con divisione intera. Il goal di galleggiamento
26.3 interrogava direttamente il fluido, saltando questa regola: ora la rispetta.

Con pilota, fuori dal volo, SPAZIO/X producono gli input verticali originali
(+/-0.5 acqua, +/-0.8 lava). In lava tornano curva di velocita originale,
sprint1.4, attrito0.7, smorzamento0.3 con entrambi i tasti e risalita0.2
contro un ostacolo orizzontale. Movimento fisico e autorita locale nativi:
il server segue il vero giocatore pilota, senza introdurre nuovi pacchetti.
Senza pilota la lava conserva il moto e applica l'attrito originale0.7.
Malus di percorso originali per fuoco/lava. Immunita al fuoco gia presente.

## Verifica

Due prove numeriche, tre nuove prove GameTest native (immersione per tutti
cinque gli stadi, movimenti effettivi UP/DOWN e sprint/attrito, immunita e
moto senza pilota). Ottava prova client: proprietario connesso realmente,
tastiera e pacchetti originali in due mondi separati acqua/lava; salita e
discesa misurate sia sul client sia sul server, entrambi i tasti in lava.
Prima della misura si attende l'assestamento fisico dell'ingresso nel fluido:
nessuna modifica alla gravita per soddisfare la prova.

Suite finale sul codice definitivo: **77JUnit,112GameTest mod (113 con baseline vanilla),8prove client e17immagini**, tutte passate. Build completa riuscita in8m35s; log phase31-final.log. Nessuna prova con due client reali.

Il primo tentativo completo ha fallito nel test storico del contrattacco
notturno. Le sette prove del sonno sono poi passate isolate senza cambiare
asserzioni, distanza o scadenze; aggiunta diagnostica al messaggio di errore.

## Limiti

Non e ancora migrato il ramo originale speciale sopra la superficie della
lava. Restano parita dei moltiplicatori di velocita, navigazione complessa,
altri comportamenti originali e prova con due client reali su server dedicato.
Nessuna modifica alle texture/punte grigie; nessuna dichiarazione di completamento
della tappa1 o del port completo. I test brevi di nuoto verificano che non si
attivi immediatamente il volo; le regole esistenti di decollo restano attive.

Il secondo tentativo ha superato il sonno, ma ha rilevato un errore di misura
nel vecchio wandering: controllava solo lo spostamento finale e poteva bocciare
un percorso circolare. La fixture registra ora la massima escursione orizzontale
nei medesimi85tick, mantenendo soglia>1blocco e salita>8. Nessuna modifica alla
IA per forzare un percorso. Osservazione terminata prima di succeed/cleanup.