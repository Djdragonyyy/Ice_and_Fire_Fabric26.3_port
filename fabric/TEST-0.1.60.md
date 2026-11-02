# Prova 0.1.60 - pelle del maschio neonato

Ripresa2novembre, checkpoint3novembre2026. Tappa1 e port incompleti.

Nel renderer originale1.20.1 la composizione maschile e selezionata soltanto
se isMale e non isSkeletal. Il secondo predicato usa DeathStage e giorni
anche sull'animale vivo; con DeathStage0 e eta0..9 la composizione non viene
applicata. Il confronto originale reale della59 aveva mostrato questa
differenza specifica nel port. Il nuovo flag client suppressMaleOverlay
conserva il filtro senza attribuire uno stato di carcassa a un animale vivo.
Nessun cambiamento a immagini, geometria, UV, animazioni, AI, crescita o rete.

DragonSexSkinClientTest ora crea un maschio posseduto reale, apre il suo menu
e fa crescere l'entita sul server ai giorni0/9/10. Attende la sincronizzazione
dell'eta al client, verifica texture base prima del10/composizione dal10 e
stato vivo/noncarcassa/nonscheletrico in tutti i casi. Tre nuove immagini.
Restano i precedenti controlli delle quattro composizioni, maschio adulto,
cache delle chiavi e ricaricamento delle risorse. Nessuna API produttiva
allargata per la prova. Focus100JUnit e singola prova client PASS1m21s.

Separatamente, il renderer60 e stato confrontato con le trenta catture
originali dei cinque stadi gia conservate: confronto PASS1m03s e nuova
corrispondenza del maschio neonato. Sono pose di rendering isolate, non
trenta nuovi test di alimentazione. Metodo/limiti in VISUAL-REFERENCE-0.1.60.md.

Full pulito phase60-final.log PASS13m 30s,exit0:100JUnit,
20client/66PNG e183server(182mod+baseline), XML senza errori. Clean senza
init privato prima della build. JAR59->60:126classi identiche,3differenti
tutte client; record PreyRenderPose codice/struttura javap identici.
Nessuna aggiunta/rimozione e classi comuni identiche. Nessun test privato,
observer o bridge nel JAR distribuito.

Nessuna nuova certificazione di riavvio dedicato o client autenticati;
la prova52 resta distinta. Restano navigazione avanzata/scavo forzato,
configurazioni/ciclo vitale e ulteriori dettagli visivi. Il confronto rosso
non certifica tutte le pose o gli altri tre colori.
