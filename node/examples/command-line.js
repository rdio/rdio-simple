var rl = require("readline"),
    Rdio = require("./../rdio"),
    cred = require("./rdio_consumer_credentials");

var i = rl.createInterface(process.stdin, process.stdout, null);

function done() {
    i.close();
    process.stdin.destroy();
}

// Create an Rdio object with consumer's credentials.
var rdio = new Rdio([cred.RDIO_CONSUMER_KEY, cred.RDIO_CONSUMER_SECRET]);

// Authenticate against the Rdio service.
rdio.beginAuthentication("oob", function (err, authUrl) {
    if (err) {
        console.log("ERROR: " + err);
        done();
        return;
    }

    console.log("Go to: " + authUrl);

    // Prompt the user for the verifier code.
    i.question("Then enter the code: ", function (verifier) {
        rdio.completeAuthentication(verifier, function (err) {
            if (err) {
                console.log("ERROR: " + err);
                done();
                return;
            }

            // Get a list of playlists.
            rdio.call("getPlaylists", {}, function (err, data) {
                if (err) {
                    console.log("ERROR: " + err);
                    done();
                    return;
                }

                var playlists = data.result.owned;

                console.log("You have the following playlists:");

                playlists.forEach(function (playlist) {
                    console.log(playlist.name);
                });

                done();
            });
        });
    });
});
