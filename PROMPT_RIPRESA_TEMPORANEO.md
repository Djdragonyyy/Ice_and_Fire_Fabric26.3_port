# Prompt temporaneo per la prossima ripresa

## Ultima istruzione utente — niente sospensione, ripresa al reset

Il 24 ottobre, dopo il risveglio della0.1.23, l'utente ha annullato esplicitamente
ogni futura sospensione del PC: continuare fino al completamento del port.
La sospensione precedente è riuscita e il helper è terminato. NON lanciare più
suspend-after-delivery.ps1 o altri comandi di sospensione/spegnimento. Questa
istruzione prevale su TUTTE le note storiche sotto e negli altri documenti.

Usare la quota residua per progresso reale e riprendere appena il limite d'uso
si reimposta, nella stessa chat e con lo stesso goal completo, senza nuove chat
o lavorazioni duplicate. Non consumare quota artificialmente e non dichiarare
completate fasi con funzioni mancanti. Al risveglio controllo quota93%; reset
indicato dall'app24ottobre12:38:03UTC (14:38Roma). Verificare stato attuale.

Aggiornato il 24 ottobre 2026, checkpoint 0.1.23, nuova autorizzazione utente a proseguire nelle fasi.
Conservare fabric/HANDOFF.md: contiene lo storico e i vincoli permanenti.

---

Continua il porting di Ice and Fire. Comunica in italiano. Leggi integralmente
fabric/HANDOFF.md, fabric/README.md, fabric/REVIEW.md, fabric/TEST-0.1.23.md e gli
AGENTS.md applicabili. Non ricominciare da zero o sovrascrivere modifiche locali.

Repository personale:
https://github.com/Djdragonyyy/Ice_and_Fire_Fabric26.3_port.git
Ramo esclusivo: port/fabric-26.3. Controlla stato, remote e git log prima di
modificare; git switch e git pull --ff-only origin port/fabric-26.3, preservando
il lavoro locale. Verifica l'esistenza del commit base 417f5a8d2. Niente reset.

Prima leggere fabric/STATO-TAPPA-1.md. Checkpoint0.1.23: config salute/danno/fame/
schiusa, default/vecchi JSON conservati e incubazione long compatibile con vecchi
NBT int, soglia massima senza overflow. Fine sessione: sospensione richiesta
dopo push/consegna. Goal completo attivo e incompleto.

Checkpoint0.1.22: presa/scuotimento di
prede mob con passeggero nativo, ID separato dal pilota e UUID/tick salvati.
Danno singolo dopo55tick, cura/fame e rilascio; cancellazioni e priorità comandi.
Pose originali e recupero della presa dal caricatore ricorsivo. Conservati tutti
i checkpoint precedenti: riproduzione/nido, stendardi, caccia, sonno/occhi,
cibo, attacchi, armature, volo, controlli e proprietà. Tappa1 incompleta.

ULTIMA AUTORIZZAZIONE: utente a dormire, autonomia completa, test di ogni blocco,
proseguire il più possibile nelle fasi usando quota disponibile. Sospensione PC
richiesta alla fine della sessione di test, dopo salvataggio/push/consegna.
Sessione0.1.18 sospesa e ripresa correttamente, nuova quota disponibile; non
avviare nuovi processi durante sospensione. Goal completo attivo e incompleto.
Non consumare quota artificialmente o dichiarare chiuse fasi non migrate.
Nuova richiesta supera attese di riscontro fra checkpoint, conserva ordine delle
tappe e loro verifica. La1 è ancora incompleta.

Prossimi: configurazioni originali ancora fisse, interazioni e animazioni
secondarie/suoni, dettagli acqua/lava e navigazione complessa. Usare lo stato
cumulativo; caccia/stendardo/sonno/riproduzione/presa sono già presenti, non
ricominciare quei blocchi. Giocatori esclusi dalla presa in questa fase.
Nessuna mutazione NoAI della preda, un solo occupante e nessun nuovo posto.
Uova raccolte senza incubazione/proprietà. Sonno libero, targeting separato dal
movimento per difesa dal sonno; occhi ordine1 e armature2..5. Punte grigie aperte.

Toolchain fissata: Minecraft Java 26.3, JDK25, Loader0.19.5, Fabric API
0.163.0+26.3, Loom1.18.4, Gradle9.7.1. Usare fabric/ come progetto Gradle
indipendente, senza compilare per errore il vecchio progetto Forge nella radice.
Sul PC di ripresa è stato usato Azul Zulu25.0.4.1+1 locale, con SHA-256 verificato;
non riutilizzare alla cieca percorsi di altri PC. Il launcher Windows usa
JAVA_HOME\bin\java.exe: il percorso misto con /bin/ falliva in questo ambiente.
Cache, JDK, mondi e build non sono nel Git e vanno ricostruiti dopo un clone.

```powershell
.\fabric\gradlew.bat -p fabric clean build runGametest --console=plain
```

Prosegui la tappa1 confrontando sempre i sorgenti originali conservati in src/.
Prossimi blocchi: configurazioni originali residue, altre interazioni, animazioni
secondarie e suoni; verificare lacune nello stato cumulativo.
Il flight manager attuale non è un pathfinder aereo completo per aggirare edifici
o grotte; acqua/lava complete e configurazioni di volo restano da migrare.
Pianifica e verifica i comportamenti prima di dichiararli migrati.

Vincoli permanenti:

- Tappa1 incompleta: completarla/verificarla prima della2; avanzamento nelle fasi ora autorizzato.
- Punte grigie ancora aperte: diagnosi e immagini già ricevute in HANDOFF.md.
  Non cambiare UV o riempire trasparenze alla cieca; non dichiararle risolte.
- Non reintrodurre il dorso solido derivato dal ghast. Un solo pilota adulto.
  Conservare camera, controlli e tutte le funzionalità confermate.
- I piccoli in spalla sono entità del mondo salvate indipendentemente dal
  giocatore. Non aggirare il divieto26.3 di usare PLAYER come veicolo.
- Volo autonomo dallo stadio2, pilota dallo stadio3: non confondere le due soglie.
- Conservare modelli, texture, statistiche e meccaniche originali, LGPL e
  attribuzioni. Distinguere limiti del port e comportamento originale.
- Ogni consegna deve includere SEMPRE due JAR installabili: mod senza suffisso
  -sources e Fabric API0.163.0+26.3, più istruzioni, sorgenti e checksum.
  Mai più versioni della mod insieme in mods.
- Revisionare il diff completo. Usare review-agent se disponibile, in sola
  lettura e senza delega; correggere fuori dalla revisione e ripetere i controlli.
  Verificare le skill sul PC corrente, senza attribuire uso a skill assenti.
- Commit e push al solo ramo port/fabric-26.3 dell'origin personale autorizzati.
  Niente merge sul principale o release pubbliche. Non stampare email o
  credenziali: usare l'autore noreply verificato dalla storia, niente config globale.
- L'ultimo comando prima della risposta finale deve mostrare 417f5a8d2:
  git show -s --format=%h --abbrev=9 417f5a8d2

Esegui il lavoro autorizzato, non fermarti al piano. Aggiorna handoff, prompt di
ripresa e documentazione. Consegna risultati e limiti senza attribuire ai test
prove mai svolte.
