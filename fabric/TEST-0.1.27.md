# Ice and Fire 0.1.27 — flauto e palpebre

Minecraft Java26.3, Java25, Loader0.19.5, API0.163.0+26.3.
Checkpoint parziale della tappa1, ancora incompleta.

## Flauto del drago

/give @s iceandfire:dragon_flute

Disponibile anche fra gli ingredienti creativi. Uso con clic destro, in entrambe
le mani: interrompe volo/hover dei draghi di fuoco addomesticati del proprietario
nella zona originale. Comando, bersaglio e pilota restano al loro posto; poi
si applicano gravità e controlli esistenti. Le cadute in aria del giocatore
conservano le regole richieste: il flauto non concede immunità da caduta.

Cooldown originale60tick (circa3secondi a20TPS), oggetto riutilizzabile e suono
originale con pitch1.75. Effetti sulle entità soltanto server; il client riceve
volo e cooldown attraverso i canali nativi. Ricerca nel livello corrente fra
entità già caricate, ordinate per distanza, senza generare nuovi chunk.

config/iceandfire-combat.json: dragonFluteDistance, default4, intervallo0..10000,
misurato in chunk. Zona originale: box da(x,y,z)a(x+1,y+1,z+1), ampliato di16*valore suX/Z e256 suY. Il raggio di base
predefinito è64blocchi;0 conserva la stretta colonna1x1 originale. Riavviare dopo
modifica; vecchi JSON mantengono4 e valori invalidi causano ripiego sui default.
Le altre creature che in origine rispondono al flauto appartengono alle fasi
successive; in questa tappa risponde il drago di fuoco.

## Palpebre

Come EntityDragonBase originale: occhi chiusi ai tick44..49 di ogni ciclo50.
Il renderer usa la texture sleeping originale e sospende lo strato emissivo,
lasciando posa del corpo, IA, stato sonno e combattimento invariati. Nessun nuovo
NBT o messaggio di rete: il conteggio locale dell'entità segue l'originale.

Le prove client controllano texture/render state e catturano occhi aperti e
chiusi con il renderer nativo, congelando il contatore soltanto nella preview
di test e ripristinandolo subito. Sono fixture grafiche, non modifiche alla mod.

## Verifica

Quattro GameTest nuovi: box configurato/proprietà, cooldown60/offhand, pilota e
stato di caduta invariati, limiti dei fotogrammi di blink su tre cicli. Estesi
JUnit di configurazione per default/intervalli/zero. Client: flauto nel sound
manager e nelle icone, vero useItem server e sincronizzazione volo/cooldown;
preview nativa aperto/chiuso con stato sleeping e progressione corpo a zero.

Le vecchie prove di fine attacco/pasto ora distinguono SPEAK cosmetico dagli
attacchi offensivi; danni, target, controllo del pilota e NoAI restano verificati.
Il caso del bersaglio rimosso è sazio per isolare nuove cacce e avvia un verso
esplicito: deve rimanere senza target pur mostrando SPEAK. Nessuna modifica
alla logica di combattimento o alla durata dei suoi attacchi per adattare i test.

Modello, texture e OGG del flauto originali. Punte grigie ancora aperte, nessuna
modifica a UV/geometria. Multiplayer con due veri client e altre lacune della
tappa1 restano nello stato cumulativo. Nessuna sospensione del PC.

Verificati 70 JUnit, 92 GameTest della mod (93 con baseline vanilla escluso) e 5 prove client con quattordici screenshot. Nessuna prova con due veri client.
Build completa e revisione finale riuscite. Dopo la suite completa, ripetute soltanto le cinque prove client per migliorare angolo/zoom della fixture: tutte passate,14immagini finali nuove. Ispezionato il confronto laterale aperto/chiuso e il flauto nelle icone. Log consegnato concatena suite completa e ripetizione client; report server completo93.
