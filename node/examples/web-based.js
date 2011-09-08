var link = require("link"),
    Rdio = require("./../rdio"),
    cred = require("./rdio_consumer_credentials");

var app = new link.Builder;

app.use(link.commonLogger);
app.use(link.contentType);
app.use(link.contentLength);
app.use(link.sessionCookie);

app.route("/", function (env, callback) {
    var session = env["link.session"];
    var accessToken = session.at;
    var accessTokenSecret = session.ats;

    if (accessToken && accessTokenSecret) {
        var rdio = new Rdio([cred.RDIO_CONSUMER_KEY, cred.RDIO_CONSUMER_SECRET],
                            [accessToken, accessTokenSecret]);

        rdio.call("currentUser", function (err, data) {
            if (err && link.handleError(err, env, callback)) {
                return;
            }

            var currentUser = data.result;

            rdio.call("getPlaylists", function (err, data) {
                if (err && link.handleError(err, env, callback)) {
                    return;
                }

                var playlists = data.result.owned;

                var body = [];

                body.push("<html><head><title>Rdio-Simple Example</title></head><body>");
                body.push("<p>" + currentUser.firstName + "'s playlists:</p>");
                body.push("<ul>");

                playlists.forEach(function (playlist) {
                    body.push('<li><a href="' + playlist.shortUrl + '">' + playlist.name + '</a></li>');
                });

                body.push("</ul>");
                body.push('<a href="/logout">Log out of Rdio</a></body></html>');

                callback(200, {}, body.join("\n"));
            });
        });
    } else {
        var body = [];

        body.push('<html><head><title>Rdio-Simple Example</title></head><body>');
        body.push('<a href="/login">Log into Rdio</a>');
        body.push('</body></html>');

        callback(200, {}, body.join("\n"));
    }
});

app.route("/login", function (env, callback) {
    var session = env["link.session"] = {};
    var req = new link.Request(env);

    // Begin the authentication process.
    var rdio = new Rdio([cred.RDIO_CONSUMER_KEY, cred.RDIO_CONSUMER_SECRET]);
    var callbackUrl = req.baseUrl + "/callback";

    rdio.beginAuthentication(callbackUrl, function (err, authUrl) {
        if (err && link.handleError(err, env, callback)) {
            return;
        }

        // Save the request token/secret in the session.
        session.rt = rdio.token[0];
        session.rts = rdio.token[1];

        // Go to Rdio to authenticate the app.
        redirect(authUrl, callback);
    });
}, "GET");

app.route("/callback", function (env, callback) {
    var session = env["link.session"];
    var req = new link.Request(env);

    req.params(function (err, params) {
        if (err && link.handleError(err, env, callback)) {
            return;
        }

        var requestToken = session.rt;
        var requestTokenSecret = session.rts;
        var verifier = params.oauth_verifier;

        if (requestToken && requestTokenSecret && verifier) {
            // Exchange the verifier and token for an access token.
            var rdio = new Rdio([cred.RDIO_CONSUMER_KEY, cred.RDIO_CONSUMER_SECRET],
                                [requestToken, requestTokenSecret]);

            rdio.completeAuthentication(verifier, function (err) {
                if (err && link.handleError(err, env, callback)) {
                    return;
                }

                // Save the access token/secret in the session (and discard the
                // request token/secret).
                session.at = rdio.token[0];
                session.ats = rdio.token[1];
                delete session.rt;
                delete session.rts;

                // Go to the home page.
                redirect("/", callback);
            });
        } else {
            // We're missing something important.
            redirect("/logout", callback);
        }
    });
}, "GET");

app.route("/logout", function (env, callback) {
    env["link.session"] = {};
    redirect("/", callback);
}, "GET");

var server = link.run(app, {}, function () {
    var addr = server.address();
    console.log("Link server started at %s:%s", addr.address, addr.port);
});

function redirect(location, callback) {
    callback(302, {"Location": location}, 'Go to <a href="' + location + '">' + location + '</a>');
}
