# Prompt temporaneo per la prossima ripresa

Aggiornato il 24 ottobre 2026, checkpoint 0.1.20, nuova autorizzazione utente a proseguire nelle fasi.
Conservare fabric/HANDOFF.md: contiene lo storico e i vincoli permanenti.

---

Continua il porting di Ice and Fire. Comunica in italiano. Leggi integralmente
fabric/HANDOFF.md, fabric/README.md, fabric/REVIEW.md, fabric/TEST-0.1.20.md e gli
AGENTS.md applicabili. Non ricominciare da zero o sovrascrivere modifiche locali.

Repository personale:
https://github.com/Djdragonyyy/Ice_and_Fire_Fabric26.3_port.git
Ramo esclusivo: port/fabric-26.3. Controlla stato, remote e git log prima di
modificare; git switch e git pull --ff-only origin port/fabric-26.3, preservando
il lavoro locale. Verifica l'esistenza del commit base 417f5a8d2. Niente reset.

Prima leggere fabric/STATO-TAPPA-1.md. Checkpoint0.1.19: riproduzione con fire_stew,
ricerca/corteggiamento, uovo/nido, cooldown6000 salvato e protezioni del terreno.
Uovo nato senza proprietario, raccolta/ripiazzamento lo assegnano; test fino alla
schiusa. Uova non spingibili come originale. Conservato tutto fino alla0.1.18.

ULTIMA AUTORIZZAZIONE: utente a dormire, autonomia completa, test di ogni blocco,
proseguire il più possibile nelle fasi usando quota disponibile. Sospensione PC
richiesta alla fine della sessione di test, dopo salvataggio/push/consegna.
Sessione0.1.18 sospesa e ripresa correttamente, nuova quota disponibile; non
avviare nuovi processi durante sospensione. Goal completo attivo e incompleto.
Non consumare quota artificialmente o dichiarare chiuse fasi non migrate.
Nuova richiesta supera attese di riscontro fra checkpoint, conserva ordine delle
tappe e loro verifica. La1 è ancora incompleta.

Checkpoint0.1.20: layer stendardo BodyUpper animato con colori/motivi e snapshot sincronizzato. Prossimi: shake prey/
caccia, stendardo, configurazioni/interazioni/animazioni e limiti navigazione/
acqua/lava nello stato cumulativo. Uova raccolte senza incubazione/proprietà.
Sonno solo libero, NoAI congela automazione; targeting separato da movimento per
difesa dal sonno. Occhi order1, armature2..5. Conservare correzioni0.1.15,
teletrasporto richiesto, unica seduta, niente dorso solido. Punte grigie aperte.

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
