zip: 
	-rm s2302437.zip
	zip s2302437.zip *.java report.pdf

run-test:
	rm -f *.class
	javac *.java
	sudo java MailServer mail.uliege.be 20

clean:
	rm -f *.class
