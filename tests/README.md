Unit tests for rdio-simple.

These just test the signing code. It works by comparing the output from the
different implementations. The Java tester needs to be built. First run
"ant compile" in ../java/, then run:
  javac -cp ../java/classes/ Tester.java

Run the tests by running: run-tests.py