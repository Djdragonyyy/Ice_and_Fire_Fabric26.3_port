# Ice and Fire 0.1.10 — fuoco, anteprima e draghetti in spalla

Minecraft Java 26.3, Java 25, Fabric Loader 0.19.5. Inserire in mods solo:

- iceandfire-fabric-26.3-0.1.10-stage1-dev.jar
- fabric-api-0.163.0+26.3.jar

Sostituire il precedente JAR della mod, senza duplicati; aggiornare insieme client
e server. ZIP sorgenti e cartella codice servono per consultazione, non per mods.

## Getto di fuoco

R mantiene il soffio, G morso, V decollo, spazio salita, X discesa.
Il getto usa i 16 fotogrammi della texture originale flame.png, senza modificarne
i pixel. Sostituisce la precedente singola dragon_flame.png; non è dichiarata
un'equivalenza esatta con il renderer Forge. Fotogrammi distribuiti lungo il viaggio,
fasi sfalsate e dissolvenza all'emissione/arrivo rendono il getto meno frammentato.
Dimensione/densità per stadio, crescita progressiva e protezioni della prima persona
restano attive. Fuoco vanilla sul terreno e proiettile a cubo di magma restano distinti.

Provare a 50 e 125 giorni, in F5 e prima persona, fermi e in volo. Controllare
continuità, rilascio di R e impatto vicino a una parete; verificare che non compaia
l'intera griglia delle 16 fiamme al posto di un solo fotogramma.

## Inventario

Maiusc + clic destro a mano vuota sul proprio drago. L'anteprima conserva stadio,
variante, sesso e armatura; la dimensione è adattata al riquadro della GUI. Rimosso
il secondo ridimensionamento che faceva apparire minuscoli gli adulti nella 26.3.
Confrontare draghi di stadio 1, 3 e 5 e muovere il mouse intorno al riquadro.

## Draghetti in spalla

Clic destro a mano vuota sul proprio draghetto di stadio 1, cioè meno di 25 giorni.
Massimo tre come nell'originale: due sulle spalle e uno sopra la testa. Maiusc li
fa scendere, quando il giocatore non sta cavalcando un altro veicolo. Anche il
volo con elitre li fa scendere. Lo stadio 2 resta non trasportabile e non cavalcabile;
la cavalcatura principale resta dallo stadio 3, con un solo pilota.

La 26.3 vieta la monta diretta di entità sul giocatore. Il port mantiene quindi
i piccoli come entità del mondo, con aggancio sincronizzato alle spalle e posa
originale sitting_shoulder. Restano salvati indipendentemente dal giocatore; logout,
morte, cambio dimensione, crescita allo stadio 2 e ricaricamento li rilasciano.
Dopo rientro/ricaricamento cercarli nel punto dove sono stati lasciati.
Non è stata reintrodotta la funzione del dorso solido del ghast.

Provare raccolta di tre piccoli e rifiuto del quarto; camminata, rotazione, salto,
Maiusc, logout/rientro e salvataggio. Provare anche mentre si cavalca un adulto
e con un secondo giocatore che osserva o tenta di raccogliere un piccolo altrui.
L'aggancio visivo segue la seduta animata dell'adulto quando il proprietario lo cavalca.

## Verifiche

Build riuscita; 45 test JUnit e 9 GameTest della mod superati (il report aggiunge
un baseline vanilla, escluso dal conteggio). Coperti pose/reset, posizioni, crescita,
raccolta/capacità, proprietario, rilascio, persistenza e logout, oltre ai test precedenti.
Client avviato: mod, grafica, audio e atlanti caricati. Nessuna prova visiva in mondo
o sessione con due veri client eseguita dall'agente; serve il riscontro in gioco.

```powershell
.\fabric\gradlew.bat -p fabric build runGametest --console=plain
```

Tappa 1 ancora incompleta e punte grigie ancora aperte. Rimangono IA autonoma,
altri attacchi/interazioni/alimentazione e layer dello stendardo. Dettagli della
revisione in REVISIONE.md; sorgenti completi, licenza e checksum nella consegna.
