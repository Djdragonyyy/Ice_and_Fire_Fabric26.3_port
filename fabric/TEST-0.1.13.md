# Ice and Fire 0.1.13 — armature, seguito e recupero a 150 blocchi

Minecraft Java26.3, Java25, Fabric Loader0.19.5. Installare su client e server:

- iceandfire-fabric-26.3-0.1.13-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il precedente JAR Ice and Fire, senza duplicati in mods.
Sorgenti ZIP e report non vanno in mods. Riepilogo cumulativo: STATO-TAPPA-1.md.

## Correzioni

Armature: ordine di disegno esplicito dopo la pelle. Riprodotta nel client la
scomparsa degli overlay testa/coda con il renderer0.1.12; nello stesso scenario
0.1.13 risultano visibili sulla GUI del drago. Screenshot prima/dopo inclusi.
Verificate anche le 32 icone originali nella griglia e gli oggetti sincronizzati
nei due slot della GUI. Nella prova pulita le icone autonome erano visibili anche
prima: non è stato identificato un PNG mancante o modificato un asset alla cieca.

Seguimi più rapido a terra: velocità1.5, 2 oltre16 blocchi, percorso ogni5 tick.
In volo: bersaglio stabile e anticipato, aggiornato ogni10 tick, recupero della
distanza più rapido e atterraggio quando il proprietario è vicino e a terra.
Conservati decollo, volo e IA in libero/seguimi; testato il movimento orizzontale
reale del vagabondaggio senza proprietario, oltre al decollo e alla salita.
Corretti anche i bersagli ripidi: conservata una componente orizzontale positiva
e una velocità minima di crociera per evitare che vanilla azzeri i piccoli impulsi.

A distanza >=150 blocchi il drago in seguimi cerca circa ogni secondo uno spazio
libero vicino al proprietario e si teletrasporta. Supporto, intero corpo, fluidi,
bordo del mondo, altezza e chunk caricati controllati. Arrivo aereo ammesso solo
dallo stadio2 se il proprietario è in aria. Seduto/libero, NoAI, pilota o passeggeri
escludono il teletrasporto; niente cambio dimensione o caricamento forzato di chunk.
Il drago deve essere caricato e simulato. Se non esiste un arrivo sicuro riprova.
Questa è una modifica al gameplay richiesta dall'utente; sostituisce il precedente
seguito senza teletrasporto. Il pilota resta dallo stadio3, una sola seduta.

## Verifiche

53 JUnit, 27 GameTest della mod (più always_pass vanilla escluso), un test client
con mondo temporaneo, modelli reali, sincronizzazione menu e screenshot GPU.
Comandi riproducibili con JDK25:

```powershell
.\fabric\gradlew.bat -p fabric clean build runGametest runClientGametest --console=plain
```

La test mod client/server è esclusa dal JAR distribuito. Revisione read-only
con skill review-agent del diff completo da8476a4143, senza delega: No findings.
Nessun collaudo multiplayer con due veri client; le schermate sono una prova
grafica automatica in un client locale, non una conferma del profilo dell'utente.

## Prova utente prima di proseguire

1. Armature head/tail di ferro e altri materiali: creativo, slot GUI e modello
   in mondo/anteprima; confrontare con gli screenshot forniti.
2. In seguimi allontanarsi a piedi/correndo, poi oltre30 blocchi o in quota per
   il decollo; osservare il recupero della distanza e l'atterraggio vicino a terra.
3. Allontanarsi circa150 blocchi mantenendo il drago simulato: arrivo vicino in
   uno spazio libero. Ripetere con seduto, pilota a bordo e spazio stretto.
4. In libero verificare cammino e decollo autonomo; il trigger originale diurno
   è casuale1/1500 per tick, non un decollo obbligatorio immediato.
5. Riprovare spalle, inventario, alimentazione, camera e controlli V/spazio/X/R/G.

La tappa1 resta incompleta e le punte grigie sono ancora aperte. Prima di
proseguire con combattimento autonomo e altre lacune, raccogliere il riscontro
su queste correzioni. L'elenco completo implementato/da fare è STATO-TAPPA-1.md.
