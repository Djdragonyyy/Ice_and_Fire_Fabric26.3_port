# Ice and Fire 0.1.16 — cibo a terra, coda, ali e palla di fuoco autonoma

Minecraft26.3, Java25, Loader0.19.5. Usare il JAR mod0.1.16-stage1-dev e
fabric-api-0.163.0+26.3.jar, sostituendo la precedente mod senza duplicati.

## Funzionalità migrate

Il drago libero e affamato cerca carne lasciata a terra, raggiungibile e visibile,
entro FOLLOW_RANGE e quattro blocchi verticali. Ricerca ogni20tick, percorso ogni10.
Cammina a velocità1, consuma un'unità alla volta, aumenta fame e salute dei punti
FoodUtils (nutrimento*10), come DragonAITargetItems. Il cibo manuale conserva la
cura inferiore già migrata. Non cresce, non si addomestica automaticamente, non
mangia pesce/pasti speciali da terra. Rispetta pickup delay, comandi, combattimento,
pilota, spalle, NoAI e stadio/volo. In seguimi non devia verso il cibo; mentre lo
cerca non parte per il vagabondaggio aereo. Animazione alimentazione20tick
sincronizzata, suono e particelle; la posa chiude gradualmente la mandibola.

Primo attacco terrestre del nuovo bersaglio ancora morso; poi selezione casuale
morso/coda/ali dallo stadio2. Coda40tick, danno nella finestra21..29; ali50tick,
impatti15/25/35. Danni server, contraccolpo stage*0.6 e controlli rinnovati di
portata/visibilità/alleati. Solo il morso può curare il drago. Asset Tabula
tail1..3 e wing_blast1..7 originali senza modifiche, con sollevamento del corpo.

Combattimento a distanza: dopo un ciclo di soffio la probabilità1/5 seleziona
la carica autonoma, mantenuta per tutta l'animazione30tick. Emissione unica al
tick20, direzione verso gli occhi del bersaglio, imprecisione0.0075 e accelerazione
0.07. Questo rende stabile la scelta rispetto al ramo casuale originale valutato
ogni tick; non è una dichiarazione di identica distribuzione temporale. Bersaglio
diverso, vicino/non visibile o comandi interrompono. Prima azione a distanza resta
il soffio. Attacchi e comandi del pilota restano invariati.

## Prove

Verificati 58 JUnit, 46 GameTest della mod (47 con baseline vanilla escluso) e un test client con mondo/screenshot. Revisione completa read-only review-agent senza delega: No findings. Nessun test con due veri client.

```powershell
.\fabric\gradlew.bat -p fabric build runGametest runClientGametest --console=plain
```

Nuove prove: camminata e consumo esatto da stack, fame/cura senza crescita,
dieta/pickup delay/comandi/portata, ultima unità rimossa, animazione sincronizzata
e reset; combattimento prolungato con coda/ali e danni; carica con proiettile reale
emesso dal frame20 e danno. Le arene del cibo sono distanziate oltre FOLLOW_RANGE
per evitare che draghi di prove concorrenti si contendano gli oggetti.
JUnit confrontano tempi/finestre originali e assenza di accumulo/reset delle pose.

Collaudo utente: lasciare carne davanti al drago libero affamato e osservare
cammino/masticazione; provare seduto/seguimi/sazio, stack e pesce. Combattere
contro bersagli resistenti e osservare coda/ali; a distanza sostenuta osservare
soffio e successiva scelta della palla di fuoco, quindi impartire seduto.

Tappa1 incompleta: mancano modalità aeree originali, shake prey, sonno/riproduzione,
stendardo e altre lacune in STATO-TAPPA-1.md. Nessun test con due veri client;
la prova client automatica verifica armature, non la resa delle nuove animazioni.
