# Prompt temporaneo per la prossima ripresa

Aggiornato il 24 ottobre 2026, checkpoint 0.1.14 dopo conferma utente della 0.1.13.
Conservare fabric/HANDOFF.md: contiene lo storico e i vincoli permanenti.

---

Continua il porting di Ice and Fire. Comunica in italiano. Leggi integralmente
fabric/HANDOFF.md, fabric/README.md, fabric/REVIEW.md, fabric/TEST-0.1.14.md e gli
AGENTS.md applicabili. Non ricominciare da zero o sovrascrivere modifiche locali.

Repository personale:
https://github.com/Djdragonyyy/Ice_and_Fire_Fabric26.3_port.git
Ramo esclusivo: port/fabric-26.3. Controlla stato, remote e git log prima di
modificare; git switch e git pull --ff-only origin port/fabric-26.3, preservando
il lavoro locale. Verifica l'esistenza del commit base 417f5a8d2. Niente reset.

Prima di continuare leggere anche fabric/STATO-TAPPA-1.md. Il 24 ottobre l'utente
ha confermato la 0.1.13 funzionante e autorizzato a proseguire. Il teletrasporto
richiesto a150 blocchi resta autorizzato. La precedente sospensione PC era una
richiesta specifica già eseguita: non sospendere di nuovo senza nuova richiesta.

Checkpoint attuale: 0.1.14-stage1-dev, prima difesa autonoma terrestre.
Goal originali per bersaglio del proprietario, aggressore del proprietario e del
drago; inseguimento1.5, ricalcolo4..10tick, morso35tick, impatto16..24, un colpo
riuscito per animazione, controlli server di portata/visibilità/alleanze.
Seduto/NoAI/pilota/spalle interrompono. Cambiare bersaglio riavvia la carica.
In aria atterra per combattere a terra: comportamento transitorio del port,
non dichiarare migrate le modalità aeree. Caccia, altri attacchi e soffio autonomo
restano da implementare. Conservare R/G controllati dal pilota.
La 0.1.13 è l'ultima confermata dall'utente; la difesa0.1.14 richiede collaudo.

Build riuscita: 53 JUnit, 34 GameTest della mod (più always_pass vanilla escluso)
e una prova client con mondo e screenshot superati. Revisione completa secondo
review-agent in sola lettura senza delega: No findings. Nessun test con due client.
Non estendere test automatici a gameplay o multiplayer con due client mai provati.

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
Prossimi blocchi: completare combattimento autonomo e altri attacchi, cibo da terra,
interazioni e animazioni mancanti, sonno/riproduzione e layer dello stendardo.
Il flight manager attuale non è un pathfinder aereo completo per aggirare edifici
o grotte; acqua/lava complete e configurazioni di volo restano da migrare.
Pianifica e verifica i comportamenti prima di dichiararli migrati.

Vincoli permanenti:

- Tappa1 incompleta: non passare alla tappa2 senza completamento e riscontro.
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
