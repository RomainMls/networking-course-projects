/*
 * recoit un socket TCP
 * lit les commandes qui arrivent dessus
 * détecte le protocole en fct du port
 * => délègue au handler correspondant
 * ecris les reponses des handlers sur le canal
 * gère la fermeture du socket proprement (fermeture du read et write aussi)
 */