# Prova 0.1.35 — due client in processi separati

Checkpoint parziale tappa1. Non sospendere o spegnere il PC.

## Risultato

Tre JVM separate: un GameTestServer nativo standalone e due client Minecraft
con rendering reale, collegati via TCP esclusivamente127.0.0.1. Profili fittizi
di sviluppo, UUID distinti. Il GameTestServer ha di norma un solo slot: un
mixin del solo harness lo porta a2. Nessun listener o mixin del test nel JAR
installabile; controllato il contenuto dell'archivio distribuito.

Passate le verifiche di proprieta: interazione del non proprietario non monta;
i suoi pacchetti di decollo/fuoco/attacco non comandano il drago. Il proprietario
monta realmente e il suo decollo produce volo sul server, osservato dai due
client. Entrambi ricevono UUID proprietario, sesso, head/tail in ferro e valori
server0.5/0.25 del moltiplicatore, mentre il loro file locale mantiene1.
Controllate immagini proprietario in prima/terza persona e osservatore rivolto
al drago. Disconnessione nativa di entrambi e chiusura di tutte tre le JVM con
codice0. Un GameTest di rete superato, XML senza errori e report JSON.

La suite ordinaria definitiva, eseguita senza opt-in di rete, passa invariata:
**79JUnit,117GameTest mod (118 con baseline),10prove client e20immagini**,
build8m45s. Aggiunte3immagini di rete, per23complessive nella consegna.
Log phase35-final.log. Report, codici di uscita e log dei tre processi sono
nella cartella network-test-results del pacchetto; piani di lancio e file di
coordinamento privati esclusi. Versione0.1.35 verificata nei log delle tre JVM.

## Harness e riproduzione

Windows/PowerShell7, Java25 e dipendenze gia preparate. Dalla radice della repo:

```powershell
$session = Join-Path $env:TEMP ('iaf-network-' + [Guid]::NewGuid())
New-Item -ItemType Directory -Path $session | Out-Null
$listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
$listener.Start()
$port = $listener.LocalEndpoint.Port
$listener.Stop()
$init = (Resolve-Path fabric/scripts/emit-network-launch-plans.gradle).Path
.\fabric\gradlew.bat -p fabric runNetworkServerGametest runNetworkOwnerGametest runNetworkGuestGametest -PiafNetworkTest "-PiafNetworkDir=$session" "-PiafNetworkPort=$port" -I $init --offline --console=plain
& ./fabric/scripts/run-network-launch-plans.ps1 -SessionDirectory $session
```

Sorgenti/supporti separati e opt-in, esclusi dalla build normale. I piani nativi
Loom1.18.4 permettono di compilare prima e avviare le tre JVM senza lock Gradle
concorrenti. Autenticazione Microsoft disattivata nel generatore dei piani:
nessun account personale letto. Il runner drena entrambi gli stream, controlla
anche i codici di uscita finali e pulisce soltanto i processi che ha creato.
Non considerare sufficiente report.json se il runner non termina con codice0.

Tentativi iniziali dell'harness: capienza fissa1 respingeva il secondo client;
poi il framework richiedeva una disconnessione esplicita prima di concludere.
Risolti nel solo test con capienza2 e handshake di chiusura. Una prima immagine
dell'osservatore guardava il suolo: corretta l'inquadratura, aggiunte armature
head/tail e ripetuta la prova. L'ultimo ciclo ha anche controllo finale di tutti
gli exit code e manifest process-exits.json. Nessuna modifica alla logica di
gioco per far superare queste prove.

## Limiti e prossima richiesta

Il server usato e GameTestServer standalone con due slot di prova. Rimangono
collaudo su DedicatedServer ordinario, account autenticati, persistenza di una
sessione multiplayer completa e stress prolungato. Il contrattacco intermittente
non e stato riprodotto nella suite finale ma resta aperto; diagnostica mantenuta.

Nuova priorita esplicita dell'utente: rivedere feeding in tutti5gli stadi e
modello, con attenzione a CORPO E ALI e aderenza alle texture originali. Analisi
avviata, nessuna correzione di feeding/modello consegnata in questa versione.
Verificare prima immagini e geometria/UV; non ricostruire o riempire texture
alla cieca. Punte grigie, altre parita originali e tappa1/port completo aperti.


Nota di tracciabilita dalla0.1.37: la vecchia consegna0.1.35 includeva anche
0000_dragon-network-owner-0.1.35.png, residua di un tentativo precedente alle tre
immagini finali. Il numero effettivo di PNG nel vecchio archivio era quindi24,
non23. Archivio/checksum storico conservati; conteggi dei test invariati.
La consegna0.1.37 usa una whitelist di tre PNG correnti con verifica della
versione nei log e dell'orario di acquisizione, senza quella immagine storica.
