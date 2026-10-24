# Ice and Fire 0.1.26 — corno del drago

Minecraft Java26.3, Java25, Loader0.19.5 e Fabric API0.163.0+26.3.
Checkpoint parziale della tappa1, ancora incompleta.

Ottenere il corno dalle componenti creative oppure:
/give @s iceandfire:dragon_horn

Con il corno vuoto nella mano principale, clic destro sul proprio drago di
fuoco per conservarlo. Il contenitore salva l'entità nativa completa e rimuove
il drago dal mondo senza morte o rilascio degli oggetti. Nome, UUID, proprietà,
crescita, salute, fame, sesso/variante, comandi e inventario restano nel dato.
La crescita e l'IA sono ferme finché il drago è conservato, come nell'originale.

Clic destro sulla faccia superiore di un blocco per rilasciarlo. Il proprietario
originale resta tale anche se un altro giocatore trasporta/rilascia il corno.
Dati rimossi SOLO dopo inserimento riuscito nel mondo. In caso di ostacoli,
quota/bordo, chunk non caricati, permessi o UUID già caricato in una dimensione,
il contenuto resta conservato. Non vengono richiesti nuovi chunk per piazzarlo.
Il confronto UUID riguarda le entità caricate; non è un registro globale delle
copie creative o dei salvataggi storici. Sono accettati solo draghi di fuoco
con dati nativi identificabili; ghiaccio/fulmine appartengono alla tappa2.

Il corno non sovrascrive dati occupati, nemmeno di tipi non supportati. Non si
può catturare un drago con pilota/preda a bordo, che sia passeggero o in spalla.
Queste protezioni aggiuntive evitano di eliminare un veicolo occupato e non
introducono altre sedute. Raccolta consentita al proprietario in sopravvivenza
e creativo; rilascio rispetta i permessi di piazzamento anche in avventura.

Icone vuoto/fuoco e trasformazioni in mano usano modelli e texture originali.
Il tooltip mostra tipo, nome (o senza nome), sesso, stadio/giorni. Nella26.3
la selezione dell'icona usa un componente modello nativo al posto del vecchio
predicate Forge. I dati del drago sono in CUSTOM_DATA e passano dal codec
nativo ItemStack, quindi persistono anche nel salvataggio dell'inventario.

La revisione ha trovato il caso creativo: Player.interactOn passa una copia
dell'ItemStack. Ora viene aggiornato il vero corno in mano, come nell'originale;
una prova passa attraverso quel ramo nativo, senza chiamare solo il metodo item.
Le prove server coprono roundtrip oggetto/entità con inventario e UUID, ostacolo,
faccia errata, rilascio singolo/UUID duplicato caricato, proprietà, mano, pilota,
dati occupati, creativo, avventura e dato senza UUID. Il client verifica anche
la vera interazione, sincronizzazione rimozione/rilascio e entrambe le icone.

Tappa1 e port completo restano aperti. Nessuna prova con due veri client;
ricette/progressione completa restano nelle fasi successive. Nessuna modifica
agli asset originali, alle punte grigie o ai controlli del drago.
La sospensione del PC è annullata: continuare dopo test e consegna.

Verificati 70 JUnit, 88 GameTest della mod (89 con baseline vanilla escluso) e 5 prove client con dodici screenshot. Nessuna prova con due veri client.
Build completa finale e revisione in sola lettura superate. Screenshot delle due icone ispezionato; i quattro asset originali di modello/texture confrontati byte per byte nel JAR.
