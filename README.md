# Client-Server Chat Anwendung (Konsolenanwendung)
![HS-KL Logo](https://git.dennisadam.de/Piket95/EVA_Chatprogramm/master/src/img/Logo_of_Hochschule_Kaiserslautern.png)

**Projektarbeit im Fach "Entwicklung verteilter Anwendungen mit Java (EVA)"**
Vertiefungsfach 5. Semster
Dr. Prof. Manuel Duque-Anton
Hochschule Kaiserslautern - Zweibrücken

**Author**
 Dennis Adam - Medieninformatik

## Geplante Features

- [ ] GUI erstellen in JavaFX
- [ ] Speichern der Daten im JSON-Format

## Bedienungsanleitung
### Client

 1. Starten des Client 
```bash
java -jar Client.jar <IP/Hostname> <Port>
```
 2. Registrieren durch eingeben der Ziffer 2 / oder Einloggen mit Ziffer 1, falls bereits ein Account besteht
```
Was möchtest du tun? (Zahl eingeben)
(1) Einloggen
(2) Registrieren
(3) Programm beenden
>2
```
 3. Benutzername (keine Sonderzeichen und nicht leer) und Passwort (nicht leer) aussuchen und angeben
 4. Mithilfe der folgenden Befehle (im Programm mithilfe von **/help** aufrufbar) kann man nun die verschiedenen Funktionen des Programms nutzen:
```
------------------------Liste der Befehle------------------------
/help                          Zeigt die Liste der verfügbaren Befehle (diese hier)
/userlist                      Zeigt eine Liste der verfügbaren Benutzer an
/chatlist                      Zeigt die Liste deiner aktiven Chats
/deleteChat <Benutzername>     Löscht den aktiven Chat mit dem angegebenen Benutzer aus der Liste (case sensitive
/chat <Benutzername>           Starte einen Chat mit dem angegebenen Benutzer (case sensitive)
/logout                        Meldet dich vom Server ab
/exit                          Meldet dich vom Server ab falls noch nicht geschehen und beendet die Anwendung
-----------------------------------------------------------------
```
5. Chatten kann man mithilfe von **/chat \<Benutzername>**
In einem Chat hat man nun Zugriff auf einige Befehle, die sich von denen im "Menü" unterscheiden. Auch diese können mithilfe von **/help**, solange man sich in einem Chat befindet, angezeigt/aufgerufen werden. 
```
----------------------Liste der Chat-Befehle---------------------
/help        Zeigt die Liste der verfügbaren Befehle (diese hier)
/archiv      Zeigt die letzten 10 Nachrichten an
/leave       Verlasse den aktuellen Chat
-----------------------------------------------------------------
```
Sind beide dem Chat beigetreten, können beide Parteien normal miteinander schreiben. Ist der Gesprächspartner Online aber nicht im Chat, bekommt er eine Benachrichtigung, dass er in dem Chat eine neue Nachricht bekommen hat. Ist der Gesprächspartner Offline, wird die Nachricht zugestellt, sobald er wieder online gekommen ist.


### Server
Starten kann man den Server, indem man die Jar-Datei mit dem Port auf dem er laufen soll als Parameter
```
java -jar Server.jar <Port>
```
Dieser muss nun nur im Hintergrund laufen und kann mithilfe von **Strg+C** "beendet" werden.
Wer sich mit dem Server verbunden hat und ob er sich sauber vom Server getrennt hat oder die Verbindung verloren hat, kann man nun während der Laufzeit in der Konsole nachverfolgen.

## Message-Sequence Diagramm
![Message-Sequence Chart](https://git.dennisadam.de/Piket95/EVA_Chatprogramm/master/src/img/mscEvaProjekt.png)