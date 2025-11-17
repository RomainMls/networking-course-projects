/*
 * Agit comme un client au sein de notre serveur pour envoyer du SMTP vers un
 * autre serveur
 * Ex: mail arrive chez mail.gembloux.uliege mais destinaire mail.info.uliege,
 * mail.gembloux.uliege doit envoyer vers mail.uliege avant (qui agit comme
 * serveur centrale)
 * et mail.uliege renverra vers mail.info.uliege
 * Ca doit d'abord passer par mail.uliege donc faut que les serveurs agissent
 * comme client SMTP
 * 
 * Ouvre un port, envoit les cmds SMTP, ferme la co
 * De ce que j'ai compris, si on a Client -> Serveur A -> Serveur B -> Serveur C
 * Client parle à A, et A répond, A parle à B et B répond,... (rejoue la
 * conversation)
 */