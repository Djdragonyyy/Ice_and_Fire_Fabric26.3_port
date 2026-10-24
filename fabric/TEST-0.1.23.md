# Ice and Fire 0.1.23 — configurazioni originali del ciclo vitale

Minecraft Java26.3, Java25, Loader0.19.5 e API0.163.0+26.3. Installare i due JAR
del pacchetto, sostituendo la mod precedente senza duplicati. Tappa1 incompleta.

## Impostazioni

In config/iceandfire-combat.json sono ora esposti quattro valori originali:

| Campo | Default | Valori accettati |
| --- | --- | --- |
| dragonHealth | 500 | 1..100000, finito |
| dragonAttackDamage | 17 | 1..10000 |
| dragonHungerTickRate | 3000 | 1..10000 tick |
| dragonEggTime | 7200 | 1..2147483647 tick |

Il nome del file resta lo stesso per compatibilità. I vecchi file senza questi
campi mantengono i default; i campi possono essere aggiunti manualmente.
Riavviare client/server dopo la modifica: non è hot reload. Salute e danno usano
le curve originali per età; i limiti nativi degli attributi Minecraft continuano
ad applicarsi anche se la configurazione ammette numeri più alti.

Intervallo fame positivo e valori validati prima dell'uso, quindi niente modulo
per zero da file errato. Configurazione invalida: log e ripiego sui default,
senza sovrascrivere il file originale. Le altre opzioni già migrate restano.

## Uova

La schiusa conserva la condizione originale strettamente maggiore del tempo
configurato. Il contatore interno/NBT DragonAge usa ora long, evitando overflow
anche con il massimo tempo configurabile. Sono ancora letti i vecchi NBT int.
Prova server: dato int1 con tempo1 e dato long2147483647 con tempo2147483647;
un altro tick nel fuoco produce un singolo piccolo per caso. Incubazione e
proprietà continuano a salvarsi; raccogliere l'uovo azzera il nuovo oggetto.

## Verifiche

Due nuovi JUnit: default dei vecchi JSON e rifiuto di valori fuori intervallo/
non finiti. Due GameTest: attributi reali a125giorni e fame ogni2tick; schiusa
con NBT vecchio e soglia massima senza overflow. Suite completa server/client,
comprese tutte le regressioni di armature, controlli, IA, riproduzione e presa.

Verificati68 JUnit,80 GameTest della mod (81 con baseline vanilla escluso) e4 prove client con dieci screenshot. Nessuna prova con due veri client. Nessuna prova con due
veri client. Configurazioni di volo, altre interazioni, animazioni secondarie/
suoni, acqua/lava e altre lacune restano nello stato cumulativo. Tappa1 aperta.

I nomi degli screenshot indicano la versione che introdusse ciascuna regressione:
sono stati tutti rigenerati con la0.1.23. Nessuna modifica agli asset originali.
