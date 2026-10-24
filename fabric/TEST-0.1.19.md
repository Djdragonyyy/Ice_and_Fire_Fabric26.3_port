# Ice and Fire 0.1.19 — riproduzione dei draghi di fuoco

Minecraft Java26.3, Java25, Loader0.19.5, Fabric API0.163.0+26.3.
Sostituire il precedente JAR della mod; non tenere versioni duplicate.

## Prova in gioco

1. Preparare un maschio e una femmina almeno allo stadio4 (75giorni) su terreno
   largo e piano. Impostare entrambi liberi, senza bersagli o passeggeri.
2. Dare uno stufato di fuoco (iceandfire:fire_stew) a ciascuno. Compariranno i
   cuori; il cibo è disponibile in creativo. Non richiede cambio proprietario.
3. Si avvicinano e dopo almeno60tick di corteggiamento, a distanza<35 e con
   visuale libera, producono un solo uovo di colore casuale e un nido3x3.
4. Lanciare una nuova riproduzione richiede6000tick di cooldown. Salvataggio e
   riapertura conservano questo valore senza alterare la crescita del drago.
5. Colpire e raccogliere il nuovo uovo, poi ripiazzarlo: diventa del giocatore
   che lo piazza. Incubarlo nel fuoco per oltre7200tick per ottenere il piccolo.

## Comportamento e limiti

Entrambi devono essere innamorati, liberi, a terra, senza IA disabilitata,
combattimento o controllo. Stadio3, stesso sesso e cooldown non generano uova.
Ricerca del partner entro180blocchi fra entità caricate; nessun caricamento
forzato. Sonno e decollo casuale non interrompono il corteggiamento a terra.

Il nido rispetta mobGriefing e configurazione del terreno. Non sovrascrive
contenitori, blocchi protetti/indistruttibili, fluidi o terreno fuori dai chunk
caricati/bordo. Posizione uovo ostruita: niente uovo, cooldown o modifica del
terreno; il tentativo può ripetersi. Queste protezioni correggono il controllo
originale troppo permissivo. I blocchi del nido e i modelli/PNG sono originali.
Uovo non spingibile, come originale, per restare nel nido fra i genitori.

L'uovo nato non ha proprietario, come createEgg originale; la raccolta produce
un nuovo oggetto senza incubazione o proprietario. Una prova server verifica
l'intero passaggio riproduzione, raccolta, piazzamento e schiusa con proprietà.
Nessuna ricetta/progressione o generazione naturale aggiunta: fasi successive.

## Verifica

Comando: .\fabric\gradlew.bat -p fabric build runGametest runClientGametest.
Verificati61 JUnit,66 GameTest della mod (67 con baseline vanilla escluso) e3 prove client con sei screenshot ispezionati. Nessuna prova con due veri client.
Le prove client verificano anche i modelli di stufato/nido e conservano le
regressioni armature, combattimento, GUI e sonno. Nessuna prova con due veri
client. Tappa1 ancora incompleta; vedere STATO-TAPPA-1.md.
