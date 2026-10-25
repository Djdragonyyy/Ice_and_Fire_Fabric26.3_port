# Prova 0.1.38 - esperienza del drago

Checkpoint di sviluppo: tappa1 e port completo ancora aperti. Nessuna sospensione
o spegnimento del PC.

## Modifiche

Il drago usa ora gli XP base dell'originale EntityDragonBase.getExperienceReward:
5,20,150,300,650 rispettivamente per gli stadi1..5. Il port precedente ereditava
Animal (1..3). Il solo hook nativo getBaseExperienceReward viene sostituito:
LivingEntity conserva attribuzione della morte, incantesimi e gamerule mobDrops.
Non vengono creati orb manualmente o aggirati i controlli del gioco.

## Verifiche

Build completa offline build/compileClientGametestJava/runGametest/runClientGametest
superata in9m11s:85JUnit,121GameTest mod (122 inclusa baseline vanilla),11prove
client e33PNG correnti. XML senza failure/error. Log:phase38-final.log, incluso
come build-e-test.log nella consegna. Prima, prova mirata:85JUnit e2GameTest nuovi
passati; il risultato della suite completa e quello utilizzato per la consegna.

JUnit copre i confini di ogni stadio e eta esterne al normale intervallo.
Due nuove prove native infliggono danno letale ai cinque stadi e contano gli XP
reali, includendo Count degli orb uniti dal gioco; verificano danno ripetuto,
25tick di morte e due oggetti dell'inventario rilasciati esattamente una volta.
Morte ambientale senza accredito e mobDrops=false non generano XP; la regola
viene ripristinata immediatamente in finally. Nessuna asserzione preesistente
indebolita o modifica a temporizzazioni/fixture per questa versione.

Renderer, geometria, texture, controlli e pacchetti invariati rispetto alla0.1.37.
La suite client e rieseguita sulla0.1.38; i nomi storici delle PNG identificano
il test introduttivo, non la versione runtime. Le prove rete a due client della
0.1.37 NON sono state rieseguite qui e NON sono incluse come prove della0.1.38.

## Limiti e seguito

Carcassa persistente/raccolta, relativi item/teschio, multipart e altre parita
restano da portare. La morte conserva ancora rimozione vanilla e drop inventario.
DedicatedServer ordinario, account autenticati e persistenza MP prolungata ancora
aperti. Punte grigie e cause dei casi intermittenti sonno/escort non dichiarate
risolte. Questo checkpoint completa soltanto la parte XP del ciclo di morte.