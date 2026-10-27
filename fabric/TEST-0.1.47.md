# Prova 0.1.47 - DragonForge di fuoco alimentata manualmente

Checkpoint di sviluppo del 27 ottobre 2026. Tappa1 e port ancora aperti.

## Funzionamento e struttura

Prima lavorazione funzionante con il soffio gia disponibile. Il nucleo usa
inventario nativo a tre slot, una ricetta dati del nuovo RecipeManager e GUI
originale. Il ferro con sangue del drago di fuoco produce un lingotto originale
di acciaio di fuoco; valore cook_time1000. Ogni impatto aggiunge al massimo una
unita nello stesso game tick e rinnova calore40; il tick del nucleo fa avanzare
il progresso come nell'originale. Non equivale a1000tick di tempo reale.
Senza ricetta valida il comportamento originale produce CENERE, valore100:
mettere quindi ferro e sangue appropriati per ottenere il lingotto.

Struttura3x3x3: otto blocchi di ossa,17mattoni della forgia di fuoco, un nucleo
centrale e un'apertura su un lato orizzontale centrale. In ciascuno strato alto
/basso: ossa ai quattro angoli, mattoni negli altri cinque posti. Strato medio:
nucleo al centro, quattro angoli di mattoni e tre lati di mattoni; quarto lato
apertura. Il getto va sull'apertura. E' gia possibile usare il drago cavalcabile
con il suo comando del soffio; il test server usa il percorso DragonFireCombat,
non una chiamata diretta alla creazione del lingotto. Anche mobGriefingfalse
consente alimentazione, preservando i blocchi protetti. Blocchi e lingotto sono
nei gruppi creativi. Ricette di costruzione e alimentazione autonoma NON incluse.

Hopper: sopra/lati accedono ai due ingressi originali, sotto al risultato.
Il filtro sangue usa le ricette reali, non un elenco finto. Il nucleo assemblato
emette15 e attiva le griglie; input riscaldato e disattivazione a fine calore,
alla rottura o tramite controllo schedulato quando manca un nucleo caldo.
Inventario/progresso salvati e contenuti rilasciati dal callback nativo alla
rottura, una sola volta. Output pieno/incompatibile ferma la produzione.

## Adattamenti e risorse

Sorgenti originali: TileEntityDragonforge, TileEntityDragonforgeInput/Brick,
BlockDragonforgeCore/Input/Bricks, DragonForgeRecipe, Container/GuiDragonForge e
IafDragonDestructionManager. LGPL3 e attribuzioni conservate nella distribuzione.

Core ACTIVE nello stesso blocco al posto di scambio fra ID attivo/disattivo;
cosi la block entity conserva inventario e menu. Input e mattoni espongono il
nucleo tramite WorldlyContainerHolder, senza inventari/BE duplicati. Il target
Forge non carica chunk mancanti per cercare un nucleo. Non e certificata
migrazione di mondi Forge1.20 o compatibilita con il vecchio ID core_disabled.

RecipeSerializer26.3 usa MapCodec/StreamCodec e ItemStackTemplate: il vecchio
ItemStack durante il caricamento anticipa i componenti degli oggetti e fallisce.
Ingredienti/risultato adottano formato nativo, tag ferro c:ingots/iron. Progresso
persistito int e GUI32bit tramite quattro parole16bit; limite ricette1..1000000.
I PNG, modelli, metadati animazione e GUI selezionati restano byte-identici
all'originale, verificati dal nuovo JUnit. Bone block conserva asse, suono legno,
resistenza e requisito utensile originali. Client/server usano i sistemi nativi.

## Prove effettivamente superate

Full: build runGametest compileClientGametestJava runClientGametest --offline
--console=plain, phase47-final.log BUILD SUCCESSFUL10m50s.96JUnit,153GameTest mod
piu baseline154,16classi client,45PNG finali. XML senza errori/failure.

Sei prove native: caricamento ricetta e roundtrip codec rete; assemblaggio,
deduplicazione potenza e output pieno/incompatibile; salvataggio/caricamento
block entity e cambio ACTIVE senza perdita inventario, luce15 e drop esatti;
hopper tramite cap/lato e menu con output effettivo nell'inventario; cenere e
arresto progresso dopo40calore; callback schedulata su input senza nucleo e
senza notifiche vicini; produzione ferro/sangue attraverso il getto reale con
mobGriefingfalse. Alcuni di questi controlli sono nello stesso metodo di test.
Cinque prove gia passate nel focus, sei nella suite finale dopo ampliamento.
Il controllo input orfano NON viene presentato come un riavvio o unload reale.

Nuovo client: modelli nativi dei cinque oggetti, multiblocco reale, apertura
C2S dall'apertura, inventario e progresso sincronizzati, shift-click autorevole
esattamente un lingotto e chiusura automatica quando il giocatore si allontana.
Una ricetta SOLTANTO nel mod client-test usa rame e durata100000 con progresso
70000 impostato nel fixture, per verificare davvero il trasporto oltre65535.
Il lingotto mostrato in quella GUI e impostato per test inventario, distinto
dalla prova di produzione con ferro del GameTest server. Ricetta lunga e classi
test assenti dal JAR. La prima asserzione C2S anticipava il pacchetto, leggendo
lo slot gia svuotato dalla previsione client: aggiunta attesa, conteggio reale
mantenuto e verificato anche nel backend nativo. Nessun bug di duplicazione
inventato per giustificare la modifica della sola fixture.

Tre PNG finali0042/0043/0044 lette: icone originali, struttura completa con base
visibile e GUI originale con barra70%. I nomi delle altre42immagini restano
storici, ma catture correnti nella run47. Nessuna nuova prova DedicatedServer,
ricarica da disco di un intero mondo Forge, crash o account autenticati.

## Ancora aperto

Alimentazione automatica tramite drago, comparatore, ricette di costruzione e
blocchi scale/progressione. Non viene dichiarata Forge completa da questo primo
nucleo manuale. Restano anche gli altri punti di STATO-TAPPA-1.md. Consegna:
due JAR, sorgenti versionati, log/XML,45PNG e checksum SHA256.
