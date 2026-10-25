# Prova 0.1.34 — overlay maschile originale

Checkpoint parziale tappa1. Non sospendere o spegnere il PC.

## Modifiche

Il renderer originale RenderDragonBase componeva la texture base con
male_red/green/bronze/gray.png; il port estraeva il sesso ma non usava questo
overlay. Ripristinata la composizione delle risorse originali tramite una
ReloadableTexture26.3: base e overlay provengono dal ResourceManager attuale,
metadati della base conservati, immagini chiuse secondo la proprieta nativa.
Nessun PNG originale modificato, nessun asset generato al posto dell'originale.

I1957pixel visibili di ciascun overlay hanno alpha parziale: un semplice
strato cutout non sarebbe equivalente. Composizione ARGB nativa della26.3,
limiti minimi delle immagini come nell'originale. Chiavi distinte per variante,
stadio e occhi chiusi (sonno/blink); cache per renderer al massimo40combina-
zioni, senza ricaricare l'immagine a ogni fotogramma. La femmina mantiene il
riferimento alla base invariata. Il gestore nativo gestisce caricamento,
ricaricamento e rilascio delle texture registrate. Occhi/armature invariati.

## Prove

Decima prova client: composizione sintetica indipendente con semitrasparenza,
trasparenza totale e immagini di dimensioni diverse; tutte40combinazioni reali
(variante*stadio*occhi), dimensioni preservate e cambiamenti limitati ai pixel
dell'overlay. Proprietario realmente connesso: cambio sesso dal server,
texture diversa/stabile sul client, screenshots femmina/maschio e ricaricamento
asincrono completo delle risorse. Nessun join bloccante sul thread render.

Primo ciclo di dieci prove client passato, ma lo screenshot dopo reload
mostrava ancora la dissolvenza Mojang: corretta la fixture attendendo overlay
assente e inventario visibile. Nessuna modifica di produzione per questa attesa.
Suite completa definitiva passata: **79JUnit,117GameTest mod (118 con baseline),10prove client e20immagini**, build riuscita in8m35s. Log phase34-final.log. Controllate le tre nuove immagini, inclusa quella dopo la dissolvenza completa del reload. Nessuna prova con due client separati.

La diagnostica del test intermittente di contrattacco aggiunge ora danno reale
e presenza di Weakness al messaggio di fallimento. Asserzioni invariate; causa
ancora non provata. Le punte grigie restano aperte: l'overlay e trasparente
sui campioni delle punte gia analizzati. Nessuna modifica cieca a UV o alpha.
Restano altre parita originali e collaudo con due client separati/standalone.
Tappa1 e port completo non sono conclusi.
