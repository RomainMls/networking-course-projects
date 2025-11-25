zip: 
	-rm s2302437-s2300791.zip
	zip s2302437-s2300791.zip *.java report.pdf

run-test:
	rm -f *.class
	javac *.java
	sudo java MailServer mail.uliege.be 20

clean:
	rm -f *.class
