zip: 
	-rm s2302437-s2300791.zip
	zip s2302437-s2300791.zip *.java report.pdf

run-test: clean
	javac *.java
	sudo java MailServer uliege.be 20

clean:
	sudo rm -rf mailstore
	rm -f *.class
