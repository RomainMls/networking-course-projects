/*
 * Représente la mailbox d'un utilisateur (au minimum INBOX par utilisateur)
 * Autrement dit, représente tous les msg d'un user
 * 
 * UIDVALIDITY, nextUID, listes des messages (références vers Message)
 * ajouter, supprimer, marquer msg
 * retrouver un msg par seq number pour POP3 (RETR 1, RETR 2,...)
 * pour POP3, numéro msg par arrivée, le premier mail est num 1,.. attention
 * quand un mail est suppr, les num changent
 * retrouver un msg par UID pour IMAP
 * Un fichier metadata par Mailbox pour stocker l'UIDVALIDITY de la mailbox, le
 * nextUID, UID des msg et leur flag + taille
 */