# Ice and Fire 0.1.20 — stendardo sul drago

Installare Minecraft Java26.3, Java25, Fabric Loader0.19.5 e i due JAR del
pacchetto. Sostituire la vecchia mod senza duplicati. Tappa1 ancora incompleta.

## Stendardo

Aprire l'inventario con Maiusc e mano vuota sul proprio drago; inserire uno
stendardo nello slot in basso a sinistra. Il drago mostra lo stendardo agganciato
al BodyUpper animato, con offset e orientamento originali. La dimensione dello
stendardo rimane indipendente dalla crescita del drago, come il layer originale.

Il modello speciale vanilla conserva colore di base e motivi dello stendardo.
Si aggiorna per chi osserva il drago anche a inventario chiuso; rimuovendo
l'oggetto scompare. Invisibilità del drago nasconde il layer. Salvataggio e drop
continuano a usare soltanto l'inventario: lo snapshot sincronizzato per il
renderer non è un secondo oggetto da salvare o rilasciare.

## Verifiche

Nuova prova server: componenti, snapshot non mutabile, salvataggio, rimozione e
rifiuto di oggetti invalidi. Prova matematica: trasformazione sul corpo e scala
costante con varie pose/stadi. Nuova prova client reale: stendardo giallo con
croce blu/bordo rosso, sincronizzazione dei componenti, modello speciale non
vuoto, screenshot equipaggiato e rimosso. Restano tutte le regressioni precedenti.

Comando: .\fabric\gradlew.bat -p fabric build runGametest runClientGametest.
Verificati62 JUnit,67 GameTest della mod (68 con baseline vanilla escluso) e4 prove client con otto screenshot. Nessuna prova con due veri client. Nessun test con due veri
client. La tappa1 e il port completo restano aperti; vedere STATO-TAPPA-1.md.

La scala reale dello stendardo è conservata separatamente dalla normalizzazione
della GUI, evitando che l'anteprima adulta lo ingrandisca oltre il riquadro.
