rdio-simple
===========

a set of simple clients libraries for Rdio's web API.

---

These libraries for Python, PHP, Ruby, Node.js and Java implement the subset
of OAuth that is required to talk to the Rdio service with minimal external
dependencies.

Documentation is at: http://www.rdio.com/developers/docs/libraries/simple/
Examples for each language are under each language directory.

Testing
-------

There is a testing framework that verifies that the languages implement
request signing consistently in the tests directory.

To run the tests:

1. Update ./java/examples/com/rdio/simple/examples/ConsumerCredentials.java to
   include your API keys.
2. Run the following:
```bash
cd java
ant compile
cd ../tests
javac -cp ../java/classes Tester.java
./run-tests.py
```

### Notes

The test suite relies on the `ruby-1.9` command to run the ruby test suite for version 1.9.
If your default Ruby is 1.9, or you use a different command to access 1.9 (i.e. using rvm),
then you'll need to comment out https://github.com/rdio/rdio-simple/blob/master/tests/run-tests.py#L24
