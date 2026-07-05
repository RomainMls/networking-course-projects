# Project 2 - Mail, DNS and Docker Network

This project builds a small simulated network with Docker. It contains several domains, DNS servers, Java mail servers, and a Thunderbird client container for testing.

The Java mail server listens on the standard mail ports and handles SMTP, POP3, and IMAP connections.

## Main Features

- Docker Compose network with multiple subnets.
- BIND DNS servers for several domains.
- Java mail server implementation.
- SMTP, POP3 and IMAP protocol handlers.
- Thunderbird container for manual testing through a browser.

## Structure

- `docker_network/docker-compose.yml`: full network topology.
- `docker_network/MailServer/`: Java mail server source code and Dockerfile.
- `docker_network/dns/`: DNS configuration for the simulated domains.
- `docker_network/frr/`: router configuration files.

## Build and Run

From the Docker network folder:

```bash
cd docker_network
docker compose up --build
```

Thunderbird is exposed on:

```text
http://localhost:5800
```

## Mail Server

The mail server is written in Java and is built inside Docker. It listens on:

- SMTP: port `25`
- POP3: port `110`
- IMAP: port `143`

The Docker Compose file starts one mail server per domain with the right domain name passed as an argument.

## Note

This is a student networking project. The goal is to experiment with DNS, routing, mail protocols, Docker networking and Java server-side programming.
