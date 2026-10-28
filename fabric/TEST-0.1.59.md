# Prova 0.1.59 - etichette originali dell'inventario

Checkpoint del28ottobre2026. Tappa1 e port ancora incompleti.

GuiDragon originale distingue nome personalizzato/Unnamed e premette
Nome, Sesso e Proprietario ai rispettivi valori. DragonScreen ora conserva
queste informazioni in entrambe le lingue portate. La salute visualizzata
e limitata al massimo corrente. Nessun cambio a dimensioni, geometria,
UV, immagini, animazioni, logica server, rete o inventario.

DragonInventoryClientTest continua a usare il proprietario montato reale,
apertura del menu sul server, smontaggio e tre prese/rilasci reali. In piu
rimuove il nome personalizzato sul server, attende la sincronizzazione
al client e fotografa il ritratto senza nome; ripristina poi Ember. Le
immagini del menu con nome/etichette e senza nome sono state aperte.

Full pulito phase59-final.log PASS13m 51s,exit0:100JUnit,20prove
client/63PNG e183server(182mod+baseline), XML senza errori. Nessun init
privato durante la build; il clean ha eliminato le classi e i metadati
generati dal confronto originale. Delta JAR58->59:128classi identiche,
solo DragonScreen differente, nessuna aggiunta/rimozione; classi comuni
identiche. Nessun observer/bridge/harness di test nel JAR.

Separatamente sono stati avviati Minecraft1.20.1/NeoForge47.1.79/Citadel
5075402 e il port Fabric per trenta pose abbinate nei cinque stadi.
VISUAL-REFERENCE-0.1.58.md documenta metodo, osservazioni e limiti: punte
grigie presenti nell'originale, corpo/ali corrispondenti nei campioni,
recupero intenzionale della mandibola nell'ultimo tratto del feeding e
differenza ancora aperta dell'overlay sul maschio appena nato. Sono pose
di rendering isolate; non vengono sommate ai test di gameplay pubblici.

Restano navigazione avanzata/scavo forzato, audit delle configurazioni,
ciclo vitale e ulteriori dettagli visivi. Non e una nuova prova con
client autenticati o riavvio dedicato; la verifica52 resta distinta.
