# Prova 0.1.48 - alimentazione autonoma della DragonForge

Checkpoint di sviluppo del 27 ottobre 2026. Tappa 1 e port ancora aperti.
Struttura, inventario, ricetta, hopper e GUI sono descritti in TEST-0.1.47.md.

## Uso

Inserire ferro e sangue del drago di fuoco nella forgia assemblata. Un drago
di fuoco addomesticato, sveglio, a terra e almeno allo stage 2 puo alimentare
l'apertura con il soffio reale. Lasciare libera la visuale dalla testa
all'apertura e tenere il drago vicino: il limite originale e distanza al
quadrato < 115 * stage, circa 15 blocchi allo stage 2 e 24 allo stage 5.
La ricerca iniziale usa un volume di 50 blocchi attorno all'apertura; non
equivale a un soffio lungo 50 blocchi. Non avviene teletrasporto alla forgia.

Il comando fermo e quello libero consentono il lavoro; scorta, bersaglio di
combattimento, pilota/passeggeri, sonno, NoAI, acqua, volo e corteggiamento
lo interrompono o impediscono. Un drago impegnato resta fermo, punta la forgia
e non decolla casualmente. Il soffio mantiene il riscaldamento iniziale e
continua oltre il timeout del combattimento; non spara proiettili alla forgia.
La lavorazione usa lo stesso percorso di danno/terreno del soffio esistente.

Una prenotazione transiente impedisce a due aperture di contendersi lo stesso
drago nello stesso intervallo. Il nucleo rinnova la prenotazione finche puo
produrre e la rilascia a ingredienti esauriti, risultato incompatibile/pieno,
struttura smontata o rimozione della block entity. Il drago ricontrolla anche
vista, priorita e nucleo; la prenotazione scade senza rinnovo. Non salva target
o riferimenti entity nell'NBT e non forza caricamenti di chunk.

Il nucleo ora espone anche il comparatore originale: segnale proporzionale
al contenuto effettivo dei tre slot, da 0 a 15.

## Verifica

Full pulito phase48-final.log PASS in 11m 11s: 98 JUnit, 158 GameTest
della mod + baseline = 159 server, 16 prove client e 46 PNG. XML senza errori.
Nuova PNG del drago al lavoro aperta e rivista dopo il full.

I nuovi test server usano un drago reale con IA attiva: produzione esatta di
un lingotto e consumo degli ingredienti, posizione ferma e assenza di charge,
rilascio dopo produzione, priorita di comando/scorta/NoAI/bersaglio, ostacolo
alla visuale, range e prenotazione, rimozione nativa del nucleo, comparatore.
Due test unitari verificano riscaldamento, continuita e mantenimento di una
nuova animazione melee quando termina il modo forgia.

La prova client conserva la fixture rame/progresso 70000 SOLO nel mod test
per controllare trasporto GUI a 32 bit. Poi sostituisce gli ingredienti con
ferro/sangue, crea un drago con IA attiva e attende getto completo, apertura
accesa e progresso autorevole della ricetta originale da 1000. La nuova PNG
mostra il lavoro reale; la fixture precedente non viene spacciata per esso.

## Limiti

Non sono ancora portate le ricette di costruzione della forgia e la catena dei
blocchi di scaglie. Non e una dichiarazione di parita completa: mancano catene,
altri tipi di drago e successive tappe. Le prenotazioni sono rivalutate dopo
caricamento, ma questa versione non aggiunge una nuova prova dedicata di
riavvio/chunk unload durante la produzione o server con account autenticati.
Restano le verifiche residue di IA/config/ciclo vitale, navigazione complessa,
anteprima passeggeri e punte grigie del modello.
