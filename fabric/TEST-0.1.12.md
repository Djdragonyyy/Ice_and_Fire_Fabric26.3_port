# Ice and Fire 0.1.12 — volo autonomo e nido

Checkpoint parziale della tappa 1, da provare in un mondo nuovo. Minecraft Java
26.3, Java 25, Fabric Loader 0.19.5. Installare insieme, su client e server:

- iceandfire-fabric-26.3-0.1.12-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il vecchio JAR della mod: una sola versione in mods. Il file dei sorgenti
non va installato in mods. Nessuna dipendenza Citadel Forge.

## Novità e regole originali

Flight manager server per vagabondaggio, seguito e ritorno notturno alla casa.
Il volo autonomo parte dallo stadio 2 (25 giorni); il pilota resta ammesso solo
dallo stadio 3 (50 giorni). Non si aggiungono posti o il dorso solido del ghast.

Il decollo verifica spazio libero sopra il drago. Prima sale in hovering per 40
tick, poi passa al volo. In comando libero, di giorno, probabilità originale di
avvio 1/1500 per tick. In seguimi decolla oltre 30 blocchi dal proprietario oppure
se questo si trova oltre 8 blocchi più in alto, entro il limite originale di 2000.
Nessun teletrasporto: il percorso a terra della 0.1.11 rimane per le brevi distanze.

Il manager usa le curve di virata/accelerazione originali e bersagli aerei con
quota massima predefinita 256, limitata anche dall'altezza della dimensione.
Guardie aggiunte contro divisioni per zero e accelerazioni non finite del vecchio
helper. Controlla chunk già caricati, visibilità e spazio del corpo alla destinazione;
un percorso aereo non disponibile avvia l'atterraggio. Non è ancora un navigatore
capace di aggirare qualsiasi edificio, grotta o labirinto. Non carica chunk per
inseguire proprietari lontani. Dopo oltre 6000 tick di crociera avvia l'atterraggio.

Il comando seduto interrompe la rotta e fa scendere il drago; posa seduta solo
dopo l'atterraggio. Il controllo del pilota ha la precedenza sull'IA. Hovering è
sincronizzato, salvato e visualizzato con la posa Tabula hovering originale;
anche il volo dello stadio 2 sopravvive al salvataggio senza sbloccare la monta.

Maiusc + bastone imposta/rimuove casa come nella 0.1.11. Di notte, in comando
libero e nella dimensione corretta, il drago ritorna a casa: a piedi vicino al nido,
con decollo se distante oltre 40 blocchi e atterraggio all'arrivo. Seguimi e seduto
hanno la precedenza. Niente vagabondaggio terrestre notturno con casa impostata.
Il sonno completo e la sua animazione NON fanno parte di questo checkpoint.

## Prova suggerita

1. Mondo nuovo creativo con trucchi; ottenere dragon_stick e creative_dragon_meal
   con /give, usare il pasto creativo per assegnare la proprietà.
2. Preparare un drago a 25 giorni in uno spazio aperto, ad esempio con
   `/data merge entity @e[type=iceandfire:fire_dragon,sort=nearest,limit=1] {AgeTicks:600000,AgingDisabled:1b}`.
   Deve poter volare da solo e continuare a rifiutare la monta del giocatore.
3. Con il bastone selezionare seguimi; allontanarsi oltre 30 blocchi o salire
   oltre 8 blocchi. Verificare decollo, movimento verso il proprietario e virate.
   Ordinare seduto e verificare discesa, contatto col terreno e arresto.
4. Impostare casa in un punto aperto. Portare il drago a 10 blocchi di distanza,
   lasciare comando libero e attendere la notte: deve tornare a piedi. Ripetere
   da oltre 40 blocchi, mantenendo caricata la zona, per il ritorno in volo.
5. Salvare/riaprire mentre vola autonomamente e durante hovering. Controllare
   casa, dimensione, proprietà e comandi. Provare una casa salvata in altra dimensione.
6. Riprovare il pilota a 50/75/100/125 giorni: V, spazio, X, R, G, prima/terza
   persona e seduta; provare spalle, inventario e alimentazione della 0.1.10/0.1.11.
7. Ripetere con due veri client; verificare sincronizzazione e impossibilità per
   un altro giocatore di comandare o pilotare il drago di proprietà.

## Verifiche automatiche e limiti

Build pulita riuscita con JDK Zulu 25.0.4.1+1 e versioni del progetto invariate.
50 test JUnit e 23 GameTest della mod superati; il report contiene inoltre
minecraft:always_pass, escluso dal conteggio. I nuovi otto GameTest coprono età,
spazio/ordini/NoAI/pilota, decollo e seguito con salita reale, discesa al suolo,
NBT/sincronizzazione dello stadio 2, limite 2000, arrivo al nido a piedi e dall'aria,
ciclo completo da 42 blocchi, precedenza comandi e dimensione della casa.
Arena GameTest separata 48x96x48; la fixture standard 8x8x8 era troppo piccola.
I cinque nuovi JUnit coprono sterzo verticale/zero/virate/limiti/progresso e posa
hovering originale con battito d'ali e reset senza accumulo.

```powershell
.\fabric\gradlew.bat -p fabric clean build runGametest --console=plain
```


Client 0.1.12 avviato in run/0.1.12-smoke con init script locale che cambia
soltanto la directory di esecuzione: caricati mod, OpenGL, audio e atlanti.
Terminato dall'agente dopo il caricamento. Nessuna entrata in mondo o verifica
visiva del drago; avvisi OSHI/Realms del profilo di sviluppo non bloccanti.

La revisione in sola lettura con skill review-agent è documentata in REVIEW.md.
Le prove automatiche non sono una conferma visiva né multiplayer reale.
Tappa 1 incompleta, punte grigie ancora aperte. Prossimi blocchi: combattimento
autonomo/altri attacchi, cibo a terra, interazioni/animazioni mancanti, sonno,
riproduzione e stendardo. Acqua/lava e ostacoli complessi richiedono ancora porting
e prove dedicate; i parametri di volo di questo checkpoint usano i default originali.
