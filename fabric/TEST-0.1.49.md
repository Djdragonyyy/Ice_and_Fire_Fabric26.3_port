# Prova 0.1.49 - costruzione della DragonForge e blocchi di scaglie

Checkpoint verificato di sviluppo del27ottobre2026; tappa 1 e port aperti.
Per struttura e funzionamento della forgia vedere TEST-0.1.47.md e TEST-0.1.48.md.

## Ricette originali

Le quattro varianti di fuoco rosso/verde/bronzo/grigio si comprimono con nove
scaglie dello stesso colore in un blocco, e si decompongono senza perdita in
nove scaglie. Conservati ID originali: oggetti dragonscales_COLOR, blocchi
dragonscale_COLOR. Questi ultimi entrano nel tag c:storage_blocks/scales/dragon/fire.

Ricette da banco 3x3, con costi originali:

- Blocco di ossa: nove dragonbone -> un blocco.
- Quattro mattoni della forgia: SBS/BSB/SBS, S blocco di scaglie di fuoco,
  B mattone di pietra. I colori possono essere misti.
- Nucleo: BBB/BHB/BBB, B mattone della forgia, H cuore di drago di fuoco.
- Apertura: BIB/I I/BIB, B mattone della forgia, I lingotto di ferro.

Il vecchio risultato core_disabled diventa l'ID unico dragonforge_fire_core:
lo stato ACTIVE continua a gestire la struttura senza sostituire la block entity.
Ferro tramite tag comune c:ingots/iron, nessuna riduzione dei costi originali.

Per una struttura completa servono otto blocchi di ossa, 17 mattoni, un nucleo
e un'apertura. Contando anche i mattoni richiesti da nucleo/apertura, sono 29;
otto lavorazioni producono 32 mattoni e ne avanzano tre. Costo teorico: 360
scaglie, 32 mattoni di pietra, 72 ossa, un cuore e quattro lingotti di ferro.
Questo e un conteggio delle ricette: non una nuova prova di raccolta completa
in survival dall'uccisione di un drago alla costruzione del multiblocco.

## Blocchi, risorse e ricettario

Proprieta originali: durezza30, resistenza500, suono pietra, BASEDRUM, dynamicShape,
strumento corretto richiesto. Tag piccone e protezione dai draghi. Tooltip col
nome del colore e stile originale. Modelli, stati e texture copiati dall'originale;
definizioni item, loot e advancement adattati al formato nativo26.3.
Le ricette sono nel ricettario e si sbloccano tramite acquisizione dei materiali.

## Verifiche

Full pulito phase49-final-2.log PASS in 11m 29s: 99 JUnit, 162 GameTest della
mod + baseline = 163 server, 16 prove client e 47 PNG. XML senza errori.
Icone finali su due righe e quattro blocchi piazzati aperti e rivisti.

Test server: compressione/decompressione di tutti i colori tramite CraftingMenu
e ResultSlot nativi, usando il risultato compresso effettivo per decomprimere;
consumo esatto della griglia, trasferimento nell'inventario, ricette di ossa/
mattoni/nucleo/apertura, rifiuto di ingredienti insufficienti o cuore errato,
tag/proprieta/loot e sblocco ricettario tramite cambi inventario.
JUnit aggiunta per confronto byte dei 16 modelli/stati/texture originali.
Client: nove icone materiali risolte dal renderer e quattro blocchi piazzati;
conservata anche la prova del drago che alimenta realmente la forgia.

## Limiti ancora aperti

Questa consegna completa le ricette di costruzione della forgia di fuoco e la
catena dei blocchi di scaglie. Non certifica parita completa con tutta la mod:
catene, draghi ghiaccio/fulmine, loro forge e tappe successive restano da portare.
Nessuna nuova prova dedicata di riavvio durante produzione. Restano gli audit
residui su configurazioni/ciclo vitale/navigazione e rifiniture del modello e
della GUI. Le prove dedicate44 restano riferite alla loro versione.
