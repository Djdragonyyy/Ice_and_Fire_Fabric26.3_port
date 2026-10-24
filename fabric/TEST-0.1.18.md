# Ice and Fire 0.1.18 — sonno notturno e occhi

Minecraft26.3, Java25, Loader0.19.5. Installare mod0.1.18-stage1-dev e
fabric-api-0.163.0+26.3.jar su client/server, sostituendo il vecchio JAR della mod.

## Sonno

Il drago libero, a terra, sostenuto e senza bersaglio può dormire di notte.
Con casa impostata ritorna prima nel raggio originale del nido. Stato Sleeping
sincronizzato e salvato, posa sleeping originale con transizione40tick, texture
sleeping per tutte le varianti/stadi e arresto della navigazione/sguardo.

Si sveglia per danno, selezione di un bersaglio difensivo, interazione del
proprietario, comandi, pilota/passeggeri/spalle, acqua, volo o perdita del supporto.
Di giorno si sveglia se addomesticato; il selvatico già addormentato al coperto
può restare a dormire, come originale. Un giocatore non alleato vicino può
svegliarlo e diventare bersaglio, mentre proprietario/creativo/spettatore restano
protetti. Difesa del proprietario resta disponibile anche mentre dorme.

Dopo combattimento/danno/interazione attende almeno20tick prima di riaddormentarsi;
la memoria vanilla di un aggressore può prolungare questa attesa. Segui e seduto
mantengono priorità; il nuovo sonno parte solo nel comando libero. NoAI congela
la gestione automatica del sonno. doDragonsSleep (default true) aggiunto al file
iceandfire-combat.json, con valore originale e compatibilità con vecchi file.

## Rendering

Overlay occhi originale emissivo, limitato al cubo HeadFront e trasformato con
la posa corrente; non compare mentre dorme, è invisibile o sta morendo. Ordine
di disegno: pelle0, occhi1, armature2..5, così i caschi possono coprire gli occhi.
Nessuna modifica a PNG/UV. Lo stato usa un flag del drago separato dal letto
vanilla per evitare effetti collaterali del sonno del giocatore.

## Uova danneggiate

Un colpo non di fuoco rimuove il mob e restituisce1oggetto della variante corretta,
senza doppio drop. Fuoco/lava ignorati; bypass invulnerabilità rimuove senza drop.
Oggetto nuovo senza incubazione/proprietà, come getItem originale.

## Verifica

Verificati60 JUnit,60 GameTest della mod (61 con baseline vanilla escluso) e3 test client con mondi/screenshot. Uova verificate sul server dopo le prove client, senza altre modifiche grafiche. Nessun test con due veri client.

```powershell
.\fabric\gradlew.bat -p fabric build runGametest runClientGametest --console=plain
```

Nuove prove server: sonno/immobilità/posa completa, sincronizzazione e salvataggio;
danno reale con risveglio e contrattacco; seguimi e supporto rimosso; giorno e
proprietario protetto; selezione del bersaglio con risveglio prima del percorso;
attacco al proprietario con difesa effettiva dal sonno; config e priorità comandi.
JUnit confrontano posa originale, sotto-modello degli occhi e risorse di tutte
le varianti/stadi. Tre prove client separate includono posa/texture sleeping
da stato salvato NoAI e successivo risveglio, oltre a armature e attacchi coda/ali.
La prova grafica non sostituisce i test server del ciclo notturno.

Prova utente: libero di notte e casa impostata, attendere riposo; poi impartire
seguimi, interagire, provocare attacco al proprietario e al drago. Verificare
movimento, risveglio, occhi e transizioni. Salvare/riaprire mentre dorme e
ricontrollare all'alba; riprovare armature, pilota, spalle e sonno senza supporto.

Tappa1 incompleta: riproduzione, shake prey/caccia, stendardo, configurazioni,
interazioni e limiti di navigazione restano in STATO-TAPPA-1.md.
