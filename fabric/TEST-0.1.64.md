# Prova 0.1.64 - inizializzazione dello spawn

Portato finalizeSpawn originale: sesso casuale, crescita di1..80giorni,
una delle quattro varianti, drago in piedi, salute massima corrente e fame50.
La crescita usa le opzioni server esistenti. L'inizializzazione riguarda
il callback nativo di spawn, non il costruttore: schiusa e caricamento NBT
continuano a usare la creazione senza inizializzazione casuale. Non viene
aggiunta generazione naturale o un nuovo oggetto per lo spawn.

AgeTicks rimane l'eta del drago; il contatore vanilla Age viene normalizzato
a0 nello spawn inizializzato, per evitare il dimezzamento fisico del gruppo
vanilla baby. Il ramo originale derivava isBaby dallo stadio; nel26.3 quel
metodo e final. Questo adattamento conserva la scala propria del drago,
non certifica la parita di ogni API vanilla relativa ai cuccioli. La crescita
manuale continua a usare le stesse regole e il blocco del pasto malato.

Due nuove prove native: vero codice SummonCommand.createEntity con
inizializzazione, tre esemplari e salvataggio/ricaricamento; NBT espliciti
con inizializzazione esclusa conservano neonato/colore/sesso scelti.
EntityType.spawn verifica salute/danno configurati; il callback con gruppo
vanilla forzato baby conserva il gruppo, azzera il suo clock e usa la scala
fisica propria una volta. Nessun seed o asserzione statistica fragile.
Baseline sul codice63 FAIL38s nelle due prove per eta iniziale0; stesse
prove dopo la correzione PASS37s:101JUnit3server(2mod+baseline).

Full pulito phase64-final.log PASS13m 59s:101JUnit191server
(190mod+baseline)20client66PNG, XML senza errori, comprese le precedenti
prove di schiusa. JAR63->64:124classi identiche,6differenti, nessuna aggiunta
o rimozione. Cinque anonimi hanno codice/struttura javap identici e soli
metadati diversi; solo FireDragon cambia comportamento. Tutte le classi
client identiche63; nessun harness nel JAR. Tappa1 e port restano incompleti.

Nessuna nuova prova dedicata o autenticazione;52 resta distinta.
