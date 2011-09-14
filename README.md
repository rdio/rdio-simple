Welcome to the Rdio-simple wiki! Here, we're going to walk you through how to interact with the Rdio-simple clients. This information is also available at: [http://developer.rdio.com/docs/read/rest/rdiosimple](http://developer.rdio.com/docs/read/rest/rdiosimple).


Rdio-Simple is a collection of simple client libraries for the Rdio Web Service API.

There are currently libraries for **PHP**, **Python** and **Ruby**. They have minimal external dependencies and are simple to use. They provide a simple API that is simliar across all of the languages. It's available from github:
[https://github.com/rdio/rdio-simple](https://github.com/rdio/rdio-simple).

## Install the library

To install the libraries simply _copy_ the rdio-simple files into your app's source code:  

**PHP**

    cp rdio-simple/om.php rdio-simple/rdio.php my-app/

**Python**

    cp rdio-simple/om.py rdio-simple/rdio.py my-app/

**Ruby**

    cp rdio-simple/om.rb rdio-simple/rdio.rb my-app/



Once you've done that, you just need to simply load the Rdio object from the library, like so:

**PHP**

    require_once 'rdio.php';

**Python**

    from rdio import Rdio

**Ruby**

    require 'rubygems'
    require 'rdio'


Next, Instantiate an Rdio object with consumer credentials and optionally an OAuth token:  

**PHP:**


    $rdio = new Rdio(array("consumerkey", "consumersecret"));

_or_

    $rdio = new Rdio(array("consumerkey", "consumersecret"), array("token", "tokensecret"));


**Python:**

    rdio = Rdio(("consumerkey", "consumersecret"))

_or_

    rdio = Rdio(("consumerkey", "consumersecret"), ("token", "tokensecret"))


**Ruby:**

    rdio = Rdio.new(["consumerkey", "consumersecret"])

_or_

    rdio = Rdio.new(["consumerkey", "consumersecret"], ["token", "tokensecret"])


## Making calls to the API (finally...)

Now's the time to make the magic happen. Make Rdio API calls by calling the **"call"** method on the Rdio instance. Pass arguments as a _dictionary_:


**PHP:**


    $ian = $rdio->call("findUser", array("vanityName" => "ian"));
    if ($ian->status == "ok") {
        print $ian->result->firstName." ".$ian->result->lastName."\n";
    } else {
        print "ERROR: ".$ian->message."\n";
    }



**Python:**

    ian = rdio.call("findUser", {"vanityName": "ian"})
    if (ian["status"] == "ok"):
        print ian["result"]["firstName"] + " " + ian["result"]["lastName"]
    else:
        print "ERROR: " + ian["message"]



**Ruby:**

    ian = rdio.call("findUser", {"vanityName" => "ian"})
    if (ian["status"] == "ok")
        puts ian["result"]["firstName"] + " " + ian["result"]["lastName"]
    else
        puts "ERROR: " + ian["message"]
    end




## Authentication


Authentication is a two phase process. First call the "begin authentication" method with a callback, either an URL or "oob" to use the OAuth PIN flow.  


**PHP:**

    $auth_url = $rdio->begin_authentication($callback);


**Python:**

    auth_url = rdio.begin_authentication(callback)


**Ruby:**

    auth_url = rdio.begin_authentication(callback)



The Rdio object's token field will be set to the OAuth request token. If authentication will be completed with a different Rdio object instance (for example in a traditional web flow) then the token should be saved and passed into the constructor:


**PHP:**

    $saved_token = $rdio->token # a two element array
    # later...
    $rdio = new Rdio(array("consumerkey", "consumersecret"), $saved_token);


**Python:**
    
    saved_token = rdio.token # a two element tuple
    # later...
    rdio = Rdio(("consumerkey", "consumersecret"), saved_token)


**Ruby:**
    
    saved_token = rdio.token # a two element array
    # later...
    rdio = Rdio.new(["consumerkey", "consumersecret"], saved_token)



## Final Step...

Direct the user to load that auth_url. Once they've approved the application they will be shown a PIN to enter, or directed to the callback URL provided with a query-string parameter called oauth_verifier. We refer to that PIN or parameter as the verifier. Pass that to the "complete authentication" method:


**PHP:**

    $rdio->complete_authentication(verifier);


**Python:**

    rdio.complete_authentication(verifier)


**Ruby:**

    rdio.complete_authentication(verifier)