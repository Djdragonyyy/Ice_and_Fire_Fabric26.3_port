# Prova 0.1.45 - incubazione, movimento e schiusa delle uova

Checkpoint finalizzato il 27 ottobre 2026; esecuzione completa del 26 ottobre.
Tappa1 e port completo restano aperti.

## Comportamento e riferimenti

L'incubazione del fuoco accetta lava oltre al tag FIRE gia supportato. L'originale
EntityDragonEgg usa BlockState.isBurning, il cui default Forge include FIRE/LAVA:
[fonte primaria Forge](https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.20.x/src/main/java/net/minecraftforge/common/extensions/IForgeBlock.java).
Il port conserva anche soul fire tramite tag nativo: adattamento esplicito, non
replica delle estensioni arbitrarie di ogni blocco Forge. Magma e campfire non
sono nuove fonti valide. Acqua/freddo non azzerano il progresso gia salvato.
Soglia originale strettamente maggiore, contatore64bit e limite preservati.

Il piccolo nasce al centro X/Z del blocco e un blocco sopra, come l'originale,
con colore, nome, proprietario e fame50. Solo dopo addFreshEntity riuscito si
rimuove il blocco incubante, si trasmettono suono FIRE_EXTINGUISH e EGG_HATCH e
si scarta l'uovo. Guardia dopo super.tick evita schiusa di uova gia rimosse.
OGG e definizione egg_hatch copiati invariati dall'originale; aggiunto alias del
sottotitolo dragon_hatch in italiano/inglese, coerente con sounds.json originale.
Broadcast server nativo al posto della chiamata locale storica. Non e stata
eseguita una verifica uditiva manuale, ne una nuova prova audio a due client.

ModelDragonEgg originale: walk/flap velocita .3, ampiezza .3, fase1 sull'asse X
invertito e fase0 su Z. Renderer corrente applica la posa soltanto sul blocco
incubante valido e resetta gli assi per l'uovo freddo. Geometria/UV/PNG invariati.

## Verifiche effettive

Comando: build runGametest compileClientGametestJava runClientGametest --offline
--console=plain, con JDK25 e Gradle isolati. phase45-final.log: BUILD SUCCESSFUL
in 10m35s; 95 JUnit,145 GameTest mod+baseline146,15 classi client,42 PNG correnti.
XML senza failure/error. Focus precedente sui quattro nuovi GameTest verde37s.

Nuovi test server verificano fuoco/lava/soul fire e pausa in acqua; quattro
colori con posizione esatta/nome/proprietario reale e fame; salvataggio/caricamento
in un livello reale e soglia dopo pausa; rimozione dentro super.tick mediante
WITHER nativo con esattamente un oggetto restituito e nessun drago duplicato.

Nuovo client integrato: otto uova (quattro colori fredde/calde), metadati/blocchi
sincronizzati, risoluzione risorsa sonora, due assi animati e reset del modello
condiviso dopo uovo caldo. Poi schiusa reale e nome/owner/stadio0giorni verificati
su server e client. Lava contenuta da vetro nella sola fixture per evitare che
raggiunga la fila fredda. Nessun helper di test nel JAR di produzione.

0040_dragon-eggs-four-colors-cold-fire-lava-0.1.45.png e
0041_dragon-egg-named-owned-hatchling-0.1.45.png sono state aperte e controllate:
quattro texture uovo corrette, movimento mostrato come posa inclinata e piccolo
completo. La tavola usa il renderer nativo InventoryScreen, non nuove texture.
Alcune etichette troppo lunghe della tavola si toccano: difetto della tavola di
test dichiarato; niente clipping delle entita. La posa statica non prova da sola
l'animazione, coperta dalle asserzioni native. Nessun cambiamento corpo/ali drago.

## Limiti e consegna

Consegna: due JAR (mod e Fabric API), sorgenti versionati, log/XML,42 PNG e SHA256.
Le prove DedicatedServer43/44 restano nei rispettivi pacchetti: non rinominate
come nuove prove45. Configurazioni originali residue, DragonForge, ulteriori
interazioni/ciclo vitale, navigazione complessa, punte grigie e anteprima
passeggeri restano aperti. Nessuna certificazione crash/server autenticati.
