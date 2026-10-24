# Ice and Fire 0.1.22 — presa e scuotimento della preda

Minecraft Java26.3, Java25, Loader0.19.5, API0.163.0+26.3. Installare i due JAR
del pacchetto sostituendo la mod precedente, senza duplicati. Tappa1 incompleta.

## Attacco

Fra gli attacchi successivi al primo morso compare SHAKE_PREY. Quando scelto,
prova la presa con probabilità1/2 originale; se non è valida usa il morso.
Richiede drago libero a terra, stadio2+, preda mob larga meno di metà del corpo,
visibilità, portata del morso, assenza di pilota o altri passeggeri. Le prede
già montate o con propri passeggeri non vengono strappate dal loro veicolo.

Il drago si ferma, solleva e scuote la preda con il movimento originale. Posa
originale65tick: grab1/2 e grab_shake1/2/3. Nessun danno fino al tick56, poi un
solo colpo pari a2*ATTACK_DAMAGE, cura del50% del danno nominale se abilitata e
+1fame. Rilascia la preda e termina il recupero al65. Il primo attacco contro
un nuovo bersaglio resta il morso; gli altri quattro vengono scelti casualmente.

Comandi, NoAI, morte/rimozione, preda morta o diventata alleata, acqua, volo e
nuovo bersaglio difensivo interrompono la presa. Sonno e movimento casuale non
partono durante l'attacco. Dopo il rilascio il proprietario può occupare subito
il posto libero e interrompere i fotogrammi residui di recupero.

C'è un solo occupante nativo: pilota proprietario oppure preda. In questa fase
la cattura riguarda soltanto i mob; i giocatori restano esclusi dalla presa,
anche se l'originale poteva catturarli. Nessun secondo posto per giocatori,
nessun dorso solido e nessuna modifica a dismount o immunità in volo.

## Salvataggio e sincronizzazione

Relazione passeggero vanilla e ID preda sincronizzato; la preda non diventa mai
il pilota. Non viene cambiato il suo NoAI. Salvataggio dei passeggeri nativo più
UUID e tick della presa: caricamento ricostruisce il legame e riprende il windup
residuo. Validazione dopo refresh delle dimensioni fisiche del drago, necessaria
al primo tick del caricamento. Nessuna seconda entità creata per la preda.

## Verifiche

Due test numerici del movimento: tempi di sollevamento, raggio, rotazione,
scuotimento e crescita. Regressioni di posa estese al nuovo attacco fino al65.
Cinque GameTest: presa reale/posizione/danno singolo/sonno; taglia e controlli,
parete e posto unico; cancellazioni/nuovo alleato/NoAI; caricatore ricorsivo nativo
con ripresa esatta e singolo danno; cura disabilitata e priorità del pilota.
Test client delle pose esteso a SHAKE, con passeggero reale sincronizzato,
screenshot GPU e verifica leggibilità delle sei righe di statistiche.

Verificati66 JUnit,78 GameTest della mod (79 con baseline vanilla escluso) e4 prove client con dieci screenshot. Nessuna prova con due veri client. Le prove NBT
usano il caricatore nativo sul server, senza due veri client o riavvio completo
del processo durante l'attacco. Non dichiarano completato il port o la tappa1.

La prova manuale della cura incrementa tickCount come ServerLevel: chiamare
Entity.tick lasciando il contatore a0 eseguiva artificialmente il controllo
della fame a ogni iterazione. Corretto il simulatore del test, non la produzione.

Aggancio visivo da LayerDragonRider originale: catena BodyUpper/Neck1/Neck2/
Neck3/Head, offset e orientamenti distinti per quadrupedi/corpi verticali/cavalli,
scala inversa del drago prima del renderer della preda. Mixin client di posizione
e rotazione, senza duplicare il mob o modificarne AI/rotazione fisica. Stato
riutilizzato azzera la rotazione al rilascio. La preda usa la visibilità del
portatore come il layer originale; prova sintetica di frustum aveva riprodotto
la sua scomparsa al bordo della vista ed è ora superata. Non modifica il culling
delle texture o il difetto delle punte grigie.

Due ulteriori JUnit verificano trasformazione rigida, scala unitaria, rotazione
e aggancio alla testa animata. Test client comprende screenshot ravvicinato in
mondo e controllo della visibilità a frustum, oltre alle pose/statistiche GUI.
L'anteprima GUI mostra il drago; il passeggero è verificato nel mondo, non come
seconda entità renderizzata dentro l'anteprima. Nessun test con shader esterni.